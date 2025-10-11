package com.example.commerce_mvp.presentation.product;


import com.example.commerce_mvp.application.common.dto.SliceResponse;
import com.example.commerce_mvp.application.product.ProductSearchUseCase;
import com.example.commerce_mvp.application.product.ProductService;
import com.example.commerce_mvp.application.product.dto.ProductDto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductSearchUseCase productSearchUseCase;
    private final ProductService productService;

    //DB의 상품 목록을 조회(커서 기반)
    @GetMapping
    public ResponseEntity<SliceResponse<ProductDto>> getProducts(
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size){
        SliceResponse<ProductDto> response = productService.getProductList(cursorId, size);
        return ResponseEntity.ok(response);
    }

    //네이버 API로 상품을 검색하고 DB에 저장
    @PostMapping("/fetch")
    public ResponseEntity<String> fetchAndSaveProducts(@RequestParam @NotBlank(message = "검색어는 필수입니다.") String query){
        productSearchUseCase.searchAndSaveProducts(query);
        return ResponseEntity.ok(query + " 상품 정보가 DB에 저장되었습니다.");
    }

}
