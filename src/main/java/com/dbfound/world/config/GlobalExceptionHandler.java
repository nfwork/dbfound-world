package com.dbfound.world.config;

import com.github.nfwork.dbfound.starter.exception.DBFoundExceptionHandler;
import com.nfwork.dbfound.dto.ResponseObject;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.dbfound.world.controlller")
public class GlobalExceptionHandler {

    private final DBFoundExceptionHandler exceptionHandler;

    public GlobalExceptionHandler(DBFoundExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    @ExceptionHandler(Throwable.class)
    public ResponseObject handleThrowable(Throwable throwable,
                                          HttpServletRequest request,
                                          HttpServletResponse response) {
        return exceptionHandler.handle(throwable, request, response);
    }
}
