package com.akichou.concurrent.request;

import com.akichou.concurrent.manager.LockManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

import static com.akichou.concurrent.constant.Constants.TARGET_TEST_SCHOOL_NAME;

@Component
@RequiredArgsConstructor
@Slf4j
public class RequestMocking implements CommandLineRunner {

    @Value("${server.port}")
    private int localPort ;

    // Remote call
    private final RestTemplate restTemplate ;

    private static final int TOTAL_REQUEST_NUMBER = 100 ;
    private static final int CONCURRENT_THREAD_NUMBER = 30 ;
    private static final String[] SCHOOLS = {"ABCSchool", "DEFSchool", "GHISchool", "JKLSchool"} ;

    private final AtomicInteger successRequestCounter = new AtomicInteger(0) ;
    private final AtomicInteger failRequestCounter = new AtomicInteger(0) ;

    @Override
    public void run(String... args) throws Exception {

        ExecutorService executorService = Executors.newFixedThreadPool(CONCURRENT_THREAD_NUMBER) ;
        CountDownLatch latch = new CountDownLatch(TOTAL_REQUEST_NUMBER * 2) ;
        //CyclicBarrier barrier = new CyclicBarrier(TOTAL_REQUEST_NUMBER) ;

        long startTime = System.currentTimeMillis() ;

        for(int i = 1 ; i <= TOTAL_REQUEST_NUMBER ; i ++) {

            final int requestEntityId = i ;

            // Task for sending requests to applyToSchool()
            executorService.submit(() -> {

                try {
                    //barrier.await() ;   // Wait all threads get ready
                    String schoolName = SCHOOLS[0] ;
                    String remoteCallUrl = "http://localhost:" + localPort + "/api/school/apply?schoolName=" + schoolName ;
                    String receiveResult = restTemplate.postForObject(remoteCallUrl, null, String.class, schoolName) ;
                    log.info("ApplyToSchool Request [{}]: {}", requestEntityId, receiveResult);

                    successRequestCounter.incrementAndGet() ;
                } catch (Exception e) {
                    log.error("ApplyToSchool Request [{}] failed: {}", requestEntityId, e.getMessage());
                    failRequestCounter.incrementAndGet() ;
                } finally {
                    latch.countDown() ;
                }
            }) ;

            // Task for sending requests to submitExam()
            executorService.submit(() -> {

                try {
                    //barrier.await(); // Wait all threads get ready
                    String schoolName = SCHOOLS[0];
                    String remoteCallUrl = "http://localhost:" + localPort + "/api/school/submitExam?schoolName=" + schoolName;
                    String receiveResult = restTemplate.postForObject(remoteCallUrl, null, String.class, schoolName);
                    log.info("SubmitExam Request [{}]: {}", requestEntityId, receiveResult);

                    successRequestCounter.incrementAndGet();
                } catch (Exception e) {
                    log.error("SubmitExam Request [{}] failed: {}", requestEntityId, e.getMessage());
                    failRequestCounter.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean isCompleted = executorService.awaitTermination(1, TimeUnit.MINUTES) ;
        executorService.shutdown() ;

        long totalTimeSpent = System.currentTimeMillis() - startTime ;

        System.out.println();
        log.info("All requests completed...") ;

        log.info("{} requests successful, {} requests failed.", successRequestCounter.get(), failRequestCounter.get());

        log.info("Total spent time: {} ms", totalTimeSpent);
        log.info("Average time spent per request: {} ms\n", (double) totalTimeSpent / (TOTAL_REQUEST_NUMBER * 2));


        // Check the status of locks for apply
        LockManager.printLockStatusForApply();

        Map<String, Lock> activeLocksForApply = LockManager.getActiveLocksForApply() ;
        if (activeLocksForApply.size() == 1 && activeLocksForApply.containsKey(TARGET_TEST_SCHOOL_NAME)) {
            log.info("Verification successful: Only one lock (for apply) of {} exists.\n", TARGET_TEST_SCHOOL_NAME);
        } else {
            log.warn("Verification failed: Unexpected lock (for apply) state. Active locks (for apply): {}\n", activeLocksForApply.keySet());
        }

        // Check the status of locks for submit
        LockManager.printLockStatusForSubmit() ;

        Map<String, Lock> activeLocksForSubmit = LockManager.getActiveLocksForSubmit() ;
        if (activeLocksForSubmit.size() == 1 && activeLocksForSubmit.containsKey(TARGET_TEST_SCHOOL_NAME)) {
            log.info("Verification successful: Only one lock (for submit) of {} exists.\n", TARGET_TEST_SCHOOL_NAME);
        } else {
            log.warn("Verification failed: Unexpected lock (for submit) state. Active locks (for submit): {}\n", activeLocksForSubmit.keySet());
        }

        // Clean up lock's references and check status again
        LockManager.cleanupLocksForApply() ;
        LockManager.printLockStatusForApply();
        LockManager.cleanupLocksForSubmit() ;
        LockManager.printLockStatusForSubmit() ;
    }
}
