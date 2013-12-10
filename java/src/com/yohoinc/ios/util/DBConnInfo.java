package com.yohoinc.ios.util;

import java.util.List;
/**
 * 数据库链接信息实体类. 包含了数据库名, 数据库用户名, 密码以及读和写数据库的访问路径的列表
 * @author dan
 *
 */
public class DBConnInfo {
    /**
     * 写数据库的访问链接的字符串列表
     */
    private List<String> writers;
    /**
     * 读数据库的访问链接的字符串列表
     */
    private List<String> readers;
    /**
     * 数据库连接的用户名
     */
    private String username;
    /**
     * 数据库连接的密码
     */
    private String password;
    /**
     * 数据库名
     */
    private String dbName;
    
    public String getDbName() {
        return dbName;
    }
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }
    public List<String> getWriters() {
        return writers;
    }
    public void setWriters(List<String> writers) {
        this.writers = writers;
    }
    public List<String> getReaders() {
        return readers;
    }
    public void setReaders(List<String> readers) {
        this.readers = readers;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    
}
