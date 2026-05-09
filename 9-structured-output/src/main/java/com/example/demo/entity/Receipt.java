package com.example.demo.entity;

import com.example.demo.ocr.ReceiptOcr;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Receipt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String store;
    private LocalDateTime issuedDate;
    private Long amount;

    public Receipt(ReceiptOcr ocr) {
        this.store = ocr.getStore();
        this.issuedDate = ocr.getIssuedDate();
        this.amount = ocr.getAmount();
    }
}
