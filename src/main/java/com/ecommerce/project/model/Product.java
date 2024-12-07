package com.ecommerce.project.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "product_id") //Change was made here
    private Long productId;

    @NotBlank
    private String productName;
    private String image;
    private String description;
    private Integer quantity;


    private Double price;
    private Double discount;
    private Double specialPrice;


    @ManyToOne
    @JoinColumn(name = "category_id")
    @JsonManagedReference
    private Category category;

    @OneToMany(mappedBy = "product", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, fetch = FetchType.EAGER,
    orphanRemoval = true)
    private List<CartItem> products = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User user;
}
