package com.example.demo.Model;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class model {
	
	  @JsonProperty("responses")
	  public Map<String, Responses> responses;

}
