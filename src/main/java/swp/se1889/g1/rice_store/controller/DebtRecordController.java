//package swp.se1889.g1.rice_store.controller;
//
////import swp.se1889.g1.rice_store.entity.DebtsRecord;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.server.ResponseStatusException;
//import swp.se1889.g1.rice_store.dto.CustomerDTO;
//import swp.se1889.g1.rice_store.entity.DebtRecords;
//import swp.se1889.g1.rice_store.service.DebtRecordService;
//
//import java.math.BigDecimal;
//import java.util.List;
//
//@Controller
//@RequestMapping("/debt")
//public class DebtRecordController {
//
//    @Autowired
//    private DebtRecordService debtRecordService;
//    @Autowired private swp.se1889.g1.rice_store.service.CustomerService customerService;
//    // Hiển thị trang form thêm phiếu nợ cho customer cụ thể
//    @GetMapping("/add")
//    public String showAddDebtForm(@RequestParam("customerId") Long customerId, Model model) {
//        CustomerDTO customer = customerService.getCustomerById(customerId);
//        if (customer == null) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
//        }
//        DebtRecords debtRecord = new DebtRecords();
//        debtRecord.setCustomerId(customerId);
//
//        model.addAttribute("debtRecord", debtRecord);
//        model.addAttribute("customer", customer); // Add customer to the model
//
//        return "addDebt";
//    }
//
//    // Xử lý submit form thêm phiếu nợ
//    @PostMapping("/add")
//    public String addDebtRecord(@ModelAttribute("debtRecord") DebtRecords debtRecord , Model model) {
//          if(debtRecord.getAmount() == null || debtRecord.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
//              model.addAttribute("error", "Số tiền nợ phải lớn hơn 0");
//              model.addAttribute("customer", customerService.getCustomerById(debtRecord.getCustomerId()));
//              return "addDebt";
//          }
//        debtRecordService.addDebt(debtRecord);
//        // Sau khi thêm, chuyển hướng về trang danh sách khách hàng hoặc trang chi tiết phiếu nợ của customer đó
//        return "redirect:/customers";
//    }
//
//    // Hiển thị trang chi tiết phiếu nợ theo customer id
//    @GetMapping("/detail")
//    public String showDebtDetail(@RequestParam("customerId") Long customerId, Model model) {
//        CustomerDTO customer = customerService.getCustomerById(customerId);
//        List<DebtRecords> debtRecords = debtRecordService.getDebtDetailsByCustomerId(customerId);
//        model.addAttribute("debtRecords", debtRecords);
//        model.addAttribute("customer", customer);
//        return "debtDetail"; // file debtDetail.html nằm trong thư mục templates
//    }
//}
