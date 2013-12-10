package com.yohoinc.ios.util;
/**
 * 推送系统的常量类.
 * 
 * 常量类分两个内部类. 一个为ConfigKey常量类. 这个类包含的是所有配置文件中的键值的字符串. 在配置文件引用常量的时候, 使用此类中的
 * 字符串作为配置类的键值, 如:
 * 
 * (Configuration) config.getValue(PushConstant.ConfigKey.KEYSTORE_PRODUCT);
 * 
 * 另一个为系统常量. 系统常量是系统中使用的不变的字符串或者值, 如任务类型, 客户端的key值等. 使用常量类为数值赋予对应的含义. 具体部署的时候也可以
 * 按照实际情况进行配置
 * 
 * @author dan
 *
 */
public class PushConstant {
    
    /**
     * ConfigKey常量类. 这个类包含的是所有配置文件中的键值的字符串.
     * @author dan
     *
     */
    public final static class ConfigKey {
        /**
         * 正式环境key文件的存储位置
         */
        public static final String KEYSTORE_PRODUCT = "keyStore_product";
        /**
         * 开发环境key文件的存储位置
         */
        public static final String KEYSTORE_DEVELOPMENT = "keyStore_development";
        /**
         * 正式环境key文件的密码
         */
        public static final String KEYSTORE_PRODUCT_PASSWORD = "keyStore_product_password";
        /**
         * 开发环境key文件的密码
         */
        public static final String KEYSTORE_DEVELOPMENT_PASSWORD = "keyStore_development_password";
        
        
        /**
         * 推送的默认过期时间
         */
        public static final String PUSH_EXPIRE_TIME = "push_expire_time";
        /**
         * 默认无用户ID(系统用户)的用户ID的值
         */
        public static final String PUSH_TASK_NO_USER_ID = "push_task_no_user_id";
        /**
         * 一次推送获取任务的最大数(任务可能有很多, 但一次获取任务不能全部获取
         */
        public static final String PUSH_ONCE_TASK_NUM = "push_once_task_num";
        /**
         * 一次向苹果服务器推送的消息的条数. 这个会影响推送性能
         */
        public static final String PUSH_ONCE_MESSAGE_NUM = "push_once_message_num";
        /**
         * 当向所有用户推送时, 一次从数据库中获取的设备token的数量
         */
        public static final String PUSH_GET_TOKEN_FROM_DB_ONCE = "push_get_token_from_db_once";
        /**
         * 推送提醒的声音, 默认为default
         */
        public static final String PUSH_ALERT_SOUND = "push_alert_sound";
        
        /**
         * 数据库用户名键值
         */
        public static final String DB_APN_USERNAME = "apn_username";
        /**
         * 数据库密码键值
         */
        public static final String DB_APN_PASSWORD = "apn_password";
        /**
         * 数据库名键值
         */
        public static final String DB_APN_DBNAME = "apn_dbname";
        /**
         * 写入的数据库的键值. 写入数据库值为以逗号分开的***.***.***.***:****格式的字符串
         */
        public static final String DB_APN_WRITERS = "apn_writers";
        /**
         * 读取的数据库的键值. 读取数据库值为以逗号分开的***.***.***.***:****格式的字符串
         */
        public static final String DB_APN_READERS = "apn_readers";
        
        /**
         * 任务队列的类型. 默认为mysql的数据库类型. 之后可以改为redis类型
         */
        public static final String TASK_QUEUE_TYPE = "task_queue_type";
    }
    
    /**
     * 系统常量类.
     * 
     * @author dan
     *
     */
    public final static class SysConstant {
        
        /**
         * 根据用户来推送的任务类型
         */
        public static final int TASK_TYPE_PUSH_BY_USER = 0;
        /**
         * 向所有用户推送的任务类型
         */
        public static final int TASK_TYPE_PUSH_ALL = 1;
        /**
         * 根据规则来推送的任务类型
         */
        public static final int TASK_TYPE_PUSH_BY_RULE = 2;
        /**
         * 准备推送的任务状态.
         */
        public static final int TASK_STATUS_STAND_BY = 0;
        /**
         * 成功推送了的任务的状态
         */
        public static final int TASK_STATUS_SENT = 1;
        /**
         * 推送错误的任务状态
         */
        public static final int TASK_STATUS_ERROR = 2;
        /**
         * 正在推送中的任务的状态
         */
        public static final int TASK_STATUS_IN_PROGRESS = 3;
        
        /**
         * 向IOS设备推送时的定制化值的键值.
         */
        public static final String PUSH_IOS_CUSTOME_PROP_KEY = "data";
        
        /**
         * 系统配置文件的路径
         */
        public static final String GLOBAL_CONFIG_PATH = "/global.properties";
        /**
         * 正式环境数据库文件路径
         */
        public static final String DB_CONFIG_PATH_PRODUCT = "/db.properties";
        /**
         * 开发环境数据库文件路径
         */
        public static final String DB_CONFIG_PATH_TEST = "/db_test.properties";
        /**
         * 数据库sql脚本的配置文件路径
         */
        public static final String SQL_SCRIPT_CONFIG_PATH = "/sql.script";
    }

}
