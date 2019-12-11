package com.example.demo.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.demo.Model.GoogleAPIRequest;
import com.example.demo.Model.PeticionRecibida;
import com.example.demo.Model.RespuestaNostra;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageClass;
import com.google.cloud.storage.StorageOptions;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class GoogleConsumeAPIServices {
	
	@Autowired
	TransformService transformService;
	
	final String urlServer = "https://vision.googleapis.com";
	
	public RespuestaNostra bundleGoogleServices(byte[] archivoEnBytes, String text) throws IOException {
		
		RespuestaNostra nostra = new RespuestaNostra();
		String peticionRecibida = transformService.jsonToPeticionRecibida(text).getTexto();
		
		Mono<String> vision = consumirGoogleVisionAPI(
				transformService.armarPeticion(
						transformService.toBase64(archivoEnBytes)));
		
		Mono<String> bucket = consumirGoogleStorageAPI(archivoEnBytes);
		
		Flux<String> flujo = Flux.merge(vision,bucket);
		
		String[] respuestaVision = getValue(vision).split("\"description\": \"");
		String[] textoEncontrado = respuestaVision[1].split("\"");
		Boolean coincide = textoEncontrado[0].contains(peticionRecibida);
		
		if(coincide) {
			nostra.setIsSuccess(coincide);
			nostra.setRutaImagen(getValue(bucket));
			nostra.setTextoRequerido(peticionRecibida);
			nostra.setTextoEncontrado(textoEncontrado[0]);
		}else {
			nostra.setIsSuccess(coincide);
			nostra.setRutaImagen(getValue(bucket));
			nostra.setTextoRequerido(peticionRecibida);
			nostra.setTextoEncontrado(textoEncontrado[0]);
		}
		
		return nostra;
		
	}
	
	public Mono<String> consumirGoogleVisionAPI(GoogleAPIRequest body) {		

		WebClient.Builder builder = WebClient.builder().baseUrl(urlServer);
	
		WebClient webClient = builder.build();
		
		String urlFinal="/v1p4beta1/images:annotate?key=AIzaSyCo8wtM9wac_74K446J0CV7oVHKLdENLYo";
		
		return webClient.post()
				.uri(urlFinal)
				.body(BodyInserters.fromValue(body))
				.exchange()
			.flatMap( x -> 
			{ 
				if ( ! x.statusCode().is2xxSuccessful())
					return 	Mono.just(urlServer+urlFinal
				+" Called. Error 4xx: "+x.statusCode()+"\n");
				return x.bodyToMono(String.class);
			});		
	}
	
	public Mono<String> consumirGoogleStorageAPI(byte[] imagenString) throws FileNotFoundException, IOException {

		String bucketName = UUID.randomUUID().toString();

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
		    
		return Mono.just(blob.getMediaLink());
	}
	
	public String getValue(Mono<String> mono) {
	    return mono.block();
	}
		    
}
