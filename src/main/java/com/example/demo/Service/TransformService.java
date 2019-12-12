package com.example.demo.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;

import org.springframework.stereotype.Service;

import com.example.demo.Model.Feature;
import com.example.demo.Model.GoogleAPIRequest;
import com.example.demo.Model.Image;
import com.example.demo.Model.Request;

@Service
public class TransformService {

	String jsonFormato = "{''texto'' : ''texto en imagen''}";
	
	public String getJsonFormato() {
		return jsonFormato;
	}

	public String toBase64(byte[] bytes) throws IOException{
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
