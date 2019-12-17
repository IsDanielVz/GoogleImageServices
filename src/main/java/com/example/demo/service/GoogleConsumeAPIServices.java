package com.example.demo.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
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

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class GoogleConsumeAPIServices {

	@Autowired
	TransformService transformService;

	Properties constants = new Properties();

	public RespuestaNostra bundleGoogleServices(byte[] archivoEnBytes, String peticionRecibida) throws IOException {

		RespuestaNostra nostra = new RespuestaNostra();
		
		Flux<String> google = Flux.merge(
				consumirGoogleVisionAPI(
						transformService.armarPeticion(transformService.toBase64(archivoEnBytes))),
				consumirGoogleStorageAPI(archivoEnBytes));
		
		List<String> respuestas = google.collectSortedList().block();
		
		String vision = respuestas.get(1);
		
		String bucket = respuestas.get(0);
		
		String textoEncontrado = transformService.textoEncontrado(vision);

		nostra.setIsSuccess(textoEncontrado.contains(peticionRecibida));
		nostra.setTextoRequerido(peticionRecibida);
		nostra.setRutaImagen(bucket);
		nostra.setTextoEncontrado(textoEncontrado);

		return nostra;

	}

	public Mono<String> consumirGoogleVisionAPI(GoogleAPIRequest body) throws IOException {

		InputStream input = GoogleConsumeAPIServices.class.getClassLoader().getResourceAsStream("config.properties");
		constants.load(input);
		String urlServer = constants.getProperty("Google.Vision.Address");
		String urlFinal = constants.getProperty("Google.Vision.Final");

		WebClient.Builder builder = WebClient.builder().baseUrl(urlServer);

		WebClient webClient = builder.build();

		return webClient.post().uri(urlFinal).body(BodyInserters.fromValue(body)).exchange().flatMap(x -> {
			if (!x.statusCode().is2xxSuccessful())
				return Mono.just(urlServer + urlFinal + " Called. Error 4xx: " + x.statusCode() + "\n");
			return x.bodyToMono(String.class);
		});
	}

	public Mono<String> consumirGoogleStorageAPI(byte[] imagenString) throws IOException {

		InputStream input = GoogleConsumeAPIServices.class.getClassLoader().getResourceAsStream("config.properties");
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

		return Mono.just(blob.getMediaLink());
	}

}
