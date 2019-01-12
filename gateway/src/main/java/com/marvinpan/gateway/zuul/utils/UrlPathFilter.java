package com.marvinpan.gateway.zuul.utils;

import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

//@Component
public class UrlPathFilter extends ZuulFilter{
 
 @Override
 public String filterType() {
  return FilterConstants.PRE_TYPE;
 }
 
 @Override
 public int filterOrder() {
  return FilterConstants.PRE_DECORATION_FILTER_ORDER + 1;
 }
 
 @Override
 public boolean shouldFilter() {
  final String serviceId = (String) RequestContext.getCurrentContext().get("proxy");
  return "demo".equals(serviceId);
 }
 
 @Override
 public Object run() {
  RequestContext context = RequestContext.getCurrentContext();
  Object originalRequestPath = context.get(FilterConstants.REQUEST_URI_KEY);
  //http://localhost:10000/demo/list/data
  //-->/api/prefix/list/data
  String modifiedRequestPath = "/api/prefix" + originalRequestPath;
  context.put(FilterConstants.REQUEST_URI_KEY, modifiedRequestPath);
  return null;
 }
}
