package com.marvinpan.gateway.zuul.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
import javax.servlet.http.HttpServletRequest;
 
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

//@Component
public class GroovyPreRequestFilter extends ZuulFilter {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Override
	public String filterType() {
        return "pre";
    }
 
    @Override
	public int filterOrder() {
        return 1000;
    }
 
    @Override
	public boolean shouldFilter() {
        return true;
    }
 
    @Override
	public Object run() {
        try {
            RequestContext ctx = RequestContext.getCurrentContext();
            HttpServletRequest request = ctx.getRequest();
            logger.info(
            		String.format("#####Groovy Pre Filter#####send %s request to %s",
            				request.getMethod(), request.getRequestURL().toString()));
        } catch (Exception e) {
            logger.error("",e);
        }
 
        return null;
    }
}

