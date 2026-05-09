package com.eduardomango.pricetracker.product.domain.mapper;

import com.eduardomango.pricetracker.common.model.IMapper;
import com.eduardomango.pricetracker.product.domain.ProductEntity;
import com.eduardomango.pricetracker.product.domain.dto.ProductRequest;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductRequestMapper extends IMapper<ProductEntity, ProductRequest> {

    ProductRequest toDTO(ProductEntity productEntity);
    ProductEntity toEntity(ProductRequest productRequest);
}
