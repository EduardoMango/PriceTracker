package com.eduardomango.pricetracker.common.model.mappers;


import com.eduardomango.pricetracker.common.model.Price;
import com.eduardomango.pricetracker.common.model.PriceDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

@Mapper(componentModel = "spring")
public interface PriceMapper {

    @Mapping(target = "formattedPrice",
            expression = "java(formatPrice(price.amount(), price.currency()))")
    PriceDTO toPriceDTO(Price price);

    default String formatPrice(BigDecimal amount, String currency) {
        try {
            NumberFormat format = NumberFormat.getCurrencyInstance(Locale.getDefault());
            format.setCurrency(Currency.getInstance(currency));
            return format.format(amount);
        } catch (Exception e) {
            // Fallback if the currency is not recognized
            return currency + " " + amount.toString();
        }
    }
}
