package io.keepcoding.pickandgol.model.mapper;

import io.keepcoding.pickandgol.manager.net.response.CategoryDetailResponse;
import io.keepcoding.pickandgol.manager.net.response.CategoryListResponse;
import io.keepcoding.pickandgol.model.Category;
import io.keepcoding.pickandgol.model.CategoryAggregate;

public class CategoryListDataToCategoryAggregateMapper {
    public CategoryAggregate map(final CategoryListResponse.CategoryListData data) {
        CategoryAggregate mappedCategories = new CategoryAggregate();
        for (CategoryDetailResponse.CategoryDetailData detailData: data.getItems()) {
            mappedCategories.add(new Category(detailData.getId(), detailData.getName()));
        }

        return mappedCategories;
    }
}
