package com.marvinpan.book.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {
	
    @RequestMapping(value = "/available")
    public String available() {
    	String active = System.getProperty("spring.profiles.active");
    	String ret = "Spring A1 in Action";
    	if(active == "a2") {
    		ret = "Spring A2 in Action";
    	}
        return ret;
    }

    @RequestMapping(value = "/checkout")
    public String checkedOut() {
    	String active = System.getProperty("spring.profiles.active");
    	String ret = "Spring Boot A1 in Action";
    	if(active == "a2") {
    		ret = "Spring Boot A2 in Action";
    	}
        return ret;
    }
	
}
