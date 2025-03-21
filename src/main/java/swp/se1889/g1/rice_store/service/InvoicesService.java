package swp.se1889.g1.rice_store.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import swp.se1889.g1.rice_store.dto.InvoiceDetailDTO;
import swp.se1889.g1.rice_store.dto.InvoicesDTO;
import swp.se1889.g1.rice_store.entity.*;
import swp.se1889.g1.rice_store.repository.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InvoicesService {
    @Autowired
    private InvoicesRepository invoiceRepository;
    @Autowired
    private InvoiceDetailRepository invoiceDetailsRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ZoneRepository zoneRepository;
    @Autowired
    private UserRepository userRepository;

    // Hàm tìm kiếm hóa đơn theo ID cửa hàng
    public List<Invoices> findByStore(Long storeId) {
        if (storeId == null) {
            throw new IllegalArgumentException("Store ID cannot be null");
        }
        return invoiceRepository.findByStoreId(storeId);
    }
public InvoiceDetailDTO getInvoice(Long id) {
    Optional<InvoicesDetails> invoice = invoiceDetailsRepository.findById(id);
    if (invoice.isPresent()) {
        InvoicesDetails detail = invoice.get();
        detail.getId();
    }
    throw new RuntimeException("Không tìm thấy hóa đơn có ID: " + id);
}


    @Transactional
    public Invoices createImportInvoice(InvoicesDTO dto, Store store) {
        // 1. Kiểm tra và lấy thông tin khách hàng
        User currentUser = getCurrentUser();
        if (dto.getCustomerPhone() == null) {
            throw new IllegalArgumentException("Số điện thoại khách hàng không được để trống");
        }
        Customer customer = customerRepository.findCustomerByPhone(dto.getCustomerPhone());
        if (customer == null) {
            throw new RuntimeException("Không tìm thấy khách hàng với số điện thoại: " + dto.getCustomerPhone());
        }

        // 2. Kiểm tra và lấy thông tin cửa hàng
        if (dto.getStoreId() == null) {
            throw new IllegalArgumentException("ID cửa hàng không được để trống");
        }

        // Lấy thông tin cửa hàng từ storeId
        Store storeFromDb = storeRepository.findById(dto.getStoreId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cửa hàng với ID: " + dto.getStoreId()));

        // 3. Tính toán tổng giá trị hóa đơn
        BigDecimal totalPrice = calculateTotalPrice(dto.getDetails());
        BigDecimal discount = dto.getDiscount() != null ? dto.getDiscount() : BigDecimal.ZERO;
        BigDecimal finalAmount = totalPrice.subtract(discount);


        // 4. Tạo hóa đơn
        Invoices invoice = new Invoices();
        invoice.setStore(storeFromDb);  // Lưu trữ cửa hàng đã xác thực
        invoice.setCustomer(customer);
        invoice.setTotalPrice(totalPrice);
        invoice.setDiscount(discount);
        invoice.setFinalAmount(finalAmount);
        invoice.setNote(dto.getNote());
        invoice.setQuantity(invoice.getQuantity());
        invoice.setType(Invoices.InvoiceType.PURCHASE);
        invoice.setStatus("Unpaid");
        invoice.setIsDeleted(false);
        invoice.setCreatedBy(currentUser);

        Invoices savedInvoice = invoiceRepository.save(invoice);

        // 5. Tạo chi tiết hóa đơn và cập nhật kho (zone)
        updateInventory(dto.getDetails(), savedInvoice, currentUser);
        return savedInvoice;
    }

    private void updateInventory(List<InvoiceDetailDTO> details, Invoices savedInvoice, User currentUser) {
        for (InvoiceDetailDTO dto : details) {
            if (dto.getProductId() == null || dto.getZoneId() == null) {
                throw new IllegalArgumentException("Product ID và Zone ID không được để trống");
            }

            Product newProduct = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + dto.getProductId()));

            Zone zone = zoneRepository.findById(dto.getZoneId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khu vực với ID: " + dto.getZoneId()));

            // Nếu khu vực đã có sản phẩm khác, thì thay thế sản phẩm mới và cập nhật số lượng mới
            if (zone.getProduct() == null || !zone.getProduct().getId().equals(newProduct.getId())) {
                zone.setProduct(newProduct);
                zone.setQuantity(dto.getQuantity()); // Ghi đè số lượng mới
            } else {
                // Nếu sản phẩm trùng với sản phẩm đang có trong kho, thì cộng dồn số lượng
                zone.setQuantity(zone.getQuantity() + dto.getQuantity());
            }

            zoneRepository.saveAndFlush(zone); // Lưu cập nhật vào DB

            // Lưu chi tiết hóa đơn
            InvoicesDetails detail = new InvoicesDetails();
            detail.setInvoice(savedInvoice);
            detail.setProduct(newProduct);
            detail.setZone(zone);
            detail.setCustomer(savedInvoice.getCustomer());
            detail.setQuantity(dto.getQuantity());
            detail.setUnitPrice(dto.getUnitPrice());
            detail.setTotalPrice(dto.getUnitPrice().multiply(BigDecimal.valueOf(dto.getQuantity())));
            detail.setCreatedBy(currentUser);
            invoiceDetailsRepository.save(detail);
        }
    }



    private BigDecimal calculateTotalPrice(List<InvoiceDetailDTO> details) {
        return details.stream()
                .map(d -> d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Lấy thông tin người dùng hiện tại
    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByUsername(username);
        }
        return null;
    }
}
