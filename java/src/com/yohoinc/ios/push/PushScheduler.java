package com.yohoinc.ios.push;

import org.apache.log4j.Logger;

import com.yohoinc.ios.util.Configuration;
import com.yohoinc.ios.util.LoggerTool;
import com.yohoinc.ios.util.PushConstant;
/**
 * 整个推送脚本的入口类. 执行此脚本按顺序提供两个参数:
 * 
 * 1. 是否是正式环境, 1为正式, 0为开发环境
 * 2. 应用类型.
 * 
 * 目前没有使用多线程的方式来实现. 如果以后负载大了就使用多线程的方式来实现
 * @author dan
 *
 */
public class PushScheduler {
    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

        Configuration config = new Configuration(PushConstant.SysConstant.GLOBAL_CONFIG_PATH);
        
        boolean product = false;
        // 推送脚本处理的消息类型. 0为yoho.cn, 1为有货. 之后可以扩展
        int customType = 0;
        
        if (args.length > 0) {
            product = Boolean.valueOf(args[0]);
        }
        
        if (args.length > 1) {
            customType = Integer.valueOf(args[1]);
        }
        
        // 设置日志系统的配置环境
        LoggerTool.setProduction(product);
        Logger logger = LoggerTool.logFactory(PushScheduler.class);
        logger.info("脚本开始时间");
        
        // 执行推送脚本
        PushWorker worker = new PushWorker(product, config, customType);
        worker.run();
    }

}
