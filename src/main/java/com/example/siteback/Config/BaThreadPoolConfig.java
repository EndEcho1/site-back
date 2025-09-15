package com.example.siteback.Config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;

@Slf4j
@Configuration
public class BaThreadPoolConfig {
    @Bean("asyncTaskExecutor_old")
    public Executor asyncTaskExecutor_old() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);//即使线程空闲也不会被回收（可以设置allowCoreThreadTimeOut为true）
        executor.setMaxPoolSize(20);
        // 设置核心线程也可超时回收（可选）
        executor.setAllowCoreThreadTimeOut(true);
        executor.setQueueCapacity(100);// 队列容量
        executor.setKeepAliveSeconds(60);//设置非核心线程的空闲存活时间
        executor.setThreadNamePrefix("async-baBlue-");
        //拒绝策略
        executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                // 记录任务被拒绝的日志
                log.warn("Task rejected: " + r.toString());
                if (!executor.isShutdown()) {
                    try {
                        r.run();
                    } catch (Exception e) {
                        log.error("Task execution failed", e);
                    }
                }
                //直接抛出异常（默认策略）ThreadPoolExecutor.AbortPolicy()
                //ThreadPoolExecutor.DiscardPolicy()
                //ThreadPoolExecutor.DiscardOldestPolicy()
                //ThreadPoolExecutor.CallerRunsPolicy()
            }
        });
        executor.initialize();
        return executor;
    }

    // 虚拟线程池配置
    @Bean("virtualThreadExecutor")
    public ExecutorService virtualThreadExecutor() {

        // 使用带有自定义名称和异常处理的虚拟线程工厂
        ThreadFactory factory = Thread.ofVirtual()
                .name("article-service-", 0)
                .uncaughtExceptionHandler((t, e) -> {
                    log.error("Uncaught exception in virtual thread " + t.getName(), e);
                })
                .factory();
        // Java 21+ 的虚拟线程执行器
        // 无核心/最大线程数概念,无任务队列概念,无keepAlive时间
        return Executors.newThreadPerTaskExecutor(factory);

        // 如果需要自定义线程工厂（设置线程名称等）
        // return Executors.newThreadPerTaskExecutor(
        //     Thread.ofVirtual().name("virtual-task-", 0).factory()
        // );
    }
}
