package com.example.kostplan.controller;

import com.example.kostplan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Controller
public class UserController {
	private final UserService service;

	@Autowired
	public UserController(UserService service) {
		this.service = service;
	}

	@GetMapping("/login")
	public String login() {
		return "login";
	}

	@GetMapping("/register")
	public String registerUser() {
		return "register";
	}

	@PostMapping("/register")
	public String registerUserPost(
		@ModelAttribute("username") String username,
		@ModelAttribute("password") String password,
		@ModelAttribute("repeat-password") String repeat_password,
		@ModelAttribute("name") String name,
		@ModelAttribute("male") String male,
		@ModelAttribute("weight") String weight,
		@ModelAttribute("dob") String dob,
		@ModelAttribute("height") String height,
		Model model
	) {
		boolean isMale = male != null && male.contentEquals("on");

		// Add attributes in case of an exception so the user doesn't have to type it all over again.
		model.addAttribute("username", username);
		model.addAttribute("password", password);
		model.addAttribute("repeat_password", repeat_password);
		model.addAttribute("name", name);
		model.addAttribute("dob", dob);
		model.addAttribute("weight", weight);
		model.addAttribute("height", height);
		model.addAttribute("male", isMale);

		if (!password.contentEquals(repeat_password)) {
			model.addAttribute("error", "repeat-password");
			return "register";
		}

		int heightInt = -1;

		try {
			heightInt = Integer.parseInt(height);
		} catch (NumberFormatException e) {
			model.addAttribute("error", "height");
			return "register";
		}

		int weightInt = -1;

		try {
			weightInt = Integer.parseInt(weight);
		} catch (NumberFormatException e) {
			model.addAttribute("error", "weight");
			return "register";
		}

		try {
			LocalDate date = LocalDate.parse(dob);
			// this.service.addUser(username, password, name, male, weight, dob, height);
		} catch (DateTimeParseException e) {
			model.addAttribute("error", "date");
			return "register";
		} catch (Exception e) {
			model.addAttribute("error", "unknown");
			return "register";
		}

		return "redirect:/login";
	}
}
