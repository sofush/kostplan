package com.example.kostplan.exception;

import com.stripe.exception.StripeException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;


@ControllerAdvice
public class GlobalExceptionHandler {
	private final Logger logger;

	public GlobalExceptionHandler() {
		this.logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	}

	@ExceptionHandler({StripeException.class})
	public String handleStripeException(StripeException e, Model model) {
		logger.error("Unhandled Stripe error", e);
		model.addAttribute("title", "Fejl i Stripe");
		model.addAttribute("content", e.getUserMessage());
		return "error";
	}

	@ExceptionHandler({AccessDeniedException.class})
	public String handleAccessDeniedException(AccessDeniedException e, Model model) {
		logger.error("Unhandled access denied error", e);
		model.addAttribute("title", "Adgang nægtet");
		model.addAttribute("content", "Du har ikke adgang til denne side.");
		return "error";
	}

	@ExceptionHandler({NoResourceFoundException.class})
	public String handleNoResourceFoundException(NoResourceFoundException e, Model model) {
		logger.error("Could not find the requested resource", e);
		model.addAttribute("title", "Ressource fejl");
		model.addAttribute("content", "Den anmodede ressource findes ikke.");
		return "error";
	}

	@ExceptionHandler({Exception.class})
	public String handleGenericException(Exception e, Model model) {
		logger.error("Unhandled error", e);
		model.addAttribute("title", "Ukendt fejl");
		model.addAttribute("content", "En ukendt fejl opstod.");
		return "error";
	}
}
