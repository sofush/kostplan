package com.example.kostplan.security;

import com.example.kostplan.service.PaymentService;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component("subscription")
public class SubscriptionComponent {
	private final PaymentService paymentService;

	@Autowired
	public SubscriptionComponent(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	public boolean isActive() {
		SecurityContext ctx = SecurityContextHolder.getContext();
		Authentication auth = ctx.getAuthentication();

		if (auth == null)
			return false;

		boolean hasRole = auth.getAuthorities().stream()
			.anyMatch((a) -> a.toString().contentEquals("ROLE_SUBSCRIBER"));

		if (hasRole)
			return true;

		try {
			boolean isSubscribed = this.paymentService.hasActiveSubscription(auth.getName());

			if (isSubscribed) {
				ArrayList<GrantedAuthority> authorities = new ArrayList<>(auth.getAuthorities());
				authorities.add(new SimpleGrantedAuthority("ROLE_SUBSCRIBER"));

				ctx.setAuthentication(new UsernamePasswordAuthenticationToken(
					auth.getPrincipal(),
					auth.getCredentials(),
					authorities
				));
			}

			return isSubscribed;
		} catch (StripeException e) {
			return false;
		}
	}
}
