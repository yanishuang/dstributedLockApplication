package com.yanis.cn.dstributedlock;

import com.yanis.cn.dstributedlock.lock.RedisLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
public class DstributedLockApplication {

    private Integer  num = 0;
    @Resource
    private RedisLock redisLock;

    public static void main(String[] args) {
        SpringApplication.run(DstributedLockApplication.class, args);

    }

    @PostConstruct
    public void init(){

        ExecutorService executorService = Executors.newFixedThreadPool(3);
        for (int i = 0; i <100; i++) {
            executorService.execute(() ->{
                while (!redisLock.tryToGetLock("testLock")){
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println(num ++ );
                redisLock.tryToReleaseLock("testLock");
            });
        }
        executorService.shutdown();

    }
}
