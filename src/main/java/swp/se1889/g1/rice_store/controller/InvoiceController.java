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
import swp.se1889.g1.rice_store.dto.*;
import swp.se1889.g1.rice_store.entity.*;
import swp.se1889.g1.rice_store.repository.*;
import swp.se1889.g1.rice_store.service.InvoicesService;
import swp.se1889.g1.rice_store.service.Iservice.UserService;
import swp.se1889.g1.rice_store.service.UserServiceIpml;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/owner/invoices")
public class InvoiceController {
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final InvoicesService invoiceService;
    private final ZoneRepository zoneRepository;
    private final InvoiceDetailRepository invoiceDetailRepository;
    private final UserServiceIpml userService;
    private final InvoicesRepository invoicesRepository;


    @Autowired
    public InvoiceController(ProductRepository productRepository, CustomerRepository customerRepository, InvoicesService invoiceService, ZoneRepository zoneRepository, InvoiceDetailRepository invoiceDetailRepository
            , UserServiceIpml userService, InvoicesRepository invoicesRepository) {
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.invoiceService = invoiceService;
        this.zoneRepository = zoneRepository;
        this.invoiceDetailRepository = invoiceDetailRepository;
        this.userService = userService;
        this.invoicesRepository = invoicesRepository;
    }

    @GetMapping
    public String listInvoices(Model model, HttpSession session,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size) {
        Store store = (Store) session.getAttribute("store");
        User user = userService.getCurrentUser();
        model.addAttribute("user", user);
        model.addAttribute("store", store);
        Pageable pageable = PageRequest.of(page, size);
        Page<Invoices> invoices = invoicesRepository.findInvoicesByStore(store, pageable);
        model.addAttribute("invoices", invoices.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", invoices.getTotalPages());
        model.addAttribute("totalItems", invoices.getTotalElements());
        model.addAttribute("recordsPerPage", size);
        return "invoice";
    }


    @GetMapping("/import")
    public String showImportForm(Model model, HttpSession session, String name) {
        model.addAttribute("invoice", new InvoicesDTO());
        Store store = (Store) session.getAttribute("store");
        model.addAttribute("store", store);
        model.addAttribute("customer", new Customer());
        model.addAttribute("product", new Product());
        model.addAttribute("zone", new Zone());
        return "import-invoice";
    }

    @GetMapping("/zones")
    @ResponseBody
    public List<Zone> getZonesByStoreId(@RequestParam("storeId") Long storeId) {
        return zoneRepository.findByStoreIdAndIsDeletedFalse(storeId);
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
        Invoices invoices = invoiceService.createImportInvoice(invoiceDTO, store);
        if (invoices != null) {
            model.addAttribute("success", "Lưu thành công");
        }
        return "redirect:/owner/invoices";
    }

    @GetMapping("/detail/{id}")
    public String getInvoiceDetail(@PathVariable Long id, Model model) {
        try {
            // Lấy hóa đơn theo ID
            Optional<Invoices> optionalInvoice = invoicesRepository.findById(id);
            if (optionalInvoice.isEmpty()) {
                return "redirect:/owner/invoices"; // Quay lại danh sách nếu không tìm thấy hóa đơn
            }

            Invoices invoice = optionalInvoice.get();

            // Lấy danh sách chi tiết hóa đơn (Chỉ lấy Zone hợp lệ)
            List<InvoicesDetails> invoiceDetails = invoiceDetailRepository.findActiveInvoiceDetails(invoice);

            // Nếu danh sách chi tiết rỗng, có thể báo lỗi
            if (invoiceDetails.isEmpty()) {
                model.addAttribute("zoneError", "Hóa đơn không có sản phẩm hợp lệ do khu vực bị xóa.");
            }

            // Thêm dữ liệu vào model
            model.addAttribute("invoice", invoice);
            model.addAttribute("invoiceDetails", invoiceDetails);
            return "invoice_detail"; // Chuyển đến trang chi tiết

        } catch (Exception e) {
            model.addAttribute("zoneError", "Có lỗi xảy ra khi lấy thông tin hóa đơn.");
            return "invoice_detail";
        }
    }
    @GetMapping("/edit/{id}")
    public  String updateInvoices(
            @PathVariable Long id,
            Model model,
            HttpSession session
    ){
        User user = userService.getCurrentUser();
        model.addAttribute("user", user);
        Store store = (Store) session.getAttribute("store");
        model.addAttribute("store", store);
        Invoices invoice = invoicesRepository.findById(id).orElse(null);
        if (invoice == null) {
            return "redirect:/owner/invoices";
        }
        model.addAttribute("invoice", invoice);
        return "edit-invoice";

    }
    @PostMapping("/edit/{id}")
    public String updateInvoices(Model model, @PathVariable Long id, @ModelAttribute Invoices invoice, HttpSession session){
        Store store = (Store) session.getAttribute("store");
        model.addAttribute("store", store);
        Invoices invoices = invoiceService.update(id , invoice.getStatus());
        if (invoices != null) {
            model.addAttribute("success", "Cập nhật thành công");
        }
        return "redirect:/owner/invoices";
    }
}



