package com.snjv.recipesbackend.repositories;

import com.snjv.recipesbackend.domain.Ingredient;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IngredientRepository extends MongoRepository<Ingredient, String> {
}
