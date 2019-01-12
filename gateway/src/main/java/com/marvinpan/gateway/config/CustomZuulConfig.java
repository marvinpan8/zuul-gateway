package com.marvinpan.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.marvinpan.gateway.zuul.ParamRouteLocator;

/**
 * Created by xujingfeng on 2017/4/1.
 */
@Configuration
public class CustomZuulConfig {

    @Autowired
    ZuulProperties zuulProperties;
    @Autowired
    ServerProperties server;

    @Bean
    public ParamRouteLocator routeLocator() {
        ParamRouteLocator routeLocator = new ParamRouteLocator
        		(this.server.getServletPrefix(), this.zuulProperties);
        return routeLocator;
    }

}
