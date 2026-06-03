package com.example.demo.tool;

import com.example.demo.repository.ProductOrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProductOrderTool {
    @Autowired
    private ProductOrderRepository repository;

    @Tool(description = "상품 주문 목록을 알려줍니다")
    String getProductOrders(ToolContext toolContext) {
        String username = (String) toolContext.getContext().get("username");
        var productOrders = repository.findByMemberName(username);
        if (productOrders.isEmpty()) {
            return "주문 목록이 없습니다.";
        } else {
            StringBuilder result = new StringBuilder("주문 목록은 다음과 같아요\n");
            for (var productOrder : productOrders) {
                result.append("주문번호: ").append(productOrder.getOrderNumber());
                result.append(", 상품이름: ").append(productOrder.getProductName());
                result.append(", 배송주소: ").append(productOrder.getShippingAddress());
                result.append(", 배송상태: ").append(productOrder.getShippingStatus());
                result.append("\n");
            }
            return result.toString();
        }
    }

    @Tool(description = "상품 주문을 취소합니다")
    String cancelProductOrder(@ToolParam(description = "주문번호") String orderNumber, ToolContext toolContext) {
        String username = (String) toolContext.getContext().get("username");
        var productOrder = repository.findByOrderNumberAndMemberName(orderNumber, username);
        if (productOrder.isPresent()) {
            if ("배송중".equals(productOrder.get().getShippingStatus())) {
                return "배송중인 상품은 취소할 수 없습니다.";
            } else {
                repository.delete(productOrder.get());
                return "주문이 취소 되었습니다.";
            }
        } else {
            return "없는 주문 번호입니다.";
        }
    }
}
