package main.server.category.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import main.server.category.dto.CategoryDto;
import main.server.category.dto.NewCategoryDto;
import main.server.category.mapper.CategoryMapper;
import main.server.category.repository.CategoryRepository;
import main.server.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class CategoryServiceImpl implements CategoryService {

    CategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        return CategoryMapper.toCategoryDto(categoryRepository.save(CategoryMapper.toCategory(newCategoryDto)));
    }

    @Override
    @Transactional
    public void deleteCategory(Long catId) {
        categoryRepository.deleteById(catId);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        if (categoryRepository.existsById(catId)) {
            categoryDto.setId(catId);
            return CategoryMapper.toCategoryDto(categoryRepository.save(CategoryMapper.updateCategory(categoryDto)));
        } else {
            throw new NotFoundException("Категория с id " + catId + " не найдена");
        }
    }

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        if (from < 0 || size < 0) {
            throw new ValidationException("Аргументы не могут быть отрицательными.");
        }
        return categoryRepository.findAll().stream()
                .map(CategoryMapper::toCategoryDto)
                .skip(from)
                .limit(size)
                .toList();
    }

    @Override
    public CategoryDto getCategory(Long catId) {
        return CategoryMapper.toCategoryDto(
                categoryRepository.findById(catId)
                        .orElseThrow(() -> new NotFoundException("Категория с id " + catId + " не найдена")));
    }
}