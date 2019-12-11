package com.example.demo.Model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FullTextAnnotation {
	
	@JsonProperty("pages")
	public Map<String, Object> pages;
	
	@JsonProperty("text")
	public String text = new String();
}
