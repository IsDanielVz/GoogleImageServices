package com.example.demo.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.model.RespuestaNostra;
import com.example.demo.service.GoogleConsumeAPIServices;

@RestController
@RequestMapping("/google")
public class ImageProcessController {

	@Autowired
	GoogleConsumeAPIServices servicioGoogle;
	
	@PostMapping(value = "/imagen",
			consumes = {MediaType.MULTIPART_FORM_DATA_VALUE,
						MediaType.APPLICATION_FORM_URLENCODED_VALUE})
	public RespuestaNostra imagen(
			@RequestParam(value = "image", required = true) MultipartFile file,
			@RequestParam(value = "json", required = true) String text
																		) throws IOException {
		
		return servicioGoogle.imageProcessService(file, text);
		
	}
	
}
