package com.ringo.service.company;

import com.ringo.auth.AuthenticationService;
import com.ringo.dto.company.CategoryDto;
import com.ringo.exception.NotFoundException;
import com.ringo.exception.UserException;
import com.ringo.mapper.company.CategoryMapper;
import com.ringo.model.company.Category;
import com.ringo.model.security.Role;
import com.ringo.repository.company.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CategoryService {

    private final CategoryRepository repository;
    private final CategoryMapper mapper;
    private final AuthenticationService authenticationService;

    public CategoryDto findCategoryById(Long id) {
        log.info("findCategoryById: {}", id);

        Category category = repository.findById(id).orElseThrow(
                () -> new NotFoundException("Category [id: %d] not found".formatted(id))
        );
        return mapper.toDto(category);
    }

    public List<CategoryDto> findAll() {
        log.info("findAll categories");
        return mapper.toDtoList(repository.findAll());
    }

    public CategoryDto saveCategory(CategoryDto categoryDto) {
        log.info("saveCategory: {}", categoryDto);

        throwIfNotAdmin();
        if(repository.findByName(categoryDto.getName()).isPresent())
            throw new UserException("Category [name: %s] already exists".formatted(categoryDto.getName()));

        Category category = mapper.toEntity(categoryDto);
        return mapper.toDto(repository.save(category));
    }

    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) {
        log.info("updateCategory: {}", categoryDto);

        throwIfNotAdmin();

        Category category = repository.findById(id).orElseThrow(
                () -> new NotFoundException("Category [id: %d] not found".formatted(id))
        );

        mapper.partialUpdate(category, categoryDto);
        throwIfUniqueConstraintsViolated(category);


        return mapper.toDto(repository.save(category));
    }

    public void deleteCategory(Long id) {
        log.info("deleteCategory: {}", id);
        throwIfNotAdmin();

        if(repository.findById(id).isEmpty())
            throw new NotFoundException("Category [id: %d] not found".formatted(id));
        repository.deleteById(id);
    }

    private void throwIfNotAdmin() {
        if(authenticationService.getCurrentUser().getRole() != Role.ROLE_ADMIN)
            throw new UserException("Only admin can perform this action");
    }

    public void throwIfUniqueConstraintsViolated(Category category) {
        if(category.getName() != null) {
            Category found = repository.findByName(category.getName()).orElse(null);
            if(found != null && !found.getId().equals(category.getId()))
                throw new UserException("Category [name: %s] already exists".formatted(category.getName()));
        }
    }
}
