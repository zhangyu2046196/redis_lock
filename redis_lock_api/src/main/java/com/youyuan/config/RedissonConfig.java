package com.youyuan.config;

import org.redisson.Redisson;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 类名称：RedissonConfig <br>
 * 类描述： redisson配置类 <br>
 *
 * @author zhangyu
 * @version 1.0.0
 * @date 创建时间：2021/3/17 23:26<br>
 */
@Configuration
public class RedissonConfig {

    /**
    * 方法名: redisson <br>
    * 方法描述: redisson配置类 <br>
    *
    * @return {@link Redisson 返回redisson对象内容信息 }
    * @date 创建时间: 2021/3/17 23:28 <br>
    * @author zhangyu
    */
    @Bean
    public Redisson redisson(){
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379").setDatabase(0);
        return (Redisson)Redisson.create(config);
    }

}
