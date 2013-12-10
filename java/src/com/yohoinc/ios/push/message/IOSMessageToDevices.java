package com.yohoinc.ios.push.message;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.yohoinc.ios.util.Configuration;
import com.yohoinc.ios.util.PushConstant;

import javapns.notification.Payload;
import javapns.notification.PushNotificationPayload;
/**
 * 同时向多台设备发送同样的内容. 这个类主要是用来做群推送.
 * 
 * 此类的对象会被类<code>IOSExecutor.pushToDevices</code>作为参数进行发送.
 * 
 * @author dan
 *
 */
public class IOSMessageToDevices {

    /**
     * payload. 推送的消息载体. 内封装的消息内容还有未读数等
     */
    private PushNotificationPayload payLoad;
    /**
     * 向这些设备推送.
     */
    private List<String> devices;
    /**
     * 系统配置. 配置内容来自global.properties
     */
    private Configuration config;
    /**
     * 苹果服务配置项
     */
    private JSONObject apsDictionary;
    
    public IOSMessageToDevices (Configuration config) {
        payLoad = PushNotificationPayload.complex();

        this.config = config;
    }
    
    public Payload getPayLoad () {
        return payLoad;
    }
    
    public List<String> getDevices () {
        return devices;
    }
    
    /**
     * 设置自定义的推送内容. 自定义是键值对形式. 目前yoho限定key为"data"
     * 
     * @param key
     * @param data
     */
    public void setCustomNotice (String key, String data) {
        
        try {
            payLoad.addCustomDictionary(key, data);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * 设置在客户端收到推送通知提醒的声音
     * 
     * @param sound
     */
    public void setSound (String sound) {
        // 2012-07-02 增加声音的参数, 暂时不定制声音
        try {
			payLoad.addSound(sound);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    /**
     * 设置展示的内容消息. 这里的text会被作为推送接收到消息的时候展示在屏幕上的内容
     * 
     * @param text
     */
    public void setText (String text) {
        try {
            payLoad.addAlert(text);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * 设置在应用上展示的数字
     * 
     * @param number
     */
    public void setBage (int number) {
        if (number <= 0) {
            number = 0;
        }
        
        try {
            payLoad.addBadge(number);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * 添加设备. 有两种方式, 清空后添加或者增加到队列后面.
     * 
     * @param devices 需要添加的设备列表
     * @param appended 是否添加到后面, 如果为否, 则会清空后再添加
     */
    public void addDevices(List<String> devices, boolean appended) {
        if (this.devices == null || appended == false) {
            this.devices = new ArrayList<String>();
        }
        this.devices.addAll(devices);
    }
    
    /**
     * 设置过期时间.如果过期时间<=0, 则使用系统默认的过期时间. 默认为30秒
     * @param expire
     */
    public void setExpire (int expire) {
        if (expire <= 0) {
            expire = Integer.valueOf(config.getValue(PushConstant.ConfigKey.PUSH_EXPIRE_TIME));
        }
        payLoad.setExpiry(expire);
    }
    
    /**
     * 设置通知, 只在后台运行. 不需要提醒
     *  
     * 因 调用 Push.contentAvailable(keystore, password, production, devices)会推一次, 
     * 再 调用 Push.payload(payLoad, keystore, password, production, devices)又会推一次,
     * 而 实际需要的是上面这两个操作只推一次, 所以这边采用payLoad put(key, value)方法去设置苹果服务的配置项.
     */
    public void setContentAvailable () {
        try {
        	this.apsDictionary = new JSONObject();
        	this.apsDictionary.put("content-available", 1);
        	payLoad.getPayload().put("aps", this.apsDictionary);
	    } catch (JSONException e) {
	    	// TODO Auto-generated catch block
	    	e.printStackTrace();
	    }
    }
    
}
