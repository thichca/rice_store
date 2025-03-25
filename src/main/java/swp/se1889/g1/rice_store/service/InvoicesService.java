package swp.se1889.g1.rice_store.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import swp.se1889.g1.rice_store.dto.InvoiceDetailDTO;
import swp.se1889.g1.rice_store.dto.InvoicesDTO;
import swp.se1889.g1.rice_store.entity.*;
import swp.se1889.g1.rice_store.repository.*;
import swp.se1889.g1.rice_store.specification.InvoiceSpecifications;

import java.math.BigDecimal;
import java.util.*;

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
    public Page<Invoices> getPage(Store store , Pageable pageable){
        return invoiceRepository.findInvoicesByStore(store , pageable);
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
        // 4. Lấy thông tin về phương thức thanh toán và số tiền đã trả
        String paymentMethod = dto.getPaymentMethod();
        BigDecimal paidAmount = dto.getPaidAmount() != null ? dto.getPaidAmount() : BigDecimal.ZERO;
        // 5. Lấy nợ hiện tại của khách hàng
        BigDecimal debtBalance = customer.getDebtBalance() != null ? customer.getDebtBalance() : BigDecimal.ZERO;
        // 6. Tính toán nợ mới dựa trên phương thức thanh toán
        BigDecimal newDebtBalance;
        if ("productAndDebt".equals(paymentMethod)) {
            // Thanh toán tiền hàng + nợ: Trả cả hóa đơn và nợ cũ
            BigDecimal totalDue = finalAmount.add(debtBalance);
            if (paidAmount.compareTo(totalDue) < 0) {
                newDebtBalance = totalDue.subtract(paidAmount); // Còn nợ nếu trả chưa đủ
            } else {
                newDebtBalance = BigDecimal.ZERO; // Hết nợ nếu trả đủ hoặc dư
            }
        } else if ("onlyProduct".equals(paymentMethod)) {
            // Chỉ tiền hàng: Chỉ trả cho hóa đơn, nợ cũ giữ nguyên
            if (paidAmount.compareTo(finalAmount) < 0) {
                newDebtBalance = debtBalance.add(finalAmount.subtract(paidAmount)); // Tăng nợ nếu trả thiếu
            } else {
                newDebtBalance = debtBalance; // Nợ không đổi nếu trả đủ hoặc dư
            }
        } else {
            throw new RuntimeException("Phương thức thanh toán không hợp lệ");
        }
        // 7. Cập nhật nợ mới cho khách hàng
        customer.setDebtBalance(newDebtBalance);
        customerRepository.save(customer);
        // 4. Tạo hóa đơn
        Invoices invoice = new Invoices();
        invoice.setStore(storeFromDb);  // Lưu trữ cửa hàng đã xác thực
        invoice.setCustomer(customer);
        invoice.setTotalPrice(totalPrice);
        invoice.setDiscount(discount);
        invoice.setFinalAmount(finalAmount);
        invoice.setNote(dto.getNote());
        invoice.setQuantity(invoice.getQuantity());
        invoice.setType(Invoices.InvoiceType.Purchase);
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

            Zone zone = zoneRepository.findByIdAndIsDeletedFalse(dto.getZoneId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khu vực với ID: " + dto.getZoneId() + " hoặc đã bị xóa!"));

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
    public List<BigDecimal> getRevenueByMonth(Long storeId) {
        List<BigDecimal> monthlyRevenue = new ArrayList<>(Collections.nCopies(6, BigDecimal.ZERO));
        List<Object[]> results;

        if (storeId != null) {
            results = invoiceRepository.getRevenueByMonthAndStore(storeId);
        } else {
            results = invoiceRepository.getRevenueByMonth();
        }

        for (Object[] row : results) {
            int month = (int) row[0]; // Lấy tháng từ database
            BigDecimal revenue = (BigDecimal) row[1]; // Lấy doanh thu
            monthlyRevenue.set(month - 1, revenue); // Gán vào danh sách
        }

        return monthlyRevenue;
    }


    // Lấy tổng số hóa đơn theo User hiện tại
    public long countInvoicesByCurrentUser() {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            return invoiceRepository.countByCreatedBy(currentUser);
        }
        return 0;
    }

    // Lấy tổng số hóa đơn theo User + Store
    public long countInvoicesByUserAndStore(Long storeId) {
        User currentUser = getCurrentUser();
        if (currentUser != null && storeId != null) {
            return invoiceRepository.countByUserAndStore(currentUser, storeId);
        }
        return 0;
    }

    // Lấy tổng doanh thu theo User hiện tại
    public BigDecimal getTotalRevenueByCurrentUser() {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            return invoiceRepository.getTotalRevenueByUser(currentUser);
        }
        return BigDecimal.ZERO;
    }

    // Lấy tổng doanh thu theo User + Store
    public BigDecimal getTotalRevenueByUserAndStore(Long storeId) {
        User currentUser = getCurrentUser();
        if (currentUser != null && storeId != null) {
            return invoiceRepository.getTotalRevenueByUserAndStore(currentUser, storeId);
        }
        return BigDecimal.ZERO;
    }
    public Invoices update(Long id, String newStatus) {
        Invoices invoices = invoiceRepository.findById(id).orElse(null);
        if (invoices == null) {
            throw new RuntimeException("Không tìm thấy hóa đơn với ID: " + id);
        }
        // Chuyển đổi trạng thái thành giá trị hợp lệ
        if ("Paid".equalsIgnoreCase(newStatus)) {
            newStatus = "Paid";  // Hoặc giá trị hợp lệ trong database
        } else if ("Unpaid".equalsIgnoreCase(newStatus)) {
            newStatus = "Unpaid";
        } else {
            throw new IllegalArgumentException("Trạng thái không hợp lệ: " + newStatus);
        }
        invoices.setStatus(newStatus);
        return invoiceRepository.save(invoices);
    }
    public Page<Invoices> getFilter(Long idMin, Long idMax, String note, String status, Date dateMin, Date dateMax, Pageable pageable ,
                                    Date dateMin1, Date dateMax1,BigDecimal amountMin ,BigDecimal amountMax){
        Specification<Invoices> spec = Specification.where(null);
        if (idMin != null) {
            spec = spec.and(InvoiceSpecifications.idGreatThan(idMin));
        }
        if (idMax != null) {
            spec = spec.and(InvoiceSpecifications.idLessThan(idMax));
        }
        if (note != null && !note.isEmpty()) {
            spec = spec.and(InvoiceSpecifications.noteContains(note));
        }
        if (status != null && !status.isEmpty()) {
            spec = spec.and(InvoiceSpecifications.hasStatus(status));
        }
        if (amountMin != null) {
            spec = spec.and(InvoiceSpecifications.amountGreaterThanOrEqual(amountMin));
        }
        if (amountMax != null) {
            spec = spec.and(InvoiceSpecifications.amountLessThanOrEqual(amountMax));
        }
        if (dateMin != null) {
            spec = spec.and(InvoiceSpecifications.createdAtAfter(dateMin));
        }
        if (dateMax != null) {
            spec = spec.and(InvoiceSpecifications.createdAtBefore(dateMax));
        }
        if (dateMin1 != null) {
            spec = spec.and(InvoiceSpecifications.updatedAtAfter(dateMin1));
        }
        if (dateMax1 != null) {
            spec = spec.and(InvoiceSpecifications.updatedAtBefore(dateMax1));
        }
        return invoiceRepository.findAll(spec,pageable);
    }
}
