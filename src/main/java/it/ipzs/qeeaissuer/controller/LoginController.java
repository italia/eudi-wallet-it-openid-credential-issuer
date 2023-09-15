package it.ipzs.qeeaissuer.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@RestController
public class LoginController {

	@RequestMapping("/login")
	public ModelAndView login() {
		ModelAndView model = new ModelAndView();
		model.setViewName("login");
		return model;
	}
}

class RedirectModel {
	@Pattern(regexp = "^/([^/].*)?$")
	@NotBlank
	private String continueUrl;

	public void setContinue(String continueUrl) {
		this.continueUrl = continueUrl;
	}

	public String getContinue() {
		return continueUrl;
	}
}