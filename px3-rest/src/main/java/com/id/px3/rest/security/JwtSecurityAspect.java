package com.id.px3.rest.security;


import com.id.px3.rest.UserContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.HashSet;

@Aspect
@Component
public class JwtSecurityAspect {

    @Autowired
    private JwtService jwtService;

    @Around("@annotation(jwtSecured)")
    public Object aroundCheckToken(ProceedingJoinPoint joinPoint, JwtSecured jwtSecured) throws Throwable {
        try {
            String token = extractAccessToken();
            String userId = jwtService.validateTokenWithRoles(token, new HashSet<>(Arrays.asList(jwtSecured.roles())));

            //  set the userId for the current request
            UserContextHolder.setUserId(userId);

            return joinPoint.proceed();
        } finally {
            //  ensure that the ThreadLocal is cleared after the request is finished
            UserContextHolder.clear();
        }
    }

    private static String extractAccessToken() {
        ServletRequestAttributes reqAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(reqAttributes == null) {
            throw new RuntimeException("No request attributes found.");
        }
        HttpServletRequest request = reqAttributes.getRequest();

        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid or missing Authorization header.");
        }

        return authorizationHeader.substring(7);
    }
}
