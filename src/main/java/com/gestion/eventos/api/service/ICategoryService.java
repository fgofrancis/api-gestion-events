package com.gestion.eventos.api.service;

import com.gestion.eventos.api.domain.Category;

import java.util.List;
import java.util.Optional;

public interface ICategoryService {
    List<Category> findAll();
    Category findById(Long id);
    Category save(Category category);
    Category update(Long id, Category category);
    void deleteById(Long id);
}
