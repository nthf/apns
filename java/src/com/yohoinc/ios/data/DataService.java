package com.yohoinc.ios.data;

import java.util.List;
/**
 * 数据服务接口类.
 * 
 * 因为数据服务可能由多种方式来提供. 如mysql或者内存数据库. 如redis. 所以抽象出接口. 以便以后切换
 * 
 * @author dan
 *
 */
public interface DataService {

    /**
     * 计算任务的数量
     * @return
     */
    public int countTask();
    /**
     * 获取任务的列表. 因为任务被执行后, 任务状态会被设置成已推送, 所以这里不需要有offset
     * @param limit
     * @return
     */
    public List<Task> getTasks(int limit);
    /**
     * 设置任务状态
     * @param id
     * @param status
     * @return
     */
    public boolean setTaskStatus(long id, int status);
    /**
     * 根据用户id获取设备号列表
     * @param userID
     * @return
     */
    public List<String> getDeviceTokensByUser(Long userID);
    /**
     * 根据偏移量和限制来获取设备号
     * @param offset
     * @param limit
     * @return
     */
    public List<String> getDeviceTokens(int offset, int limit);
    /**
     * 计算所有设备号的数量
     * @return
     */
    public int countAllDeviceToken();
}
