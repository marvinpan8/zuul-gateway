package com.marvinpan.gateway.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import com.marvinpan.gateway.entity.ZuulRouteVO;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.TimeUnit;

//@Component
public class RedisUtil{
	
	@SuppressWarnings("rawtypes")
	@Autowired
	private RedisTemplate redisTemplate;

	/**
	* 批量删除对应的value
	*
	* @param keys
	*/
	public void remove(final String... keys) {
		for (String key : keys) {
			remove(key);
		}
	}

	/**
	* 批量删除key
	*
	* @param pattern
	*/
	@SuppressWarnings("unchecked")
	public void removePattern(final String pattern) {
		Set<Serializable> keys = redisTemplate.keys(pattern);
		if (keys.size() > 0)
			redisTemplate.delete(keys);
	}

	/**
	* 删除对应的value
	*
	* @param key
	*/
	@SuppressWarnings("unchecked")
	public void remove(final String key) {
		if (exists(key)) {
			redisTemplate.delete(key);
		}
	}

	/**
	* 判断缓存中是否有对应的value
	*
	* @param key
	* @return
	*/
	@SuppressWarnings("unchecked")
	public boolean exists(final String key) {
		return redisTemplate.hasKey(key);
	}

	/**
	* 读取缓存
	*
	* @param key
	* @return
	*/
	@SuppressWarnings("unchecked")
	public Object get(final String key) {
		Object result = null;
		ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
		result = operations.get(key);
		return result;
	}

	/**
	* 写入缓存
	*
	* @param key
	* @param value
	* @return
	*/
	@SuppressWarnings("unchecked")
	public boolean set(final String key, Object value) {
		boolean result = false;
		try {
			ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
			operations.set(key, value);
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	* 写入缓存
	*
	* @param key
	* @param value
	* @return
	*/
	@SuppressWarnings("unchecked")
	public boolean set(final String key, Object value, Long expireTime) {
		boolean result = false;
		try {
			ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
			operations.set(key, value);
			redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
     * 判断Redis中是否存在对应key的信息，若存在则从reids中获取，
     * 若不存在则从数据库查询，同时存入redis缓存中
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

}

