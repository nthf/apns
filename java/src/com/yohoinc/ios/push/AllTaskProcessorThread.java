package com.yohoinc.ios.push;

import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.yohoinc.ios.data.DataService;
import com.yohoinc.ios.data.Task;
import com.yohoinc.ios.push.executor.IOSExecutor;
import com.yohoinc.ios.push.message.IOSMessageToDevices;
import com.yohoinc.ios.util.Configuration;
import com.yohoinc.ios.util.LoggerTool;
import com.yohoinc.ios.util.PushConstant;
/**
 * 执行群推功能的线程类. 群推需要一次向几十万甚至上百万的用户推送. 因为太大了, 所以使用线程来推.
 * 
 * TODO: 这里需要限制推送的时间, 不能大半夜还推. 需要在半夜的时候暂停线程
 * @author dan
 *
 */
public class AllTaskProcessorThread extends Thread {

    private Task task = null;
    private DataService dataService;
    private Configuration config;
    private IOSExecutor executor;
    private Logger logger;
    
    public AllTaskProcessorThread(Task task, DataService dataService, Configuration config, IOSExecutor executor) {
        this.task = task;
        this.dataService = dataService;
        this.config = config;
        this.executor = executor;
        
        logger = LoggerTool.logFactory(AllTaskProcessorThread.class);
    }
    
    public void run() {
        // 先获取数据库或者内存数据库中的设备的数量
        int countAllDevice = dataService.countAllDeviceToken();
        int offset = 0;
        // 单词从数据库中获取的设备的数量. 目前单次的限制为10000个
        int limit = Integer.valueOf(config.getValue(PushConstant.ConfigKey.PUSH_GET_TOKEN_FROM_DB_ONCE));
        String taskContent = task.getTaskContent();
        IOSMessageToDevices iosMessages = new IOSMessageToDevices(config);
        // 控制线程是否间段停止 (是:true, 否:false)
        boolean wait = false;
        
        // 装配需要推送的消息
        try {
            JSONObject json = new JSONObject(taskContent);
            // 消息提醒
            if (json.has("c")) {
	            String content = json.getString("c");
	            iosMessages.setSound(config.getValue(PushConstant.ConfigKey.PUSH_ALERT_SOUND));
	            iosMessages.setText(content);
	            json.remove("c");
            }
            // 消息不提醒
            else if (json.has("n")) {
            	String nValue = json.getString("n");
	            if (nValue != null && "1".equals(nValue)) {
	            	// 设置线程需要间段停止
	            	wait = true;
	            	// 设置只在后台运行, 不提醒
	            	iosMessages.setContentAvailable();
	            }
	            json.remove("n");
            }
            iosMessages.setCustomNotice(PushConstant.SysConstant.PUSH_IOS_CUSTOME_PROP_KEY, json.toString());
        } catch (JSONException e) {
            logger.warn("JSON数组异常!" + e.getMessage());
        }
        
        logger.info("全站推送开始!总共需要推送: " + countAllDevice);
        while (countAllDevice > 0) {
        	logger.info("全站推送执行中, 推送第: " + offset + " 开始" + limit + "条");
            List<String> deviceList = dataService.getDeviceTokens(offset, limit);
            if (deviceList != null && deviceList.size() > 0) {
                // 注意这里的参数为false, 表示非跟在之前队列后, 而是清空之前的队列, 加入当前的队列
                iosMessages.addDevices(deviceList, false);
                // 执行推送
                executor.pushToDevices(iosMessages);
                logger.info("全站推送执行结束推送第: " + offset + " 开始" + limit + "条");
                countAllDevice -= limit;
                offset += limit;
                
                if (wait) {
                	try {
                		// FIXME 还需要考虑JAVA占用内存情况
                		// 线程停止1小时, 阻塞状态
                		Thread.sleep(1000 * 60 * 3600);
				    } catch (InterruptedException e) {
				    	e.printStackTrace();
				    }
                }
            }
        }
    }
}
