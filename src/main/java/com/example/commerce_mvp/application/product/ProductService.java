package com.example.commerce_mvp.application.product;

import com.example.commerce_mvp.application.common.dto.SliceResponse;
import com.example.commerce_mvp.application.product.dto.ProductDto;
import com.example.commerce_mvp.domain.product.Product;
import com.example.commerce_mvp.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public SliceResponse<ProductDto> getProductList(Long cursorId, int size){
        PageRequest pageRequest = PageRequest.of(0, size+1);

        Slice<Product> productSlice = (cursorId == null || cursorId==0)
                ? productRepository.findAllByOrderByIdAsc(pageRequest)
                : productRepository.findProductsAfterCursor(cursorId, pageRequest);

        List<Product> products = productSlice.getContent();
        boolean hasNext = products.size() > size;

        List<ProductDto> content = products.stream()
                .map(ProductDto::new)
                .limit(size)
                .collect(Collectors.toList());

        Long nextCursor = null;
        if(hasNext && !content.isEmpty()){
            nextCursor = content.get(content.size()-1).getProductId();
        }
        return new SliceResponse<>(content, hasNext, nextCursor);
    }
}
