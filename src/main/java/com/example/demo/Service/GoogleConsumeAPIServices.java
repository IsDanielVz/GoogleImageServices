package com.example.demo.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
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
import com.google.gson.Gson;

import reactor.core.publisher.Mono;

@Service
public class GoogleConsumeAPIServices {
	
	@Autowired
	TransformService transformService;
	
	final String urlServer = "https://vision.googleapis.com";
	
//	constants.setFeatureType(p.getProperty("featureType"));
//	constants.setFileCredentials(p.getProperty("fileCredentials"));
//	constants.setVisionAddress(p.getProperty("visionAddress"));
	
	public RespuestaNostra imageProcessService(MultipartFile file, String text) throws IOException {
		
		RespuestaNostra nostra = new RespuestaNostra();
		
		if( text != null && !text.contentEquals("") && !text.isEmpty() &&
				file != null && !file.isEmpty() ) {
			
			byte[] archivoEnBytes = file.getBytes();
			
			try {
				Gson gson = new Gson();
				PeticionRecibida recibida = gson.fromJson(text, PeticionRecibida.class);
				return bundleGoogleServices(archivoEnBytes, recibida.getTexto());
			}catch(Exception siobe){
				nostra.setTextoRequerido("Entrada 'json' = "+ text +" es invalida, "
						+ "se requiere: " + transformService.getJsonFormato());
				nostra.setTextoEncontrado(siobe.getMessage());
				nostra.setRutaImagen("Debido a un error en el archivo 'json' no fue posible subir el archivo :"
						+file.getOriginalFilename());
				return nostra;
			}
			
		}else {
			nostra.setIsSuccess(false);
			nostra.setRutaImagen("Debido a un error en los archivos recibidos no fue posible subir el archivo");
			nostra.setTextoRequerido("El texto recibidó fué :"+text);
			nostra.setTextoEncontrado("La imagen recibida fué :"+file.getOriginalFilename());
		}
		
		return nostra;
		
	}
	
	public RespuestaNostra bundleGoogleServices(byte[] archivoEnBytes, String peticionRecibida) throws IOException {
		
		RespuestaNostra nostra = new RespuestaNostra();
		
		Mono<String> vision = consumirGoogleVisionAPI(
				transformService.armarPeticion(
						transformService.toBase64(archivoEnBytes)));
		
		Mono<String> bucket = consumirGoogleStorageAPI(archivoEnBytes);
		
//		return Flux.merge(
//				consumirGoogleVisionAPI(
//						transformService.armarPeticion(
//								transformService.toBase64(archivoEnBytes))),
//				consumirGoogleStorageAPI(archivoEnBytes));
		
		String textoEncontrado = transformService.textoEncontrado(getMonoValue(vision));
		Boolean coincide = textoEncontrado.contains(peticionRecibida);
		
		if(coincide) {
			nostra.setIsSuccess(coincide);
			nostra.setRutaImagen(getMonoValue(bucket));
			nostra.setTextoRequerido(peticionRecibida);
			nostra.setTextoEncontrado(textoEncontrado);
		}else {
			nostra.setIsSuccess(coincide);
			nostra.setRutaImagen(getMonoValue(bucket));
			nostra.setTextoRequerido(peticionRecibida);
			nostra.setTextoEncontrado(textoEncontrado);
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
				if ( ! x.statusCode().is2xxSuccessful()) {
					return 	Mono.just(urlServer+urlFinal
				+" Called. Error 4xx: "+x.statusCode()+"\n");	
				}
				System.out.println("TERMINADO VISION");
				return x.bodyToMono(String.class);
			});		
	}
	
	public Mono<String> consumirGoogleStorageAPI(byte[] imagenString) throws FileNotFoundException, IOException {

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
