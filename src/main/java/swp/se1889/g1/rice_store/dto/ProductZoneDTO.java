package swp.se1889.g1.rice_store.dto;

import java.math.BigDecimal;

public class ProductZoneDTO {
    private Long productId;
    private String productName;
    private Long zoneId;
    private String zoneName;
    private BigDecimal price;
    private Integer quantity;

    public ProductZoneDTO(Long productId, String productName, Long zoneId, String zoneName, BigDecimal price, Integer quantity) {
        this.productId = productId;
        this.productName = productName;
        this.zoneId = zoneId;
        this.zoneName = zoneName;
        this.price = price;
        this.quantity = quantity;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public void setZoneId(Long zoneId) {
        this.zoneId = zoneId;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
