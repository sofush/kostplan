package com.example.kostplan.exception;

import com.stripe.exception.StripeException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.nio.file.AccessDeniedException;

@ControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler({StripeException.class})
	public String handleStripeException(StripeException e, Model model) {
		model.addAttribute("title", "Fejl i Stripe");
		model.addAttribute("content", e.getUserMessage());
		return "error";
	}

	@ExceptionHandler({AccessDeniedException.class})
	public String handleAccessDeniedException(AccessDeniedException e, Model model) {
		model.addAttribute("title", "Adgang nægtet");
		model.addAttribute("content", "Du har ikke adgang til denne side.");
		return "error";
	}

	@ExceptionHandler({Exception.class})
	public String handleGenericException(Exception e, Model model) {
		model.addAttribute("title", "Ukendt fejl");
		model.addAttribute("content", "En ukendt fejl opstod.");
		return "error";
	}
}
