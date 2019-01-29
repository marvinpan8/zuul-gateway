package com.marvinpan.gateway.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties.ZuulRoute;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.marvinpan.gateway.entity.ZuulRouteVO;

@Service
public class ApiGatewayService {
	public final static Logger log = LoggerFactory.getLogger(ApiGatewayService.class);
    private static final String SQL_SELECT_API_ROUTE_INFO_FROM_TENANT_ID = 
    		"select id, tenant_id, path, url, strip_prefix, api_name, service_id, retryable from t_api_route where enabled = true and tenant_id = ?"; 
    private static final String SQL_SELECT_API_ROUTE_INFO_ALL = 
    		"select id, tenant_id, path, url, strip_prefix, api_name, service_id, retryable from t_api_route where enabled = true"; 
    private static final String SQL_INSERT_API_ROUTE_INFO = 
    "INSERT INTO t_api_route (tenant_id, path, url, strip_prefix, enabled) VALUES(?,?,?,?,?)";
    private static final String SQL_SELECT_API_ROUTE_INFO_FROM_TOKEN = 
    "select a.id, a.tenant_id, a.path, a.url, a.strip_prefix, a.api_name,a.service_id,a.retryable, t.token, t.expire_time from t_api_route a, tenant_info t where a.enabled = true and t.enabled = true and a.tenant_id = t.tenant_id and t.token = ?";
    
    @Autowired
    JdbcTemplate jdbcTemplate;
    
    
	public List<ZuulRouteVO> locateRouteFromDBInit() {
		return jdbcTemplate.query(SQL_SELECT_API_ROUTE_INFO_FROM_TENANT_ID, 
				new Object[] {"initialization_id"}, new BeanPropertyRowMapper<ZuulRouteVO>(ZuulRouteVO.class));
	}
    
    @Cacheable(value="zuulRouteVoListFromTenantId")
	public List<ZuulRouteVO> locateRouteFromTenantId(String tenantId) {
		return jdbcTemplate.query(SQL_SELECT_API_ROUTE_INFO_FROM_TENANT_ID, 
				new Object[] {tenantId}, new BeanPropertyRowMapper<ZuulRouteVO>(ZuulRouteVO.class));
	}
    
    @CacheEvict(value="zuulRouteVoListFromTenantId")
    public void deleteZuulRouteVoListFromTenantId(String tenantId) {
    	log.info("delete route by tenantid: {}" , tenantId);
    }
    
//    @Cacheable(value="zuulRouteVoListFromToken")
    @Deprecated
	public List<ZuulRouteVO> locateRouteFromToken(String tenantId) {
		return jdbcTemplate.query(SQL_SELECT_API_ROUTE_INFO_FROM_TOKEN, 
				new Object[] {tenantId}, new BeanPropertyRowMapper<ZuulRouteVO>(ZuulRouteVO.class));
	}
    
    @Deprecated
    public Map<String, ZuulRoute> locateRoutesFromDB(){
        Map<String, ZuulRoute> routes = new LinkedHashMap<>();
        List<ZuulRouteVO> results = jdbcTemplate.query(SQL_SELECT_API_ROUTE_INFO_ALL,
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
    	return jdbcTemplate.update(SQL_INSERT_API_ROUTE_INFO, 
    			new Object[]{vo.getTenantId(), vo.getPath(), vo.getUrl(), 0, 1});
    }
    
    
}
