package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.repositories.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository; // Repository for database operations related to 'Category'

    @Autowired
    private ModelMapper modelMapper; // Mapper to convert between entity and DTO objects

    @Override
    public CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        // Determine sort direction (ascending/descending) based on the 'sortOrder' parameter
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        // Create a Pageable object to specify pagination and sorting details
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        // Fetch paginated categories from the database
        Page<Category> categoryPage = categoryRepository.findAll(pageDetails);

        // Get the list of categories from the Page object
        List<Category> categories = categoryPage.getContent();

        // Check if the category list is empty; if true, throw an exception
        if (categories.isEmpty())
            throw new APIException("No category created till now.");

        // Convert each Category entity into a CategoryDTO using the ModelMapper
        List<CategoryDTO> categoryDTOS = categories.stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .toList();

        // Create a response object with paginated details and the category list
        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setContent(categoryDTOS);
        categoryResponse.setPageNumber(categoryPage.getNumber());
        categoryResponse.setPageSize(categoryPage.getSize());
        categoryResponse.setTotalElements(categoryPage.getTotalElements());
        categoryResponse.setTotalPages(categoryPage.getTotalPages());
        categoryResponse.setLastPage(categoryPage.isLast());

        return categoryResponse; // Return the response object
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        // Map the incoming DTO to a Category entity
        Category category = modelMapper.map(categoryDTO, Category.class);

        // Check if a category with the same name already exists
        Category categoryFromDb = categoryRepository.findByCategoryName(category.getCategoryName());
        if (categoryFromDb != null)
            throw new APIException("Category with the name " + category.getCategoryName() + " already exists !!!");

        // Save the new category in the database
        Category savedCategory = categoryRepository.save(category);

        // Map the saved Category entity back to a DTO and return it
        return modelMapper.map(savedCategory, CategoryDTO.class);
    }

    @Override
    public CategoryDTO deleteCategory(Long categoryId) {
        // Find the category by ID or throw an exception if not found
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        // Delete the category from the database
        categoryRepository.delete(category);

        // Return the deleted category details as a DTO
        return modelMapper.map(category, CategoryDTO.class);
    }

    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId) {
        // Check if the category exists in the database; throw an exception if not found
        Category savedCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        // Map the incoming DTO to a Category entity and set the category ID
        Category category = modelMapper.map(categoryDTO, Category.class);
        category.setCategoryId(categoryId);

        // Update the category in the database
        savedCategory = categoryRepository.save(category);

        // Map the updated Category entity back to a DTO and return it
        return modelMapper.map(savedCategory, CategoryDTO.class);
    }
}