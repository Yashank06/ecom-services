package com.ecom.inventory_service.service;

import com.ecom.inventory_service.dto.InventoryDto;
import com.ecom.inventory_service.entity.Inventory;
import com.ecom.inventory_service.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional
    public void addToInventory(InventoryDto inventoryDto){
        Inventory inventory = new Inventory();
        inventory.setQty(inventoryDto.getQty());
        inventory.setSkuCode(inventoryDto.getSkuCode());
        inventoryRepository.save(inventory);
        log.info("inventory added for {}",inventoryDto.getSkuCode());
    }

    public boolean isInStock(String skuCode){
        return inventoryRepository.findBySkuCode(skuCode).isPresent();
    }
}
