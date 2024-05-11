package com.example.kostplan.controller;

import com.example.kostplan.entity.Day;
import com.example.kostplan.entity.Recipe;
import com.example.kostplan.entity.User;
import com.example.kostplan.service.UserService;
import com.example.kostplan.util.DateUtil;
import com.example.kostplan.util.HealthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class RecipeController {
	private final UserService service;

	@Autowired
	public RecipeController(UserService service) {
		this.service = service;
	}

	@GetMapping("/week")
	public String displayWeek(
		@RequestParam(required = false, name = "index") Integer weekIndex,
		Principal principal,
		Model model
	) {
		if (weekIndex == null) {
			weekIndex = DateUtil.calculateCurrentWeekIndex();
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
		int weekIndex = DateUtil.calculateCurrentWeekIndex();

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
		int weekIndex = DateUtil.calculateCurrentWeekIndex();
		Optional<LocalDate> date = DateUtil.calculateDatesOfNthWeek(weekIndex)
			.stream()
			.filter((d) -> d.getDayOfWeek().toString().toLowerCase().contentEquals(weekday))
			.findFirst();

		if (date.isEmpty()) {
			return "redirect:/pick/" + weekday + "/" + meal + "?error=day-of-week";
		}

		Day day;
		Recipe recipe;

		try {
			recipe = this.service.findRecipeById(recipeId);
			day = this.service.findDay(principal.getName(), date.get());

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
			return "redirect:/pick/" + weekday + "/" + meal + "?error=database";
		}

		if (recipe == null) {
			return "redirect:/pick/" + weekday + "/" + meal + "?error=invalid-recipe";
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

	@GetMapping("/recipe/{weekday}/{meal}")
	public String showRecipe(
		@PathVariable("weekday") String weekday,
		@PathVariable("meal") String meal,
		Principal principal,
		Model model
	) {
		int weekIndex = DateUtil.calculateCurrentWeekIndex();
		Optional<LocalDate> date = DateUtil.calculateDatesOfNthWeek(weekIndex)
			.stream()
			.filter((d) -> d.getDayOfWeek().toString().toLowerCase().contentEquals(weekday))
			.findFirst();

		if (date.isEmpty()) {
			return "redirect:/week?error=day-of-week";
		}

		Day day = this.service.findDay(principal.getName(), date.get());

		if (day == null) {
			return "redirect:/week?error=no-meal-assigned";
		}

		Recipe recipe;
		List<Recipe> otherRecipes = new ArrayList<>();

		switch (meal.toLowerCase()) {
			case "breakfast" -> {
				recipe = day.getBreakfast();
				otherRecipes.add(day.getLunch());
				otherRecipes.add(day.getDinner());
			}
			case "lunch" -> {
				recipe = day.getLunch();
				otherRecipes.add(day.getBreakfast());
				otherRecipes.add(day.getDinner());
			}
			case "dinner" -> {
				recipe = day.getDinner();
				otherRecipes.add(day.getBreakfast());
				otherRecipes.add(day.getLunch());
			}
			default -> {
				return "redirect:/week?error=invalid-meal";
			}
		}

		if (recipe == null) {
			return "redirect:/week?error=no-meal-assigned";
		}

		User user = this.service.findUserByUsername(principal.getName());
		double calorieGoal = user.calculateBMR();
		HealthUtil.scaleRecipe(calorieGoal, recipe, otherRecipes);

		model.addAttribute("calorieGoal", calorieGoal);
		model.addAttribute("recipe", recipe);
		return "recipe";
	}
}
