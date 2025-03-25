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

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(required = false) String idMin ,
                               @RequestParam(required = false) String idMax ,
                               @RequestParam(required = false) String note ,
                               @RequestParam(required = false) String status ,
                               @RequestParam(required = false) String amountMin ,
                               @RequestParam(required = false) String amountMax ,
                               @RequestParam(required = false) String dateMin ,
                               @RequestParam(required = false) String dateMax ,
                               @RequestParam(required = false) String dateMin1 ,
                               @RequestParam(required = false) String dateMax1,
                               @RequestParam(required = false) String type) {
        Store store = (Store) session.getAttribute("store");
        User user = userService.getCurrentUser();
        model.addAttribute("user", user);
        model.addAttribute("store", store);
        // Chuyển đổi type thành Invoices.InvoiceType
        Invoices.InvoiceType invoiceType = null;
        if (type != null && !type.isEmpty()) {
            try {
                invoiceType = Invoices.InvoiceType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Có thể thêm thông báo lỗi nếu type không hợp lệ
                model.addAttribute("error", "Loại hóa đơn không hợp lệ: " + type);
            }
        }
        Long parsedIdMin = parseLong(idMin);
        Long parsedIdMax = parseLong(idMax);
        BigDecimal parsedAmountMin = parseBigDecimal(amountMin);
        BigDecimal parsedAmountMax = parseBigDecimal(amountMax);
        Date parsedDateMin = parseDate(dateMin);
        Date parsedDateMax = parseDate(dateMax);
        Date parsedDateMin1 = parseDate(dateMin1);
        Date parsedDateMax1 = parseDate(dateMax1);
        Pageable pageable = PageRequest.of(page, size);
        Page<Invoices> invoices = invoiceService.getFilter(store , parsedIdMin, parsedIdMax, note, status, parsedDateMin, parsedDateMax, pageable,  parsedDateMin1, parsedDateMax1 , parsedAmountMin , parsedAmountMax , invoiceType);
        model.addAttribute("invoices", invoices.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", invoices.getTotalPages());
        model.addAttribute("totalItems", invoices.getTotalElements());
        model.addAttribute("recordsPerPage", size);
        model.addAttribute("idMin", idMin);
        model.addAttribute("idMax", idMax);
        model.addAttribute("note", note);
        model.addAttribute("status", status);
        model.addAttribute("amountMin", amountMin);
        model.addAttribute("amountMax", amountMax);
        model.addAttribute("dateMin", dateMin);
        model.addAttribute("dateMax", dateMax);
        model.addAttribute("dateMin1", dateMin1);
        model.addAttribute("dateMax1", dateMax1);
        model.addAttribute("type", type);
        return "invoice";
    }
    private Long parseLong(String value) {
        if (value != null && !value.isEmpty()) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                return null; // Ignore invalid input
            }
        }
        return null;
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value != null && !value.isEmpty()) {
            try {
                return new BigDecimal(value);
            } catch (NumberFormatException e) {
                return null; // Ignore invalid input
            }
        }
        return null;
    }

    private Date parseDate(String value) {
        if (value != null && !value.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                return sdf.parse(value);
            } catch (ParseException e) {
                return null; // Ignore invalid input
            }
        }
        return null;
    }

    @GetMapping("/import")
    public String showImportForm(Model model, HttpSession session, String name) {
        model.addAttribute("invoice", new InvoicesDTO());
        Store store = (Store) session.getAttribute("store");
        model.addAttribute("store", store);
        User user = userService.getCurrentUser();
        model.addAttribute("user", user);
        model.addAttribute("customer", new Customer());
        model.addAttribute("product", new Product());
        model.addAttribute("zone", new Zone());
        return "import-invoice";
    }

    @GetMapping("/search-zones")
    @ResponseBody
    public List<ZoneDTO> getZonesByStoreId(@RequestParam("storeId") Long storeId) {
        List<Zone> zones = zoneRepository.findByStoreIdAndIsDeletedFalse(storeId);
        return zones.stream()
                .map(zone -> new ZoneDTO(zone.getId(), zone.getName()))
                .collect(Collectors.toList());
    }

    @GetMapping("/search-products")
    @ResponseBody
    public List<ProductDTO> searchProducts(@RequestParam String query) {
        List<Product> products = productRepository.searchProducts(query);
        return products.stream()
                .map(p -> new ProductDTO(p.getId(), p.getName(), p.getDescription(), p.getPrice()))
                .collect(Collectors.toList());
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
    public String getInvoiceDetail(@PathVariable Long id, Model model ) {
        Invoices invoices = invoicesRepository.findById(id).orElse(null);
        List<InvoicesDetails> invoicesDetails = invoiceDetailRepository.findByInvoice(invoices);
        for (InvoicesDetails detail : invoicesDetails) {
            Zone zone = detail.getZone();
            if (zone != null) {
                if (zone.getIsDeleted()) {
                    detail.setZoneName("Khu vực đã bị xóa");
                } else {
                    detail.setZoneName(zone.getName());
                }

            } else {
                detail.setZoneName("Khu vực đã bị xóa");
            }
        }
        model.addAttribute("invoiceDetails", invoicesDetails);
        model.addAttribute("invoice", invoices);
        User user = userService.getCurrentUser();
        model.addAttribute("user", user);
        Store store = (Store) model.getAttribute("store");
        model.addAttribute("store", store);
        return "invoice_detail";
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



