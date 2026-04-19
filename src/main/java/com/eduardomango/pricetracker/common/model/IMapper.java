package com.eduardomango.pricetracker.common.model;

public interface IMapper <T,U>{
    T toEntity(U u);
    U toDTO(T t);
}
