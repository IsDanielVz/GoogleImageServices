package com.example.demo.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;

import org.springframework.stereotype.Service;

import com.example.demo.Model.Feature;
import com.example.demo.Model.GoogleAPIRequest;
import com.example.demo.Model.Image;
import com.example.demo.Model.PeticionRecibida;
import com.example.demo.Model.Request;
import com.example.demo.Model.model;
import com.google.gson.Gson;

@Service
public class TransformService {
	
	public String toBase64(byte[] bytes) throws IOException{
        return Base64.getEncoder().encodeToString(bytes);
	}
	
	public PeticionRecibida jsonToPeticionRecibida(String text) {
		Gson json = new Gson();
		return json.fromJson(text, PeticionRecibida.class);
	}
	
	public model jsonToModel(String text) {
		Gson json = new Gson();
		return json.fromJson(text, model.class);
	}	
	
	public GoogleAPIRequest armarPeticion(String context) {
		GoogleAPIRequest solicitud = new GoogleAPIRequest();
		
		Image imagen = new Image();
		imagen.setContent(context);
	
		Feature caracteristicas = new Feature();
		caracteristicas.setType("TEXT_DETECTION");
	
		Request request = new Request();
		ArrayList<Feature> features = new ArrayList<>();
		features.add(caracteristicas);
		request.setFeatures(features);
		request.setImage(imagen);
	
		ArrayList<Request> peticiones = new ArrayList<>();
		peticiones.add(request);
		solicitud.setRequests(peticiones);
		
		return solicitud;
	}


}
