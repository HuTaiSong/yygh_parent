package com.atguigu.yygh.common.exception;

import com.atguigu.yygh.common.exception.MyException.YyghException;
import com.atguigu.yygh.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author nicc
 * @version 1.0
 * @className GlobalExceptionHandler
 * @description TODO
 * @date 2022-08-25 09:43
 *
 * 统一异常处理类
 *
 */

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public R error(Exception e){
        e.printStackTrace();
        return R.error();
    }

    //异常处理方法
    @ExceptionHandler(ArithmeticException.class)
    @ResponseBody
    public R error(ArithmeticException e){
        e.printStackTrace();
        return R.error().message("出现了ArithmeticException异常");
    }

    @ExceptionHandler(YyghException.class)
    @ResponseBody
    public R error(YyghException e){
        e.printStackTrace();
        return R.error().message(e.getMsg()).code(e.getCode());
    }

}
