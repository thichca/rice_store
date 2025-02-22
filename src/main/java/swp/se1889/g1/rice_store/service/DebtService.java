package swp.se1889.g1.rice_store.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import swp.se1889.g1.rice_store.entity.Customer;
import swp.se1889.g1.rice_store.entity.DebtRecord;
import swp.se1889.g1.rice_store.entity.User;
import swp.se1889.g1.rice_store.repository.CustomerRepository;
import swp.se1889.g1.rice_store.repository.DebtRepository;
import swp.se1889.g1.rice_store.repository.StoreRepository;
import swp.se1889.g1.rice_store.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;

@Service
public class DebtService {
    @Autowired
    private DebtRepository debtRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private UserRepository userRepository;

    public List<Customer> getCustomersByStore(Long storeId) {
        return customerRepository.findByStoreIdOrderByIdDesc(storeId);
    }

//    public List<DebtRecord> getDebtRecordsByStore(Long storeId) {
//        return debtRepository.findByStoreId(storeId);
//    }
    public List<DebtRecord> getDebtRecordsByCustomerAndStore(Long customerId, Long storeId) {
        return debtRepository.findByCustomerIdAndStoreId(customerId, storeId);
    }
    public Optional<DebtRecord> getDebtRecordById(Long id) {
        return debtRepository.findById(id);
    }


    public void saveDebtRecord(DebtRecord debtRecord , Long customerId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            String username = ((UserDetails) authentication.getPrincipal()).getUsername();
            User currentUser = userRepository.findByUsername(username);
            if (currentUser != null) {
                debtRecord.setCreatedBy(currentUser.getUsername());
            } else {
                debtRecord.setCreatedBy("Hệ thống");
            }
        } else {
            debtRecord.setCreatedBy("Hệ thống");
        }
        debtRecord.setCreatedAt(LocalDateTime.now());
        debtRecord.setUpdatedAt(LocalDateTime.now());
        debtRepository.save(debtRecord);
        Optional<Customer> customerOptional = customerRepository.findById(customerId);
        if (customerOptional.isPresent()) {
            updateDebtBalance(customerOptional.get());
        }

    }
    private void updateDebtBalance(Customer customer) {
        // Lấy tất cả bản ghi nợ của khách hàng, sắp xếp theo createdAt
        List<DebtRecord> debtRecords = debtRepository.findByCustomerIdOrderByCreatedAtAsc(customer.getId());
        double debtBalance = 0.0;
        // Dùng PriorityQueue để xử lý theo thứ tự thời gian
        PriorityQueue<DebtRecord> queue = new PriorityQueue<>((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));
        queue.addAll(debtRecords);
        while (!queue.isEmpty()) {
            DebtRecord record = queue.poll();
            if (record.getType() == DebtRecord.DebtType.GHI_NO) {
                debtBalance += record.getAmount(); // Nếu getAmount() trả về double, nếu không cần chuyển đổi
            } else if (record.getType() == DebtRecord.DebtType.TRA_NO) {
                debtBalance -= record.getAmount();
            }
        }

        // Cập nhật debtBalance cho khách hàng và lưu lại
        customer.setDebtBalance(debtBalance);
        customerRepository.save(customer);
    }

}
