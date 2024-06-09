package com.example.kostplan.controller;

import com.example.kostplan.entity.ActivityLevel;
import com.example.kostplan.entity.WeightGoal;
import com.example.kostplan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

	@GetMapping("/logout")
	public String logout() {
		SecurityContext ctx = SecurityContextHolder.getContext();
		ctx.setAuthentication(null);
		return "redirect:/login?logout";
	}

	@GetMapping("/register")
	public String registerUser() {
		return "register";
	}

	@PostMapping("/register")
	public String registerUserPost(
		@ModelAttribute("username") String username,
		@ModelAttribute("password") String password,
		@ModelAttribute("repeat-password") String repeatPassword,
		@ModelAttribute("email-address") String emailAddress,
		@ModelAttribute("phone-number") String phoneNumber,
		@ModelAttribute("weight-goal") String weightGoal,
		@ModelAttribute("activity-level") String activityLevel,
		@ModelAttribute("gender") String gender,
		@ModelAttribute("weight") String weight,
		@ModelAttribute("dob") String dob,
		@ModelAttribute("height") String height,
		Model model
	) {
		boolean isMale = gender != null && gender.contentEquals("male");

		// Add the various attributes to the model in case of an exception so the user doesn't
		// have to fill it out all over again.
		model.addAttribute("username", username);
		model.addAttribute("password", password);
		model.addAttribute("repeatPassword", repeatPassword);
		model.addAttribute("emailAddress", emailAddress);
		model.addAttribute("phoneNumber", phoneNumber);
		model.addAttribute("activityLevel", activityLevel);
		model.addAttribute("weightGoal", weightGoal);
		model.addAttribute("dob", dob);
		model.addAttribute("weight", weight);
		model.addAttribute("height", height);
		model.addAttribute("gender", gender);

		if (!password.contentEquals(repeatPassword)) {
			model.addAttribute("error", "repeat-password");
			return "register";
		}

		int parsedHeight, parsedWeight;

		try {
			parsedHeight = Integer.parseInt(height);
		} catch (NumberFormatException e) {
			model.addAttribute("error", "height");
			return "register";
		}

		try {
			parsedWeight = Integer.parseInt(weight);
		} catch (NumberFormatException e) {
			model.addAttribute("error", "weight");
			return "register";
		}

		WeightGoal parsedWeightGoal = null;
		ActivityLevel parsedActivityLevel = null;

		switch (weightGoal) {
			case "gain" -> parsedWeightGoal = WeightGoal.GAIN;
			case "loss" -> parsedWeightGoal = WeightGoal.LOSS;
			case "equilibrium" -> parsedWeightGoal = WeightGoal.EQUILIBRIUM;
			case "muscle" -> parsedWeightGoal = WeightGoal.MUSCLE;
		}

		switch (activityLevel) {
			case "inactive" -> parsedActivityLevel = ActivityLevel.INACTIVE;
			case "low" -> parsedActivityLevel = ActivityLevel.LOW;
			case "moderate" -> parsedActivityLevel = ActivityLevel.MODERATE;
			case "high" -> parsedActivityLevel = ActivityLevel.HIGH;
			case "very high" -> parsedActivityLevel = ActivityLevel.VERY_HIGH;
		}

		try {
			LocalDate parsedDate = LocalDate.parse(dob);
			this.service.addUser(
				username,
				password,
				emailAddress,
				phoneNumber,
				parsedWeightGoal,
				parsedActivityLevel,
				isMale,
				parsedWeight,
				parsedDate,
				parsedHeight
			);
		} catch (DateTimeParseException e) {
			model.addAttribute("error", "date");
			return "register";
		} catch (IllegalArgumentException e) {
			String msg = e.getMessage().toLowerCase();

			if (msg.contains("username")) {
				model.addAttribute("error", "username");
			} else if (msg.contains("password")) {
				model.addAttribute("error", "password");
			} else if (msg.contains("birth")) {
				model.addAttribute("error", "dob");
			} else if (msg.contains("email")) {
				model.addAttribute("error", "email");
			} else if (msg.contains("phone")) {
				model.addAttribute("error", "phone");
			} else if (msg.contains("activity")) {
				model.addAttribute("error", "activity-level");
			} else if (msg.contains("weight goal")) {
				model.addAttribute("error", "weight-goal");
			} else {
				model.addAttribute("error", "generic");
			}

			return "register";
		} catch (DuplicateKeyException e) {
			model.addAttribute("error", "user-exists");
			return "register";
		} catch (Exception e) {
			model.addAttribute("error", "unknown");
			return "register";
		}

		return "redirect:/login?registered";
	}
}
