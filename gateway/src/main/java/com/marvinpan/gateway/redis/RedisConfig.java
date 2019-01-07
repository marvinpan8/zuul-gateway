package com.marvinpan.gateway.redis;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marvinpan.gateway.zuul.CustomRouteLocator;

import redis.clients.jedis.JedisPoolConfig;


@Configuration
@EnableCaching
public class RedisConfig extends CachingConfigurerSupport {
	public final static Logger log = LoggerFactory.getLogger(RedisConfig.class);
	
	@Autowired
	private RedisConn redisConn;

	/**
	* 生产key的策略
	*
	* @return
	*/
	@Bean
	@Override
	public KeyGenerator keyGenerator() {
		return new KeyGenerator() {
			@Override
			public Object generate(Object target, Method method, Object... params) {
				StringBuilder sb = new StringBuilder();
				sb.append(target.getClass().getName());
				sb.append(method.getName());
				for (Object obj : params) {
					sb.append(obj.toString());
				}
					return sb.toString();
			}
		};
	}

	/**
	* 管理缓存
	*
	* @param redisTemplate
	* @return
	*/
	
	@SuppressWarnings("rawtypes")
	@Bean
	public CacheManager CacheManager(RedisTemplate redisTemplate) {
		RedisCacheManager rcm = new RedisCacheManager(redisTemplate);
		// 设置cache过期时间,时间单位是秒
		rcm.setDefaultExpiration(86400L);
		return rcm;
	}
	
	/**
	* redis 数据库连接池
	* @return
	*/
	
	@Bean
	public JedisConnectionFactory redisConnectionFactory() {
		JedisConnectionFactory factory = new JedisConnectionFactory();
		factory.setHostName(redisConn.getHost());
		factory.setPort(redisConn.getPort());
		if(StringUtils.isNotBlank(redisConn.getPassword()))
			factory.setPassword(redisConn.getPassword());
		factory.setTimeout(redisConn.getTimeout()); // 设置连接超时时间
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(redisConn.getPool().getMaxTotal());
		poolConfig.setMaxIdle(redisConn.getPool().getMaxIdle());
		poolConfig.setMinIdle(redisConn.getPool().getMinIdle());
		poolConfig.setMaxWaitMillis(redisConn.getPool().getMaxWaitMillis());
		poolConfig.setBlockWhenExhausted(true);
		poolConfig.setTestOnBorrow(true);
//		poolConfig.setTestOnReturn(testOnReturn);
//		poolConfig.setTestWhileIdle(testWhileIdle);
//		poolConfig.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
//		poolConfig.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
//		poolConfig.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
		factory.setPoolConfig(poolConfig);
		return factory;
	}

	/**
	* redisTemplate配置
	*
	* @param factory
	* @return
	*/
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Bean
	public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
		StringRedisTemplate template = new StringRedisTemplate(factory);
		Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
		ObjectMapper om = new ObjectMapper();
		om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
		om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		jackson2JsonRedisSerializer.setObjectMapper(om);
		template.setValueSerializer(jackson2JsonRedisSerializer);
		StringRedisSerializer keySerializer = new StringRedisSerializer();
		template.setKeySerializer(keySerializer);
		template.setHashKeySerializer(keySerializer);
		template.setHashValueSerializer(jackson2JsonRedisSerializer);
		template.afterPropertiesSet();
		return template;
	}
	
	/**
	 * 自定义异常捕获，（缓存异常则直接查数据库）
	 */
	@Override
    public CacheErrorHandler errorHandler() {
        CacheErrorHandler cacheErrorHandler = new CacheErrorHandler() {
 
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                RedisErrorException(exception, key);
            }
 
            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                RedisErrorException(exception, key);
            }
 
            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                RedisErrorException(exception, key);
            }
 
            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                RedisErrorException(exception, null);
            }
        };
        return cacheErrorHandler;
    }
 
    protected void RedisErrorException(Exception exception,Object key){
        log.error("redis异常：key=[{}], exception={}", key, exception.getMessage());
    }
    
    /**
     * 配置 JedisConnectionFactory
     * @return 返回JedisConnectionFactory对象
     */
//    @Bean
//    public JedisConnectionFactory jedisConnectionFactory(){
//        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(redisClusterConfiguration(), jedisPoolConfig());
//        jedisConnectionFactory.setPassword(password);
//        jedisConnectionFactory.setTimeout(timeout);
//        return jedisConnectionFactory;
//    }
    
    /**
     * 配置 RedisClusterConfiguration
     * @return RedisClusterConfiguration对象
     */
//    @Bean
//    public RedisClusterConfiguration redisClusterConfiguration(){
//        RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration();
//        redisClusterConfiguration.setMaxRedirects(3);
//        redisClusterConfiguration.setClusterNodes(getRedisNodes());
//        return redisClusterConfiguration;
//    }
    
    /**
     * 封装各Redis节点信息
     * @return Redis节点Set集合
     */
//    private Set<RedisNode> getRedisNodes(){
//        Set<RedisNode> set = new HashSet<RedisNode>();
//        RedisNode redisNode1 = new RedisNode(nodeOne, nodeOnePort);
//        set.add(redisNode1);
//        RedisNode redisNode2 = new RedisNode(nodeTwo, nodeTwoPort);
//        set.add(redisNode2);
//        RedisNode redisNode3 = new RedisNode(nodeThree, nodeThreePort);
//        set.add(redisNode3);
//        RedisNode redisNode4 = new RedisNode(nodeFour, nodeFourPort);
//        set.add(redisNode4);
//        RedisNode redisNode5 = new RedisNode(nodeFive, nodeFivePort);
//        set.add(redisNode5);
//        RedisNode redisNode6 = new RedisNode(nodeSix, nodeSixPort);
//        set.add(redisNode6);
//        RedisNode redisNode7 = new RedisNode(nodeSeven, nodeSevenPort);
//        set.add(redisNode7);
//        return set;
//    }

}
