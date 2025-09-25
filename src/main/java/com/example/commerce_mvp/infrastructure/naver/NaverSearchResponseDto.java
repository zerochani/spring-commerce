package com.example.commerce_mvp.infrastructure.naver;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class NaverSearchResponseDto {
    private List<Item> items;
    @Getter
    @Setter
    public static class Item{
        private String title;
        private String link;
        private String image;
        private String lprice;
        private String mallName;
        private String productId;
        private String category1;
        private String category2;
    }
}
