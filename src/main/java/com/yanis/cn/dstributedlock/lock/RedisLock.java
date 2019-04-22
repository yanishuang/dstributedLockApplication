package com.yanis.cn.dstributedlock.lock;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class RedisLock {

    private ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    private Long MAX_TIME  = 500L;

    @Resource
    private RedisTemplate<String,String> redisTemplate;


    /**
     * 尝试获得锁
     * @param lockName
     * @return
     */
    public Boolean tryToGetLock(String lockName){
        Long currentTime = new Date().getTime() + MAX_TIME;
        Boolean getLockSuccess = redisTemplate.opsForValue().setIfAbsent(lockName,currentTime.toString());
        if (!getLockSuccess){
            // 锁已经被其他人获取
            while (true){
                String t1 =  redisTemplate.opsForValue().get(lockName);
                currentTime = new Date().getTime();
                // 判断是否超时
                if(!StringUtils.isEmpty(t1) && currentTime  >  Long.parseLong(t1)){
                    //超时
                    currentTime  = new Date().getTime()  + MAX_TIME;
                     String t2 = redisTemplate.opsForValue().getAndSet(lockName, currentTime.toString());
                    if(t2 == null || t2.equals(t1) ){
                        // 则原来的过期或者被删除了 设置成功  或者中间没有被人修改过
                        threadLocal.set(currentTime);
                        return true;
                    }else {
                        return  false;
                    }
                }else{
                    //没有超时
                    currentTime = new Date().getTime() + MAX_TIME;
                    if(redisTemplate.opsForValue().setIfAbsent(lockName,currentTime.toString())){
                        threadLocal.set(currentTime);
                        return true;
                    }
                }
            }
        }else {
            threadLocal.set(currentTime);
            return getLockSuccess;// 返回成功获取锁
        }
    }


    /**
     * 释放锁
     * @param lockName
     * @return
     */
    public Boolean tryToReleaseLock(String lockName){
        // 判断是否已经被删除了
        String timeString = redisTemplate.opsForValue().get(lockName);
        if(!StringUtils.isEmpty(timeString) && threadLocal.get().equals(Long.parseLong(timeString))){
            return  redisTemplate.delete(lockName);
        }else {
            return false;
        }
    }

    private void sleep(){
        try {
            Thread.sleep(500L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}