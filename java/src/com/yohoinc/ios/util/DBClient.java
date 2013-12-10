package com.yohoinc.ios.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
/**
 * 数据库连接客户端接口类. 数据库接口主要提供两种方式:
 * 1. 更新数据库接口: executeUpdate
 * 2. 读取数据库接口: executeQuery
 * 
 * 另外还有自动判断接口: executeSql
 * 
 * SQL语句统一在sql.script文件中编写,并赋予此语句一个名字. 如果语句中只有读操作, 则名字需要加上前缀READER_
 * 如果语句中有写操作, 则名字需要加上WRITER_. READER和WRITER会被用来判读使用读取的链接还是使用写的连接来访问数据库.
 * 默认情况如果没有前缀都会向写入数据库执行sql
 * @author dan
 *
 */
public class DBClient {

    /**
     * 数据库链接信息
     */
    private DBConnInfo connInfo;
    /**
     * 实际执行的写入池. 类型为键值对的map类型, 键值为连接的字符串
     */
    private Map<String, Connection> writerConns;
    /**
     * 实际执行的读取池. 类型为键值对的map类型, 键值为连接的字符串
     */
    private Map<String, Connection> readerConns;
    
    public DBClient (boolean production) {
        this.buildConnInfo(production);
    }
    
    /**
     * 根据配置文件初始化连接类. 将配置文件中的配置赋值给当前类的成员变量
     * @param production
     */
    private void buildConnInfo (boolean production) {
        connInfo = new DBConnInfo();
        Configuration config = null;
        
        if (production) {
            config = new Configuration(PushConstant.SysConstant.DB_CONFIG_PATH_PRODUCT);
        } else {
            config = new Configuration(PushConstant.SysConstant.DB_CONFIG_PATH_TEST);
        }
        
        connInfo.setUsername(config.getValue(PushConstant.ConfigKey.DB_APN_USERNAME));
        connInfo.setPassword(config.getValue(PushConstant.ConfigKey.DB_APN_PASSWORD));
        connInfo.setDbName(config.getValue(PushConstant.ConfigKey.DB_APN_DBNAME));
        
        String readersStr = config.getValue(PushConstant.ConfigKey.DB_APN_READERS);
        List<String> readers = Arrays.asList(readersStr.trim().split(","));
        connInfo.setReaders(readers);
        
        String writersStr = config.getValue(PushConstant.ConfigKey.DB_APN_WRITERS);
        List<String> writers = Arrays.asList(writersStr.trim().split(","));
        connInfo.setWriters(writers);
        
    }
    
    /**
     * 执行sql方法. 如果用户不知道执行的sql是读还是写, 就访问此方法. 调用者自己将返回值进行强制类型转换后访问
     * @param sql 执行的sql的名字
     * @param params 执行的sql的参数(sql均为preparedStatement类型), 可以传参
     * @return ResultSet(如果是读取操作) 或者 int(如果是更新操作)
     */
    public Object executeSql(String sql, List<Object> params) {
        boolean isWriter = this.checkIsWriter(sql);
        Object result = null;
        
        if (isWriter) {
            result = this.executeUpdate(sql, params);
        } else {
            result = this.executeQuery(sql, params);
        }
        
        return result;
    }
    
