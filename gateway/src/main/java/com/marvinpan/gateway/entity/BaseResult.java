package com.marvinpan.gateway.entity;

import java.time.LocalDateTime;

import org.apache.commons.lang.StringUtils;

/**
 *
 * http请求返回的最外层对象
 */
public class BaseResult<T> {
	// 总结:A-B-C
	// A:首先固定格式,通过Result对象固定这三个字段-
	// 跳转到B:GirlService类-public void getAge(Integer id) throws Exception{}下看
	
	public BaseResult() {}
	
	public BaseResult(Integer status, String message) {
		this.status = status;
		this.message = message;
		this.setData(LocalDateTime.now().toString());
	}
	
	//错误码
	private Integer status;
	//提示信息
	private String message;
	//具体的内容
	private T data;
	
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public T getData() {
		return data;
	}
	public void setData(T data) {
		this.data = data;
	}
	public void setData(String data) {
		this.data = (T) data;
	}
	
	/**
	 * 404 or 50x code
	 * @param status
	 * @return
	 */
	public static BaseResult<String> choose(Integer status, String message) {
//		String msg = status == 404 ? "访问地址不存在" : "内部服务器错误,正在处理";
		String msg = StringUtils.isBlank(message) ? "Success" : message;
		if(status >= 300 && status < 400)
			msg = StringUtils.isBlank(message) ? "Redirection error" : message;
		if(status >= 400 && status < 500 && StringUtils.isBlank(message))
			msg = StringUtils.isBlank(message) ? "Resources can't be found" : message;
		if(status >= 500 && status < 600 && StringUtils.isBlank(message)) 
			msg = StringUtils.isBlank(message) ? "Internal server error" : message;
		return new BaseResult<String>(status, msg);
	}

}
