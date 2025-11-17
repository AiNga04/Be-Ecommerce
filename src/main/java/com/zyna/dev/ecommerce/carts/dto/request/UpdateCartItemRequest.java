package com.zyna.dev.ecommerce.carts.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCartItemRequest {

    @NotNull
    @Min(0)
    private Integer quantity; // nếu = 0 thì coi như xoá
}
