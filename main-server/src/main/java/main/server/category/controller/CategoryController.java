package main.server.category.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import main.server.category.dto.CategoryDto;
import main.server.category.dto.NewCategoryDto;
import main.server.category.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryController {

    CategoryService categoryService;

    @PostMapping("/admin/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@Valid @RequestBody NewCategoryDto newCategoryDto) {
        return categoryService.createCategory(newCategoryDto);
    }

    @DeleteMapping("/admin/categories/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long catId) {
        categoryService.deleteCategory(catId);
    }

    @PatchMapping("/admin/categories/{catId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto updateCategory(@PathVariable Long catId, @Valid @RequestBody CategoryDto categoryDto) {
        return categoryService.updateCategory(catId, categoryDto);
    }

    @GetMapping("/categories")
    @ResponseStatus(HttpStatus.OK)
    public List<CategoryDto> getAllCategories(@PositiveOrZero @RequestParam(name = "from",
                                                          defaultValue = "0") Integer from,
                                              @Positive @RequestParam(name = "size",
                                                      defaultValue = "10") Integer size) {
        return categoryService.getAllCategories(from, size);
    }

    @GetMapping("/categories/{catId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto getCategory(@PathVariable Long catId) {
        return categoryService.getCategory(catId);
    }
}