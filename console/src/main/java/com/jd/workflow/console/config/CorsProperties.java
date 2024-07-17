package com.jd.workflow.console.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "cors.filter")
public class CorsProperties {

//	public CorsProperties() {
//		originList.add("http://local.jd.com");
//		originList.add("https://local.jd.com");
//		originList.add("http://test.debug.tool.jd.com");
//		originList.add("https://test.debug.tool.jd.com");
//	}
//
//	private List<String> originList = new ArrayList<>();
//
//	public List<String> getOriginList() {
//		return originList;
//	}
//
//	public void setOriginList(List<String> originList) {
//		this.originList = originList;
//	}

	private CorsConfiguration configuration = new CorsConfiguration().applyPermitDefaultValues();

	private String path = "/**";

	public CorsConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(CorsConfiguration configuration) {
		this.configuration = configuration;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
