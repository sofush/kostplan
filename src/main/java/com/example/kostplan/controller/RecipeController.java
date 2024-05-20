package com.example.kostplan.controller;

import com.example.kostplan.entity.Day;
import com.example.kostplan.entity.Recipe;
import com.example.kostplan.service.UserService;
import com.example.kostplan.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

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
		model.addAttribute("fromDate", daysOfWeek.get(0).getDate());
		model.addAttribute("toDate", daysOfWeek.get(6).getDate());
		model.addAttribute("days", daysOfWeek);
		return "calendar";
	}

	@GetMapping("/pick/{dayOfWeek}/{meal}")
	public String pickRecipe(
		@PathVariable("dayOfWeek") String dayOfWeek,
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

		String translatedMeal;

		switch (meal.toLowerCase()) {
			case "breakfast" -> translatedMeal = "morgenmad";
			case "lunch" -> translatedMeal = "frokost";
			case "dinner" -> translatedMeal = "aftensmad";
			default -> {
				return "redirect:/week?error=invalid-meal";
			}
		}

		String translatedDayOfWeek;

		switch (dayOfWeek.toLowerCase()) {
			case "monday" -> translatedDayOfWeek = "mandag";
			case "tuesday" -> translatedDayOfWeek = "tirsdag";
			case "wednesday" -> translatedDayOfWeek = "onsdag";
			case "thursday" -> translatedDayOfWeek = "torsdag";
			case "friday" -> translatedDayOfWeek = "fredag";
			case "saturday" -> translatedDayOfWeek = "lørdag";
			case "sunday" -> translatedDayOfWeek = "søndag";
			default -> {
				return "redirect:/week?error=day-of-week";
			}
		}

		model.addAttribute("dayOfWeek", dayOfWeek);
		model.addAttribute("meal", meal);
		model.addAttribute("translatedMeal", translatedMeal);
		model.addAttribute("translatedDayOfWeek", translatedDayOfWeek);
		return "pick-a-recipe";
	}

	@GetMapping("/assign/{dayOfWeek}/{meal}/{id}")
	public String assignRecipeToDay(
		@PathVariable("dayOfWeek") String dayOfWeek,
		@PathVariable("meal") String meal,
		@PathVariable("id") Integer recipeId,
		Principal principal
	) {
		int weekIndex = DateUtil.calculateCurrentWeekIndex();
		Optional<LocalDate> date = DateUtil.calculateDatesOfNthWeek(weekIndex)
			.stream()
			.filter((d) -> d.getDayOfWeek().toString().toLowerCase().contentEquals(dayOfWeek))
			.findFirst();

		if (date.isEmpty()) {
			return "redirect:/pick/" + dayOfWeek + "/" + meal + "?error=day-of-week";
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
			return "redirect:/pick/" + dayOfWeek + "/" + meal + "?error=database";
		}

		if (recipe == null) {
			return "redirect:/pick/" + dayOfWeek + "/" + meal + "?error=invalid-recipe";
		}

		switch (meal) {
			case "breakfast" -> day.setBreakfast(recipe);
			case "lunch" -> day.setLunch(recipe);
			case "dinner" -> day.setDinner(recipe);
		}

		this.service.updateDay(
			day.getUsername(),
			day.getDate(),
			day.getBreakfast() == null ? null : day.getBreakfast().getId(),
			day.getLunch() == null ? null : day.getLunch().getId(),
			day.getDinner() == null ? null : day.getDinner().getId()
		);
		return "redirect:/week?success";
	}

	@GetMapping("/recipe/{dayOfWeek}/{meal}")
	public String showRecipe(
		@PathVariable("dayOfWeek") String dayOfWeek,
		@PathVariable("meal") String meal,
		Principal principal,
		Model model
	) {
		int weekIndex = DateUtil.calculateCurrentWeekIndex();
		Optional<LocalDate> date = DateUtil.calculateDatesOfNthWeek(weekIndex)
			.stream()
			.filter((d) -> d.getDayOfWeek().toString().toLowerCase().contentEquals(dayOfWeek))
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

		double calorieGoal = this.service.calculateCalorieGoal(principal.getName());
		this.service.scaleRecipe(calorieGoal, recipe, otherRecipes);

		model.addAttribute("calorieGoal", calorieGoal);
		model.addAttribute("recipe", recipe);
		return "recipe";
	}

	@GetMapping("/upload")
	public String uploadRecipeImage() {
		return "upload";
	}

	@PostMapping("/upload")
	public String uploadRecipeImagePost(
		@RequestParam("file") MultipartFile image,
		@RequestParam("recipe-id") Integer recipeId
	) {
		if (recipeId == null) {
			return "redirect:upload?error=id";
		}

		if (image == null
			|| image.isEmpty()
			|| image.getContentType() == null
			|| !image.getContentType().contentEquals("image/jpeg")
		) {
			return "redirect:upload?error=image";
		}

		try {
			this.service.addRecipeImage(recipeId, image.getBytes());
		} catch (Exception e) {
			return "redirect:upload?error=unknown";
		}
		return "redirect:/upload?success";
	}

	@GetMapping(
		value = "/img/recipe/{id}",
		produces = MediaType.IMAGE_JPEG_VALUE
	)
	@ResponseBody
	public byte[] getRecipeImage(@PathVariable("id") Integer recipeId) {
		if (recipeId == null) {
			throw new ResponseStatusException(
				HttpStatus.NOT_FOUND, "Image could not be found."
			);
		}

		byte[] image = this.service.findRecipeImage(recipeId);

		if (image == null) {
			throw new ResponseStatusException(
				HttpStatus.NOT_FOUND, "Image could not be found."
			);
		}

		return image;
	}
}
