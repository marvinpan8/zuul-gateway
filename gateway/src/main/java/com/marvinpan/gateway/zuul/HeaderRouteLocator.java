package com.marvinpan.gateway.zuul;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
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
import org.springframework.cloud.netflix.zuul.util.ZuulRuntimeException;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import com.marvinpan.gateway.entity.ZuulRouteVO;
import com.marvinpan.gateway.service.ApiGatewayService;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
/**
 * Created by pantj on 2019/1/12.
 */
public class HeaderRouteLocator extends SimpleRouteLocator implements RefreshableRouteLocator{

    public final static Logger log = LoggerFactory.getLogger(HeaderRouteLocator.class);

    private ZuulProperties properties;
   
    private AtomicReference<Map<String, ZuulRoute>> routes = new AtomicReference<>();
    
	private String dispatcherServletPath = "/";
	private String zuulServletPath;
	
	private PathMatcher pathMatcher = new AntPathMatcher();
	
	@Autowired
	private ApiGatewayService apiGatewayService;
	
    public HeaderRouteLocator(String servletPath, ZuulProperties properties) {
        super(servletPath, properties);
        this.properties = properties;
        log.info("servletPath:{}",servletPath);
        
        if (servletPath != null && org.springframework.util.StringUtils.hasText(servletPath)) {
			this.dispatcherServletPath = servletPath;
		}

		this.zuulServletPath = properties.getServletPath();
    }
    
    /**
     *  父类已经提供了这个方法，这里写出来只是为了说明这一个方法很重要！！！
     */
    @Override
    public void refresh() {
        doRefresh();
    }
    
    /**
           * 注册初始化path的zuulHandlerMapping，
           * 比如：静态文件"/**" -> "zuulHandlerMapping"，动态path "/asset/**" -> "zuulHandlerMapping"
     */
    @Override
    protected Map<String, ZuulRoute> locateRoutes() {
        LinkedHashMap<String, ZuulRoute> routesMap = new LinkedHashMap<String, ZuulRoute>();
        //从application.properties中加载路由信息
//        routesMap.putAll(super.locateRoutes());
        //从db中加载路由信息
        List<ZuulRouteVO> initRouteList = apiGatewayService.locateRouteFromDBInit();
        if( initRouteList != null && initRouteList.size() > 0 ) {
        	for (int i = 0; i < initRouteList.size(); i++) {
        		ZuulRouteVO zuulRouteVO = initRouteList.get(i);
        		String path = zuulRouteVO.getPath();
        		// Prepend with slash("/") if not already present.
	            if (!path.startsWith("/")) {
	                path = "/" + path;
	            }
	            if (StringUtils.isNotBlank(this.properties.getPrefix())) {
	                path = this.properties.getPrefix() + path;
	                if (!path.startsWith("/")) {
	                    path = "/" + path;
	                }
	            }
        		
        		ZuulRoute  zuulRoute = new ZuulRoute();
        		BeanUtils.copyProperties(zuulRouteVO,zuulRoute);
        		routesMap.put(zuulRouteVO.getTenantId()+path, zuulRoute);
			}
        }
        return routesMap;
    }
    
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
		if(adjustedPath == null) {
			throw new ZuulRuntimeException(new ZuulException(
					"Can not find Route Path",HttpServletResponse.SC_NOT_FOUND,
					"Can not find Route Path"));
		}
		/*=====================获取tenantId=========================*/
		 RequestContext ctx = RequestContext.getCurrentContext();
		 String tenantId = (String)ctx.remove("tenantid");
		 if(StringUtils.isBlank(tenantId)) {
			 throw new ZuulRuntimeException(new ZuulException(
						"Can not find Route ID",HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"Internal server error, pls contact Customer Service"));
		 }
        /*===========================找路由 adjustedPath=================================*/
        ZuulRoute route = null;
		if (!matchesIgnoredPatterns(adjustedPath)) {
			List<ZuulRouteVO> voList = new ArrayList<ZuulRouteVO>();
			try {
				voList = apiGatewayService.locateRouteFromTenantId(tenantId);
			} catch (Exception e) {
				throw new ZuulRuntimeException(new ZuulException(
								"locate Route error",HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
								"Internal server error, pls contact Customer Service"));
			}
			
			if(voList == null || voList.size() == 0) {
				throw new ZuulRuntimeException(new ZuulException(
						"unauthorized 2",HttpServletResponse.SC_UNAUTHORIZED,
						"Api unauthorized 2"));
			}
			
			for (int i = 0; i < voList.size(); i++) {
				ZuulRouteVO routeVO = voList.get(i);
				String zuulpath = routeVO.getPath();
				// Prepend with slash("/") if not already present.
	            if (!zuulpath.startsWith("/")) {
	            	zuulpath = "/" + zuulpath;
	            }
	            if (StringUtils.isNotBlank(this.properties.getPrefix())) {
	            	zuulpath = this.properties.getPrefix() + zuulpath;
	                if (!zuulpath.startsWith("/")) {
	                	zuulpath = "/" + zuulpath;
	                }
	            }
				
				if (this.pathMatcher.match(zuulpath, adjustedPath)) {
					route = new ZuulRoute();
					BeanUtils.copyProperties(routeVO,route);
					break;
				}
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("route matched=" + route);
		}
		
		return super.getRoute(route, adjustedPath);

	}
    
    //copy parent class, no modify
	private String adjustPath(final String path) {
		if(StringUtils.isBlank(path)) return null;
		
		String adjustedPath = path;
		//dispatcherServletPath=/ 默认进入此方法，去除最前面的dispatcherServletPath
		if (RequestUtils.isDispatcherServletRequest()
				&& StringUtils.isNotBlank(this.dispatcherServletPath)) {
			if (!this.dispatcherServletPath.equals("/")) {
				adjustedPath = path.substring(this.dispatcherServletPath.length());
				log.debug("Stripped dispatcherServletPath");
			}
		}
		else if (RequestUtils.isZuulServletRequest()) {//上传文件/zuul
			if (StringUtils.isNotBlank(this.zuulServletPath)
					&& !this.zuulServletPath.equals("/")) {
				adjustedPath = path.substring(this.zuulServletPath.length());
				log.debug("Stripped zuulServletPath");
			}
		}
		else {
			// do nothing
		}
		log.debug("adjustedPath=" + adjustedPath);
		return adjustedPath;
	}

}
