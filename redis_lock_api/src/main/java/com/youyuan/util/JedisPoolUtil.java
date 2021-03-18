package com.youyuan.util;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 类名称：JedisPoolUtil <br>
 * 类描述： jedis连接池 <br>
 *
 * @author zhangyu
 * @version 1.0.0
 * @date 创建时间：2021/3/17 22:28<br>
 */
public class JedisPoolUtil {

    //标记为static内存中只有一份，volatile多线程数据修改时是可见的，修改完之后立即写入到主内存
    private static volatile JedisPool jedisPool=null;

    private JedisPoolUtil(){}

    /**
     * 单例获取jedis连接池对象
     * @return
     */
    public static JedisPool getInstance(){
        //为了提高性能先判断不为空直接获取
        if (jedisPool!=null){
            return jedisPool;
        }
        //为空判断双重检查
        if (jedisPool==null){
            synchronized (JedisPoolUtil.class){
                if (jedisPool==null){
                    //jedispool连接池的配置信息
                    GenericObjectPoolConfig poolConfig=new GenericObjectPoolConfig();
                    //连接池最大创建连接数
                    poolConfig.setMaxTotal(1000);
                    //设置连接池最多有多少个连接是空闲的
                    poolConfig.setMaxIdle(32);
                    //设置连接池最少有多少个连接是空闲的
                    poolConfig.setMinIdle(10);
                    //设置连接超时等待时间,单位秒
                    poolConfig.setMaxWaitMillis(100*1000);
                    //设置连接拿到后是否测试连通性,true测试
                    poolConfig.setTestOnBorrow(true);
                    jedisPool=new JedisPool(poolConfig,"127.0.0.1",6379);
                }
            }
        }
        return jedisPool;
    }

    /**
     * 连接用完后放回池子
     * @param jedisPool 连接池
     * @param jedis 连接
     */
    public static void relace(JedisPool jedisPool, Jedis jedis){
        if (jedis!=null){
            if (jedisPool==null){
                jedisPool=getInstance();
            }
            jedisPool.returnResource(jedis);
        }
    }
}
