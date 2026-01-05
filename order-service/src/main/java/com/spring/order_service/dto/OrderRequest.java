package com.spring.order_service.dto;

import com.spring.order_service.model.OrderLineItems;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderRequest {
    private Long id;
    private String orderNumber;
    private List<OrderLineItemsDto> orderLineItemsDtoList;
}
