package com.snjv.recipesbackend.service;

import com.snjv.recipesbackend.domain.Category;
import com.snjv.recipesbackend.domain.QRecipe;
import com.snjv.recipesbackend.domain.Recipe;
import com.snjv.recipesbackend.repositories.IngredientRepository;
import com.snjv.recipesbackend.repositories.RecipeRepository;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.webjars.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;

    private final IngredientRepository ingredientRepository;

    @Transactional
    public Recipe add(Recipe recipe) {
        if (! CollectionUtils.isEmpty(recipe.getIngredients()))
            recipe.setIngredients(recipe.getIngredients().stream().map(ingredientRepository::save).collect(Collectors.toSet()));
        return recipeRepository.save(recipe);
    }

    public Recipe update(String id, Recipe recipe) {
        Recipe oldRecipe = recipeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Can't found recipe by ID %s", id)));
        BeanUtils.copyProperties(recipe, oldRecipe, "id", "ingredients", "createdDate");
        oldRecipe.getIngredients().clear();
        if (! CollectionUtils.isEmpty(recipe.getIngredients())) {
            oldRecipe.setIngredients(recipe.getIngredients().stream().map(ingredientRepository::save).collect(Collectors.toSet()));
        }
        return recipeRepository.save(oldRecipe);
    }

    public void delete(String id) {
        recipeRepository.delete(recipeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Can't found recipe by ID %s", id))));
    }

    public List<Recipe> findAllByParameters(LocalDateTime creationTime, Category category, Integer servings) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if (creationTime != null) {
            booleanBuilder.and(QRecipe.recipe.createdDate.between(creationTime.withSecond(0), creationTime.withSecond(59)));
        }

        if (category != null) {
            booleanBuilder.and(QRecipe.recipe.category.eq(category));
        }

        if (servings != null) {
            booleanBuilder.and(QRecipe.recipe.servings.eq(servings));
        }

        return booleanBuilder.getValue() == null ?
                recipeRepository.findAll() :
                StreamSupport.stream(recipeRepository.findAll(booleanBuilder.getValue()).spliterator(), false)
                        .collect(Collectors.toList());
    }

}
