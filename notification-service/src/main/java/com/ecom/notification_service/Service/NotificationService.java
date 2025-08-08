package com.ecom.notification_service.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {
    @KafkaListener(topics = "order-notification",groupId = "notificationId")
    public void listenNotification(OrderPlacedEvent orderPlacedEvent){
        // send email
        log.info("Your order placed successfully, order id {} ", orderPlacedEvent.getOrderNumber());
    }
}
