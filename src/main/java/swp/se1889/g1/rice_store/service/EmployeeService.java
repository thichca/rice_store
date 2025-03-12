package swp.se1889.g1.rice_store.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import swp.se1889.g1.rice_store.dto.EmployeeDTO;
import swp.se1889.g1.rice_store.entity.User;
import swp.se1889.g1.rice_store.repository.EmployeeRepository;
import swp.se1889.g1.rice_store.service.Iservice.UserService;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {
    @Autowired
    private UserService userService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    //lấy danh sách nhân viên
    public List<User> getEmployees(Long storeId, String role) {
        return employeeRepository.findByCreatedByAndRole(storeId, role);
    }

    public List<User> getEmployeesActive(Long storeId, String role, boolean isDeleted) {
        return employeeRepository.findAllByCreatedByAndRoleAndIsDeleted(storeId, role, isDeleted);
    }

    //thêm tài khoản và thông tin employee
    public User addNewEmployee(Long storeId, EmployeeDTO employeeDTO, RedirectAttributes redirectAttributes) {
        if (employeeRepository.findByUsername(employeeDTO.getUserName()).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Tên đăng nhập đã tồn tại");
            return null;
        }
        if (employeeRepository.findByEmail(employeeDTO.getEmail()).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Email đã tồn tại");
            return null;
        }
        if (employeeRepository.findByPhone(employeeDTO.getPhone()).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "SĐT đã tồn tại");
            return null;
        }


        User user = new User();
        user.setUsername(employeeDTO.getUserName());
        user.setPassword(passwordEncoder.encode(employeeDTO.getPassword()));
        user.setEmail(employeeDTO.getEmail());
        user.setRole("ROLE_EMPLOYEE");
        user.setName(employeeDTO.getName());
        user.setAddress(employeeDTO.getAddress());
        user.setPhone(employeeDTO.getPhone());
        user.setNote(employeeDTO.getNote());
        user.setCreatedBy(storeId);


        try {
            employeeRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Error while saving employee: " + e.getMessage());
        }
        return user;
    }

    //lấy nhân viên theo id
    public User getEmployeeById(Long id) {
        return employeeRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    //xóa và khôi phục
    public void deleteAndRestoreEmployee(Long id) {
        User user = getEmployeeById(id);
        user.setDeleted(!user.isDeleted());
        employeeRepository.saveAndFlush(user);
    }


}
