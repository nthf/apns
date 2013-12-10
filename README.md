apns
====

(APNS) 使用说明

# APNS简介
   APNS是用Java实现的一个用于手机客户端向苹果服务器推送通知的程序. 

# APNS程序的配置
1. 将苹果服务推送的证书放入keystore下(注意需要是.p12格式的). 
2. 修改config/global.properties的路径配置为上面的证书
3. 修改config/db.properties和config/log4j.properties中数据库配置

# 执行服务器脚本
sh push.sh

java -cp "push.jar:lib/*" com.yohoinc.ios.push.PushScheduler true 0 

crontab -e

插入: */5 * * * * sh /Data/code/apns/java/push.sh > /dev/null 2>&1
