package com.marvinpan.gateway.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties.ZuulRoute;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.marvinpan.gateway.entity.ZuulRouteVO;

@Service
public class ApiGatewayService {
	public final static Logger log = LoggerFactory.getLogger(ApiGatewayService.class);
    private static final String SQL_SELECT_GATEWAY_API_DEFINE_FROM_TENANT_ID = 
    		"select * from gateway_api_define where enabled = true and tenant_id = ?"; 
    private static final String SQL_SELECT_GATEWAY_API_DEFINE_ALL = 
    		"select * from gateway_api_define where enabled = true"; 
    private static final String SQL_INSERT_GATEWAY_API_DEFINE = 
    "INSERT INTO gateway_api_define (tenant_id, path, url, strip_prefix, enabled) VALUES(?,?,?,?,?)"; 
    
    @Autowired
    JdbcTemplate jdbcTemplate;
    
    
    @Cacheable(value="zuulRouteVoList")
	public List<ZuulRouteVO> locateRouteFromDB(String tenantId) {
		return jdbcTemplate.query(SQL_SELECT_GATEWAY_API_DEFINE_FROM_TENANT_ID, 
				new Object[] {tenantId}, new BeanPropertyRowMapper<ZuulRouteVO>(ZuulRouteVO.class));
	}
    
    @Deprecated
    public Map<String, ZuulRoute> locateRoutesFromDB(){
        Map<String, ZuulRoute> routes = new LinkedHashMap<>();
        List<ZuulRouteVO> results = jdbcTemplate.query(SQL_SELECT_GATEWAY_API_DEFINE_ALL,
        		new BeanPropertyRowMapper<ZuulRouteVO>(ZuulRouteVO.class));
        for (ZuulRouteVO result : results) {
            if(org.apache.commons.lang3.StringUtils.isBlank(result.getPath()) 
            	|| org.apache.commons.lang3.StringUtils.isBlank(result.getUrl())
            	|| org.apache.commons.lang3.StringUtils.isBlank(result.getTenantId())){
                continue;
            }
            ZuulRoute zuulRoute = new ZuulRoute();
            try {
                BeanUtils.copyProperties(result,zuulRoute);
            } catch (Exception e) {
                log.error("=============load zuul route info from db with error==============",e);
            }
            routes.put(result.getTenantId() + result.getPath(),zuulRoute);
        }
        return routes;
    }
    
    public int createOneRoute(ZuulRouteVO vo) {
    	return jdbcTemplate.update(SQL_INSERT_GATEWAY_API_DEFINE, 
    			new Object[]{vo.getTenantId(), vo.getPath(), vo.getUrl(), 1, 1});
    }
    
    
}
