package org.comicVaultBackend.lockAspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Aspect
@Component
public class UserLockAspect {
    private static final Logger log = LoggerFactory.getLogger(UserLockAspect.class);

    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    @Around("@annotation(org.comicVaultBackend.annotations.WithUserLock)")
    public Object withUSerLock(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        if (username == null) {
            log.warn("No username found, proceeding without lock");
            return pjp.proceed();
        }

        ReentrantLock lock = locks.computeIfAbsent(username, id -> new ReentrantLock());

        lock.lock();
        try {
            Object result = pjp.proceed();
            return result;
        } finally {
            lock.unlock();
        }
    }
}
