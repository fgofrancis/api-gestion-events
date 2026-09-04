package com.gestion.eventos.api.mapper;

import com.gestion.eventos.api.domain.Category;
import com.gestion.eventos.api.dto.CategoryDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDto toDto(Category category);
    Category toEntity(CategoryDto categoryDto);
}
