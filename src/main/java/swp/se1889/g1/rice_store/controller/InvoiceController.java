package swp.se1889.g1.rice_store.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import swp.se1889.g1.rice_store.dto.InvoiceDetailDTO;
import swp.se1889.g1.rice_store.dto.InvoicesDTO;
import swp.se1889.g1.rice_store.dto.ZoneDTO;
import swp.se1889.g1.rice_store.entity.*;
import swp.se1889.g1.rice_store.repository.*;
import swp.se1889.g1.rice_store.service.InvoicesService;
import swp.se1889.g1.rice_store.service.Iservice.UserService;
import swp.se1889.g1.rice_store.service.UserServiceIpml;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/owner/invoices")
public class InvoiceController {
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final InvoicesService invoiceService;
    private  final ZoneRepository zoneRepository;
    private final InvoiceDetailRepository invoiceDetailRepository;
    private final UserServiceIpml userService ;


    @Autowired
    public InvoiceController(ProductRepository productRepository, CustomerRepository customerRepository, InvoicesService invoiceService, ZoneRepository zoneRepository , InvoiceDetailRepository invoiceDetailRepository
    , UserServiceIpml userService) {
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.invoiceService = invoiceService;
        this.zoneRepository = zoneRepository;
       this.invoiceDetailRepository = invoiceDetailRepository;
        this.userService = userService;
    }
    @GetMapping
    public String listInvoices(Model model, HttpSession session ,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size , Invoices invoice) {
        Store store = (Store) session.getAttribute("store");
        User user = userService.getCurrentUser();
        model.addAttribute("user", user);
        model.addAttribute("store", store);
        Pageable pageable = PageRequest.of(page, size);
        Page<InvoicesDetails> invoices = invoiceDetailRepository.findAllByIsDeletedFalse(pageable);
        model.addAttribute("invoices", invoices.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", invoices.getTotalPages());
        model.addAttribute("totalItems", invoices.getTotalElements());
        model.addAttribute("recordsPerPage", size);
        return "invoice-detail";
    }


    @GetMapping("/import")
    public String showImportForm(Model model, HttpSession session , String name) {
        model.addAttribute("invoice", new InvoicesDTO());
        Store store = (Store) session.getAttribute("store");
        model.addAttribute("store", store);
        model.addAttribute("customer" , new Customer());
        model.addAttribute("product" , new Product());
        model.addAttribute("zone" , new Zone());
        return "import-invoice";
    }
    @GetMapping("/zones")
    @ResponseBody
    public List<ZoneDTO> getZonesByStoreId(@RequestParam("storeId") Long storeId) {
        List<Zone> zones = zoneRepository.findByStoreId(storeId);
        return zones.stream()
                .map(zone -> new ZoneDTO(zone.getId(), zone.getName()))
                .collect(Collectors.toList());
    }

    @GetMapping("/search-products")
    @ResponseBody
    public List<Product> searchProducts(@RequestParam String query) {
        return productRepository.findByNameContaining(query);
    }

    @GetMapping("/search-customer")
    @ResponseBody
    public Customer searchCustomer(@RequestParam String phone) {
        return customerRepository.findCustomerByPhone(phone);
    }

    @PostMapping("/import")
    public String saveInvoice(@ModelAttribute InvoicesDTO invoiceDTO,
                              Authentication authentication, HttpSession session, Model model) {
        String username = authentication.getName(); // Lấy tên người dùng đã đăng nhập
        Store store = (Store) session.getAttribute("store");
        model.addAttribute("store", store);
        // Xử lý logic lưu vào database thông qua service
        Invoices invoices = invoiceService.createImportInvoice(invoiceDTO , store);
        if(invoices !=null) {
            model.addAttribute("success", "Lưu thành công");
        }
        return "redirect:/owner/invoices";
    }

    }
