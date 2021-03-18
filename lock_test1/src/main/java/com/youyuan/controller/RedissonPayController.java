package com.youyuan.controller;

import cn.hutool.core.util.StrUtil;
import com.youyuan.constant.GoodsConstant;
import com.youyuan.util.JedisPoolUtil;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 类名称：PayController <br>
 * 类描述： 模拟电商购物库存controller <br>
 *
 * @author zhangyu
 * @version 1.0.0
 * @date 创建时间：2021/3/16 11:38<br>
 */
@RestController
@RequestMapping("/redisson")
public class RedissonPayController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 服务端口号
     */
    @Value("${server.port}")
    private String serverPort;

    @Autowired
    private Redisson redisson;

    /**
     * 方法名:  buy <br>
     * 方法描述: 购买商品扣减库存 <br>
     *
     * @return {@link String 返回购买后文案 }
     * @date 创建时间: 2021/3/16 13:42 <br>
     * @author zhangyu
     */
    @GetMapping("/buy")
    public String buy() {
        //redis加锁
        String lockValue = UUID.randomUUID().toString() + Thread.currentThread().getName();
        RLock lock = redisson.getLock(GoodsConstant.LOCK_KEY);
        try {
            //加锁
            lock.lock();
            String result = stringRedisTemplate.opsForValue().get(GoodsConstant.GOODS_KEY);
            int dbResult = (StrUtil.isBlank(result) ? 0 : Integer.parseInt(result));
            if (dbResult > 0) {
//            stringRedisTemplate.opsForValue().set(GoodsConstant.GOODS_KEY, String.valueOf(dbResult));
                stringRedisTemplate.opsForValue().decrement(GoodsConstant.GOODS_KEY);
                return "从服务器" + serverPort + "购买成功,剩余库存数量" + dbResult;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //释放锁 只有当前是锁定状态且是当前线程锁定在释放锁
            if (lock.isLocked() && lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
        return "从服务器" + serverPort + "购买失败,商品已售完";
    }

}
