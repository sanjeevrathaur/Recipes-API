package com.snjv.recipesbackend.controller;

import com.snjv.recipesbackend.domain.Category;
import com.snjv.recipesbackend.domain.Ingredient;
import com.snjv.recipesbackend.domain.Recipe;
import com.snjv.recipesbackend.domain.UnitOfMeasure;
import com.snjv.recipesbackend.mapper.RecipeMapper;
import com.snjv.recipesbackend.payload.request.IngredientRequest;
import com.snjv.recipesbackend.payload.request.LoginRequest;
import com.snjv.recipesbackend.payload.request.RecipeRequest;
import com.snjv.recipesbackend.properties.AppDataProperties;
import com.snjv.recipesbackend.repositories.IngredientRepository;
import com.snjv.recipesbackend.repositories.RecipeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void shouldReturnAValidToken(@Autowired AppDataProperties appDataProperties, @Autowired ObjectMapper objectMapper) throws Exception {
        // Given
        AppDataProperties.User user = appDataProperties.getUsers().get(0);
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(user.getUserName());
        loginRequest.setPassword(user.getPassword());

        // When
        ResultActions actions = this.mockMvc.perform(
                post("/auth/sign-in").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(loginRequest))
        );

        // Then
        actions
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andDo(print())
        ;
    }

    @Test
    @WithMockUser(username = "mockuser")
    void userIsAbleToPostARecipe(@Autowired ObjectMapper objectMapper) throws Exception {
        // Given
        RecipeRequest recipeRequest = RecipeRequest.builder()
                .name("First Recipe")
                .description("My first recipe")
                .category(Category.VEGETARIAN)
                .servings(10)
                .ingredients(Stream.of(
                                IngredientRequest.builder().description("Sugar").amount(BigDecimal.valueOf(5)).uom(UnitOfMeasure.gr).build()
                        )
                        .collect(Collectors.toList()))
                .cookingInstructions(Stream.of(
                        "First step", "Second step", "Third step"
                        )
                        .collect(Collectors.toList()))
                .build();

        // When
        ResultActions actions = this.mockMvc.perform(
                post("/recipe").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(recipeRequest))
        );

        // Then
        actions
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value(recipeRequest.getName()))
                .andExpect(jsonPath("$.description").value(recipeRequest.getDescription()))
                .andExpect(jsonPath("$.category").value(recipeRequest.getCategory().name()))
                .andExpect(jsonPath("$.servings").value(recipeRequest.getServings()))
                .andExpect(jsonPath("$.ingredients", hasSize(recipeRequest.getIngredients().size())))
                .andExpect(jsonPath("$.ingredients[0].id").doesNotExist())
                .andExpect(jsonPath("$.ingredients[0].createdDate").doesNotExist())
                .andExpect(jsonPath("$.ingredients[0].lastModifiedDate").doesNotExist())
                .andExpect(jsonPath("$.ingredients[0].description").value(recipeRequest.getIngredients().get(0).getDescription()))
                .andExpect(jsonPath("$.ingredients[0].amount").value(recipeRequest.getIngredients().get(0).getAmount()))
                .andExpect(jsonPath("$.cookingInstructions", hasSize(recipeRequest.getCookingInstructions().size())))
                .andDo(print())
        ;
    }

    @Test
    @WithMockUser(username = "mockuser")
    void shouldBadRequestForPostEmptyRecipeTitle(@Autowired ObjectMapper objectMapper) throws Exception {
        // Given
        RecipeRequest recipeRequest = RecipeRequest.builder()
                .description("My first recipe")
                .category(Category.VEGETARIAN)
                .servings(10)
                .ingredients(Stream.of(
                                IngredientRequest.builder().description("Sugar").amount(BigDecimal.valueOf(5)).uom(UnitOfMeasure.gr).build()
                        )
                        .collect(Collectors.toList()))
                .cookingInstructions(Stream.of(
                                "First step", "Second step", "Third step"
                        )
                        .collect(Collectors.toList()))
                .build();

        // When
        ResultActions actions = this.mockMvc.perform(
                post("/recipe").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(recipeRequest))
        );

        // Then
        actions
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]",
                        allOf(
                                hasEntry("name", "must not be blank")
                        )))
                .andDo(print())
        ;
    }

    @Test
    @WithMockUser(username = "mockuser")
    void shouldBadRequestForPostUnknownCategory(@Autowired ObjectMapper objectMapper) throws Exception {
        // Given
        RecipeRequest recipeRequest = RecipeRequest.builder()
                .name("First Recipe")
                .description("My first recipe")
                .servings(10)
                .ingredients(Stream.of(
                                IngredientRequest.builder().description("Sugar").amount(BigDecimal.valueOf(5)).uom(UnitOfMeasure.gr).build()
                        )
                        .collect(Collectors.toList()))
                .cookingInstructions(Stream.of(
                                "First step", "Second step", "Third step"
                        )
                        .collect(Collectors.toList()))
                .build();
        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(recipeRequest));
        ((ObjectNode) json).put("category", "UNKNOWN_CATEGORY");

        // When
        ResultActions actions = this.mockMvc.perform(
                post("/recipe").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(json))
        );

        // Then
        actions
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]",
                        allOf(
                                hasEntry("category", "value should be: NON_VEGETARIAN, VEGETARIAN")
                        )))
        ;
    }

    @Test
    @WithMockUser(username = "mockuser")
    void shouldBadRequestForPostUnknownUnitOfMeasure(@Autowired ObjectMapper objectMapper) throws Exception {
        // Given
        RecipeRequest recipeRequest = RecipeRequest.builder()
                .name("First Recipe")
                .description("My first recipe")
                .category(Category.VEGETARIAN)
                .servings(10)
                .cookingInstructions(Stream.of(
                                "First step", "Second step", "Third step"
                        )
                        .collect(Collectors.toList()))
                .build();
        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(recipeRequest));
        JsonNode ingredientJson = objectMapper.readTree(objectMapper.writeValueAsString(IngredientRequest.builder().description("Sugar").amount(BigDecimal.valueOf(5)).build()));
        ((ObjectNode) ingredientJson).put("uom", "gram");
        ArrayNode array = objectMapper.createArrayNode();
        array.add(ingredientJson);
        ((ObjectNode) json).putArray("ingredients").addAll(array);

        // When
        ResultActions actions = this.mockMvc.perform(
                post("/recipe").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(json))
        );

        // Then
        actions
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]",
                        allOf(
                                hasEntry("ingredients", "value should be: tsp, gr, kg, cup, mL, tbsp, L")
                        )))
                .andDo(print())
        ;
    }

    @Test
    @WithMockUser(username = "mockuser")
    void userIsAbleToUpdateARecipe(@Autowired RecipeRepository recipeRepository,
                                   @Autowired IngredientRepository ingredientRepository,
                                   @Autowired RecipeMapper recipeMapper,
                                   @Autowired ObjectMapper objectMapper) throws Exception {
        // Given
        Recipe recipe = recipeRepository.save(Recipe.builder()
                .name("First Recipe")
                .description("My first recipe")
                .category(Category.VEGETARIAN)
                .servings(10)
                .ingredients(
                        Stream.of(
                                ingredientRepository.save(Ingredient.builder().description("Sugar").amount(BigDecimal.valueOf(5)).uom(UnitOfMeasure.gr).build())
                        ).collect(Collectors.toSet())
                )
                .cookingInstructions(
                        Stream.of(
                                "First step", "Second step", "Third step"
                        )
                        .collect(Collectors.toList())
                )
                .build());
        RecipeRequest recipeRequest = recipeMapper.toDto(recipe);
        recipeRequest.setName("First Recipe - Update");
        recipeRequest.setDescription("My first recipe - update");

        // When
        ResultActions actions = this.mockMvc.perform(
                put("/recipe/" + recipe.getId()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(recipeRequest))
        );

        // Then
        actions
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value(recipeRequest.getName()))
                .andExpect(jsonPath("$.description").value(recipeRequest.getDescription()))
                .andExpect(jsonPath("$.category").value(recipeRequest.getCategory().name()))
                .andExpect(jsonPath("$.servings").value(recipeRequest.getServings()))
                .andExpect(jsonPath("$.ingredients", hasSize(recipeRequest.getIngredients().size())))
                .andExpect(jsonPath("$.ingredients[0].description").value(recipeRequest.getIngredients().get(0).getDescription()))
                .andExpect(jsonPath("$.ingredients[0].amount").value(recipeRequest.getIngredients().get(0).getAmount()))
                .andExpect(jsonPath("$.cookingInstructions", hasSize(recipeRequest.getCookingInstructions().size())))
                .andDo(print())
        ;
    }

    @Test
    @WithMockUser(username = "mockuser")
    void shouldBadRequestForPutInvalidRecipeId(@Autowired RecipeRepository recipeRepository,
                                               @Autowired IngredientRepository ingredientRepository,
                                               @Autowired RecipeMapper recipeMapper,
                                               @Autowired ObjectMapper objectMapper) throws Exception {
        // Given
        Recipe recipe = recipeRepository.save(Recipe.builder()
                .name("First Recipe")
                .description("My first recipe")
                .category(Category.VEGETARIAN)
                .servings(10)
                .ingredients(
                        Stream.of(
                                ingredientRepository.save(Ingredient.builder().description("Sugar").amount(BigDecimal.valueOf(5)).uom(UnitOfMeasure.gr).build())
                        ).collect(Collectors.toSet())
                )
                .cookingInstructions(
                        Stream.of(
                                        "First step", "Second step", "Third step"
                                )
                                .collect(Collectors.toList())
                )
                .build());
        RecipeRequest recipeRequest = recipeMapper.toDto(recipe);
        recipeRequest.setName("First Recipe - Update");
        recipeRequest.setDescription("My first recipe - update");
        String invalidId = "aaa";

        // When
        ResultActions actions = this.mockMvc.perform(
                put("/recipe/" + invalidId).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(recipeRequest))
        );

        // Then
        actions
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value("Can't found recipe by ID " + invalidId))
                .andExpect(jsonPath("$.errors").value("Can't found recipe by ID " + invalidId))
                .andDo(print())
        ;

    }

    @Test
    @WithMockUser(username = "mockuser")
    void userIsAbleToDeleteARecipe(@Autowired RecipeRepository recipeRepository,
                                   @Autowired IngredientRepository ingredientRepository) throws Exception {
        // Given
        Recipe recipe = recipeRepository.save(Recipe.builder()
                .name("First Recipe")
                .description("My first recipe")
                .category(Category.VEGETARIAN)
                .servings(10)
                .ingredients(
                        Stream.of(
                                ingredientRepository.save(Ingredient.builder().description("Sugar").amount(BigDecimal.valueOf(5)).uom(UnitOfMeasure.gr).build())
                        ).collect(Collectors.toSet())
                )
                .cookingInstructions(
                        Stream.of(
                                        "First step", "Second step", "Third step"
                                )
                                .collect(Collectors.toList())
                )
                .build());

        // When
        ResultActions actions = this.mockMvc.perform(
                delete("/recipe/" + recipe.getId())
        );

        // Then
        actions
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Recipe has been deleted"))
                .andDo(print())
        ;
    }

    @Test
    @WithMockUser(username = "mockuser")
    void shouldBadRequestForDeleteInvalidRecipeId() throws Exception {
        // Given
        String invalidId = "aaa";

        // When
        ResultActions actions = this.mockMvc.perform(
                delete("/recipe/" + invalidId)
        );

        // Then
        actions
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value("Can't found recipe by ID " + invalidId))
                .andExpect(jsonPath("$.errors").value("Can't found recipe by ID " + invalidId))
                .andDo(print())
        ;

    }

}
