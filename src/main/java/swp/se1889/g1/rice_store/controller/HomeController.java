package swp.se1889.g1.rice_store.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import swp.se1889.g1.rice_store.entity.Store;
import swp.se1889.g1.rice_store.entity.User;
import swp.se1889.g1.rice_store.service.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private StoreService storeService;

    @Autowired
    private UserServiceIpml userService;
    @Autowired
    private ProductService productService;

    @Autowired
    private CustomerService customerService;
    @Autowired
    private InvoiceService invoiceService;

    @PostMapping("/home")
    public String storeSelection(@RequestParam("storeName") String name,
                                 @RequestParam("createdBy") String createdBy,
                                 HttpSession session,
                                 Model model) {
        Store store = storeService.getStoreByNameAndCreatedBy(name, createdBy);
        session.setAttribute("store", store);
        model.addAttribute("store", store);
        User user = userService.getCurrentUser();
        long totalProducts = productService.countProductsByCurrentUser();
        long totalCustomers = customerService.countCustomersByCurrentUser();
        long totalInvoices = invoiceService.countInvoicesByUserAndStore(store.getId());
        BigDecimal totalRevenue = invoiceService.getTotalRevenueByUserAndStore(store.getId());

        model.addAttribute("user", user);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalCustomers", totalCustomers);
        model.addAttribute("totalInvoices", totalInvoices);
        model.addAttribute("totalRevenue", totalRevenue);
        List<BigDecimal> monthlyRevenue = invoiceService.getRevenueByMonth(store != null ? store.getId() : null);
        List<String> monthLabels = Arrays.asList("Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6");

        model.addAttribute("monthlyRevenue", monthlyRevenue);
        model.addAttribute("monthLabels", monthLabels);
        return "home";
    }

    @GetMapping("/home")
    public String getHome(HttpSession session, Model model) {
        Store store = (Store) session.getAttribute("store");
        model.addAttribute("store", store);

        User user = userService.getCurrentUser();
        model.addAttribute("user", user);

        long totalProducts = productService.countProductsByCurrentUser();
        long totalCustomers = customerService.countCustomersByCurrentUser();
        long totalInvoices = (store != null) ? invoiceService.countInvoicesByUserAndStore(store.getId()) : invoiceService.countInvoicesByCurrentUser();
        BigDecimal totalRevenue = (store != null) ? invoiceService.getTotalRevenueByUserAndStore(store.getId()) : invoiceService.getTotalRevenueByCurrentUser();

        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalCustomers", totalCustomers);
        model.addAttribute("totalInvoices", totalInvoices);
        model.addAttribute("totalRevenue", totalRevenue);

        // Lấy dữ liệu doanh thu theo tháng
        List<BigDecimal> monthlyRevenue = invoiceService.getRevenueByMonth(store != null ? store.getId() : null);
        List<String> monthLabels = Arrays.asList("Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6");

        model.addAttribute("monthlyRevenue", monthlyRevenue);
        model.addAttribute("monthLabels", monthLabels);

        return "home";
    }


    @GetMapping("/employee/home")
    public String getHomeEmployee(HttpSession session, Model model) {
        Long createdBy = userService.getCurrentCreatedBy();
        Store store = storeService.getStoreByCreatedBy(createdBy);
        model.addAttribute("store", store);
        session.setAttribute("store", store);
        User user = userService.getCurrentUser();
        model.addAttribute("user", user);
        return "home";
    }

    @GetMapping("/baseFE")
    public String getBaseFE(HttpSession session, Model model) {
        Store store = (Store) session.getAttribute("store");
        model.addAttribute("store", store);
        return "baseFE";
    }
}
