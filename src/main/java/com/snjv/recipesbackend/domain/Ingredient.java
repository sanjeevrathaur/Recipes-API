package com.snjv.recipesbackend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(collection = "Ingredient")
@JsonIgnoreProperties({"id", "createdDate", "lastModifiedDate", "createdBy", "lastModifiedBy"})
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Ingredient extends AbstractBaseEntity {

    private String description;

    private BigDecimal amount;

    private UnitOfMeasure uom;

}
