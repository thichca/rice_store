package swp.se1889.g1.rice_store.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import swp.se1889.g1.rice_store.entity.Store;
import swp.se1889.g1.rice_store.entity.User;
import swp.se1889.g1.rice_store.repository.UserRepository;
import swp.se1889.g1.rice_store.service.EmployeeManagementService;

import java.util.List;
import java.util.Optional;

@Controller
public class EmployeeManagementController {

    @Autowired
    private EmployeeManagementService employeeManagementService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/manage")
    public String listEmployees(Model model, HttpSession session) {
        Store store = (Store) session.getAttribute("store");

        if (store == null) {
            return "redirect:/store";
        }
        List<User> employees = employeeManagementService.getEmployeesByStore(store.getId());
        model.addAttribute("employees", employees);
        model.addAttribute("store", store);
        return "employeeManagement";
    }
    @PostMapping("/manage/{id}")
    public String deleteEmployee(@PathVariable Long id, HttpSession session) {
        Store store = (Store) session.getAttribute("store");

        if (store == null) {
            return "redirect:/store";
        }

        Optional<User> xoaUser = employeeManagementService.getEmployeeById(id);
        User user = xoaUser.get();
        user.setDelete(true);
        userRepository.saveAndFlush(user);
        return "redirect:/manage";
    }
    @GetMapping("/manage/{id}")
    public String restoreEmployee(@PathVariable Long id, HttpSession session) {
        Store store = (Store) session.getAttribute("store");

        if (store == null) {
            return "redirect:/store";
        }

        Optional<User> xoaUser = employeeManagementService.getEmployeeById(id);
        User user = xoaUser.get();
        user.setDelete(false);
        userRepository.saveAndFlush(user);
        return "redirect:/manage";
    }
    @GetMapping("/manage/add-employee")
    public String addEmployee(Model model, HttpSession session) {
        Store store = (Store) session.getAttribute("store");
        model.addAttribute("store", store);
        return "add-employee";
    }

    @PostMapping("/manage/add-employee")
    public String register(Model model, HttpSession session,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String email,
            @RequestParam String phone) {

        Store store = (Store) session.getAttribute("store");

        if (store == null) {
            return "redirect:/store";
        }
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setEmail(email);
        newUser.setRole("ROLE_EMPLOYEE");
        newUser.setStoreId(store.getId());
        newUser.setStoreName("");
        newUser.setAddress("");
        newUser.setPhone(phone);
        userRepository.save(newUser);

        return "redirect:/manage/add-employee";
    }
    //    @GetMapping("/manage")
//    public String employeeMain() {
//        return "employeeManagement";
//    }
//
//    @GetMapping("/employees")
//    public Page<User> getEmployeesForCurrentUser(@RequestParam(defaultValue = "0") int page,
//                                                 @RequestParam(defaultValue = "10") int size) {
//        return employeeManagementService.getEmployeesForCurrentUser(page, size);
//    }


//    @GetMapping("/manage")
//    public String getEmployeesForCurrentUser(@RequestParam(defaultValue = "0") int page,
//                                             @RequestParam(defaultValue = "2") int size,
//                                             @RequestParam(required = false) String search,
//                                             HttpSession session,
//                                             Model model) {
//
//        Store store = (Store) session.getAttribute("store");
//
//
//        Page<User> employees = employeeManagementService.getEmployeesForCurrentUser(page, size, search, store.getId());
//
//        model.addAttribute("employees", employees.getContent());
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", employees.getTotalPages());
//        model.addAttribute("search", search);
//        model.addAttribute("store", store);
//
//        return "employeeManagement";
//    }
}