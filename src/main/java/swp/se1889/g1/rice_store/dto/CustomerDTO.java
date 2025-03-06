package swp.se1889.g1.rice_store.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CustomerDTO {

    private Long id;  // ID khách hàng

    @NotBlank(message = "Tên khách hàng không được để trống")
    @Size(max = 100, message = "Tên khách hàng không được dài quá 100 ký tự")
    private String name;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "\\d{10,15}", message = "Số điện thoại phải có từ 10 đến 15 số")
    private String phone;

    @Size(max = 255, message = "Địa chỉ không được dài quá 255 ký tự")
    private String address;

    @Email(message = "Email không hợp lệ")
    @Size(max = 255, message = "Email không được dài quá 255 ký tự")
    private String email;

    private BigDecimal debtBalance = BigDecimal.ZERO;
    private LocalDateTime createdAt;  // Ngày tạo
    private LocalDateTime updatedAt;  // Ngày cập nhật


    // Getters và Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BigDecimal getDebtBalance() {
        return debtBalance;
    }

    public void setDebtBalance(BigDecimal debtBalance) {
        this.debtBalance = debtBalance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
