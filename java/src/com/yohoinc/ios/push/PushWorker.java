package com.yohoinc.ios.push;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import javapns.devices.Device;
import javapns.devices.exceptions.InvalidDeviceTokenFormatException;
import javapns.devices.implementations.basic.BasicDevice;
import javapns.notification.PayloadPerDevice;
import javapns.notification.PushNotificationPayload;

import com.yohoinc.ios.data.DataService;
import com.yohoinc.ios.data.MysqlDataService;
import com.yohoinc.ios.data.RedisDataService;
import com.yohoinc.ios.data.Task;
import com.yohoinc.ios.push.executor.IOSExecutor;
import com.yohoinc.ios.push.message.IOSMessageToDevices;
import com.yohoinc.ios.util.Configuration;
import com.yohoinc.ios.util.LoggerTool;
import com.yohoinc.ios.util.PushConstant;

/**
 * 推送的线程类. 此类被设计成以多线程方式运行. 但目前没有以多线程方式执行. 因为还不确定是否会有重复推送的问题出现.
 * 
 * @author dan
 *
 */
public class PushWorker implements Runnable{
    /**
     * 是否是正式环境, 默认是
     */
    private boolean production = true;
    /**
     * 配置文件. 这里是系统配置. 来自于global.properties
     */
    private Configuration config;
    /**
     * 数据服务的对象. 用来操作数据库. 获取需要推送的消息或者更新数据库状态
     */
    private DataService dataService;
    /**
     * 推送执行者
     */
    private IOSExecutor iOSExecutor;
    /**
     * 日志记录
     */
    private Logger logger;
    
    public PushWorker(boolean production, Configuration config, int customType) {
        this.production = production;
        this.config = config;
        this.initDataService(config, customType);
        iOSExecutor = new IOSExecutor(this.production, config, customType);
        
        logger = LoggerTool.logFactory(PushWorker.class);
    }
    
    /**
     * 初始化数据服务. 因为初始化比较麻烦, 所以单独使用一个方法来初始化
     * 
     * @param config 配置文件
     * @param customType 应用类型.见PushConstant. 0为yoho!e, 1为yoho!有货, 2为yoho!ezine. 用来配置customKey
     */
    private void initDataService (Configuration config, int customType) {
        
        String customKey  = "";
        
        int taskQueueType = Integer.valueOf(config.getValue(PushConstant.ConfigKey.TASK_QUEUE_TYPE)).intValue();
        // 根据脚本执行设置的参数来设置customKey
        // TODO
        
        // 任务队列类型. 用来初始化使用mysql方式连接还是以内存数据库方式
        switch (taskQueueType) {
            case 0:
                this.dataService = new RedisDataService(customKey);
                break;
            case 1:
                this.dataService = new MysqlDataService(customKey, production);
                break;
            default:
                this.dataService = new MysqlDataService(customKey, production);
                break;
        }
    }
    
