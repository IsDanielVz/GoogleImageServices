package com.example.demo.Model;

public class RespuestaNostra {
			
	Boolean isSuccess = new Boolean(false);
	
	String textoRequerido = new String();
	
	String textoEncontrado = new String();
	
	String rutaImagen = new String();

	public Boolean getIsSuccess() {
		return isSuccess;
	}

	public void setIsSuccess(Boolean isSuccess) {
		this.isSuccess = isSuccess;
	}

	public String getTextoRequerido() {
		return textoRequerido;
	}

	public void setTextoRequerido(String textoRequerido) {
		this.textoRequerido = textoRequerido;
	}

	public String getTextoEncontrado() {
		return textoEncontrado;
	}

	public void setTextoEncontrado(String textoEncontrado) {
		this.textoEncontrado = textoEncontrado;
	}

	public String getRutaImagen() {
		return rutaImagen;
	}

	public void setRutaImagen(String rutaImagen) {
		this.rutaImagen = rutaImagen;
	}
	
	
}
