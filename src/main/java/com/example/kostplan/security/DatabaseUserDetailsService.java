package com.example.kostplan.security;

import com.example.kostplan.entity.User;
import com.example.kostplan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class DatabaseUserDetailsService implements UserDetailsService {
	private final UserService service;

	public DatabaseUserDetailsService(UserService service) {
		this.service = service;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = null;
		
		try {
			user = this.service.getUserByUsername(username);
		} catch (DataAccessException e) {
			// Let the program handle user as null below.
		}

		if (user == null)
			throw new UsernameNotFoundException("User with the provided username could not be found.");

		return new org.springframework.security.core.userdetails.User(
			user.getUsername(),
			user.getPassword(),
			user.getAuthorities()
				.stream()
				.map(SimpleGrantedAuthority::new)
				.toList()
		);
	}
}
