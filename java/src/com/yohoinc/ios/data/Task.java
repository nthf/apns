package com.yohoinc.ios.data;
/**
 * 任务实体类. 对应数据库中的tbl_push_task表
 * 
 * @author dan
 *
 */
public class Task {
    
    /**
     * 应用的key值.
     */
    private String customKey;
    /**
     * 任务的id
     */
    private long id;
    /**
     * 任务的推送内容, 此内容是以json格式存储, 后面还要加工
     */
    private String taskContent;
    /**
     * 任务类型, 以被推送器决定需要以何种方式来推送
     */
    private int taskType;
    /**
     * 任务规则. 目前没有用到
     */
    private int taskRule;
    /**
     * 用户id. 需要向此用户推送
     */
    private long userID;
    /**
     * 是否加入日程安排. 目前没有用到
     */
    private String schedule;
    /**
     * 任务状态. 0待推送 1推送成功 2推送失败 3正在推送
     */
    private int status;
    /**
     * 创建时间
     */
    private long createTime;
    /**
     * 更新时间
     */
    private long updateTime;
    
    public String getCustomKey() {
        return customKey;
    }
    public void setCustomKey(String customKey) {
        this.customKey = customKey;
    }
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getTaskContent() {
        return taskContent;
    }
    public void setTaskContent(String taskContent) {
        this.taskContent = taskContent;
    }
    public int getTaskType() {
        return taskType;
    }
    public void setTaskType(int taskType) {
        this.taskType = taskType;
    }
    public int getTaskRule() {
        return taskRule;
    }
    public void setTaskRule(int taskRule) {
        this.taskRule = taskRule;
    }
    public long getUserID() {
        return userID;
    }
    public void setUserID(long userID) {
        this.userID = userID;
    }
    public String getSchedule() {
        return schedule;
    }
    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public long getCreateTime() {
        return createTime;
    }
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
    public long getUpdateTime() {
        return updateTime;
    }
    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

}
