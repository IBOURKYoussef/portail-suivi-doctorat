package com.spring.inventory_service.controller;


import com.spring.inventory_service.dto.InventoryRsponse;
import com.spring.inventory_service.repository.InventoryRepository;
import com.spring.inventory_service.service.InventoryService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryRsponse> isInvStock(@RequestParam ("sku-code") List<String> skuCode) {
            return inventoryService.isInStock(skuCode);
    }

}
