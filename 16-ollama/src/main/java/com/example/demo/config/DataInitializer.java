package com.example.demo.config;

import com.example.demo.entity.Member;
import com.example.demo.entity.MemberAuthority;
import com.example.demo.entity.ProductOrder;
import com.example.demo.repository.MemberRepository;
import com.example.demo.repository.ProductOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {
    private final MemberRepository memberRepository;
    private final ProductOrderRepository productOrderRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (memberRepository.count() == 0) {
            Member user = Member.builder()
                    .username("user")
                    .password(passwordEncoder.encode("1234"))
                    .build();

            MemberAuthority userRole = MemberAuthority.builder()
                    .authority("ROLE_USER")
                    .member(user)
                    .build();

            user.setAuthorities(List.of(userRole));

            Member admin = Member.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("1234"))
                    .build();

            MemberAuthority adminUserRole = MemberAuthority.builder()
                    .authority("ROLE_USER")
                    .member(admin)
                    .build();

            MemberAuthority adminRole = MemberAuthority.builder()
                    .authority("ROLE_ADMIN")
                    .member(admin)
                    .build();

            admin.setAuthorities(List.of(adminUserRole, adminRole));

            memberRepository.save(user);
            memberRepository.save(admin);
        }

        productOrderRepository.deleteAll();
        if (productOrderRepository.count() == 0) {
            Member member = memberRepository.findByUsername("user").orElseThrow();
            var orders = List.of(
                    ProductOrder.builder()
                            .orderNumber("ORD-20260001")
                            .member(member)
                            .productName("무선 기계식 키보드 K87")
                            .shippingAddress("서울특별시 강남구 테헤란로 123, 캠퍼스타워 10층")
                            .shippingStatus("상품준비중").build(),
                    ProductOrder.builder()
                            .orderNumber("ORD-20260002")
                            .member(member)
                            .productName("27인치 QHD 모니터 M27")
                            .shippingAddress("경기도 성남시 분당구 판교역로 235, 판교테크센터 8층")
                            .shippingStatus("배송중").build()
            );
            productOrderRepository.saveAll(orders);
        }
    }
}
