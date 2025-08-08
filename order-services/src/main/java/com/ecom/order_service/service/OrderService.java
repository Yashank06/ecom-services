package com.ecom.order_service.service;

import com.ecom.order_service.dto.InventoryResponse;
import com.ecom.order_service.dto.OrderLineItemsDto;
import com.ecom.order_service.dto.OrderPlacedEvent;
import com.ecom.order_service.dto.OrderRequest;
import com.ecom.order_service.entity.Order;
import com.ecom.order_service.entity.OrderLineItems;
import com.ecom.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    private final Tracer tracer;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    @Transactional
    public String placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList().stream().map(this::mapToDto).toList();
        order.setOrderLineItemsList(orderLineItems);

        List<String> skuCodeList = order.getOrderLineItemsList().stream().map(OrderLineItems::getSkuCode).toList();

        Span span = tracer.currentSpan();
        if(span == null){
            span = tracer.nextSpan().name("inventory-service-lookup").start();
        }

        try(Tracer.SpanInScope inScope = tracer.withSpan(span)){
            InventoryResponse[] inventoryResponseArray = webClientBuilder.build().get().uri("http://inventory-service/api/inventory",
                            uriBuilder -> uriBuilder.queryParam("skuCodes",skuCodeList).build())
                    .retrieve().bodyToMono(InventoryResponse[].class).block();

            boolean result = Arrays.stream(inventoryResponseArray).allMatch(InventoryResponse::isInStock);

            if(result){
                orderRepository.save(order);
                log.info("Order saved");
                kafkaTemplate.send("order-notification",new OrderPlacedEvent(order.getOrderNumber()));
                log.info("notification sent to customer");
                return "Order Saved";
            } else {
                throw new IllegalArgumentException("Product is not in stock, please try again later");
            }
        } finally {
            span.end();
        }



    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQty(orderLineItemsDto.getQty());
        return orderLineItems;
    }

}
