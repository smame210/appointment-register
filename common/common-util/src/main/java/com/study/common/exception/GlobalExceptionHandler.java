package com.study.common.exception;

import com.study.common.result.Result;
import com.study.common.result.ResultCodeEnum;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    public Result exceptionHandler(Exception e){
        return Result.build(null, ResultCodeEnum.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = BizException.class)
    public Result bizExceptionHandler(BizException e){
        return Result.build(e.getCode(), e.getMessage());
    }
}
