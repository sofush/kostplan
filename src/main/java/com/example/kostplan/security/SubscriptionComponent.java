package com.example.kostplan.security;

import com.example.kostplan.service.PaymentService;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("subscription")
public class SubscriptionComponent {
	private final PaymentService paymentService;

	@Autowired
	public SubscriptionComponent(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	public boolean isActive() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth == null)
			return false;

		boolean hasRole = auth.getAuthorities().stream()
			.anyMatch((a) -> a.toString().contentEquals("ROLE_SUBSCRIBER"));

		if (hasRole)
			return true;

		try {
			return this.paymentService.hasActiveSubscription(auth.getName());
		} catch (StripeException e) {
			return false;
		}
	}
}
