package com.spring.order_service.service;

//import org.apache.catalina.Service;

import com.spring.order_service.dto.InventoryRsponse;
import com.spring.order_service.dto.OrderLineItemsDto;
import com.spring.order_service.dto.OrderRequest;
import com.spring.order_service.model.Order;
import com.spring.order_service.model.OrderLineItems;
import com.spring.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;

    public void placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();

        order.setOrderLineItemsList(orderLineItems);

        List<String> skuCodes = order.getOrderLineItemsList().stream()
                        .map(OrderLineItems::getSkuCode)
                                .toList();

        InventoryRsponse[] inventoryRsponsesArray = webClientBuilder.build().get()
                .uri("lb://inventory-service/inventory", uriBuilder -> uriBuilder.queryParam("sku-code", skuCodes).build())
                .retrieve()
                .bodyToMono(InventoryRsponse[].class)
                .block();


        // Vérifier si la liste est vide
                if (inventoryRsponsesArray == null || inventoryRsponsesArray.length == 0) {
                    throw new IllegalArgumentException("Aucun produit trouvé dans l'inventaire pour les SKU demandés !");
                }

        // Vérifier si tous les produits sont en stock
                boolean allProductInStock = Arrays.stream(inventoryRsponsesArray)
                        .allMatch(InventoryRsponse::isInStock);

                if (allProductInStock) {
                    orderRepository.save(order);
                } else {
                    throw new IllegalArgumentException("Product is not in stock, please try again later");
                }



    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }
}
