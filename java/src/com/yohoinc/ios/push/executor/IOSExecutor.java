package com.yohoinc.ios.push.executor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.log4j.Logger;

import com.yohoinc.ios.push.message.IOSMessageToDevices;
import com.yohoinc.ios.util.Configuration;
import com.yohoinc.ios.util.LoggerTool;
import com.yohoinc.ios.util.PushConstant;

import javapns.Push;
import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import javapns.notification.Payload;
import javapns.notification.PayloadPerDevice;
/**
 * 推送消息的执行类.有两种推送方式:
 * 
 * 1. 一次向多个设备推送不同的消息
 * 2. 一次向多个设备推送相同的消息
 * 
 * 第一种情况是用户为基础, 向单个用户推送如评论消息, @我的等消息
 * 第二种情况是以设备为基础, 向多个用户群发相同的消息, 如系统通知等
 * 
 * TODO 这个类还可以扩展. 在javapns中有检查设备号是否还有效. 如果无效就需要从数据库中删除. 这个暂时还未实现
 * 
 * @author dan
 *
 */
public class IOSExecutor{

    /**
     * 是否是正式环境, 这个会被运行脚本的时候作为参数传入
     */
    private boolean production;
    /**
     * 推送证书的路径
     */
    private byte[] keystore;
    /**
     * 推送证书的密码. 默认为空
     */
    private String password = "";
    
    private Logger logger;
    
    public IOSExecutor (boolean isProduction, Configuration config, int customType) {
        production = isProduction;
        // 根据是否是正式环境初始化证书和密码
        if (production) {
            InputStream keystoreInputStream = this.getClass().getResourceAsStream(config.getValue(PushConstant.ConfigKey.KEYSTORE_PRODUCT + customType));
            keystore = this.inputStreamToByte(keystoreInputStream);
            password = config.getValue(PushConstant.ConfigKey.KEYSTORE_PRODUCT_PASSWORD + customType);
        } else {
            InputStream keystoreInputStream = this.getClass().getResourceAsStream(config.getValue(PushConstant.ConfigKey.KEYSTORE_DEVELOPMENT + customType));
            keystore = this.inputStreamToByte(keystoreInputStream);
            password = config.getValue(PushConstant.ConfigKey.KEYSTORE_DEVELOPMENT_PASSWORD + customType);
        }
        
        logger = LoggerTool.logFactory(IOSExecutor.class);
    }

    /**
     * 向单台设备推送不同的消息. 此接口允许同时推送多台设备. 方式为构建一个PayloadPerDevice并加入到一个list里面里
     * 
     * TODO 这里需要改善. 返回值是一个Notification的列表, 可以使用此列表判断哪些消息推送成功, 哪些没有
     * 
     * @param payloadPerDevices
     * @return 
     */
    public void pushToOneDevice(List<PayloadPerDevice> payloadPerDevices) {
        logger.info("推送至" + payloadPerDevices.size() + "单个设备");
        try {
            Push.payloads(keystore, password, production, payloadPerDevices);
        } catch (CommunicationException e) {
        	logger.warn("推送通信异常(pushToOneDevice): " + e.getMessage());
        } catch (KeystoreException e) {
        	logger.warn("推送KeystoreException异常(pushToOneDevice): " + e.getMessage());
        }
    }
    
    /**
     * 同时向多台设备推送相同的消息.主要用于群发消息
     * 
     * TODO 这里需要改善. 返回值是一个Notification的列表, 可以使用此列表判断哪些消息推送成功, 哪些没有
     * 
     * @param IOSMessageToDevices message
     */
    public void pushToDevices(IOSMessageToDevices message) {
        List<String> devices = message.getDevices();
        logger.info("推送至" + devices.size() + "批量设备");
        Payload payLoad = message.getPayLoad();
        
        try {
            Push.payload(payLoad, keystore, password, production, devices);
//            for (PushedNotification notification : notifications) {
//                
//            }
        } catch (CommunicationException e) {
        	logger.warn("推送通信异常(pushToDevices): " + e.getMessage());
        } catch (KeystoreException e) {
        	logger.warn("推送KeystoreException异常(pushToDevices): " + e.getMessage());
        }
    }
    
    /**
     * javapns的推送keystore好像有问题. 以file和string类型的参数传给payload都会在jar里造成文件路径错误. 这个很郁闷. 所以现在用了byte的方式
     * 
     * @param in
     * @return
     */
    private byte[] inputStreamToByte(InputStream in) {
        
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        if (in != null) {
            int nRead;
            byte[] data = new byte[16384];

            try {
                while ((nRead = in.read(data, 0, data.length)) != -1) {
                  buffer.write(data, 0, nRead);
                }
                buffer.flush();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
        return buffer.toByteArray();
    }
    
    public static void main(String[] args) {
        // TODO Auto-generated method stub

        File file = new File(IOSExecutor.class.getResource("/yohoe_apns_development_key.p12").getPath());
        System.out.println(file.getPath());
        
    }
}
