package com.smartmenu.ai.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "promo_dishes")
public class PromoDish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "marketing_description", nullable = false, length = 1000)
    private String marketingDescription;

    @Column(name = "original_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal originalPrice;

    @Column(name = "promo_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal promoPrice;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected PromoDish() {
    }

    public PromoDish(String name, String marketingDescription, BigDecimal originalPrice, BigDecimal promoPrice) {
        this.name = name;
        this.marketingDescription = marketingDescription;
        this.originalPrice = originalPrice;
        this.promoPrice = promoPrice;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMarketingDescription() {
        return marketingDescription;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public BigDecimal getPromoPrice() {
        return promoPrice;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PromoDish other) || id == null) {
            return false;
        }
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
