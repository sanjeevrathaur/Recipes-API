package com.snjv.recipesbackend.mapper;

import com.snjv.recipesbackend.domain.Ingredient;
import com.snjv.recipesbackend.payload.request.IngredientRequest;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IngredientMapper {

    Ingredient toEntity(IngredientRequest dto);

}
