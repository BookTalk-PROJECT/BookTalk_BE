package com.booktalk_be.domain.category.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class CategoryResponse {

     private final Integer categoryId;
     private final String value;
     private final List<CategoryResponse> subCategories;

     /*
     CategoryResponse subCategory1 = new CategoryResponse(2, "Java", Collections.emptyList());
     CategoryResponse subCategory2 = new CategoryResponse(3, "Python", Collections.emptyList());

     CategoryResponse parentCategory = new CategoryResponse(
         1,
         "Programming",
         Arrays.asList(subCategory1, subCategory2)
     );
     */

}
