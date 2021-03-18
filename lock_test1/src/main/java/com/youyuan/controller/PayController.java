package com.youyuan.controller;

import cn.hutool.core.util.StrUtil;
import com.youyuan.constant.GoodsConstant;
import com.youyuan.util.JedisPoolUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

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
@RequestMapping
public class PayController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 服务端口号
     */
    @Value("${server.port}")
    private String serverPort;

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
        try {
            Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(GoodsConstant.GOODS_KEY, lockValue, 10,
                    TimeUnit.SECONDS);

            if (!flag) {
                return "抢锁失败";
            }

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
//            //1.通过事务释放redis锁
//            //模拟自旋锁来释放锁(删除key)
//            while (true){
//                //释放锁  使用redis事务
//                stringRedisTemplate.watch(GoodsConstant.LOCK_KEY);//监控redis锁的key
//                if (Objects.equals(stringRedisTemplate.opsForValue().get(GoodsConstant.LOCK_KEY), lockValue)) {
//                    stringRedisTemplate.setEnableTransactionSupport(true);//设置redis支持事务
//                    stringRedisTemplate.multi();//开启事务
//                    stringRedisTemplate.delete(GoodsConstant.LOCK_KEY);//业务代码
//                    stringRedisTemplate.exec();//提交事务
//                }
//                stringRedisTemplate.unwatch();//删除监控
//                break;
//            }

            //2.通过lua脚本释放redis锁
            String script="if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" +
                    "then\n" +
                    "    return redis.call(\"del\",KEYS[1])\n" +
                    "else\n" +
                    "    return 0\n" +
                    "end";
            Jedis jedis = JedisPoolUtil.getInstance().getResource();
            try {
                //调用lua脚本根据指定value删除key
                Object eval = jedis.eval(script, Collections.singletonList(GoodsConstant.LOCK_KEY), Collections.singletonList(lockValue));
                if (Objects.equals(0,eval.toString())){
                    System.out.println("通过lua脚本释放锁成功");
                }else {
                    System.out.println("通过lua脚本释放锁失败");
                }
            }finally {
                if (jedis!=null){
                    jedis.close();
                }
            }
        }
        return "从服务器" + serverPort + "购买失败,商品已售完";
    }

}
