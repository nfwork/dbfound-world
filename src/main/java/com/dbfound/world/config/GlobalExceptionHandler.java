package com.dbfound.world.config;

import com.github.nfwork.dbfound.starter.exception.DBFoundExceptionHandler;
import com.nfwork.dbfound.dto.ResponseObject;
import com.nfwork.dbfound.exception.DBFoundErrorException;
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

    @ExceptionHandler(Exception.class)
    public ResponseObject handleException(Exception exception,
                                          HttpServletRequest request,
                                          HttpServletResponse response) {
        return exceptionHandler.handle(exceptionHandler.getException(exception), request, response);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseObject handleThrowable(Throwable throwable,
                                          HttpServletRequest request,
                                          HttpServletResponse response) {
        Exception exception = new DBFoundErrorException("dbfound execute error, cause by " + throwable.getMessage(), throwable);
        return exceptionHandler.handle(exception, request, response);
    }
}
