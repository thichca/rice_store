package swp.se1889.g1.rice_store.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import swp.se1889.g1.rice_store.entity.Customer;
import swp.se1889.g1.rice_store.entity.DebtRecord;
import swp.se1889.g1.rice_store.entity.Store;
import swp.se1889.g1.rice_store.repository.CustomerRepository;
import swp.se1889.g1.rice_store.repository.StoreRepository;
import swp.se1889.g1.rice_store.service.DebtService;
import jakarta.validation.Valid;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/debt")
public class DebtController {
@Autowired
private StoreRepository storeRepository;
    @Autowired
    private DebtService debtService;

    @Autowired
    private CustomerRepository customerRepository;

    // Hiển thị form thêm nợ
    @GetMapping("/add")
    public String showAddDebtForm(@RequestParam("customerId") Long customerId, Model model, HttpSession session) {
        Store store = (Store) session.getAttribute("store");

        Customer customer = customerRepository.findByIdAndStoreId(customerId, store.getId());
        if (customer != null) {
            model.addAttribute("store", store);
            model.addAttribute("customer", customer);
            model.addAttribute("debtRecord", new DebtRecord());
            return "debt/addDebt";
        }
        return "redirect:/customers";
    }

    // Xử lý thêm nợ
    @PostMapping("/add")
    public String addDebt(@RequestParam("customerId") Long customerId,
                          // BindingResult result,
                          Model model,
                          //      RedirectAttributes redirectAttributes,
                          @RequestParam("amount") double amount,
                          @RequestParam("type") DebtRecord.DebtType type,
                          HttpSession session, RedirectAttributes redirectAttributes) {
        Store store = (Store) session.getAttribute("store");
       if(amount < 0  ){
          redirectAttributes.addFlashAttribute("error", "Số tiền nợ không hợp lệ");
           return "redirect:/debt/add?customerId=" + customerId;
      }
        Customer customer = customerRepository.findByIdAndStoreId(customerId, store.getId());
     //   model.addAttribute("store", store);
        if (customer != null) {
          DebtRecord debtRecord = new DebtRecord();
            debtRecord.setStoreId(store.getId());
            debtRecord.setCustomerId(customerId);
            debtRecord.setAmount(amount);
            debtRecord.setType(type);
            // Các thông tin khác (ngày tạo, người tạo,...) sẽ được set trong service
            debtService.saveDebtRecord(debtRecord, customerId);
           redirectAttributes.addFlashAttribute("success", "Thêm nợ thành công");
            return "redirect:/debt/detail?customerId=" + customerId;
        }
        return "redirect:/customers";
    }

    // Hiển thị danh sách nợ của khách hàng theo storeId
    @GetMapping("/detail")
    public String debtDetails(@RequestParam("customerId") Long customerId, Model model, HttpSession session) {
        Store store = (Store) session.getAttribute("store");

        List<DebtRecord> debts = debtService.getDebtRecordsByCustomerAndStore(customerId, store.getId());
        model.addAttribute("debts", debts);
        model.addAttribute("customerId", customerId);
        return "debt/detailDebt"; // view hiển thị chi tiết nợ
    }

}
