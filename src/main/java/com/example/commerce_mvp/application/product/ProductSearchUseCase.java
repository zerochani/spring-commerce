package com.example.commerce_mvp.application.product;

import com.example.commerce_mvp.application.product.dto.ProductDto;
import com.example.commerce_mvp.domain.product.Product;
import com.example.commerce_mvp.domain.product.ProductRepository;
import com.example.commerce_mvp.infrastructure.naver.NaverApiClient;
import com.example.commerce_mvp.infrastructure.naver.NaverSearchResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductSearchUseCase {
    private final NaverApiClient naverApiClient;
    private final ProductRepository productRepository;

    @Transactional
    public List<ProductDto> searchAndSaveProducts(String query){
        NaverSearchResponseDto response = naverApiClient.search(query);
        List<Product> productsToSave = response.getItems().stream()
                .filter(item-> !productRepository.existsByNaverProductId(item.getProductId()))
                .map(this::mapItemToProduct)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if(!productsToSave.isEmpty()){
            productRepository.saveAll(productsToSave);
        }
        return productRepository.findAll().stream()
                .map(ProductDto::from)
                .collect(Collectors.toList());
    }

    private Product mapItemToProduct(NaverSearchResponseDto.Item item){
        String title = item.getTitle().replaceAll("<[^>]*>", "");
        int price = Integer.parseInt(item.getLprice());
        return Product.of(title, price, item.getImage(), item.getProductId());
    }
}
