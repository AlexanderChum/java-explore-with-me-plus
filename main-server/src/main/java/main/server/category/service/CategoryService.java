package main.server.category.service;

import main.server.category.dto.CategoryDto;
import main.server.category.dto.NewCategoryDto;

public interface CategoryService {
    CategoryDto createCategory(NewCategoryDto newCategoryDto);
}