    /**
     * 执行查询的方法. 此方法只为只读的sql语句进行查找. 返回ResultSet. 用户需要自己关闭ResultSet
     * 
     * @param sql 执行的sql的名字
     * @param params 执行的sql的参数(sql均为preparedStatement类型), 可以传参
     * @return ResultSet 注意手动关闭此结果类
     */
    public ResultSet executeQuery(String sql, List<Object> params) {
        
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = routeConnection(sql, params);
        try {
            resultSet = preparedStatement.executeQuery();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resultSet;
    }
    
    /**
     * 执行更新的方法. 此方法只为写入的sql语句进行查找. 返回受影响的行数
     * 
     * @param sql
     * @param params
     * @return int 返回的受影响的行数
     */
    public int executeUpdate (String sql, List<Object> params) {
        
        int result = 0;
        PreparedStatement preparedStatement = routeConnection(sql, params);
        
        try {
            result = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
    
    /**
     * 根据sql名字和参数获得 PreparedStatement. sql可以是只读的, 也可以是写入的. 此方法根据sql名的前缀来进行判断.
     * 
     * @param sql
     * @param params
     * @return PreparedStatement
     */
    private PreparedStatement routeConnection(String sql, List<Object> params) {
        PreparedStatement preparedStatement = null;
        
        boolean routeToWriter = true;
        // 判断是否是写的sql
        routeToWriter = checkIsWriter(sql);
        
        List<String> conns = null;
        Map<String, Connection> connMap = null;
        
        // 如果是写的sql, 则获取写的链接信息, 并将句柄指向写的连接池
        if (routeToWriter) {
            conns = connInfo.getWriters();
            
            if (this.writerConns == null) {
                this.writerConns = new HashMap<String, Connection>();
            }
            connMap = this.writerConns;
        }
        // 否则指向读的连接池
        else {
            conns = connInfo.getReaders();
            
            if (this.readerConns == null) {
                this.readerConns = new HashMap<String, Connection>();
            }
            connMap = this.readerConns;
        }
        
        if (conns != null && conns.size() > 0) {
            // 从链接池中随机获取一个链接, 首先从配置里得到一个链接的字符串, 此字符串会作为获取连接池中连接的key值
            int index = (int) (Math.random() * (conns.size()));
            String conn = conns.get(index);
            Connection connection = null;
            
            if (connMap.containsKey(conn)) {
                connection = connMap.get(conn);
            } else {
                // 如果连接池中没有此连接, 则创建一个并加入连接池
                String host = conn.split(":")[0];
                int port = Integer.valueOf(conn.split(":")[1]).intValue();
                connection = this.getConnection(host, port, connInfo.getDbName(), connInfo.getUsername(), connInfo.getPassword());
                connMap.put(conn, connection);
            }
            
            Configuration config = new Configuration(PushConstant.SysConstant.SQL_SCRIPT_CONFIG_PATH);
            String preparedSql = config.getValue(sql);
            try {
                preparedStatement = connection.prepareStatement(preparedSql);
                if (params != null && params.size() > 0) {
                    // 调用设置值的方法. 此方法向preparedstatement中插入值
                    setValues(preparedStatement, params);
                }
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        return preparedStatement;
    }
    
    /**
     * 检查是否是写的sql. 这个方法需要晚上. 目前只是判断是否以READER开头.
     * 
     * @param sql
     * @return
     */
    private boolean checkIsWriter (String sql){
        
        boolean isWriter = true;
        
        if (sql.startsWith("READER_")) {
            isWriter = false;
        }
        
        if (isWriter == false) {
            // TODO 这里可以通过正则对sql语句本身进行再判断
        }
        
        return isWriter;
    }
    
    /**
     * 向PreparedStatement中插入值
     * @param pstmt
     * @param values
     * @throws SQLException
     */
    private void setValues(PreparedStatement pstmt, List<Object> values) throws SQLException {  
        // 循环，将SQL语句参数列表中的值依次赋给预执行语句  
        for (int i = 0; i < values.size(); i++) {  
            Object v = values.get(i);  
            // 注意，setObject()方法的索引值从1开始，所以有i+1  
            pstmt.setObject(i + 1, v);  
        }  
    }  
    
    /**
     * 创建连接方法. 目前使用mysql的方式链接
     * @param host
     * @param port
     * @param dbName
     * @param userName
     * @param password
     * @return
     */
    private Connection getConnection (String host, int port, String dbName, String userName, String password) {
        Connection conn = null;
        
        try {
            Properties connectionProps = new Properties();
            connectionProps.put("user", userName);
            connectionProps.put("password", password);
//            connectionProps.put("useUnicode", true);
            // 使用utf-8链接, 否则会出错
            connectionProps.put("characterEncoding", "utf-8");
            connectionProps.put("autoReconnect", "true");
            conn = DriverManager.getConnection(
                       "jdbc:mysql://" + host +
                       ":" + port + "/" + dbName, connectionProps);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
        
        return conn;
    }
    
    public static void main(String[] strs) {
        String sql = "UPDATE_TEST";
        DBClient client = new DBClient(false);
        List<Object> params = new ArrayList<Object>();
        params.add(242);
        System.out.println(client.executeUpdate(sql, params));
        
        sql = "UPDATE_TEST1";
        params = new ArrayList<Object>();
        params.add(242);
        System.out.println(client.executeUpdate(sql, params));
        
//        ResultSet rs = client.executeQuery(sql, params);
//        try {
//            while (rs.next()) {
//                System.out.println(rs.getInt("id"));
//                System.out.println(rs.getString("custom_key"));
//                System.out.println(rs.getString("device_token"));
//            }
//        } catch (SQLException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } finally {
//            if (rs != null) {
//                try {
//                    rs.close();
//                } catch (SQLException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//            }
//        }
        
    }
}
