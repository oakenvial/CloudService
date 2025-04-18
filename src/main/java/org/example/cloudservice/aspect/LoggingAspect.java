package org.example.cloudservice.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    /**
     * Wraps every call in the controller package to log HTTP method, URI,
     * return type (or exception), and execution time.
     */
    @Around("within(org.example.cloudservice.controller..*)")
    public Object logController(ProceedingJoinPoint pjp) throws Throwable {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest req = (attrs != null ? attrs.getRequest() : null);

        String method = (req != null ? req.getMethod() : "N/A");
        String uri    = (req != null ? req.getRequestURI() : "N/A");

        long start = System.currentTimeMillis();
        try {
            Object result = pjp.proceed();
            long duration = System.currentTimeMillis() - start;
            String type = (result != null ? result.getClass().getSimpleName() : "void");
            log.info("{} {} → {} ({} ms)", method, uri, type, duration);
            return result;
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - start;
            log.error("{} {} → threw {} after {} ms",
                    method, uri, ex.getClass().getSimpleName(), duration, ex);
            throw ex;
        }
    }
}
