package main.server.category.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import main.server.category.dto.CategoryDto;
import main.server.category.dto.NewCategoryDto;
import main.server.category.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CategoryController {

    CategoryService categoryService;

    @PostMapping("/admin/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@Valid @RequestBody NewCategoryDto newCategoryDto) {
        log.info("Создание категории");
        log.debug("Поступил запрос на создание категории: {}", newCategoryDto);
        return categoryService.createCategory(newCategoryDto);
    }

    @DeleteMapping("/admin/categories/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long catId) {
        log.info("Удаление категории");
        log.debug("Поступил запрос на удаление категории c id: {}", catId);
        categoryService.deleteCategory(catId);
    }

    @PatchMapping("/admin/categories/{catId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto updateCategory(@PathVariable Long catId, @Valid @RequestBody CategoryDto categoryDto) {
        log.info("Обновление категории");
        log.debug("Поступил запрос на обновление категории: {}", categoryDto);
        return categoryService.updateCategory(catId, categoryDto);
    }

    @GetMapping("/categories")
    @ResponseStatus(HttpStatus.OK)
    public List<CategoryDto> getCategories(@PositiveOrZero @RequestParam(name = "from",
                                                          defaultValue = "0") Integer from,
                                              @Positive @RequestParam(name = "size",
                                                      defaultValue = "10") Integer size) {
        log.info("Получение списка категорий с учетом пагинации");
        log.debug("Поступил запрос на получение списка категорий, параметры запроса: {}, {}", from, size);
        return categoryService.getCategories(from, size);
    }

    @GetMapping("/categories/{catId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto getCategory(@PathVariable Long catId) {
        log.info("Получение категории");
        log.debug("Поступил запрос на получение категории с id: {}", catId);
        return categoryService.getCategory(catId);
    }
}