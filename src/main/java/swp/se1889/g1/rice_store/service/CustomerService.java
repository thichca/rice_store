package swp.se1889.g1.rice_store.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import swp.se1889.g1.rice_store.dto.CustomerDTO;
import swp.se1889.g1.rice_store.entity.Customer;
import swp.se1889.g1.rice_store.entity.User;
import swp.se1889.g1.rice_store.repository.CustomerRepository;
import swp.se1889.g1.rice_store.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;
    // Lấy tổng số khách hàng theo user hiện tại
    public long countCustomersByCurrentUser() {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            return customerRepository.countByCreatedBy(currentUser);
        }
        return 0;
    }


    public Page<CustomerDTO> getCustomersByCurrentUser(int page, int size) {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            return customerRepository.findByCurrentUser(currentUser, pageable);
        }
        return Page.empty();
    }
    public Page<CustomerDTO> filterCustomers(String id, String name, String phone,
                                             String address, String email, String debt,
                                             LocalDate createdDate, LocalDate updatedDate,
                                             int page, int size) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Page.empty();
        }

        Long parsedId = null;
        if (id != null && !id.isBlank()) {
            try {
                parsedId = Long.parseLong(id);
            } catch (NumberFormatException e) {
                throw new RuntimeException("ID không hợp lệ.");
            }
        }

        BigDecimal parsedDebt = null;
        if (debt != null && !debt.isBlank()) {
            try {
                parsedDebt = new BigDecimal(debt);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Dư nợ không hợp lệ.");
            }
        }

        // ✅ Chuyển LocalDate thành khoảng thời gian để so sánh chính xác 1 ngày
        LocalDateTime createdFrom = createdDate != null ? createdDate.atStartOfDay() : null;
        LocalDateTime createdTo = createdDate != null ? createdDate.plusDays(1).atStartOfDay() : null;

        LocalDateTime updatedFrom = updatedDate != null ? updatedDate.atStartOfDay() : null;
        LocalDateTime updatedTo = updatedDate != null ? updatedDate.plusDays(1).atStartOfDay() : null;

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return customerRepository.filterByFields(
                currentUser.getId(), parsedId, name, phone, address, email, parsedDebt,
                createdFrom, createdTo, updatedFrom, updatedTo, pageable
        );
    }




    public CustomerDTO getCustomerById(Long id) {
        Optional<Customer> customerOpt = customerRepository.findById(id);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            return new CustomerDTO(
                    customer.getId(),
                    customer.getName(),
                    customer.getPhone(),
                    customer.getAddress(),
                    customer.getEmail(),
                    customer.getDebtBalance(),
                    customer.getCreatedBy().getUsername(), // Lấy username của người tạo
                    customer.getUpdatedBy() // Lấy username của người sửa (có thể null)
            );
        }
        throw new RuntimeException("Không tìm thấy khách hàng có ID: " + id);
    }

    // 🟢 Cập nhật thông tin khách hàng (Thêm updatedBy)
    public void updateCustomer(CustomerDTO customerDTO) {
        Optional<Customer> customerOpt = customerRepository.findById(customerDTO.getId());
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();

            // ✅ Kiểm tra trùng số điện thoại với khách hàng khác
            List<Customer> samePhone = customerRepository.findByPhone(customerDTO.getPhone());
            boolean phoneExists = samePhone.stream().anyMatch(c -> !c.getId().equals(customer.getId()));
            if (phoneExists) {
                throw new RuntimeException("Số điện thoại đã tồn tại, vui lòng nhập số khác.");
            }

            // ✅ Kiểm tra trùng email với khách hàng khác
            List<Customer> sameEmail = customerRepository.findByemail(customerDTO.getEmail());
            boolean emailExists = sameEmail.stream().anyMatch(c -> !c.getId().equals(customer.getId()));
            if (emailExists) {
                throw new RuntimeException("Email đã tồn tại, vui lòng nhập email khác.");
            }

            customer.setName(customerDTO.getName());
            customer.setPhone(customerDTO.getPhone());
            customer.setAddress(customerDTO.getAddress());
            customer.setEmail(customerDTO.getEmail());
            customer.setDebtBalance(customerDTO.getDebtBalance());
            customer.setUpdatedAt(LocalDateTime.now());

            User currentUser = getCurrentUser();
            if (currentUser != null) {
                customer.setUpdatedBy(currentUser.getUsername());
            }

            customerRepository.save(customer);
        } else {
            throw new RuntimeException("Không tìm thấy khách hàng để cập nhật!");
        }
    }


    // 🟢 Tạo khách hàng mới (Đảm bảo có updatedBy khi tạo)
    public void createCustomer(CustomerDTO customerDTO) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Không thể xác định người dùng hiện tại.");
        }

        // Kiểm tra số điện thoại đã tồn tại hay chưa
        List<Customer> existingCustomers = customerRepository.findByPhone(customerDTO.getPhone());
        if (!existingCustomers.isEmpty()) {
            throw new RuntimeException("Số điện thoại đã tồn tại, vui lòng nhập số khác.");
        }
        List<Customer> existingCustom1 = customerRepository.findByemail(customerDTO.getEmail());
        if (!existingCustom1.isEmpty()) {
            throw new RuntimeException("Email đã tồn tại, vui lòng nhập email khác.");
        }

        Customer customer = new Customer();
        customer.setName(customerDTO.getName());
        customer.setPhone(customerDTO.getPhone());
        customer.setAddress(customerDTO.getAddress());
        customer.setEmail(customerDTO.getEmail());
        customer.setDebtBalance(customerDTO.getDebtBalance());
        customer.setCreatedBy(currentUser);
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());

        // ✅ Khi tạo, "Người sửa" cũng là người tạo
//        customer.setUpdatedBy(currentUser.getUsername());

        customerRepository.save(customer);
    }

    // 🟢 Lấy thông tin người dùng hiện tại từ SecurityContext
    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByUsername(username);
        }
        return null;
    }



    public List<CustomerDTO> searchCustomers(String query) {
        return customerRepository.searchCustomerDetails(query);
    }

    public Customer findCustomerById(Long id) {
        return customerRepository.findById(id).get();
    }

    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }
}
