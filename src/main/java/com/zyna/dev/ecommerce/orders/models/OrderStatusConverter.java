package com.zyna.dev.ecommerce.orders.models;

import com.zyna.dev.ecommerce.common.enums.OrderStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Converter
public class OrderStatusConverter implements AttributeConverter<OrderStatus, String> {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusConverter.class);

    @Override
    public String convertToDatabaseColumn(OrderStatus attribute) {
        return attribute != null ? attribute.name() : null;
    }

    @Override
    public OrderStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return null;
        try {
            return OrderStatus.valueOf(dbData);
        } catch (IllegalArgumentException ex) {
            log.warn("Unknown OrderStatus '{}' in database. Fallback to CONFIRMED.", dbData);
            return OrderStatus.CONFIRMED;
        }
    }
}
