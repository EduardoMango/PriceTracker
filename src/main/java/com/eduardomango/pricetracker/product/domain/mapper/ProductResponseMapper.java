package com.eduardomango.pricetracker.product.domain.mapper;

import com.eduardomango.pricetracker.common.model.IMapper;
import com.eduardomango.pricetracker.common.model.mappers.PriceMapper;
import com.eduardomango.pricetracker.product.domain.ProductEntity;
import com.eduardomango.pricetracker.product.domain.dto.ProductRequest;
import com.eduardomango.pricetracker.product.domain.dto.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {PriceMapper.class})
public interface ProductResponseMapper extends IMapper<ProductEntity, ProductResponse> {

    @Mapping(source = "externalId", target = "productId")
    @Mapping(source = "currentPrice", target = "price")
    ProductResponse toDTO(ProductEntity productEntity);
    ProductEntity toEntity(ProductRequest productRequest);

}
