package swp.se1889.g1.rice_store.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import swp.se1889.g1.rice_store.entity.Customer;
import swp.se1889.g1.rice_store.entity.Store;
import swp.se1889.g1.rice_store.repository.StoreRepository;
import swp.se1889.g1.rice_store.repository.UserRepository;
import swp.se1889.g1.rice_store.service.CustomerService;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;
    @Autowired
    private StoreRepository storeRepository;


    @GetMapping
    public String listCustomers(Model model, HttpSession session) {
        Store store = (Store) session.getAttribute("store");

        if (store == null) {
            return "redirect:/store";
        }
        List<Customer> customers = customerService.getCustomersByStore(store.getId());
        model.addAttribute("customers", customers);
        model.addAttribute("store", store);
        return "customers";
    }

    @GetMapping("/add")
    public String showAddCustomerForm(Model model, HttpSession session) {
        Store store = (Store) session.getAttribute("store");

        if (store == null) {
            return "redirect:/store";
        }
        Optional<Store> storeOpt = storeRepository.findById(store.getId());
        if (storeOpt.isPresent()) {
            model.addAttribute("store", storeOpt.get());
            model.addAttribute("customer", new Customer());
            return "add-customer";
        }
        return "redirect:/customers";
    }

    @PostMapping("/add")
    public String addCustomer(@ModelAttribute("customer") @Valid Customer customer,
                              BindingResult result,
                              Model model,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Store store = (Store) session.getAttribute("store");
        if (store == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần chọn cửa hàng trước khi thêm khách hàng.");
            return "redirect:/store";
        }
        Optional<Store> storeOpt = storeRepository.findById(store.getId());
        if (storeOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cửa hàng không tồn tại hoặc đã bị xóa.");
            return "redirect:/customers";
        }
        if (result.hasErrors()) {
            model.addAttribute("store", storeOpt.get());
            return "add-customer";
        }
        customer.setStoreId(storeOpt.get().getId());
        customerService.saveCustomer(customer);
        redirectAttributes.addFlashAttribute("successMessage", "Khách hàng đã được thêm thành công.");
        return "redirect:/customers";
    }
    @GetMapping("/edit/{id}")
    public String showEditCustomerForm(@PathVariable("id") Long customerId, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Store store = (Store) session.getAttribute("store");

        if (store == null) {
            return "redirect:/store";
        }
        Optional<Customer> customerOpt = customerService.getCustomerById(customerId);
        if (customerOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Khách hàng không tồn tại.");
            return "redirect:/customers";
        }
        Customer customer = customerOpt.get();
        if (!customer.getStoreId().equals(store.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Khách hàng không thuộc cửa hàng này.");
            return "redirect:/customers";
        }

        model.addAttribute("store", store);
        model.addAttribute("customer", customer);
        return "edit-customer";
    }

    @PostMapping("/edit")
    public String updateCustomer(@ModelAttribute("customer") @Valid Customer customer,
                                 BindingResult result,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        Store store = (Store) session.getAttribute("store");

        if (store == null) {
            return "redirect:/store";
        }
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin.");
            return "redirect:/customers/edit/" + customer.getId();
        }
        Optional<Customer> existingCustomerOpt = customerService.getCustomerById(customer.getId());
        if (existingCustomerOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Khách hàng không tồn tại.");
            return "redirect:/customers";
        }

        Customer existingCustomer = existingCustomerOpt.get();
        if (!existingCustomer.getStoreId().equals(store.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Khách hàng không thuộc cửa hàng này.");
            return "redirect:/customers";
        }
        existingCustomer.setName(customer.getName());
        existingCustomer.setPhone(customer.getPhone());
        existingCustomer.setAddress(customer.getAddress());
        existingCustomer.setDebtBalance(customer.getDebtBalance());

        customerService.updateCustomer(existingCustomer);

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật khách hàng thành công.");
        return "redirect:/customers";
    }






}
