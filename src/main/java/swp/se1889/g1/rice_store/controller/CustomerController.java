package swp.se1889.g1.rice_store.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import swp.se1889.g1.rice_store.dto.CustomerDTO;
import swp.se1889.g1.rice_store.entity.Store;
import swp.se1889.g1.rice_store.entity.User;
import swp.se1889.g1.rice_store.service.CustomerService;

import jakarta.validation.Valid;
import swp.se1889.g1.rice_store.service.UserServiceIpml;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private UserServiceIpml userService;

    // 🟢 Hiển thị danh sách khách hàng
    @GetMapping("/customers")
    public String getCustomers(Model model, HttpSession session) {
        if (!model.containsAttribute("newCustomer")) {
            model.addAttribute("newCustomer", new CustomerDTO());
        }

        Store store = (Store) session.getAttribute("store");
        model.addAttribute("store", store);

        User user = userService.getCurrentUser();
        model.addAttribute("user", user);

        // ✅ Truyền danh sách khách hàng trực tiếp từ service
        model.addAttribute("customers", customerService.getCustomersByCurrentUser());
        model.addAttribute("editCustomer", new CustomerDTO());

        return "customers";
    }

    // 🟢 Xử lý thêm khách hàng mới
    @PostMapping("/customers/add")
    @ResponseBody
    public ResponseEntity<?> addCustomer(@Valid @ModelAttribute("newCustomer") CustomerDTO customerDTO,
                                         BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            customerService.createCustomer(customerDTO);
            return ResponseEntity.ok().body(Collections.singletonMap("message", "Thêm khách hàng thành công!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("errorMessage", e.getMessage()));
        }
    }

    // 🟢 API lấy thông tin khách hàng để chỉnh sửa
    @GetMapping("/edit-customer/{id}")
    @ResponseBody
    public ResponseEntity<?> getCustomerForEdit(@PathVariable Long id) {
        try {
            CustomerDTO customerDTO = customerService.getCustomerById(id);
            return ResponseEntity.ok(customerDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("errorMessage", e.getMessage()));
        }
    }

    // 🟢 Cập nhật khách hàng
    @PostMapping("/customers/update")
    public String updateCustomer(@Valid @ModelAttribute("editCustomer") CustomerDTO customerDTO,
                                 BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("customers", customerService.getCustomersByCurrentUser());
            return "customers";
        }

        try {
            customerService.updateCustomer(customerDTO);
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("customers", customerService.getCustomersByCurrentUser());
            return "customers";
        }

        return "redirect:/customers";
    }

    @GetMapping("/manageUser")
    public String GetViewManageUser(){
        return "manage-user";
    }
}
