package it.ipzs.pidprovider.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import it.ipzs.pidprovider.oidclib.OidcConfig;
import it.ipzs.pidprovider.oidclib.OidcWrapper;
import it.ipzs.pidprovider.oidclib.schemas.WellKnownData;
import it.ipzs.pidprovider.util.KeyStoreUtil;
import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/admin/federation")
public class HomeController {

	@Autowired
	private OidcWrapper oidcWrapper;
	
	@Autowired
	private OidcConfig oidcConfig;
	
	@GetMapping(path = { "/","/home" })
	public ModelAndView home(HttpServletRequest request)
		throws Exception {

		ModelAndView mav = new ModelAndView("home");

		WellKnownData wellKnow = oidcWrapper.getFederationEntityData();

		mav.addObject("onlyJwks", wellKnow.hasOnlyJwks());
		mav.addObject("intermediate", wellKnow.isIntermediate());
		mav.addObject("showLanding", wellKnow.isComplete());
		mav.addObject("trustAnchorHost", oidcConfig.getHosts().getTrustAnchor());

		if (wellKnow.hasOnlyJwks()) {
			mav.addObject("mineJwks", wellKnow.getValue());
			mav.addObject("configFile", oidcConfig.getRelyingParty().getJwkFilePath());
			KeyStoreUtil.storeKey(oidcConfig.getRelyingParty().getJwkFilePath(), wellKnow.getValue());

			oidcWrapper.reloadKeys();
		}

		if (wellKnow.isIntermediate()) {
			mav.addObject("rpName", oidcConfig.getRelyingParty().getApplicationName());
			mav.addObject("rpClientId", oidcConfig.getRelyingParty().getClientId());
			mav.addObject("rpPublicJwks", wellKnow.getPublicJwks());
			mav.addObject(
				"configFile", oidcConfig.getRelyingParty().getTrustMarksFilePath());
		}

		return mav;
	}

}