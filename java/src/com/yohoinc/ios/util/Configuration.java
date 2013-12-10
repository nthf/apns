package com.yohoinc.ios.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
/**
 * 读取properties文件. 从网上搞来的读取配置文件的类
 * 
 * @author dan
 *
 */
public class Configuration {
    private Properties propertie;
    private InputStream inputFile;
    private FileOutputStream outputFile;

    /**
     * 初始化Configuration类
     * @param filePath 要读取的配置文件的路径+名称
     */
    public Configuration(String filePath)
    {
        propertie = new Properties();
        try {
            inputFile = Configuration.class.getResourceAsStream( filePath );
            propertie.load(inputFile);
            inputFile.close();
        } catch (FileNotFoundException ex) {
            System.out.println("读取属性文件--->失败！- 原因：文件路径错误或者文件不存在");
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println("装载文件--->失败!");
            ex.printStackTrace();
        }
    }

    /**
     * 重载函数，得到key的值
     * @param key 取得其值的键
     * @return key的值
     */
    public String getValue(String key)
    {
        if(propertie.containsKey(key)){
            String value = propertie.getProperty(key);//得到某一属性的值
            return value;
        }
        else
            return "";
    }

    /**
     * 重载函数，得到key的值
     * @param fileName properties文件的路径+文件名
     * @param key 取得其值的键
     * @return key的值
     */
    public String getValue(String fileName, String key)
    {
        try {
            String value = "";
            inputFile = new FileInputStream(fileName);
            propertie.load(inputFile);
            inputFile.close();
            if(propertie.containsKey(key)) {
                value = propertie.getProperty(key);
                return value;
            }else
                return value;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "";
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    /**
     * 清除properties文件中所有的key和其值
     */
    public void clear()
    {
        propertie.clear();
    }
    
    /**
     * 改变或添加一个key的值，当key存在于properties文件中时该key的值被value所代替，
     * 当key不存在时，该key的值是value
     * @param key 要存入的键
     * @param value 要存入的值
     */
    public void setValue(String key, String value)
    {
        propertie.setProperty(key, value);
    }

    /**
     * 将更改后的文件数据存入指定的文件中，该文件可以事先不存在。
     * @param fileName 文件路径+文件名称
     * @param description 对该文件的描述
     */
    public void saveFile(String fileName, String description)
    {
        try {
            outputFile = new FileOutputStream(fileName);
            propertie.store(outputFile, description);
            outputFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    public Properties getProperties () {
    	return this.propertie;
    }

    public static void main(String[] args)
    {
        String configFilePath = "/global.properties";
//        System.out.println(Configuration.class.getResource( configFilePath ).getPath());
        Configuration rc = new Configuration(configFilePath);//相对路径
        String ip = rc.getValue("port");//以下读取properties文件的值

        System.out.println("ip = " + ip);//以下输出properties读出的值

//        Configuration saveCf = new Configuration();
//        saveCf.setValue("min", "10");
//        saveCf.setValue("max", "1000");
//        saveCf.saveFile(".\config\save.perperties");

    }//end main()

}//end class ReadConfigInfo