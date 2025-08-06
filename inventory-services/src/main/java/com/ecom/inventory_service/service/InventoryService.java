package com.ecom.inventory_service.service;

import com.ecom.inventory_service.dto.InventoryResponse;
import com.ecom.inventory_service.entity.Inventory;
import com.ecom.inventory_service.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional
    public void addToInventory(InventoryResponse inventoryResponse){
        Inventory inventory = new Inventory();
        inventory.setQty(inventoryResponse.getQty());
        inventory.setSkuCode(inventoryResponse.getSkuCode());
        inventoryRepository.save(inventory);
        log.info("inventory added for {}", inventoryResponse.getSkuCode());
    }

    @SneakyThrows
    public List<InventoryResponse> isInStock(List<String> skuCodes){

        log.info("wait started");
        Thread.sleep(10000);
        log.info("wait end");

        return inventoryRepository.findBySkuCodeIn(skuCodes).stream()
                .map(inventory -> InventoryResponse.builder()
                        .skuCode(inventory.getSkuCode())
                        .isInStock(inventory.getQty() > 0)
                        .qty(inventory.getQty())
                        .build()).toList();
    }
}
