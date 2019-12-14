package com.example.demo.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.model.PeticionRecibida;
import com.example.demo.model.RespuestaNostra;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.cloud.storage.StorageException;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

@Service
public class ProxyAccesService {
	
	@Autowired
	GoogleConsumeAPIServices apiServices;

	public RespuestaNostra imageProcessService(MultipartFile file, String text) throws IOException {

		RespuestaNostra nostra = new RespuestaNostra();

		if (text != null && !text.contentEquals("") && !text.isEmpty() 
				&& file != null && !file.isEmpty()) {
			
			return validatingFactory(file, text);
			
		} else {
			nostra.setIsSuccess(false);
			nostra.setRutaImagen("Por favor verifique sus archivos, ya que se encuentran vacios");
			nostra.setTextoRequerido("El texto recibidó fué :" + text);
			if (file != null)
				nostra.setTextoEncontrado("La imagen recibida fué :" + file.getOriginalFilename());
			else
				nostra.setTextoEncontrado("La imagen recibida está vacia");
		}

		return nostra;

	}
	
	public RespuestaNostra validatingFactory(MultipartFile file, String text) throws IOException {

		RespuestaNostra nostra = new RespuestaNostra();
		
		try {
			Gson gson = new Gson();
			PeticionRecibida recibida = gson.fromJson(text, PeticionRecibida.class);
			return apiServices.bundleGoogleServices(verifyImage(file), recibida.getTexto());
		} catch (JsonSyntaxException causa) {
			nostra.setTextoRequerido("Entrada json = ''" + text + "'' es invalida, "
					+ "se requiere un formato json para un string de nombre texto");
			nostra.setTextoEncontrado(causa.getMessage());
			nostra.setRutaImagen("Debido a un error en el archivo 'json' no fue posible subir el archivo :"
					+ file.getOriginalFilename());
			return nostra;
		} catch (IllegalStateException causa) {
			nostra.setTextoEncontrado(causa.getMessage());
			nostra.setRutaImagen("Error imprevisto con : " + causa.getStackTrace());
			return nostra;
		} catch (StorageException causa) {
			nostra.setTextoEncontrado(causa.getMessage());
			nostra.setRutaImagen("Se originó una coincidencia en el nombre del bucket, mande de nuevo su petcición");
			return nostra;
		} catch (GoogleJsonResponseException causa) {
			nostra.setTextoEncontrado(causa.getMessage());
			nostra.setRutaImagen("Error imprevisto con : " + causa.getCause());
			return nostra;
		} catch (Exception causa) {
			nostra.setTextoEncontrado(causa.getMessage());
			nostra.setRutaImagen("Error imprevisto con : " + causa.getStackTrace());
			return nostra;
		}
		
	}
	
	public byte[] verifyImage(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        byte[] imagenString = null;
        if (fileName.contains(".")) {
            final String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
            String[] allowedExt = { "jpg", "jpeg", "png", "gif" };
            for (String s : allowedExt) {
                if (extension.equals(s)) {
                    imagenString = file.getBytes();
                }
            }
        }
        return imagenString;
    }

}
