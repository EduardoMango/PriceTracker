package com.eduardomango.pricetracker.subscription.domain.mapper;

import com.eduardomango.pricetracker.common.model.IMapper;
import com.eduardomango.pricetracker.common.model.Price;
import com.eduardomango.pricetracker.common.model.mappers.PriceMapper;
import com.eduardomango.pricetracker.product.domain.AlertCondition;
import com.eduardomango.pricetracker.subscription.domain.SubscriptionEntity;
import com.eduardomango.pricetracker.subscription.domain.dto.SubscriptionRequest;
import com.eduardomango.pricetracker.subscription.domain.dto.SubscriptionResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {PriceMapper.class})
public interface SubscriptionMapper extends IMapper<SubscriptionEntity, SubscriptionResponse> {

    @Override
    @Mapping(source = "externalId", target = "subscriptionId")
    @Mapping(source = "user.externalId", target = "userId")
    @Mapping(source = "product.externalId", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "alertCondition.targetPrice", target = "targetPrice")
    @Mapping(source = "alertCondition.comparisonType", target = "comparisonType")
    SubscriptionResponse toDTO(SubscriptionEntity entity);

    @Override
    @BeanMapping(ignoreByDefault = true)
    SubscriptionEntity toEntity(SubscriptionResponse response);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "alertCondition", expression = "java(mapAlertCondition(request))")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "externalId", ignore = true)
    SubscriptionEntity toEntity(SubscriptionRequest request);

    default AlertCondition mapAlertCondition(SubscriptionRequest request) {
        return new AlertCondition(
                new Price(request.targetPrice(), request.currency()),
                request.comparisonType()
        );
    }
}
