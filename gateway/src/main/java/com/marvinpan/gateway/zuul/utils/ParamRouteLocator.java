//package com.marvinpan.gateway.zuul.utils;
//
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.atomic.AtomicReference;
//
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.BeanUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cloud.netflix.zuul.filters.RefreshableRouteLocator;
//import org.springframework.cloud.netflix.zuul.filters.Route;
//import org.springframework.cloud.netflix.zuul.filters.SimpleRouteLocator;
//import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
//import org.springframework.cloud.netflix.zuul.filters.ZuulProperties.ZuulRoute;
//import org.springframework.cloud.netflix.zuul.util.RequestUtils;
//
//import com.marvinpan.gateway.entity.ZuulRouteVO;
//import com.marvinpan.gateway.service.ApiGatewayService;
//import com.netflix.zuul.util.HTTPRequestUtils;
///**
// * Created by pantj on 2019/1/4.
// */
//public class ParamRouteLocator extends SimpleRouteLocator implements RefreshableRouteLocator{
//
//    public final static Logger log = LoggerFactory.getLogger(ParamRouteLocator.class);
//
//    private ZuulProperties properties;
//   
//    private AtomicReference<Map<String, ZuulRoute>> routes = new AtomicReference<>();
//    
//	private String dispatcherServletPath = "/";
//	private String zuulServletPath;
//	
//	@Autowired
//	private ApiGatewayService apiGatewayService;
//    
//    public ParamRouteLocator(String servletPath, ZuulProperties properties) {
//        super(servletPath, properties);
//        this.properties = properties;
//        log.info("servletPath:{}",servletPath);
//        
//        if (servletPath != null && org.springframework.util.StringUtils.hasText(servletPath)) {
//			this.dispatcherServletPath = servletPath;
//		}
//
//		this.zuulServletPath = properties.getServletPath();
//    }
//    
//    /**
//     *  父类已经提供了这个方法，这里写出来只是为了说明这一个方法很重要！！！
//     */
//    @Override
//    public void refresh() {
//        doRefresh();
//    }
//    
//    /**
//     * 注册初始化path的zuulHandlerMapping，
//     * 比如：静态文件"/**" -> "zuulHandlerMapping"，动态path "/asset/**" -> "zuulHandlerMapping"
//     */
//    @Override
//    protected Map<String, ZuulRoute> locateRoutes() {
//        LinkedHashMap<String, ZuulRoute> routesMap = new LinkedHashMap<String, ZuulRoute>();
//        //从application.properties中加载路由信息
////        routesMap.putAll(super.locateRoutes());
//        //从db中加载路由信息
//        List<ZuulRouteVO> initRouteList = apiGatewayService.locateRouteFromDB("initialization_id");
//        if( initRouteList != null && initRouteList.size() > 0 ) {
//        	for (int i = 0; i < initRouteList.size(); i++) {
//        		ZuulRouteVO zuulRouteVO = initRouteList.get(i);
//        		String path = zuulRouteVO.getPath();
//        		// Prepend with slash("/") if not already present.
//	            if (!path.startsWith("/")) {
//	                path = "/" + path;
//	            }
//	            if (StringUtils.isNotBlank(this.properties.getPrefix())) {
//	                path = this.properties.getPrefix() + path;
//	                if (!path.startsWith("/")) {
//	                    path = "/" + path;
//	                }
//	            }
//        		
//        		ZuulRoute  zuulRoute = new ZuulRoute();
//        		BeanUtils.copyProperties(zuulRouteVO,zuulRoute);
//        		routesMap.put(zuulRouteVO.getTenantId()+path, zuulRoute);
//			}
//        }
//        return routesMap;
//    }
//    
//    @Override
//	public Route getMatchingRoute(final String path) {
//
//		if (log.isDebugEnabled()) {
//			log.debug("Finding route for path: " + path);
//		}
//
//		if (this.routes.get() == null) {
//			this.routes.set(locateRoutes());
//		}
//
//		if (log.isDebugEnabled()) {
//			log.debug("servletPath=" + this.dispatcherServletPath);
//			log.debug("zuulServletPath=" + this.zuulServletPath);
//			log.debug("RequestUtils.isDispatcherServletRequest()="
//					+ RequestUtils.isDispatcherServletRequest());
//			log.debug("RequestUtils.isZuulServletRequest()="
//					+ RequestUtils.isZuulServletRequest());
//		}
//		//调整路径，与参数无关
//		String adjustedPath = adjustPath(path);
//		/*=====================获取reqTenantId=========================*/
//		String reqTenantId = "";
//		Map<String, List<String>> map = HTTPRequestUtils.getInstance().getQueryParams();
//        if (map == null) {
//			return null;
//		}
//        
//        for (String key : map.keySet()) {
//        	if(StringUtils.equals("tenantid", key)) {
//        		reqTenantId = map.get(key).get(0);
//			}
//		}
//        /*===========================找路由 adjustedPath => contextPath=================================*/
//        ZuulRoute route = null;
//		if (!matchesIgnoredPatterns(adjustedPath)) {
//			List<ZuulRouteVO> voList = apiGatewayService.locateRouteFromDB(reqTenantId);
//			if( adjustedPath.startsWith("/") && voList != null && voList.size() > 0 ) {
//				String contextPath = adjustedPath.substring(1, adjustedPath.length());
//				if(contextPath.contains("/")) {
//					int pathIndex = contextPath.indexOf("/");
//					contextPath = contextPath.substring(0, pathIndex);
//					contextPath = "/" + contextPath;
//				}
//				
//				ZuulRouteVO staticRouteVO = null; 
//				for (int i = 0; i < voList.size(); i++) {
//					ZuulRouteVO routeVO = voList.get(i);
//					int index = routeVO.getPath().indexOf("*") - 1;
//					if (index > 0) {
//						String routePathPrefix = routeVO.getPath().substring(0, index);
//						if(StringUtils.equals(routePathPrefix, contextPath)) {
//							route = new ZuulRoute();
//							BeanUtils.copyProperties(routeVO,route);
//							break;
//						}
//					}else {
//						staticRouteVO = routeVO;
//					}
//				}
//				if(route == null && staticRouteVO != null) {//静态文件
//					route = new ZuulRoute();
//					BeanUtils.copyProperties(staticRouteVO,route);
//				}
//			}
//		}
//		if (log.isDebugEnabled()) {
//			log.debug("route matched=" + route);
//		}
//
//		return getRoute(route, adjustedPath);
//
//	}
//    
//    //copy parent class, no modify
//	protected Route getRoute(ZuulRoute route, String path) {
//		if (route == null) {
//			return null;
//		}
//		String targetPath = path;
//		String prefix = this.properties.getPrefix();
//		if (path.startsWith(prefix) && this.properties.isStripPrefix()) {
//			targetPath = path.substring(prefix.length());
//		}
//		if (route.isStripPrefix()) {
//			int index = route.getPath().indexOf("*") - 1;
//			if (index > 0) {
//				String routePrefix = route.getPath().substring(0, index);
//				targetPath = targetPath.replaceFirst(routePrefix, "");
//				prefix = prefix + routePrefix;
//			}
//		}
//		Boolean retryable = this.properties.getRetryable();
//		if (route.getRetryable() != null) {
//			retryable = route.getRetryable();
//		}
//		return new Route(route.getId(), targetPath, route.getLocation(), prefix,
//				retryable,
//				route.isCustomSensitiveHeaders() ? route.getSensitiveHeaders() : null);
//	}
//	
//	//copy parent class, no modify
//	private String adjustPath(final String path) {
//		String adjustedPath = path;
//
//		if (RequestUtils.isDispatcherServletRequest()
//				&& org.springframework.util.StringUtils.hasText(this.dispatcherServletPath)) {
//			if (!this.dispatcherServletPath.equals("/")) {
//				adjustedPath = path.substring(this.dispatcherServletPath.length());
//				log.debug("Stripped dispatcherServletPath");
//			}
//		}
//		else if (RequestUtils.isZuulServletRequest()) {//上传文件/zuul
//			if (org.springframework.util.StringUtils.hasText(this.zuulServletPath)
//					&& !this.zuulServletPath.equals("/")) {
//				adjustedPath = path.substring(this.zuulServletPath.length());
//				log.debug("Stripped zuulServletPath");
//			}
//		}
//		else {
//			// do nothing
//		}
//
//		log.debug("adjustedPath=" + path);
//		return adjustedPath;
//	}
//    
//}
