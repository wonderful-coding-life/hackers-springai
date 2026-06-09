package com.example.demo.config;

import com.example.demo.model.ProductOrder;
import com.example.demo.repository.ProductOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {
    private final ProductOrderRepository productOrderRepository;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        productOrderRepository.save(ProductOrder.builder()
                .orderNumber("ORD-20260001")
                .productName("무선 기계식 키보드 K87")
                .shippingAddress("서울특별시 강남구 테헤란로 123, 캠퍼스타워 10층")
                .shippingStatus("상품준비중")
                .memberName("user")
                .build());
        productOrderRepository.save(ProductOrder.builder()
                .orderNumber("ORD-20260002")
                .productName("27인치 QHD 모니터 M27")
                .shippingAddress("경기도 성남시 분당구 판교역로 235, 판교테크센터 8층")
                .shippingStatus("배송중")
                .memberName("user")
                .build());
    }
}