    /**
     * 奔跑和推送吧, 小强!!
     */
    public void run() {
        List<PayloadPerDevice> payloadPerDeviceList = new ArrayList<PayloadPerDevice>();
        
        while (true) {
            // 判断是否有任务要被推送
            if (this.dataService.countTask() > 0) {
                // 单次获取任务有最大值.目前默认一次100个
                int maxTaskNum = Integer.valueOf(config.getValue(PushConstant.ConfigKey.PUSH_ONCE_TASK_NUM));
                List<Task> tasks = this.dataService.getTasks(maxTaskNum);
                for(Task task : tasks) {
                    int taskType = task.getTaskType();
                    // 不同的任务类型使用不同的方法来推送
                    switch (taskType) {
                        // 根据用户来推送. 这是推送单条消息
                        case PushConstant.SysConstant.TASK_TYPE_PUSH_BY_USER:
                            payloadPerDeviceList = executeIOSMessagesByUser(payloadPerDeviceList, task);
                            break;
                        // 向所有用户推送. 用来推送系统消息
                        case PushConstant.SysConstant.TASK_TYPE_PUSH_ALL:
                            executeIOSMessagesByAll(task);
                            break;
                        // 根据规则来推送. 目前还没实现
                        case PushConstant.SysConstant.TASK_TYPE_PUSH_BY_RULE:
                            executeIOSMessagesByRule(task);
                            break;
                        default:
                            break;
                    }
                    // 推送后, 就设置任务状态为推送成功
                    this.dataService.setTaskStatus(task.getId(), PushConstant.SysConstant.TASK_STATUS_SENT);
                }
                
            }
            // 如果没有任务推送了, 则判断下当前推送队列中的内容是否还有, 有的话就推送出去. 推送后休息一会. 因为队列和数据库里都没有消息了.
            else {
                if (payloadPerDeviceList != null && payloadPerDeviceList.size() > 0) {
                    pushIOSMessageList(payloadPerDeviceList);
                    // 重置下为空
                    payloadPerDeviceList = new ArrayList<PayloadPerDevice>();
                }
                // 不要一直推, 休息一会系统负载会降好多
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * 执行根据用户来推送消息. 不是一有一条就推, 而是会节约一点, 看是否满几条后再推. 除非数据库中没任务了. 如果一起推, 效率会高很多
     * 
     * @param payloadPerDeviceList
     * @param task
     * @return List<PayloadPerDevice>
     */
    private List<PayloadPerDevice> executeIOSMessagesByUser(List<PayloadPerDevice> payloadPerDeviceList, Task task) {
        
        if (payloadPerDeviceList == null) {
            payloadPerDeviceList = new ArrayList<PayloadPerDevice>();
        }
        // 根据任务产生推送的列表. 因为一个用户可能使用多个设备登录. 所以会是一个列表
        List<PayloadPerDevice> payloadPerDevices = genIOSMessageByTask(task);
        
        int listSize = payloadPerDeviceList.size();
        
        int maxPushMsgNum = Integer.valueOf(config.getValue(PushConstant.ConfigKey.PUSH_ONCE_MESSAGE_NUM));
        
        // 如果任务列表里的消息已经超过了最大值了, 先推了再说
        if (listSize >= maxPushMsgNum) {
            pushIOSMessageList(payloadPerDeviceList);
            payloadPerDeviceList = new ArrayList<PayloadPerDevice>();
        }
        // 推完了或者不满最大值的时候, 将这个任务产生的列表加到列表中去, 并返回
        if (payloadPerDevices != null && payloadPerDevices.size() > 0) {
            payloadPerDeviceList.addAll(payloadPerDevices);
        }
        
        return payloadPerDeviceList;
    }
    
    /**
     * 根据任务来产生需要向单人推送的列表. 因为一个人可能登录过多台设备, 多台设备都需要被推送
     * 
     * @param task
     * @return
     */
    private List<PayloadPerDevice> genIOSMessageByTask(Task task) {
        
        Long userID = task.getUserID();
        String customKey = task.getCustomKey();
        List<PayloadPerDevice> payloadPerDeviceList = new ArrayList<PayloadPerDevice>();
        
        if (userID != null && customKey != null && customKey.length() > 0) {
            List<String> deviceTokens = dataService.getDeviceTokensByUser(userID);
            
            if (deviceTokens != null && deviceTokens.size() > 0) {
                
                String taskContent = task.getTaskContent();
                String content = "";
                int num = 0;
                
                try {
                    JSONObject json = new JSONObject(taskContent);
                    content = json.getString("c");
                    num = json.getInt("num");
                    json.remove("c");
                    json.remove("num");
                    taskContent = json.toString();
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
                PushNotificationPayload payload = PushNotificationPayload.complex();
                payload.setExpiry(Integer.valueOf(config.getValue(PushConstant.ConfigKey.PUSH_EXPIRE_TIME)));
                try {
                    payload.addAlert(content);
                    payload.addBadge(num);
                    payload.addSound(config.getValue(PushConstant.ConfigKey.PUSH_ALERT_SOUND));
                    payload.addCustomDictionary(PushConstant.SysConstant.PUSH_IOS_CUSTOME_PROP_KEY, taskContent);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
                for (String deviceToken : deviceTokens) {
                    Device device = null;
                    try {
                        device = new BasicDevice(deviceToken);
                    } catch (InvalidDeviceTokenFormatException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    
                    if (device != null) {
                        PayloadPerDevice payloadPerDevice = new PayloadPerDevice(payload, device);
                        payloadPerDeviceList.add(payloadPerDevice);
                    }
                }
                
            }
        }
        
        return payloadPerDeviceList;
    }
    
    /**
     * 执行向全部用户推送.这里使用了多线程的方式来推. 以避免阻塞其他任务
     * 
     * @param task
     */
    private void executeIOSMessagesByAll(Task task) {
        // 打开群推的线程
        Thread pushAllThread = new AllTaskProcessorThread(task, dataService, config, iOSExecutor);
        pushAllThread.start();
        logger.info("全量推送线程开始!线程号:" + pushAllThread.getId());
    }
    
    /**
     * 根据规则推. 这里未实现
     * TODO 
     * @param task
     * @return
     */
    private List<IOSMessageToDevices> executeIOSMessagesByRule(Task task) {
        return null;
    }
    
    /**
     * 执行推送接口 这里是向单人推送
     * 
     * @param payloadPerDeviceList
     */
    private void pushIOSMessageList(List<PayloadPerDevice> payloadPerDeviceList) {
        if (payloadPerDeviceList != null && payloadPerDeviceList.size() > 0) {
            
            int index = 0;
            // 单次有推送最大值. 目前最大值50
            final int maxPushMsgNum = Integer.valueOf(config.getValue(PushConstant.ConfigKey.PUSH_ONCE_MESSAGE_NUM));
            logger.info("单次推送(pushIOSMessageList): " + payloadPerDeviceList.size());
            while (index < payloadPerDeviceList.size()) {
                int maxlength = index + maxPushMsgNum;
                if (maxlength > payloadPerDeviceList.size()) {
                    maxlength = payloadPerDeviceList.size();
                }
                List<PayloadPerDevice> pushCands = payloadPerDeviceList.subList(index, maxlength);
                iOSExecutor.pushToOneDevice(pushCands);
                
                index += maxPushMsgNum;
            }
        }
    }
}
