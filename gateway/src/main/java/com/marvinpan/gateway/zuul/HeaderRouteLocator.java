package com.marvinpan.gateway.zuul;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
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
import com.netflix.zuul.util.HTTPRequestUtils;
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
        List<ZuulRouteVO> initRouteList = apiGatewayService.locateRouteFromTenantId("initialization_id");
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
		/*=====================获取token=========================*/
		
		String token = HTTPRequestUtils.getInstance().getHeaderValue("Authorization");
		
		if(token == null) {
			exportUnauthorizedException();
		}
        
        /*===========================找路由 adjustedPath=================================*/
        ZuulRoute route = null;
		if (!matchesIgnoredPatterns(adjustedPath)) {
			List<ZuulRouteVO> voList = new ArrayList<ZuulRouteVO>();
			try {
				voList = apiGatewayService.locateRouteFromToken(token);
			} catch (Exception e) {
				throw new ZuulRuntimeException(new ZuulException(
								"locate Route error",HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
								"Internal server error, pls contact Customer Service"));
			}
			
			if(voList == null || voList.size() == 0) {
				exportUnauthorizedException();
			}
			
			for (int i = 0; i < voList.size(); i++) {
				ZuulRouteVO routeVO = voList.get(i);
				if (this.pathMatcher.match(routeVO.getPath(), adjustedPath)) {
					Date expireTime = routeVO.getExpireTime();
					if(expireTime.getTime() < Clock.systemDefaultZone().millis()) {
						exportUnauthorizedException();
					}
					route = new ZuulRoute();
					BeanUtils.copyProperties(routeVO,route);
					break;
				}
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("route matched=" + route);
		}
		
		return getRoute(route, adjustedPath);

	}

	private void exportUnauthorizedException() {
		throw new ZuulRuntimeException(new ZuulException(
				"unauthorized",HttpServletResponse.SC_UNAUTHORIZED,
				"Api unauthorized"));
	}
    
    //copy parent class, no modify
	protected Route getRoute(ZuulRoute route, String path) {
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
	
	//copy parent class, no modify
	private String adjustPath(final String path) {
		String adjustedPath = path;

		if (RequestUtils.isDispatcherServletRequest()
				&& org.springframework.util.StringUtils.hasText(this.dispatcherServletPath)) {
			if (!this.dispatcherServletPath.equals("/")) {
				adjustedPath = path.substring(this.dispatcherServletPath.length());
				log.debug("Stripped dispatcherServletPath");
			}
		}
		else if (RequestUtils.isZuulServletRequest()) {//上传文件/zuul
			if (org.springframework.util.StringUtils.hasText(this.zuulServletPath)
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
