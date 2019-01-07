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
    private static final String SQL_SELECT_GATEWAY_API_DEFINE_FROM_ID = 
    		"select * from gateway_api_define where enabled = true and id = ?"; 
    private static final String SQL_SELECT_GATEWAY_API_DEFINE_ALL = 
    		"select * from gateway_api_define where enabled = true"; 
    private static final String SQL_INSERT_GATEWAY_API_DEFINE = 
    		"INSERT INTO gateway_api_define (id, path, service_id, url, retryable, enabled, "
    		+ "strip_prefix, api_name) VALUES(?,?,?,?,?,?,?,?)"; 
    
    @Autowired
    JdbcTemplate jdbcTemplate;
    
    
    @Cacheable(value="zuulRouteVO")
	public ZuulRouteVO locateRouteFromDB(String id) {
		try {
			Thread.currentThread().sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		List<ZuulRouteVO> list = jdbcTemplate.query(SQL_SELECT_GATEWAY_API_DEFINE_FROM_ID, 
				new Object[] {id}, new BeanPropertyRowMapper<ZuulRouteVO>(ZuulRouteVO.class));
		ZuulRouteVO zuulRouteVO = null;
		if(list != null && list.size() > 0) zuulRouteVO = list.get(0);
		return zuulRouteVO;
	}
    
    public Map<String, ZuulRoute> locateRoutesFromDB(){
        Map<String, ZuulRoute> routes = new LinkedHashMap<>();
        List<ZuulRouteVO> results = jdbcTemplate.query(SQL_SELECT_GATEWAY_API_DEFINE_ALL,
        		new BeanPropertyRowMapper<ZuulRouteVO>(ZuulRouteVO.class));
        for (ZuulRouteVO result : results) {
            if(org.apache.commons.lang3.StringUtils.isBlank(result.getPath()) 
            	|| org.apache.commons.lang3.StringUtils.isBlank(result.getUrl())
            	|| org.apache.commons.lang3.StringUtils.isBlank(result.getId())){
                continue;
            }
            ZuulRoute zuulRoute = new ZuulRoute();
            try {
                BeanUtils.copyProperties(result,zuulRoute);
            } catch (Exception e) {
                log.error("=============load zuul route info from db with error==============",e);
            }
            routes.put(result.getId(),zuulRoute);
        }
        return routes;
    }
    
    public int createOneRoute(ZuulRouteVO vo) {
    	return jdbcTemplate.update(SQL_INSERT_GATEWAY_API_DEFINE, 
    			new Object[]{vo.getId(), vo.getPath(), vo.getServiceId(), vo.getUrl(),
    					0,1,1,vo.getApiName()});
    }
    
    
}
