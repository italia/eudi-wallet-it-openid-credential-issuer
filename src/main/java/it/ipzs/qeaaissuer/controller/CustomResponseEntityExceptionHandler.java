package it.ipzs.qeaaissuer.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import it.ipzs.qeaaissuer.exception.MissingOrBlankParamException;


@ControllerAdvice
public class CustomResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler({ MissingOrBlankParamException.class })
	public ResponseEntity<?> handleConstraintViolation(final MissingOrBlankParamException ex,
			final WebRequest request) {
		logger.info(ex.getMessage());
		return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
	}
}
