package com.example.kostplan.security;

import com.example.kostplan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {
	@Bean
	public UserDetailsService userDetailsService(UserService service) {
		return new DatabaseUserDetailsService(service);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
			.authorizeHttpRequests((conf) -> conf
				.requestMatchers("/svg/**").permitAll()
				.requestMatchers("/css/**").permitAll()
				.requestMatchers("/register").permitAll()
				.anyRequest().hasRole("USER")
			)
			.formLogin((conf) -> conf
				.loginPage("/login")
				.permitAll()
			)
			.logout(LogoutConfigurer::permitAll)
			.httpBasic(Customizer.withDefaults())
			.build();
	}
}
