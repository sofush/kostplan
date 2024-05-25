package com.example.kostplan;

import com.example.kostplan.entity.*;
import com.example.kostplan.repository.MysqlRepository;
import com.example.kostplan.service.UserService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
class RecipeTests {
	private static Ingredient createSimpleIngredient(double quantity, int calories) {
		return new Ingredient(-1, "name", quantity, "unit", calories);
	}

	private static Recipe createRecipe() {
		return new Recipe(
			-1,
			-1,
			"title",
			"description",
			List.of(),
			"instructions",
			"time"
		);
	}

	@Test
	public void testScaleRecipe(@Autowired UserService userService) {
		final int calorieGoal = 100;

		Recipe recipeToModify = createRecipe();
		Recipe recipeA = createRecipe();
		Recipe recipeB = createRecipe();

		// Modify the recipes' ingredient lists to each sum to 100, making
		// the total 300 calories.
		recipeToModify.setIngredients(List.of(
			createSimpleIngredient(4, 10),
			createSimpleIngredient(1, 30),
			createSimpleIngredient(2, 15)
		));
		recipeA.setIngredients(List.of(
			createSimpleIngredient(10, 8),
			createSimpleIngredient(2, 10)
		));
		recipeB.setIngredients(List.of(
			createSimpleIngredient(3, 25),
			createSimpleIngredient(5, 5)
		));

		// This should have a value of ~100 (imprecision due to floating-point
		// arithmetic).
		final double caloriesBefore = recipeToModify.sumCalories();
		Assertions.assertThat(caloriesBefore).isBetween(99.0, 101.0);

		userService.scaleRecipe(
			calorieGoal,
			recipeToModify,
			List.of(recipeA, recipeB)
		);

		// This should have a value of ~33.3 (imprecision due to floating-point
		// arithmetic) so the total calories across all meals/recipes when
		// scaled will be ~100 (the calorie goal).
		final double caloriesAfter = recipeToModify.sumCalories();
		Assertions.assertThat(caloriesAfter).isBetween(32.0, 34.0);
	}


	@Test
	public void testCalculateCalorieGoal(@Autowired PasswordEncoder passwordEncoder) {
		User testUser = new User(
			"test-testUser",
			"",
			"",
			"",
			Role.USER,
			WeightGoal.LOSS,
			ActivityLevel.MODERATE,
			false,
			84,
			LocalDate.now().minusYears(42),
			174
		);

		MysqlRepository mockRepo = Mockito.mock(MysqlRepository.class);
		UserService service = new UserService(mockRepo, passwordEncoder);

		Mockito
			.when(mockRepo.findUserByUsername(testUser.getUsername()))
			.thenReturn(testUser);

		double calorieGoal = service.calculateCalorieGoal(testUser.getUsername());
		Assertions.assertThat(calorieGoal).isBetween(2146.0, 2147.0);
	}
}
