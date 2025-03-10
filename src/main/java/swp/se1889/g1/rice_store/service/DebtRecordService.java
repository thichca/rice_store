package swp.se1889.g1.rice_store.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import swp.se1889.g1.rice_store.entity.Customer;
import swp.se1889.g1.rice_store.entity.DebtRecords;
import swp.se1889.g1.rice_store.entity.User;
import swp.se1889.g1.rice_store.repository.CustomerRepository;
import swp.se1889.g1.rice_store.repository.DebtRecordRepository;
import swp.se1889.g1.rice_store.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;

@Service
public class DebtRecordService {

    //    @Autowired
//    private DebtRecordRepository debtRecordRepository;
    @Autowired private DebtRecordRepository debtRecordRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private UserRepository userRepository;


    //public Page<DebtRecords> getPage(Long customerId, int page, int size){
//        return debtRecordRepository.findDebtRecordsByPage(customerId , page, size);
//}
    // Phương thức thêm mới một bản ghi nợ
    public DebtRecords addDebt(DebtRecords debtRecord) {
        // return debtRecordRepository.save(debtRecord);
        User currentUser = getCurrentUser();
        if(currentUser == null) {
            throw new RuntimeException("Không tìm thấy thông tin người dùng");
        }
        //   DebtRecords newDebtRecord = new DebtRecords();
        debtRecord.setNote(debtRecord.getNote());
        debtRecord.setCreatedBy(currentUser);
        debtRecord.setCreatedAt(LocalDateTime.now());
        debtRecord.setUpdatedAt(LocalDateTime.now());
        debtRecordRepository.save(debtRecord);
        Optional<Customer> customerOpt = customerRepository.findById(debtRecord.getCustomerId());
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            updateDebtBalances(customer );
        } else {
            throw new RuntimeException("Không tìm thấy khách hàng để cập nhật số dư nợ!");
        }
        return  debtRecord;
    }

    // Phương thức lấy danh sách chi tiết nợ theo customer id
    public List<DebtRecords> getDebtDetailsByCustomerId(Long customerId ) {
        return debtRecordRepository.findByCustomerId(customerId );
    }

    private void updateDebtBalances(Customer customer ) {
        // Lấy tất cả các bản ghi nợ của khách hàng, không nhất thiết đã được sắp xếp
        List<DebtRecords> debtRecords = debtRecordRepository.findByCustomerId(customer.getId() );

        // Sử dụng PriorityQueue để sắp xếp các bản ghi theo thời gian tạo (createdAt)
        PriorityQueue<DebtRecords> queue = new PriorityQueue<>(
                (a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt())
        );
        queue.addAll(debtRecords);

        // Khởi tạo số dư nợ với BigDecimal để tránh sai số làm tròn
        BigDecimal debtBalance = BigDecimal.ZERO;

        // Duyệt qua các bản ghi theo thứ tự thời gian từ sớm nhất đến muộn nhất
        while (!queue.isEmpty()) {
            DebtRecords record = queue.poll();
            if (record.getType() == DebtRecords.DebtType.GHI_NO) {
                debtBalance = debtBalance.add(record.getAmount());
            } else if (record.getType() == DebtRecords.DebtType.TRA_NO) {
                debtBalance = debtBalance.subtract(record.getAmount());
            }
        }

        // Cập nhật số dư nợ cho khách hàng và lưu lại vào cơ sở dữ liệu
        customer.setDebtBalance(debtBalance);
        customerRepository.save(customer);
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByUsername(username);
        }
        return null;
    }

}

