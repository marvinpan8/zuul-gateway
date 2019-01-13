package com.marvinpan.gateway.zuul;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.marvinpan.gateway.entity.BaseResult;

/**
 * 自定义error错误页面
 * @author zhiguang
 */
@RestController
public class ErrorHandlerController implements ErrorController {
 
    /**
     * 出异常后进入该方法，交由下面的方法处理
     */
    @Override
    public String getErrorPath() {
        return "/error";
    }
 
    @RequestMapping("/error")
    public BaseResult error(HttpServletRequest request, HttpServletResponse response){
        /*RequestContext ctx = RequestContext.getCurrentContext();
        ZuulException exception = (ZuulException)ctx.getThrowable();
        return Result.choose(exception.nStatusCode, exception.getMessage());*/
        Integer status = (Integer)request.getAttribute("javax.servlet.error.status_code");
        String message = (String)request.getAttribute("javax.servlet.error.message");
        return BaseResult.choose(status, message);
    }
 
}

