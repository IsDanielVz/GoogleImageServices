package com.example.demo.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.GoogleApplication;
import com.example.demo.model.PeticionRecibida;
import com.example.demo.model.RespuestaNostra;
import com.example.demo.service.GoogleConsumeAPIServices;
import com.example.demo.service.ProxyAccesService;
import com.google.gson.Gson;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = GoogleApplication.class)
@WebAppConfiguration
public
class GoogleApplicationTestsService {
	
	@Autowired
	GoogleConsumeAPIServices servicioGoogle;
	
	@Autowired
	ProxyAccesService servicioAccess;
	
//	MockMvc se utiliza para llamar al controlador
	
	@Test
	public
	void imageProcessServiceNull() throws IOException {
		
		RespuestaNostra API = servicioAccess.imageProcessService(null, null);
		
		Boolean uno = API.getIsSuccess();
		Boolean dos = API.getRutaImagen().contentEquals(
				"La imagen recibida está vacia");
		Boolean tre = API.getTextoRequerido().contentEquals(
				"El texto recibidó fué :null");
		Boolean cua = API.getTextoEncontrado().contentEquals(
				"Por favor verifique sus archivos, ya que se encuentran vacios");

		assertFalse(uno);
		assertTrue(dos);
		assertTrue(tre);
		assertTrue(cua);
		
	}
	
	@Test
	public
	void imageProcessServiceNullFIle() throws IOException {
		
		Gson gson = new Gson();
		PeticionRecibida peticion = new PeticionRecibida();
		peticion.setTexto("texto de prueba");
		String json = gson.toJson(peticion);
		
		RespuestaNostra API = servicioAccess.imageProcessService(null, json);
		
		Boolean uno = API.getIsSuccess();
		Boolean dos = API.getRutaImagen().contentEquals(
				"La imagen recibida está vacia");
		Boolean tre = API.getTextoRequerido().contentEquals(
				"El texto recibidó fué :"+json);
		Boolean cua = API.getTextoEncontrado().contentEquals(
				"Por favor verifique sus archivos, ya que se encuentran vacios");

		assertFalse(uno);
		assertTrue(dos);
		assertTrue(tre);
		assertTrue(cua);
		
	}
	
	@Test
	public
	void imageProcessServiceNullString() throws IOException {
		
		File file = new File("./src/main/resources/imageTest/google.jpg");
		Path path = file.toPath();
		String contentType = Files.probeContentType(path);
		byte[] bytesImagen = Files.readAllBytes(path);
		
		MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), 
				contentType, bytesImagen);
		
		RespuestaNostra API = servicioAccess.imageProcessService(multipartFile, null);
		
		Boolean uno = API.getIsSuccess();
		Boolean dos = API.getRutaImagen().contentEquals(
				"La imagen recibida fué :"+multipartFile.getOriginalFilename());
		Boolean tre = API.getTextoRequerido().contentEquals(
				"El texto recibidó fué :null");
		Boolean cua = API.getTextoEncontrado().contentEquals(
				"Por favor verifique sus archivos, ya que se encuentran vacios");

		assertFalse(uno);
		assertTrue(dos);
		assertTrue(tre);
		assertTrue(cua);
		
	}
	
	@Test
	public
	void imageProcessServiceNoJsonStructure() throws IOException {
		
		String json = "Google";
		
		File file = new File("./src/main/resources/imageTest/google.jpg");
		Path path = file.toPath();
		String contentType = Files.probeContentType(path);
		byte[] bytesImagen = Files.readAllBytes(path);
		
		MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), 
				contentType, bytesImagen);
		
		RespuestaNostra API = servicioAccess.imageProcessService(multipartFile, json);
		
		Boolean uno = API.getIsSuccess();
		Boolean dos = API.getRutaImagen().contains(
				"Debido a un error en el archivo 'json' no fue posible subir el archivo :"
				+ multipartFile.getOriginalFilename());
		Boolean tre = API.getTextoRequerido().contentEquals(
				"Entrada json = ''" + json + "'' es invalida, "
				+ "se requiere un formato json para un string de nombre texto");
		Boolean cua = API.getTextoEncontrado().isEmpty();

		assertFalse(uno);
		assertTrue(dos);
		assertTrue(tre);
		assertFalse(cua);
		
	}
	
	@Test
	public
	void imageProcessServiceBucketNameError() throws IOException {
		
		Gson gson = new Gson();
		PeticionRecibida peticion = new PeticionRecibida();
		peticion.setTexto("Google");
		String json = gson.toJson(peticion);
		
		File file = new File("./src/main/resources/imageTest/google.jpg");
		Path path = file.toPath();
		String contentType = Files.probeContentType(path);
		byte[] bytesImagen = Files.readAllBytes(path);
		
		MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), 
				contentType, bytesImagen);
		
		RespuestaNostra API = new RespuestaNostra();
		
		do {
			API = servicioAccess.imageProcessService(multipartFile, json);
		}while(!API.getRutaImagen().contentEquals(
				"Se originó una coincidencia en el nombre del bucket, mande de nuevo su petcición")
				);
		
		Boolean uno = API.getIsSuccess();
		Boolean dos = API.getRutaImagen().contentEquals(
				"Se originó una coincidencia en el nombre del bucket, mande de nuevo su petcición");
		Boolean tre = API.getTextoRequerido().isEmpty();
		Boolean cua = API.getTextoEncontrado().isEmpty();

		assertFalse(uno);
		assertTrue(dos);
		assertTrue(tre);
		assertFalse(cua);
		
	}
	
	@Test
	public
	void imageProcessServiceIdeal() throws IOException {
		
		Gson gson = new Gson();
		PeticionRecibida peticion = new PeticionRecibida();
		peticion.setTexto("Google");
		String json = gson.toJson(peticion);
		
		File file = new File("./src/main/resources/imageTest/google.jpg");
		Path path = file.toPath();
		String contentType = Files.probeContentType(path);
		byte[] bytesImagen = Files.readAllBytes(path);
		
		MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), 
				contentType, bytesImagen);
		
		RespuestaNostra API = servicioAccess.imageProcessService(multipartFile, json);
		
		Boolean uno = API.getIsSuccess();
		Boolean dos = API.getRutaImagen().contains("http");
		Boolean tre = API.getTextoRequerido().contentEquals(
				peticion.getTexto());
		Boolean cua = API.getTextoEncontrado().contains(
				peticion.getTexto());

		assertTrue(uno);
		assertTrue(dos);
		assertTrue(tre);
		assertTrue(cua);
		
	}

}
