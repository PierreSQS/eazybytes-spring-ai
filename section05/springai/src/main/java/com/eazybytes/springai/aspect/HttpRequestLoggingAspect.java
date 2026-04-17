package com.eazybytes.springai.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;

// @Aspect declares the AOP role; @Component is still needed so Spring registers it as a bean
@Aspect
@Component
public class HttpRequestLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(HttpRequestLoggingAspect.class);

    // @Around (vs @Before) lets us call joinPoint.proceed() ourselves, keeping full control of the execution chain
    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object logRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        // RequestContextHolder gives access to the current HTTP request without injecting HttpServletRequest everywhere
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        // guard against building the args string when INFO logging is disabled
        if (attrs != null && logger.isInfoEnabled()) {
            HttpServletRequest request = attrs.getRequest();
            logger.info("### HTTP {} {} | handler={} | args={} ###",
                    request.getMethod(),
                    request.getRequestURI(),
                    joinPoint.getSignature().toShortString(),
                    Arrays.toString(joinPoint.getArgs()));
        }

        return joinPoint.proceed();
    }
}