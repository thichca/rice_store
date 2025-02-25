package swp.se1889.g1.rice_store.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import swp.se1889.g1.rice_store.entity.User;
import swp.se1889.g1.rice_store.repository.EmployeeManagementRepository;
import swp.se1889.g1.rice_store.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeManagementService {

    @Autowired
    private EmployeeManagementRepository employeeManagementRepository;

    private UserRepository userRepository;

    public List<User> getEmployeesByStore(Long storeId) {
        return employeeManagementRepository.findByStoreIdAndRole(storeId, "ROLE_EMPLOYEE");
    }

    public Optional<User> getEmployeeById(Long id) {
        return employeeManagementRepository.findById(id);
    }


    public Page<User> getEmployeesForCurrentUser(int page, int size, String search, Long store_id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = employeeManagementRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (search != null && !search.isEmpty()) {
            return employeeManagementRepository.findByStoreIdAndRoleAndUsernameContainingIgnoreCase(
                    store_id, "ROLE_EMPLOYEE", search, PageRequest.of(page, size));
        }

        return employeeManagementRepository.findByStoreIdAndRole(store_id, "ROLE_EMPLOYEE", PageRequest.of(page, size));
    }
}
