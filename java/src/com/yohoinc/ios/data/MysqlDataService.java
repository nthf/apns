package com.yohoinc.ios.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.yohoinc.ios.util.DBClient;
import com.yohoinc.ios.util.PushConstant;

/**
 * mysql的数据服务类. 目前使用此方式来存储和推送消息
 * 
 * @author dan
 *
 */
public class MysqlDataService implements DataService {

    private String customKey = "";
    private DBClient client = null;
    
    public MysqlDataService(String customKey, boolean production) {
        this.customKey = customKey;
        client = new DBClient(production);
    }
    
    /**
     * 计算还没有推送的任务的数量. customKey必须的. 状态为0
     */
    @Override
    public int countTask() {
        // TODO Auto-generated method stub
        int count = 0;
        List<Object> params = new ArrayList<Object>();
        params.add(customKey);
        ResultSet rs = client.executeQuery("READER_COUNT_TASK", params);
        try {
            if (rs.next()) {
                count = rs.getInt("count");
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        
        return count;
    }

    @Override
    /**
     * 根据限制条件获取任务列表.
     * customKey必须. 并且状态是0的任务. 获取后, 将其装配到Task实体类中. 返回实体类的列表
     */
    public List<Task> getTasks(int limit) {
        List<Task> tasks = new ArrayList<Task>();
        List<Object> params = new ArrayList<Object>();
        params.add(customKey);
        params.add(limit);
        ResultSet rs = client.executeQuery("READER_GET_TASKS", params);
        try {
            while (rs.next()) {
                Task task = new Task();
                // 数据库中存的是date类型的, 所以这里的timestamp不是很精确
                task.setCreateTime(rs.getTimestamp("create_time").getTime());
                task.setCustomKey(customKey);
                task.setId(rs.getLong("id"));
                task.setSchedule(rs.getString("schedule"));
                task.setStatus(PushConstant.SysConstant.TASK_STATUS_IN_PROGRESS);
                task.setTaskContent(rs.getString("task_content"));
                task.setTaskRule(rs.getInt("task_rule"));
                task.setTaskType(rs.getInt("task_type"));
                // 数据库中存的是date类型的, 所以这里的timestamp不是很精确
                task.setUpdateTime(rs.getTimestamp("update_time").getTime());
                task.setUserID(rs.getLong("user_id"));
                // 获取后设置任务为正在处理, 以防止被其他线程获取造成重复推送
                setTaskStatus(task.getId(), PushConstant.SysConstant.TASK_STATUS_IN_PROGRESS);
                tasks.add(task);
            }
            
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        
        return tasks;
    }

    @Override
    /**
     * 设置任务的状态.
     */
    public boolean setTaskStatus(long id, int status) {
        boolean result = true;
        
        List<Object> params = new ArrayList<Object>();
        params.add(status);
        params.add(id);
        
        int rowNum = client.executeUpdate("WRITER_UPDATE_TASK_STATUS", params);
        
        if (rowNum <= 0) {
            result = false;
        }
        return result;
    }
    
    @Override
    /**
     * 根据用户id获取此用户的设备号列表. 因为一个用户可能同时登录多台设备
     */
    public List<String> getDeviceTokensByUser(Long userID) {
        
        List<String> tokens = new ArrayList<String>();
        
        List<Object> params = new ArrayList<Object>();
        params.add(customKey);
        params.add(userID);
        
        ResultSet rs = client.executeQuery("READER_SELECT_DEVICE_TOKENS_BY_USER", params);
        try {
            while (rs.next()) {
                tokens.add(rs.getString("device_token"));
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return tokens;
    }

    @Override
    /**
     * 获取所有的设备号. 需要有限制条件
     */
    public List<String> getDeviceTokens(int offset, int limit) {
        List<String> tokens = new ArrayList<String>();
        
        List<Object> params = new ArrayList<Object>();
        params.add(customKey);
        params.add(offset);
        params.add(limit);
        
        ResultSet rs = client.executeQuery("READER_SELECT_DEVICE_TOKENS", params);
        try {
            while (rs.next()) {
                tokens.add(rs.getString("device_token"));
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return tokens;
    }

    @Override
    /**
     * 计算所有设备号的数量
     */
    public int countAllDeviceToken() {
        int result = 0;
        List<Object> params = new ArrayList<Object>();
        params.add(customKey);
        
        ResultSet rs = client.executeQuery("READER_COUNT_ALL_DEVICE_TOKENS", params);
        
        try {
            if (rs.next()) {
                result = rs.getInt("count");
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
}
