package swp.se1889.g1.rice_store.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
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

    public List<Customer> getCustomersByStore(Long storeId) {
        return customerRepository.findByStoreIdOrderByIdDesc(storeId);
    }

    public void saveCustomer(Customer customer) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            String username = ((UserDetails) authentication.getPrincipal()).getUsername();
            User currentUser = userRepository.findByUsername(username);
            if (currentUser != null) {
                customer.setCreatedBy(currentUser.getUsername());
            } else {
                customer.setCreatedBy("Hệ thống");
            }
        } else {
            customer.setCreatedBy("Hệ thống");
        }

        customer.setDebtBalance(0.0);
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        customerRepository.save(customer);
    }
    public void updateCustomer(Customer customer) {
        Optional<Customer> existingCustomerOpt = customerRepository.findById(customer.getId());
        if (existingCustomerOpt.isPresent()) {
            Customer existingCustomer = existingCustomerOpt.get();
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                String username = ((UserDetails) authentication.getPrincipal()).getUsername();

                User currentUser = userRepository.findByUsername(username);
                if (currentUser != null) {
                    existingCustomer.setUpdatedBy(currentUser.getUsername()); // Gán User vào updatedBy
                } else {
                    existingCustomer.setUpdatedBy("Hệ thống");
                }
            } else {
                existingCustomer.setUpdatedBy("Hệ thống");
            }
            existingCustomer.setName(customer.getName());
            existingCustomer.setPhone(customer.getPhone());
            existingCustomer.setAddress(customer.getAddress());
            existingCustomer.setDebtBalance(customer.getDebtBalance());
            existingCustomer.setUpdatedAt(LocalDateTime.now());

            customerRepository.save(existingCustomer);
        }
    }
    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

}


