package org.comicVaultBackend.lockAspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Aspect
@Component
public class ComicLockAspect {

    private static final Logger log = LoggerFactory.getLogger(ComicLockAspect.class);

    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    @Around("@annotation(org.comicVaultBackend.annotations.WithComicLock)")
    public Object withComicLock(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        String comicID = (args.length > 0 && args[0] instanceof String s) ? s : null;

        if (comicID == null) {
            log.warn("No comicID found, proceeding without lock");
            return pjp.proceed();
        }

        ReentrantLock lock = locks.computeIfAbsent(comicID, id -> new ReentrantLock());

        lock.lock();
        try {
            Object result = pjp.proceed();
            return result;
        } finally {
            lock.unlock();
        }
    }
}
