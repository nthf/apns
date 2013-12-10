#!/bin/bash
# 重启消息处理脚本
# 需要设置cron 设置方法为
# crontab -e
# 插入: */5 * * * * sh /Data/code/mobilepush/java/push.sh > /dev/null 2>&1
# 每一分钟执行一次
#
pid_num=`ps -ef | grep "push.jar" | grep -v grep | wc -l`

if [ $pid_num -le 0 ] #如果进程数小于等于 1 （系统进程不存在） 为错误
then
T_error=`date +%Y-%m-%d\ %H\:%M\:%S`
touch check_server_err.log
echo “$T_error ios apn server is down! Ready to restart!” >> check_server_err.log
ulimit -n 10240
cd /Data/code/mobilepush/java/
/usr/bin/java -cp "yohopush.jar:lib/*" com.yohoinc.ios.push.PushScheduler true 0 & 
/usr/bin/java -cp "yohopush.jar:lib/*" com.yohoinc.ios.push.PushScheduler true 2 & 
fi #if条件结束