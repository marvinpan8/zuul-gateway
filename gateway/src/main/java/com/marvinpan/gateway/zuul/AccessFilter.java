package com.marvinpan.gateway.zuul;

import java.io.UnsupportedEncodingException;
import java.time.Clock;
import java.util.Base64;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.zuul.util.ZuulRuntimeException;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.marvinpan.gateway.entity.TokenCheckVo;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.netflix.zuul.util.HTTPRequestUtils;

@Component
public class AccessFilter extends ZuulFilter {
	public final static Logger log = LoggerFactory.getLogger(AccessFilter.class);
	
	private final Base64.Decoder decoder = Base64.getDecoder();
	
	@Autowired
    RestTemplate restTemplate;
	
	@Value("${auth.url}")
    String authURL;

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
//        ctx.set("startTime",System.currentTimeMillis());
        
        /*=====================获取token=========================*/
		String token = HTTPRequestUtils.getInstance().getHeaderValue("Authorization");
		
		if(token == null || !token.startsWith("Bearer ")) {
			exportUnauthorizedException();
		}
		
		token = token.substring(7);
		if(StringUtils.isBlank(token)) {
			exportUnauthorizedException();
		}
		
		try {
			token = new String(decoder.decode(token.getBytes("UTF-8")));
		} catch (Exception e) {
			exportUnauthorizedException();
		}
		
		//==========================调用认证中心===================================

        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<String, Object>();
        paramMap.add("token", token);
        long startTime = Clock.systemDefaultZone().millis();
        TokenCheckVo vo = restTemplate.postForObject(authURL+"/token/check", paramMap, TokenCheckVo.class);
        long endTime = Clock.systemDefaultZone().millis();
        log.info("post authServer check token lost: {} ms", endTime- startTime);
        if(vo.getExpired() || StringUtils.isBlank(vo.getTenantId())) {
        	exportUnauthorizedException();
        }
        ctx.set("tenantid", vo.getTenantId());
        return null;
    }
    
	private void exportUnauthorizedException() {
		throw new ZuulRuntimeException(new ZuulException(
				"unauthorized",HttpServletResponse.SC_UNAUTHORIZED,
				"Api unauthorized"));
	}
	
}
