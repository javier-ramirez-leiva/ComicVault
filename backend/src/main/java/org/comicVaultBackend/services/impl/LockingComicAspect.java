package org.comicVaultBackend.services.impl;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.comicVaultBackend.services.LockComicsService;
import org.springframework.beans.factory.annotation.Autowired;

public class LockingComicAspect {

    @Autowired
    private LockComicsService LockComicsService;
    
    @Around("@annotation(WithLock)")
    public Object aroundLockedMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        LockComicsService.lock();
        try {
            return joinPoint.proceed();
        } finally {
            LockComicsService.unlock();
        }
    }
}
