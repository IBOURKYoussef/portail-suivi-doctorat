package com.spring.inventory_service.service;

import com.spring.inventory_service.dto.InventoryRsponse;
import com.spring.inventory_service.model.Inventory;
import com.spring.inventory_service.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public List<InventoryRsponse> isInStock(List<String> skuCode) {
        List<Inventory> inventories = inventoryRepository.findBySkuCodeIn(skuCode);
        if (inventories.isEmpty()) {
            throw new IllegalArgumentException("Aucun produit trouvé dans l'inventaire !");
        }

        return inventories.stream()
                .map(inventory -> InventoryRsponse.builder()
                        .skuCode(inventory.getSkuCode())
                        .isInStock(inventory.getQuantity() != null && inventory.getQuantity() > 0)
                        .build())
                .toList();
    }

}
