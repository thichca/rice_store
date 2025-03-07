package swp.se1889.g1.rice_store.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import swp.se1889.g1.rice_store.dto.CustomerDTO;
import swp.se1889.g1.rice_store.entity.Customer;
import swp.se1889.g1.rice_store.entity.User;
import swp.se1889.g1.rice_store.repository.CustomerRepository;
import swp.se1889.g1.rice_store.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    // Lấy danh sách khách hàng của người dùng hiện tại
    public List<Customer> getCustomersByCurrentUser() {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            return customerRepository.findByCreatedBy(currentUser);
        }
        return List.of();
    }
    // Lấy thông tin khách hàng theo ID
    public CustomerDTO getCustomerById(Long id) {
        Optional<Customer> customerOpt = customerRepository.findById(id);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            CustomerDTO customerDTO = new CustomerDTO();
            customerDTO.setId(customer.getId());
            customerDTO.setName(customer.getName());
            customerDTO.setPhone(customer.getPhone());
            customerDTO.setAddress(customer.getAddress());
            customerDTO.setEmail(customer.getEmail());
            customerDTO.setDebtBalance(customer.getDebtBalance());
            return customerDTO;
        }
        throw new RuntimeException("Không tìm thấy khách hàng có ID: " + id);
    }

    // Cập nhật thông tin khách hàng
    public void updateCustomer(CustomerDTO customerDTO) {
        Optional<Customer> customerOpt = customerRepository.findById(customerDTO.getId());
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            customer.setName(customerDTO.getName());
            customer.setPhone(customerDTO.getPhone());
            customer.setAddress(customerDTO.getAddress());
            customer.setEmail(customerDTO.getEmail());
            customer.setDebtBalance(customerDTO.getDebtBalance());
            customer.setUpdatedAt(LocalDateTime.now());
            customerRepository.save(customer);
        } else {
            throw new RuntimeException("Không tìm thấy khách hàng để cập nhật!");
        }
    }

    // Tạo khách hàng mới
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

        Customer customer = new Customer();
        customer.setName(customerDTO.getName());
        customer.setPhone(customerDTO.getPhone());
        customer.setAddress(customerDTO.getAddress());
        customer.setEmail(customerDTO.getEmail());
        customer.setDebtBalance(customerDTO.getDebtBalance());
        customer.setCreatedBy(currentUser);
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());

        customerRepository.save(customer);
    }



    // Lấy thông tin người dùng hiện tại từ SecurityContext
    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByUsername(username);
        }
        return null;
    }
}
