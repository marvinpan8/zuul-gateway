//package com.marvinpan.gateway.zuul.utils;
//
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.time.LocalDateTime;
//
//import org.springframework.cloud.netflix.zuul.filters.route.ZuulFallbackProvider;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.client.ClientHttpResponse;
//import org.springframework.stereotype.Component;
//
//import com.google.common.collect.ImmutableMap;
//import com.google.gson.GsonBuilder;
//
//@Component
//public class FallBackHandler implements ZuulFallbackProvider {
//
//	@Override
//	public String getRoute() {
//		// 代表所有的路由都适配该设置
//		return "*";
//	}
//
//	@Override
//	public ClientHttpResponse fallbackResponse() {
//		return new ClientHttpResponse() {
//			@Override
//			public HttpStatus getStatusCode() throws IOException {
//				return HttpStatus.OK;
//			}
//
//			@Override
//			public int getRawStatusCode() throws IOException {
//				return 200;
//			}
//
//			@Override
//			public String getStatusText() throws IOException {
//				return "OK";
//			}
//
//			@Override
//			public void close() {
//
//			}
//
//			@Override
//			public InputStream getBody() throws IOException {
//				String result = new GsonBuilder().create()
//						.toJson(ImmutableMap.of("errorCode", 500, "content", "请求失败", "time", LocalDateTime.now()));
//				return new ByteArrayInputStream(result.getBytes());
//			}
//
//			@Override
//			public HttpHeaders getHeaders() {
//				HttpHeaders headers = new HttpHeaders();
//				headers.setContentType(MediaType.APPLICATION_JSON);
//				return headers;
//			}
//		};
//	}
//}
