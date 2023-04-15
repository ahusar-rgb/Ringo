package com.ringo.service.company;

import com.ringo.dto.company.CategoryDto;
import com.ringo.exception.IllegalInsertException;
import com.ringo.exception.NotFoundException;
import com.ringo.mapper.company.CategoryMapper;
import com.ringo.model.company.Category;
import com.ringo.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

        private final CategoryRepository repository;
        private final CategoryMapper mapper;

        public CategoryDto findCategoryById(Long id) {
            log.info("findCategoryById: {}", id);
            return mapper.toDto(
                    repository.findById(id).orElseThrow(
                            () -> new NotFoundException("Category [id: %d] not found".formatted(id)))
            );
        }

        public List<CategoryDto> findAll() {
            log.info("findAll categories");
            return mapper.toDtos(repository.findAll());
        }

        public CategoryDto saveCategory(CategoryDto categoryDto) {
            log.info("saveCategory: {}", categoryDto);
            if(repository.findByName(categoryDto.getName()).isPresent())
                throw new IllegalInsertException("Category [name: %s] already exists".formatted(categoryDto.getName()));
            Category category = mapper.toEntity(categoryDto);
            return mapper.toDto(repository.save(category));
        }

        public CategoryDto updateCategory(CategoryDto categoryDto) {
            log.info("updateCategory: {}", categoryDto);
            if(repository.findById(categoryDto.getId()).isEmpty())
                throw new NotFoundException("Category [id: %d] not found".formatted(categoryDto.getId()));
            if(repository.findByName(categoryDto.getName()).isPresent())
                throw new IllegalInsertException("Category [name: %s] already exists".formatted(categoryDto.getName()));

            Category category = mapper.toEntity(categoryDto);
            return mapper.toDto(repository.save(category));
        }

        public void deleteCategory(Long id) {
            log.info("deleteCategory: {}", id);
            if(repository.findById(id).isEmpty())
                throw new NotFoundException("Category [id: %d] not found".formatted(id));
            repository.deleteById(id);
        }
}
