package com.example.demo.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.demo.model.GoogleAPIRequest;
import com.example.demo.model.PeticionRecibida;
import com.example.demo.model.RespuestaNostra;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageClass;
import com.google.cloud.storage.StorageException;
import com.google.cloud.storage.StorageOptions;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import reactor.core.publisher.Mono;

@Service
public class GoogleConsumeAPIServices {
	
	@Autowired
	TransformService transformService;
	
	String urlServer = "https://vision.googleapis.com";
	
	public RespuestaNostra imageProcessService(MultipartFile file, String text) throws IOException {
		
		RespuestaNostra nostra = new RespuestaNostra();
		
		if( text != null && !text.contentEquals("") && !text.isEmpty() &&
				file != null && !file.isEmpty() ) {
			
			byte[] archivoEnBytes = file.getBytes();
			
			try {
				Gson gson = new Gson();
				PeticionRecibida recibida = gson.fromJson(text, PeticionRecibida.class);
				return bundleGoogleServices(archivoEnBytes, recibida.getTexto());
			}catch(JsonSyntaxException causa){
				nostra.setTextoRequerido("Entrada json = ''"+ text +"'' es invalida, "
						+ "se requiere un formato json para un string de nombre texto");
				nostra.setTextoEncontrado(causa.getMessage());
				nostra.setRutaImagen("Debido a un error en el archivo 'json' no fue posible subir el archivo :"
						+file.getOriginalFilename());
				return nostra;
			}catch(IllegalStateException causa){
				nostra.setTextoEncontrado(causa.getMessage());
				nostra.setRutaImagen("Error imprevisto con : " + causa.getStackTrace());
					return nostra;
			}catch(StorageException causa){
				nostra.setTextoEncontrado(causa.getMessage());
				nostra.setRutaImagen("Se originó una coincidencia en el nombre del bucket, mande de nuevo su petcición");
					return nostra;
			}catch(GoogleJsonResponseException causa){
				nostra.setTextoEncontrado(causa.getMessage());
				nostra.setRutaImagen("Error imprevisto con : " + causa.getCause());
					return nostra;
			}catch(Exception causa){
				nostra.setTextoEncontrado(causa.getMessage());
				nostra.setRutaImagen("Error imprevisto con : " + causa.getStackTrace());
					return nostra;
			}	
			
		}else {
			nostra.setIsSuccess(false);
			nostra.setRutaImagen("Por favor verifique sus archivos, ya que se encuentran vacios");
			nostra.setTextoRequerido("El texto recibidó fué :"+text);
			if(file != null)
				nostra.setTextoEncontrado("La imagen recibida fué :"+file.getOriginalFilename());
			else
				nostra.setTextoEncontrado("La imagen recibida está vacia");
		}
		
		return nostra;
		
	}
	
	public RespuestaNostra bundleGoogleServices(byte[] archivoEnBytes, String peticionRecibida) throws IOException {
		
		RespuestaNostra nostra = new RespuestaNostra();
		System.out.println("Iniciamos peticiones a google VISION");
		Mono<String> vision = consumirGoogleVisionAPI(
				transformService.armarPeticion(
						transformService.toBase64(archivoEnBytes)));
		System.out.println("Se termina operación con VISION, comensamos con STORAGE");
		Mono<String> bucket = consumirGoogleStorageAPI(archivoEnBytes);
		System.out.println("Se termina operación STORAGE");
		String textoEncontrado = transformService.textoEncontrado(getMonoValue(vision));
		
			nostra.setIsSuccess(textoEncontrado.contains(peticionRecibida));
			nostra.setTextoRequerido(peticionRecibida);
			nostra.setRutaImagen(getMonoValue(bucket));
			nostra.setTextoEncontrado(textoEncontrado);
		
		return nostra;
		
	}
	
	public Mono<String> consumirGoogleVisionAPI(GoogleAPIRequest body) {		

		WebClient.Builder builder = WebClient.builder().baseUrl(urlServer);
	
		WebClient webClient = builder.build();
		
		String urlFinal="/v1p4beta1/images:annotate?key=AIzaSyCo8wtM9wac_74K446J0CV7oVHKLdENLYo";
		System.out.println("Antes del return VISION webClient");
		
		return webClient.post()
				.uri(urlFinal)
				.body(BodyInserters.fromValue(body))
				.exchange()
			.flatMap( x -> 
			{ 
				if ( ! x.statusCode().is2xxSuccessful())
					return 	Mono.just(urlServer+urlFinal
				+" Called. Error 4xx: "+x.statusCode()+"\n");
				System.out.println("TERMINADO VISION en return x.bodyToMono");
				return x.bodyToMono(String.class);
			});		
	}
	
	public Mono<String> consumirGoogleStorageAPI(byte[] imagenString) throws IOException {

		String bucketName = UUID.randomUUID().toString().substring(0, 5);

		Credentials credentials = GoogleCredentials
				  .fromStream(new FileInputStream("C:/workingSET/Clave.json"));
		Storage storage = StorageOptions.newBuilder().setCredentials(credentials)
				.build().getService();	
		
		Bucket bucket =
	    	    storage.create(
	    	        BucketInfo.newBuilder(bucketName)
	    	            // See here for possible values: http://g.co/cloud/storage/docs/storage-classes
	    	            .setStorageClass(StorageClass.COLDLINE)
	    	            // Possible values: http://g.co/cloud/storage/docs/bucket-locations#location-mr
	    	            .setLocation("asia")
	    	            .build());

		Blob blob = bucket.create("my-first-blob", imagenString);
		
		System.out.println("TERMINADO STORAGE");
		    
		return Mono.just(blob.getMediaLink());
	}
	
	public String getMonoValue(Mono<String> mono) {
	    return mono.block();
	}
		    
}
