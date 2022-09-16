package com.atguigu.yygh.common.exception.MyException;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author nicc
 * @version 1.0
 * @className YyghException
 * @description TODO
 * @date 2022-08-25 09:47
 *
 *  自定义异常类
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class YyghException extends RuntimeException {

    @ApiModelProperty(value = "自定义状态码")
    private Integer code;

    @ApiModelProperty(value = "自定义状态信息")
    private String msg;

}
