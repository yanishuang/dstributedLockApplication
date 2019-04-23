package com.yanis.cn.dstributedlock;

import com.yanis.cn.dstributedlock.lock.RedisLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
public class DstributedLockApplication {

    private static Integer  num = 0;
    @Resource
    private RedisLock redisLock;

    public static void main(String[] args) {
        SpringApplication.run(DstributedLockApplication.class, args);

    }

    @PostConstruct
    public void init(){
        //testRedisLock();
        System.out.println(new Date().getTime());
    }

    private void testRedisLock() {
        CountDownLatch cdl = new CountDownLatch(50000);
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        for (int i = 0; i <50000; i++) {
            executorService.execute(() ->{

                while (!redisLock.tryToGetLock("testLock")){
                    try {
                        Thread.sleep(5L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                num ++;
                if(num % 1000 == 0){
                    System.out.println(num);
                }
                cdl.countDown();
                redisLock.tryToReleaseLock("testLock");
            });
        }
        executorService.shutdown();
        try {
            cdl.await();
            System.out.println(num);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
