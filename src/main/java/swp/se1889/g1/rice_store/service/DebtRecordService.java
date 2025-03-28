package swp.se1889.g1.rice_store.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import swp.se1889.g1.rice_store.entity.Customer;
import swp.se1889.g1.rice_store.entity.DebtRecords;
import swp.se1889.g1.rice_store.entity.User;
import swp.se1889.g1.rice_store.repository.CustomerRepository;
import swp.se1889.g1.rice_store.repository.DebtRecordRepository;
import swp.se1889.g1.rice_store.repository.UserRepository;
import swp.se1889.g1.rice_store.specification.DebtRecordsSpecifications;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
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
    public Page<DebtRecords> getFilteredDebtRecords(Long customerId, Pageable pageable, Long idMin, Long idMax,
                                                    String note, String type, BigDecimal amountMin, BigDecimal amountMax,
                                                    Date dateMin, Date dateMax) {
        Specification<DebtRecords> spec = Specification.where(DebtRecordsSpecifications.hasCustomerId(customerId));
        if (idMin != null) {
            spec = spec.and(DebtRecordsSpecifications.idGreaterThanOrEqual(idMin));
        }
        if (idMax != null) {
            spec = spec.and(DebtRecordsSpecifications.idLessThanOrEqual(idMax));
        }
        if (note != null && !note.isEmpty()) {
            spec = spec.and(DebtRecordsSpecifications.noteContains(note));
        }
        if (type != null && !type.isEmpty()) {
            spec = spec.and(DebtRecordsSpecifications.hasType(type));
        }
        if (amountMin != null) {
            spec = spec.and(DebtRecordsSpecifications.amountGreaterThanOrEqual(amountMin));
        }
        if (amountMax != null) {
            spec = spec.and(DebtRecordsSpecifications.amountLessThanOrEqual(amountMax));
        }
        if (dateMin != null) {
            spec = spec.and(DebtRecordsSpecifications.createdAtAfter(dateMin));
        }
        if (dateMax != null) {
            spec = spec.and(DebtRecordsSpecifications.createdAtBefore(dateMax));
        }
        return debtRecordRepository.findAll(spec, pageable);
    }

    // Phương thức lấy danh sách chi tiết nợ theo customer id
    public List<DebtRecords> getDebtDetailsByCustomerId(Long customerId ) {
        return debtRecordRepository.findByCustomerId(customerId );
    }
     public Page<DebtRecords> getCustomerPage(Long customerId , Pageable pageable){
        return debtRecordRepository.findByCustomerId(customerId , pageable);
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

