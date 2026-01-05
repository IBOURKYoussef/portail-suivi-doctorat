package com.spring.product_service.controller;

import com.spring.product_service.dto.ProductRequest;
import com.spring.product_service.dto.ProductResponse;
import com.spring.product_service.model.Product;
import com.spring.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService ProductService;
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createProduct(@RequestBody ProductRequest productRequest) {
        ProductService.createProduct(productRequest);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProductResponse> hello() {
        return ProductService.getALLProducts();
    }
}
