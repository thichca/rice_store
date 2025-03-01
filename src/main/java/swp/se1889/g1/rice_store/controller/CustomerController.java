package swp.se1889.g1.rice_store.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import swp.se1889.g1.rice_store.dto.CustomerDTO;
import swp.se1889.g1.rice_store.service.CustomerService;

import jakarta.validation.Valid;

@Controller
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    // Hiển thị danh sách khách hàng của người dùng đăng nhập
    @GetMapping("/customers")
    public String getCustomers(Model model) {
        model.addAttribute("customers", customerService.getCustomersByCurrentUser());
//        model.addAttribute("customerDTO", new CustomerDTO());
        model.addAttribute("newCustomer", new CustomerDTO());
        model.addAttribute("editCustomer", new CustomerDTO());

        return "customers";
    }

    // Xử lý thêm khách hàng mới
    @PostMapping("/customers/add")
    public String addCustomer(@Valid @ModelAttribute("customerDTO") CustomerDTO customerDTO, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("customers", customerService.getCustomersByCurrentUser());
            return "customers";
        }

        customerService.createCustomer(customerDTO);
        return "redirect:/customers";
    }
    // API lấy thông tin khách hàng để chỉnh sửa
    @GetMapping("/edit/{id}")
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
