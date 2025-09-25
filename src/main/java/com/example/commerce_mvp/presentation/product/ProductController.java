package com.example.commerce_mvp.presentation.product;


import com.example.commerce_mvp.application.product.ProductSearchUseCase;
import com.example.commerce_mvp.application.product.dto.ProductDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductSearchUseCase productSearchUseCase;

    @GetMapping("/search")
    public ResponseEntity<List<ProductDto>> searchProducts(@RequestParam String query){
        List<ProductDto> products = productSearchUseCase.searchAndSaveProducts(query);
        return ResponseEntity.ok(products);
    }
}
