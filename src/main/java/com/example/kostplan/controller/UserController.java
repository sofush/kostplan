package com.example.kostplan.controller;

import com.example.kostplan.entity.Day;
import com.example.kostplan.entity.Recipe;
import com.example.kostplan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

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
		@ModelAttribute("repeat-password") String repeat_password,
		@ModelAttribute("name") String name,
		@ModelAttribute("male") String male,
		@ModelAttribute("weight") String weight,
		@ModelAttribute("dob") String dob,
		@ModelAttribute("height") String height,
		Model model
	) {
		boolean isMale = male != null && male.contentEquals("on");

		// Add the various attributes to the model in case of an exception so the user doesn't
		// have to fill it out all over again.
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

		try {
			LocalDate parsedDate = LocalDate.parse(dob);
			this.service.addUser(username, password, name, isMale, parsedWeight, parsedDate, parsedHeight);
		} catch (DateTimeParseException e) {
			model.addAttribute("error", "date");
			return "register";
		} catch (IllegalArgumentException e) {
			if (e.getMessage().toLowerCase().contains("username")) {
				model.addAttribute("error", "username");
			} else if (e.getMessage().toLowerCase().contains("password")) {
				model.addAttribute("error", "password");
			} else if (e.getMessage().toLowerCase().contains("birth")) {
				model.addAttribute("error", "dob");
			} else {
				model.addAttribute("error", "unknown");
			}

			return "register";
		} catch (Exception e) {
			model.addAttribute("error", "unknown");
			return "register";
		}

		return "redirect:/login?registered";
	}

	@GetMapping("/week")
	public String displayWeek(
		@RequestParam(required = false, name = "index") Integer weekIndex,
		Principal principal,
		Model model
	) {
		if (weekIndex == null) {
			weekIndex = this.service.calculateCurrentWeekIndex();
		}

		List<Day> daysOfWeek = this.service.findDaysOfWeek(principal.getName(), weekIndex);
		model.addAttribute("weekIndex", weekIndex);
		model.addAttribute("days", daysOfWeek);
		return "calendar";
	}

	@GetMapping("/pick/{weekday}/{meal}")
	public String pickRecipe(
		@PathVariable("weekday") String weekday,
		@PathVariable("meal") String meal,
		Model model
	) {
		int weekIndex = this.service.calculateCurrentWeekIndex();

		try {
			List<Recipe> recipes = this.service.findRecipesForWeek(weekIndex);
			model.addAttribute("recipes", recipes);
		} catch (DataAccessException e) {
			return "redirect:/week?error=database";
		}

		model.addAttribute("weekday", weekday);
		model.addAttribute("meal", meal);
		return "pick-a-recipe";
	}

	@GetMapping("/assign/{weekday}/{meal}/{id}")
	public String assignRecipeToDay(
		@PathVariable("weekday") String weekday,
		@PathVariable("meal") String meal,
		@PathVariable("id") Integer recipeId,
		Principal principal,
		Model model
	) {
		int weekIndex = this.service.calculateCurrentWeekIndex();
		Optional<LocalDate> date = this.service.calculateDatesOfNthWeek(weekIndex)
			.stream()
			.filter((d) -> d.getDayOfWeek().toString().toLowerCase().contentEquals(weekday))
			.findFirst();

		if (date.isEmpty()) {
			model.addAttribute("error", "day-of-week");
			return "redirect:/pick/" + weekday + "/" + meal;
		}

		Day day;
		Recipe recipe;

		try {
			day = this.service.findDay(principal.getName(), date.get());
			recipe = this.service.findRecipeById(recipeId);

			if (day == null) {
				this.service.addDay(
					principal.getName(),
					date.get(),
					null,
					null,
					null
				);

				day = this.service.findDay(principal.getName(), date.get());
			}
		} catch (DataAccessException e) {
			model.addAttribute("error", "database");
			return "redirect:/pick/" + weekday + "/" + meal;
		}

		if (recipe == null) {
			model.addAttribute("error", "invalid-recipe");
			return "redirect:/pick/" + weekday + "/" + meal;
		}

		switch (meal) {
			case "breakfast" -> day.setBreakfast(recipe);
			case "lunch" -> day.setLunch(recipe);
			case "dinner" -> day.setDinner(recipe);
		}

		this.service.updateDay(
			day.getUsername(),
			day.getDate(),
			day.getBreakfast(),
			day.getLunch(),
			day.getDinner()
		);
		return "redirect:/week";
	}
}
