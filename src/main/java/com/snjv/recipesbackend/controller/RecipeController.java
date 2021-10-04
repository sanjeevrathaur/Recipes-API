package com.snjv.recipesbackend.controller;

import com.snjv.recipesbackend.domain.Category;
import com.snjv.recipesbackend.domain.Recipe;
import com.snjv.recipesbackend.mapper.RecipeMapper;
import com.snjv.recipesbackend.payload.request.RecipeRequest;
import com.snjv.recipesbackend.service.RecipeService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/recipe")
public class RecipeController {

    private final RecipeService recipeService;

    private final RecipeMapper recipeMapper;

    @GetMapping
    public List<Recipe> getAll(@RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm") @Parameter(schema = @Schema(type="string" , format = "date", example = "31-12-2021 23:59")) LocalDateTime creationTime,
                               @RequestParam(required = false) Category category,
                               @RequestParam(required = false) Integer servings) {
        return recipeService.findAllByParameters(creationTime, category, servings);
    }

    @PostMapping
    public Recipe create(@Valid @RequestBody RecipeRequest recipeRequest) {
        return recipeService.add(recipeMapper.toEntity(recipeRequest));
    }

    @PutMapping("/{id}")
    public Recipe update(@PathVariable String id,
                         @Valid @RequestBody RecipeRequest recipeRequest) {
        return recipeService.update(id, recipeMapper.toEntity(recipeRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity delete(@PathVariable String id) {
        recipeService.delete(id);
        return ResponseEntity.ok(Collections.singletonMap("message", "Recipe has been deleted"));
    }

}
