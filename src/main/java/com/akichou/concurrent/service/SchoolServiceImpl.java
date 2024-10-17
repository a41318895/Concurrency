package com.akichou.concurrent.service;

import com.akichou.concurrent.manager.LockManager;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.Lock;

@Service
public class SchoolServiceImpl implements SchoolService{

    @Override
    public String applyToSchool(String schoolName) {

        // Get the lock of corresponding school name
        Lock schoolLock = LockManager.getLockForApply(schoolName.intern()) ;

        schoolLock.lock();
        try {
            Thread.sleep(300) ;    // Mock Handling time
            return "Successfully applied to " + schoolName ;
        } catch (InterruptedException e) {
            return "Application to " + schoolName + " was interrupted !" ;
        } finally {
            schoolLock.unlock() ;
        }
    }

    @Override
    public String submitExam(String schoolName) {

        // Get the lock of corresponding school name
        Lock schoolLock = LockManager.getLockForSubmit(schoolName.intern()) ;

        schoolLock.lock();
        try {
            Thread.sleep(300) ;    // Mock Handling time
            return "Successfully submitted to " + schoolName ;
        } catch (InterruptedException e) {
            return "Submission to " + schoolName + " was interrupted !" ;
        } finally {
            schoolLock.unlock() ;
        }
    }
}
