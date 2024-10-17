package com.akichou.concurrent.manager;

import lombok.extern.slf4j.Slf4j;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.akichou.concurrent.constant.Constants.TARGET_TEST_SCHOOL_NAME;

@Slf4j
public class LockManager {

    private static final ConcurrentHashMap<String, WeakReference<Lock>> locksForApply = new ConcurrentHashMap<>() ;
    private static final ConcurrentHashMap<String, WeakReference<Lock>> locksForSubmit = new ConcurrentHashMap<>() ;

    public static Lock getLockForApply(String key) {

        return locksForApply.computeIfAbsent(key, k -> new WeakReference<>(new ReentrantLock()))
                .get() ;
    }

    public static Lock getLockForSubmit(String key) {

        return locksForSubmit.computeIfAbsent(key, k -> new WeakReference<>(new ReentrantLock()))
                .get() ;
    }

    public static int getLockCountForApply() {

        return locksForApply.size() ;
    }

    public static int getLockCountForSubmit() {

        return locksForSubmit.size() ;
    }

    public static Map<String, Lock> getActiveLocksForApply() {

        return locksForApply.entrySet().stream()
                // Get all existing locks
                .map(entry -> {
                    Lock lock = entry.getValue().get() ;
                    return lock != null ? Map.entry(entry.getKey(), lock) : null ;
                })
                // filter invalid
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                )) ;
    }

    public static Map<String, Lock> getActiveLocksForSubmit() {

        return locksForSubmit.entrySet().stream()
                .map(entry -> {
                    Lock lock = entry.getValue().get() ;
                    return lock != null ? Map.entry(entry.getKey(), lock) : null ;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                )) ;
    }

    public static void printLockStatusForApply() {

        log.info("Current lock (for apply) count: {}", getLockCountForApply()) ;
        log.info("Active locks (for apply): {}\n", getActiveLocksForApply().keySet()) ;
    }

    public static void printLockStatusForSubmit() {

        log.info("Current lock (for submit) count: {}", getLockCountForSubmit()) ;
        log.info("Active locks (for submit): {}\n", getActiveLocksForSubmit().keySet()) ;
    }

    public static void cleanupLocksForApply() {

        int beforeCleanup = locksForApply.size();

        makeReferenceInvalidForApply(TARGET_TEST_SCHOOL_NAME) ;     // Make the reference of school name invalid
        locksForApply.entrySet().removeIf(entry -> entry.getValue().get() == null) ;

        int afterCleanup = locksForApply.size();

        log.info("Cleanup completed. Locks (for apply) number before: {}, after: {}", beforeCleanup, afterCleanup);
    }

    public static void cleanupLocksForSubmit() {

        int beforeCleanup = locksForSubmit.size() ;

        makeReferenceInvalidForSubmit(TARGET_TEST_SCHOOL_NAME) ;
        locksForSubmit.entrySet().removeIf(entry -> entry.getValue().get() == null) ;

        int afterCleanup = locksForSubmit.size() ;

        log.info("Cleanup completed. Locks (for submit) number before: {}, after: {}", beforeCleanup, afterCleanup) ;
    }

    public static void makeReferenceInvalidForApply(String schoolName) {

        WeakReference<Lock> lockReferenced = locksForApply.remove(schoolName) ;

        if (lockReferenced != null) {
            lockReferenced.clear() ;
        }
    }

    public static void makeReferenceInvalidForSubmit(String schoolName) {

        WeakReference<Lock> lockReferenced = locksForSubmit.remove(schoolName) ;

        if (lockReferenced != null) {
            lockReferenced.clear() ;
        }
    }
}
