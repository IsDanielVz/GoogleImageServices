package com.example.demo.service;

import java.util.ArrayList;
import java.util.Base64;

import org.springframework.stereotype.Service;

import com.example.demo.model.Feature;
import com.example.demo.model.GoogleAPIRequest;
import com.example.demo.model.Image;
import com.example.demo.model.Request;

@Service
public class TransformService {

	public String toBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
	}
	
	public String textoEncontrado(String text) {
		String[] respuestaVision = text.split("\"description\": \"");
		String[] textoEncontrado = respuestaVision[1].split("\"");
		return textoEncontrado[0];
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
