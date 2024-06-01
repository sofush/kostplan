package com.example.kostplan.controller;

import com.example.kostplan.service.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class PaymentController {
	private final PaymentService paymentService;

	@Autowired
	public PaymentController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@GetMapping("/subscribe")
	public String subscribe(Principal principal)
		throws StripeException
	{
		String username = principal.getName();

		if (this.paymentService.hasActiveSubscription(username)) {
			return "redirect:/week";
		}

		try {
			Session session = this.paymentService.createPaymentSession(username);
			return "redirect:" + session.getUrl();
		} catch (StripeException e) {
			return "redirect:/login?error=stripe";
		}
	}
}
