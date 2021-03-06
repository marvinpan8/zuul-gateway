package com.marvinpan.gateway.web;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.web.ZuulHandlerMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.marvinpan.gateway.entity.ZuulRouteVO;
import com.marvinpan.gateway.event.RefreshRouteService;
import com.marvinpan.gateway.service.ApiGatewayService;

/**
 * Created by marvinpan on 2019/1/27.
 */
@RestController
@RequestMapping(value = "/gwroute")
public class RouteController {

    @Autowired
    RefreshRouteService refreshRouteService;
    @Autowired
    ApiGatewayService apiGatewayService;
    
    @RequestMapping("/refresh")
    public String refreshRoute(){
        refreshRouteService.refreshRoute();
        return "refreshRoute";
    }

    @Autowired
    ZuulHandlerMapping zuulHandlerMapping;

    @RequestMapping("/watch")
    public String watchNowRoute(){
        //可以用debug模式看里面具体是什么
        Map<String, Object> handlerMap = zuulHandlerMapping.getHandlerMap();
        return "watchroute";
    }
    
    @RequestMapping("/add")
    public String addRoute(@RequestParam String id, @RequestParam String path, @RequestParam String url){
    	ZuulRouteVO vo = new ZuulRouteVO();
    	vo.setTenantId(id);
    	vo.setPath(path);
    	vo.setUrl(url);
    	int result = apiGatewayService.createOneRoute(vo);
    	if(result == 1) {
    		apiGatewayService.deleteZuulRouteVoListFromTenantId(id);
    		return "SUCCESS";
    	}
        return "FAILURE";
    }
    
    @RequestMapping("/delete/{id}")
    public String deleteRouteById(@PathVariable("id") String tenantId){
    	apiGatewayService.deleteZuulRouteVoListFromTenantId(tenantId);
    	return "SUCCESS";
    }
    
    /**
           *   心跳健康检查
     * @return
     */
    @RequestMapping("/health")
    public String checkHealth(){
        return "OK";
    }

}
