package com.example.demo.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.demo.model.GoogleAPIRequest;
import com.example.demo.model.RespuestaNostra;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageClass;
import com.google.cloud.storage.StorageOptions;

import reactor.core.publisher.Mono;

@Service
public class GoogleConsumeAPIServices {

	@Autowired
	TransformService transformService;

	Properties constants = new Properties();

	public RespuestaNostra bundleGoogleServices(byte[] archivoEnBytes, String peticionRecibida) throws IOException {

		RespuestaNostra nostra = new RespuestaNostra();
		System.out.println("Iniciamos peticiones a google VISION");
		Mono<String> vision = consumirGoogleVisionAPI(
				transformService.armarPeticion(transformService.toBase64(archivoEnBytes)));
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

	public Mono<String> consumirGoogleVisionAPI(GoogleAPIRequest body) throws IOException {

		InputStream input = GoogleConsumeAPIServices.class.getClassLoader().getResourceAsStream("config.properties");
		if (input == null) {
			System.out.println("NO HAY PROPIEDADES PARA EL PROYECTO");
		}
		constants.load(input);
		String urlServer = constants.getProperty("Google.Vision.Address");
		String urlFinal = constants.getProperty("Google.Vision.Final");

		WebClient.Builder builder = WebClient.builder().baseUrl(urlServer);

		WebClient webClient = builder.build();

		System.out.println("Antes del return VISION webClient");

		return webClient.post().uri(urlFinal).body(BodyInserters.fromValue(body)).exchange().flatMap(x -> {
			if (!x.statusCode().is2xxSuccessful())
				return Mono.just(urlServer + urlFinal + " Called. Error 4xx: " + x.statusCode() + "\n");
			System.out.println("TERMINADO VISION en return x.bodyToMono");
			return x.bodyToMono(String.class);
		});
	}

	public Mono<String> consumirGoogleStorageAPI(byte[] imagenString) throws IOException {

		InputStream input = GoogleConsumeAPIServices.class.getClassLoader().getResourceAsStream("config.properties");
		if (input == null) {
			System.out.println("NO HAY PROPIEDADES PARA EL PROYECTO");
		}
		constants.load(input);
		String glob = constants.getProperty("Google.Storage.Glob");
		String cert = constants.getProperty("Google.Storage.Credentials");
		String chump = constants.getProperty("Google.Storage.Chump");

		String bucketName = chump+UUID.randomUUID().toString().substring(0, 5);

		Credentials credentials = GoogleCredentials.fromStream(new FileInputStream(cert));
		Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

		Bucket bucket = storage.create(BucketInfo.newBuilder(bucketName)
				// See here for possible values: http://g.co/cloud/storage/docs/storage-classes
				.setStorageClass(StorageClass.COLDLINE)
				// Possible values: http://g.co/cloud/storage/docs/bucket-locations#location-mr
				.setLocation("asia").build());

		Blob blob = bucket.create(glob, imagenString);

		System.out.println("TERMINADO STORAGE");

		return Mono.just(blob.getMediaLink());
	}

	public String getMonoValue(Mono<String> mono) {
		return mono.block();
	}

}
