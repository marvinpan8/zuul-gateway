package com.marvinpan.gateway.zuul;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.util.UrlPathHelper;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

//@Component
public class ActuatorPreRequestFilter extends ZuulFilter {
    
	public final static Logger log = LoggerFactory.getLogger(ActuatorPreRequestFilter.class);
	
	private UrlPathHelper urlPathHelper = new UrlPathHelper();
	
	private PathMatcher pathMatcher = new AntPathMatcher();
	
	private ZuulProperties properties;
	
	public ActuatorPreRequestFilter(ZuulProperties properties) {
		this.properties = properties;
		this.urlPathHelper.setRemoveSemicolonContent(properties.isRemoveSemicolonContent());
	}
	
    @Override
	public String filterType() {
        return "pre";
    }
 
    @Override
	public int filterOrder() {
        return 0;
    }
 
    @Override
	public boolean shouldFilter() {
        return true;
    }
 
    @Override
	public Object run() {
    	RequestContext ctx = RequestContext.getCurrentContext();
		final String requestURI = this.urlPathHelper.getPathWithinApplication(ctx.getRequest());
		if( matchesIgnoredPatterns(requestURI) ) {
			ctx.set("forward.to", requestURI);
		}
        return null;
    }
    
    // copy from SimpleRouteLocator
	protected boolean matchesIgnoredPatterns(String path) {
		for (String pattern : this.properties.getIgnoredPatterns()) {
			log.debug("Matching ignored pattern:" + pattern);
			if (this.pathMatcher.match(pattern, path)) {
				log.debug("Path " + path + " matches ignored pattern " + pattern);
				return true;
			}
		}
		return false;
	}
}

