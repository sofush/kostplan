package com.example.kostplan;

import com.example.kostplan.entity.Ingredient;
import com.example.kostplan.entity.Recipe;
import com.example.kostplan.util.HealthUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class RecipeTests {
	private static Ingredient createSimpleIngredient(double quantity, int calories) {
		return new Ingredient(-1, "name", quantity, "unit", calories);
	}

	private static Recipe createRecipeTemplate() {
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
	public void testScaleRecipe() {
		final int calorieGoal = 100;

		Recipe recipeToModify = createRecipeTemplate();
		Recipe recipeA = createRecipeTemplate();
		Recipe recipeB = createRecipeTemplate();

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

		HealthUtil.scaleRecipe(
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
}
