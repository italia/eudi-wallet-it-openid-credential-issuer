package it.ipzs.pidprovider.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import it.ipzs.pidprovider.exception.AlreadyGeneratedCredentialException;
import it.ipzs.pidprovider.exception.AuthorizationRequestValidationException;
import it.ipzs.pidprovider.exception.CallbackRequestValidationException;
import it.ipzs.pidprovider.exception.ClientAssertionValidationException;
import it.ipzs.pidprovider.exception.CredentialDpopParsingException;
import it.ipzs.pidprovider.exception.CredentialJwtMissingClaimException;
import it.ipzs.pidprovider.exception.DpopJwtMissingClaimException;
import it.ipzs.pidprovider.exception.InvalidHtmAndHtuClaimsException;
import it.ipzs.pidprovider.exception.JwsHeaderMissingFieldException;
import it.ipzs.pidprovider.exception.MalformedCredentialProofException;
import it.ipzs.pidprovider.exception.MissingOrBlankParamException;
import it.ipzs.pidprovider.exception.ParRequestJwtMissingParameterException;
import it.ipzs.pidprovider.exception.ParRequestJwtValidationException;
import it.ipzs.pidprovider.exception.PkceCodeValidationException;
import it.ipzs.pidprovider.exception.SessionInfoByClientIdNotFoundException;
import it.ipzs.pidprovider.exception.SessionInfoByStateNotFoundException;
import it.ipzs.pidprovider.exception.TokenCodeValidationException;
import it.ipzs.pidprovider.exception.TokenDpopParsingException;
import it.ipzs.pidprovider.exception.WalletInstanceAttestationVerificationException;

@ControllerAdvice
public class CustomResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler({ MissingOrBlankParamException.class, CallbackRequestValidationException.class,
			SessionInfoByStateNotFoundException.class, WalletInstanceAttestationVerificationException.class,
			SessionInfoByClientIdNotFoundException.class, TokenCodeValidationException.class,
			AlreadyGeneratedCredentialException.class, AuthorizationRequestValidationException.class,
			ClientAssertionValidationException.class, CredentialDpopParsingException.class,
			CredentialJwtMissingClaimException.class, DpopJwtMissingClaimException.class,
			InvalidHtmAndHtuClaimsException.class, JwsHeaderMissingFieldException.class,
			MalformedCredentialProofException.class, ParRequestJwtMissingParameterException.class,
			ParRequestJwtValidationException.class, PkceCodeValidationException.class,
			TokenDpopParsingException.class })
	public ResponseEntity<?> handleConstraintViolation(final Exception ex,
			final WebRequest request) {
		logger.info(ex.getMessage());
		return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
	}
}
