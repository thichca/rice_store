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
import swp.se1889.g1.rice_store.service.CustomerService;

import jakarta.validation.Valid;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Controller
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    // Hiển thị danh sách khách hàng của người dùng đăng nhập
    @GetMapping("/customers")
    public String getCustomers(Model model, HttpSession session) {
        if (!model.containsAttribute("newCustomer")) {
            model.addAttribute("newCustomer", new CustomerDTO());  // Đảm bảo không bị null
        }
        Store store = (Store) session.getAttribute("store");
        model.addAttribute("store", store);
        model.addAttribute("customers", customerService.getCustomersByCurrentUser());
        model.addAttribute("editCustomer", new CustomerDTO());

        return "customers";
    }


    // Xử lý thêm khách hàng mới
    @PostMapping("/customers/add")
    @ResponseBody
    public ResponseEntity<?> addCustomer(@Valid @ModelAttribute("newCustomer") CustomerDTO customerDTO,
                                         BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);  // Trả về lỗi dưới dạng JSON
        }

        try {
            customerService.createCustomer(customerDTO);
            return ResponseEntity.ok().body(Collections.singletonMap("message", "Thêm khách hàng thành công!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("errorMessage", e.getMessage()));
        }
    }



    // API lấy thông tin khách hàng để chỉnh sửa
    @GetMapping("/edit-customer/{id}")
    @ResponseBody
    public CustomerDTO getCustomerForEdit(@PathVariable Long id) {
        return customerService.getCustomerById(id);
    }
    @PostMapping("/customers/update")
    public String updateCustomer(@Valid @ModelAttribute("editCustomer") CustomerDTO customerDTO, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("customers", customerService.getCustomersByCurrentUser());
            return "customers";
        }
        customerService.updateCustomer(customerDTO);
        return "redirect:/customers";
    }

}
