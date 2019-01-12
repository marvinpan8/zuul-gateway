package com.marvinpan.gateway.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import redis.clients.jedis.JedisPoolConfig;

@Component
@ConfigurationProperties(prefix = "spring.redis")
public class RedisConn {
	//prefix+参数名  对应于配置文件config.properties中的spring.redis.*信息
	private String host;
	
	private int port ;
	
	private String password;
	
	private int timeout;
	
	private int database = 0;
	
	private JedisPoolConfig pool;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	public int getDatabase() {
		return database;
	}

	public void setDatabase(int database) {
		this.database = database;
	}

	public JedisPoolConfig getPool() {
		return pool;
	}

	public void setPool(JedisPoolConfig pool) {
		this.pool = pool;
	}
	
}
