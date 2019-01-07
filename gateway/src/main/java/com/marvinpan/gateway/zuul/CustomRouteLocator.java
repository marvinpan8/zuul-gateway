package com.marvinpan.gateway.zuul;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.RefreshableRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.SimpleRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties.ZuulRoute;
import org.springframework.cloud.netflix.zuul.util.RequestUtils;
import org.springframework.util.StringUtils;

import com.marvinpan.gateway.entity.ZuulRouteVO;
import com.marvinpan.gateway.service.ApiGatewayService;
import com.netflix.zuul.util.HTTPRequestUtils;

/**
 * Created by pantj on 2019/1/4.
 */
public class CustomRouteLocator extends SimpleRouteLocator implements RefreshableRouteLocator{

    public final static Logger log = LoggerFactory.getLogger(CustomRouteLocator.class);

    private ZuulProperties properties;
   
    private AtomicReference<Map<String, ZuulRoute>> routes = new AtomicReference<>();
    
	private String dispatcherServletPath = "/";
	private String zuulServletPath;
	
//	@Autowired
//	private RedisUtil redisUtil;
	@Autowired
	private ApiGatewayService apiGatewayService;
    
    public CustomRouteLocator(String servletPath, ZuulProperties properties) {
        super(servletPath, properties);
        this.properties = properties;
        log.info("servletPath:{}",servletPath);
        
        if (servletPath != null && StringUtils.hasText(servletPath)) {
			this.dispatcherServletPath = servletPath;
		}

		this.zuulServletPath = properties.getServletPath();
    }

    //父类已经提供了这个方法，这里写出来只是为了说明这一个方法很重要！！！
//    @Override
//    protected void doRefresh() {
//        super.doRefresh();
//    }

    @Override
    public void refresh() {
        doRefresh();
    }
    
    @Override
    protected Map<String, ZuulRoute> locateRoutes() {
        LinkedHashMap<String, ZuulRoute> routesMap = new LinkedHashMap<String, ZuulRoute>();
        //从application.properties中加载路由信息
        routesMap.putAll(super.locateRoutes());
        //从db中加载路由信息
//        routesMap.putAll(locateRoutesFromDB());
        //优化一下配置
        LinkedHashMap<String, ZuulRoute> values = new LinkedHashMap<>();
        for (Map.Entry<String, ZuulRoute> entry : routesMap.entrySet()) {
//            String path = entry.getKey();
//            // Prepend with slash if not already present.
//            if (!path.startsWith("/")) {
//                path = "/" + path;
//            }
//            if (StringUtils.hasText(this.properties.getPrefix())) {
//                path = this.properties.getPrefix() + path;
//                if (!path.startsWith("/")) {
//                    path = "/" + path;
//                }
//            }
//            values.put(path, entry.getValue());
        	
        	 String tenantId = entry.getKey();
        	 values.put(tenantId, entry.getValue());
        }
        return values;
    }
    /**
           * 判断Redis中是否存在对应key的信息，若存在则从reids中获取，若不存在则从数据库查询，同时存入redis缓存中
     * @param tenantId
     * @return
     */
//    private ZuulRouteVO locateRouteFromRedisOrDB(String tenantId) {
//    	ZuulRouteVO vo = null;
//    	String key = "JRTZ-APIGATEWAY$" + tenantId; 
//    	boolean flagException = false;
//		try {
//			if (redisUtil.exists(key)) {
//				log.info("从Redis缓存中获取路由:{}信息", tenantId);
//				return (ZuulRouteVO) redisUtil.get(key);
//			} 
//		} catch (Exception e) {
//			flagException = true;
//			log.info("从Redis缓存中获取路由 {} 异常：{}", tenantId, e);
//		}
//		vo = apiGatewayService.locateRouteFromDB(tenantId);
//		if(!flagException) redisUtil.set(key, vo, 60 * 60 * 24L);
//		return vo;
//    }
    
    @Override
	public Route getMatchingRoute(final String path) {

		if (log.isDebugEnabled()) {
			log.debug("Finding route for path: " + path);
		}

		if (this.routes.get() == null) {
			this.routes.set(locateRoutes());
		}

		if (log.isDebugEnabled()) {
			log.debug("servletPath=" + this.dispatcherServletPath);
			log.debug("zuulServletPath=" + this.zuulServletPath);
			log.debug("RequestUtils.isDispatcherServletRequest()="
					+ RequestUtils.isDispatcherServletRequest());
			log.debug("RequestUtils.isZuulServletRequest()="
					+ RequestUtils.isZuulServletRequest());
		}
		//调整路径，与参数无关
		String adjustedPath = adjustPath(path);

		ZuulRoute route = null;
		/*=====================获取reqTenantId=========================*/
		String reqTenantId = "";
		Map<String, List<String>> map = HTTPRequestUtils.getInstance().getQueryParams();
        if (map == null) {
			return null;
		}
        
        for (String key : map.keySet()) {
        	if(org.apache.commons.lang3.StringUtils.equals("tenantId", key)) {
        		reqTenantId = map.get(key).get(0);
			}
		}
        /*============================================================*/
		if (!matchesIgnoredPatterns(adjustedPath)) {
			ZuulRouteVO routeVO = apiGatewayService.locateRouteFromDB(reqTenantId);
			if( routeVO != null ) {
				route = new ZuulRoute();
				BeanUtils.copyProperties(routeVO,route);
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("route matched=" + route);
		}

		return getRoute(route, adjustedPath);

	}
    
    //copy parent class
	private Route getRoute(ZuulRoute route, String path) {
		if (route == null) {
			return null;
		}
		String targetPath = path;
		String prefix = this.properties.getPrefix();
		if (path.startsWith(prefix) && this.properties.isStripPrefix()) {
			targetPath = path.substring(prefix.length());
		}
		if (route.isStripPrefix()) {
			int index = route.getPath().indexOf("*") - 1;
			if (index > 0) {
				String routePrefix = route.getPath().substring(0, index);
				targetPath = targetPath.replaceFirst(routePrefix, "");
				prefix = prefix + routePrefix;
			}
		}
		Boolean retryable = this.properties.getRetryable();
		if (route.getRetryable() != null) {
			retryable = route.getRetryable();
		}
		return new Route(route.getId(), targetPath, route.getLocation(), prefix,
				retryable,
				route.isCustomSensitiveHeaders() ? route.getSensitiveHeaders() : null);
	}
	
	//copy parent class
	private String adjustPath(final String path) {
		String adjustedPath = path;

		if (RequestUtils.isDispatcherServletRequest()
				&& StringUtils.hasText(this.dispatcherServletPath)) {
			if (!this.dispatcherServletPath.equals("/")) {
				adjustedPath = path.substring(this.dispatcherServletPath.length());
				log.debug("Stripped dispatcherServletPath");
			}
		}
		else if (RequestUtils.isZuulServletRequest()) {//上传文件/zuul
			if (StringUtils.hasText(this.zuulServletPath)
					&& !this.zuulServletPath.equals("/")) {
				adjustedPath = path.substring(this.zuulServletPath.length());
				log.debug("Stripped zuulServletPath");
			}
		}
		else {
			// do nothing
		}

		log.debug("adjustedPath=" + path);
		return adjustedPath;
	}
    
}
