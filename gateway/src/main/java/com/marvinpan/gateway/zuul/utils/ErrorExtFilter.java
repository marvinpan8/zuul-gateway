//package com.marvinpan.gateway.zuul.utils;
//
//import org.springframework.cloud.netflix.zuul.filters.post.SendErrorFilter;
//
//import com.netflix.zuul.ZuulFilter;
//import com.netflix.zuul.context.RequestContext;
//
//public class ErrorExtFilter extends SendErrorFilter {
//    @Override
//    public String filterType() {
//        return "error";
//    }
//    @Override
//    public int filterOrder() {
//        return 30;
//    }
//    @Override
//    public boolean shouldFilter() {
//        RequestContext ctx = RequestContext.getCurrentContext();
//        ZuulFilter failedFilter = (ZuulFilter) ctx.get("failed.filter");
//        if(failedFilter != null && failedFilter.filterType().equals("post")) {
//            return true;
//        }
//        return false;
//    }
//}
