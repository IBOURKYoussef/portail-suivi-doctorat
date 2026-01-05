package com.spring.order_service.dto;


import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class OrderLineItemsDto {
    private Long id;
    private String skuCode;
    private BigDecimal price;
    private Integer quantity;
}
