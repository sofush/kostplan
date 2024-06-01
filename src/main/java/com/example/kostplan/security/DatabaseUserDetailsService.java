package com.example.kostplan.security;

import com.example.kostplan.entity.User;
import com.example.kostplan.service.PaymentService;
import com.example.kostplan.service.UserService;
import com.stripe.exception.StripeException;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class DatabaseUserDetailsService implements UserDetailsService {
	private final UserService userService;
	private final PaymentService paymentService;

	public DatabaseUserDetailsService(UserService userService, PaymentService paymentService) {
		this.userService = userService;
		this.paymentService = paymentService;
	}

	@Override
	public UserDetails loadUserByUsername(String username)
		throws UsernameNotFoundException
	{
		User user = null;
		
		try {
			user = this.userService.findUserByUsername(username);
		} catch (DataAccessException e) {
			// Let the program handle user as null below.
		}

		if (user == null)
			throw new UsernameNotFoundException("User with the provided username could not be found.");

		List<String> authorities = new ArrayList<>(user.getAuthorities());

		try {
			if (this.paymentService.hasActiveSubscription(username)) {
				authorities.add("ROLE_SUBSCRIBER");
			}
		} catch (StripeException e) {
			// Do nothing.
		}


		return new org.springframework.security.core.userdetails.User(
			user.getUsername(),
			user.getPassword(),
			authorities.stream()
				.map(SimpleGrantedAuthority::new)
				.toList()
		);
	}
}
