package com.example.commerce_mvp.application.product;


import com.example.commerce_mvp.domain.product.ProductRepository;
import com.example.commerce_mvp.infrastructure.naver.NaverApiClient;
import com.example.commerce_mvp.infrastructure.naver.NaverSearchResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductSearchUseCaseTest {

    @InjectMocks
    private ProductSearchUseCase productSearchUsecase;

    @Mock
    private NaverApiClient naverApiClient;

    @Mock
    private ProductRepository productRepository;

    @Test
    @DisplayName("새로운 상품이 검색되면 DB에 저장한다")
    void searchAndSaveProducts_whenNewProductsFound_shouldSaveToDb(){
        String query = "노트북";
        String newProductId = "product123";
        String existingProductId = "product999";

        NaverSearchResponseDto fakeResponse = createFakeNaverResponse(newProductId, existingProductId);
        when(naverApiClient.search(query)).thenReturn(fakeResponse);

        when(productRepository.existsByNaverProductId(newProductId)).thenReturn(false);
        when(productRepository.existsByNaverProductId(existingProductId)).thenReturn(true);

        productSearchUsecase.searchAndSaveProducts(query);

        verify(naverApiClient, times(1)).search(query);

        verify(productRepository, times(1)).saveAll(anyList());

    }

    private NaverSearchResponseDto createFakeNaverResponse(String newProductId, String existingProductId){
        NaverSearchResponseDto responseDto = new NaverSearchResponseDto();

        NaverSearchResponseDto.Item newItem = new NaverSearchResponseDto.Item();
        newItem.setTitle("새로운 노트북");
        newItem.setLprice("2000000");
        newItem.setProductId(newProductId);
        newItem.setImage("image_url_new");
        newItem.setCategory1("디지털/가전");
        newItem.setCategory2("노트북");

        NaverSearchResponseDto.Item existingItem = new NaverSearchResponseDto.Item();
        existingItem.setTitle("기존 노트북");
        existingItem.setLprice("1000000");
        existingItem.setProductId(existingProductId);
        existingItem.setImage("image_url_existing");
        existingItem.setCategory1("디지털/가전");
        existingItem.setCategory2("노트북");

        responseDto.setItems(List.of(newItem, existingItem));
        return responseDto;
    }
}
