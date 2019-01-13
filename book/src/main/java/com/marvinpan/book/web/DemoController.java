package com.marvinpan.book.web;

import java.util.concurrent.TimeUnit;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {
	
    @RequestMapping(value = "/available")
    public String available() {
    	String active = System.getProperty("spring.profiles.active");
    	String ret = "Spring A1 available in Action";
    	if(active == "a2") {
    		ret = "Spring A2 available in Action";
    	}
        return ret;
    }

    @RequestMapping(value = "/checkout/action")
    public String checkedOut() {
    	String active = System.getProperty("spring.profiles.active");
    	String ret = "Spring Boot A1 checkout in Action";
    	if(active == "a2") {
    		ret = "Spring Boot A2 checkout in Action";
    	}
        return ret;
    }
    
    @RequestMapping("/sleep/{sleepTime}")
    public String sleep(@PathVariable Long sleepTime) throws InterruptedException {
     TimeUnit.SECONDS.sleep(sleepTime);
     return "SUCCESS";
    }
	
}
