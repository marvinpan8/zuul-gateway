package com.marvinpan.gateway.zuul.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.netflix.zuul.FilterFileManager;
import com.netflix.zuul.FilterLoader;
import com.netflix.zuul.groovy.GroovyCompiler;
import com.netflix.zuul.groovy.GroovyFileFilter;
import com.netflix.zuul.monitoring.MonitoringHelper;

//启动时加载groovy filter
//@Component
public class GroovyLoadLineRunner implements CommandLineRunner{
	private Logger logger = LoggerFactory.getLogger(GroovyLoadLineRunner.class);
	
	@Value("${ecej.groovy.path}")
	private String groovyPath;
	
	@Override
	public void run(String... args) throws Exception {
		MonitoringHelper.initMocks();
		FilterLoader.getInstance().setCompiler(new GroovyCompiler());
		try {
			FilterFileManager.setFilenameFilter(new GroovyFileFilter());
			logger.info(groovyPath);
			FilterFileManager.init(1, groovyPath + "pre", groovyPath + "post");
		} catch	(Exception e) {
			throw new RuntimeException(e);
		}
	}

}
