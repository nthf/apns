package com.yohoinc.ios.data;

import java.util.List;
/**
 * redis的数据服务类. 还没有使用
 * @author dan
 *
 */
public class RedisDataService implements DataService {

    public RedisDataService (String customKey) {
    	
    }
    
    @Override
    public int countTask() {
        // TODO Auto-generated method stub
        
        return 0;
    }

    @Override
    public List<Task> getTasks(int limit) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean setTaskStatus(long id, int status) {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public List<String> getDeviceTokensByUser(Long userID) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public List<String> getDeviceTokens(int offset, int limit) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public int countAllDeviceToken() {
        // TODO Auto-generated method stub
        return 0;
    }

}
