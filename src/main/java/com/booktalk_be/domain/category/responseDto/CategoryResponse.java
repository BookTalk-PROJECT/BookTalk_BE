package com.booktalk_be.domain.category.responseDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class CategoryResponse {

     private final Integer categoryId;
     private final String value;
     @JsonProperty("isActive")
     private final boolean isActive;
     private final List<CategoryResponse> subCategories =  new ArrayList<>();

     public void addSubCategory(CategoryResponse categoryResponse) {
          subCategories.add(categoryResponse);
     }

}
