package com.example.demo.Model;

import java.util.ArrayList;

public class Request {
	
	Image image = new Image();
	
	ArrayList<Feature> features = new ArrayList<>();

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
	}

	public ArrayList<Feature> getFeatures() {
		return features;
	}

	public void setFeatures(ArrayList<Feature> features) {
		this.features = features;
	}

		

}
