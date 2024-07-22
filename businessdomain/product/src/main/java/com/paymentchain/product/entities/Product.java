package com.paymentchain.product.entities;

import lombok.Data;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "product")
public class Product {
    
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private long id;
    private String code;
    private String name;
}
