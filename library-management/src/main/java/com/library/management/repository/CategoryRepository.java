package com.library.management.repository;

import com.library.management.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    boolean existsByName(String name);

    boolean existsBySlug(String slug);
}
