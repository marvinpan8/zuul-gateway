package com.marvinpan.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.marvinpan.gateway.zuul.HeaderRouteLocator;

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
    public HeaderRouteLocator routeLocator() {
        HeaderRouteLocator routeLocator = new HeaderRouteLocator
        		(this.server.getServletPrefix(), this.zuulProperties);
        return routeLocator;
    }

}
