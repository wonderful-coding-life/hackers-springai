package com.example.demo;

import com.example.demo.entity.ProductOrder;
import com.example.demo.repository.ProductOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements ApplicationRunner {
    @Autowired
    private ProductOrderRepository repository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (repository.count() == 0) {
            var orders = List.of(
                    ProductOrder.builder()
                            .orderNumber("H001")
                            .memberId("user123")
                            .productName("맥북에어")
                            .shippingAddress("서울시 강남구 역삼동")
                            .shippingStatus("상품준비중").build(),
                    ProductOrder.builder()
                            .orderNumber("H002")
                            .memberId("user123")
                            .productName("아이폰")
                            .shippingAddress("서울시 영등포구 여의도동")
                            .shippingStatus("배송중").build()
            );
            repository.saveAll(orders);
        }

    }
}
