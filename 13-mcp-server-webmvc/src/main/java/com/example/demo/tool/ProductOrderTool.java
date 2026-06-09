package com.example.demo.tool;

import com.example.demo.repository.ProductOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpMeta;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductOrderTool {
    private final ProductOrderRepository productOrderRepository;

    // name → 실제 MCP 프로토콜에서 사용하는 식별자 (kebab-case를 사용하면 LLM이 get product orders와 같이 토큰을 자연스럽게 분리할 수 있다)
    // title → UI(사람이 보는 화면)용 표시 이름
    // description → AI에게 언제 이 tool을 써야 하는지 설명, LLM이 tool을 선택할 때 가장 중요하게 참고하는 필드
    @McpTool(name="get-product-orders", title = "상품 주문 조회", description="상품 주문 목록을 조회합니다")
    public String getProductOrders(McpMeta mcpMeta) {
        String username = (String) mcpMeta.get("username");
        var productOrders = productOrderRepository.findByMemberName(username);
        if (productOrders.isEmpty()) {
            return "주문 내역이 없습니다.";
        } else {
            String result = "주문 목록은 다음과 같아요\n";
            for (var productOrder : productOrders) {
                result += "주문번호: " + productOrder.getOrderNumber();
                result += ", 상품이름: " + productOrder.getProductName();
                result += ", 배송주소: " + productOrder.getShippingAddress();
                result += ", 배송상태: " + productOrder.getShippingStatus();
                result += "\n";
            }
            return result;
        }
    }

    @McpTool(name="cancel-product-order", title = "상품 주문 취소", description = "특정 상품 주문을 취소할 때 사용합니다")
    String cancelProductOrder(@McpToolParam(description="주문번호") String orderNumber, McpMeta mcpMeta) {
        String username = (String) mcpMeta.get("username");
        var productOrder = productOrderRepository.findByOrderNumberAndMemberName(orderNumber, username);
        if (productOrder.isPresent()) {
            if ("배송중".equals(productOrder.get().getShippingStatus())) {
                return "배송중인 상품은 취소할 수 없습니다.";
            } else {
                productOrderRepository.delete(productOrder.get());
                return "주문이 취소 되었습니다.";
            }
        } else {
            return "없는 주문 번호입니다.";
        }
    }
}
