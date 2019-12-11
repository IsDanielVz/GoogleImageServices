package com.example.demo.Model;

import java.util.ArrayList;

import org.springframework.stereotype.Component;

@Component
public class GoogleAPIRequest {

	ArrayList<Request> requests = new ArrayList<>();
	
	public ArrayList<Request> getRequests() {
		return requests;
	}

	public void setRequests(ArrayList<Request> requests) {
		this.requests = requests;
	}

	@Override
	public String toString() {
		return "json [request=" + requests + ", getClass()=" + getClass() + ", hashCode()=" + hashCode()
				+ ", toString()=" + super.toString() + "]";
	}

}
