package com.example.demo.Model;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class Responses {
	
	@JsonProperty("textAnnotations")
	public Map<String, Object> textAnnotations;
	
	@JsonProperty("fullTextAnnotation")
	public FullTextAnnotation fullTextAnnotation = new FullTextAnnotation();

}
