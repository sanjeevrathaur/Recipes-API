package com.snjv.recipesbackend.mapper;

import com.snjv.recipesbackend.domain.Recipe;
import com.snjv.recipesbackend.payload.request.RecipeRequest;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {IngredientMapper.class})
public interface RecipeMapper {

    Recipe toEntity(RecipeRequest dto);

    RecipeRequest toDto(Recipe entity);

}
