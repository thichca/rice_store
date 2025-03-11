package swp.se1889.g1.rice_store.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import swp.se1889.g1.rice_store.dto.EmployeeDTO;
import swp.se1889.g1.rice_store.entity.Store;
import swp.se1889.g1.rice_store.entity.User;
import swp.se1889.g1.rice_store.service.EmployeeService;

import java.util.List;

@Controller
@RequestMapping("/employees")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private StoreController storeController;

    @GetMapping("")
    public String employee(@RequestParam(value = "trangthai", required = false) String trangthai, Model model, HttpSession session) {
        Store store = (Store) session.getAttribute("store");
        if (store == null) {
            return "redirect:/store";
        }

        List<User> employees;
        if (trangthai == null || trangthai.equals("working")) {
            employees = employeeService.getEmployeesActive(store.getId(), "ROLE_EMPLOYEE", false);
        } else {
            employees = employeeService.getEmployeesActive(store.getId(), "ROLE_EMPLOYEE", true);
        }

        model.addAttribute("employees", employees);
        model.addAttribute("store", store);
        model.addAttribute("trangthai", trangthai);
        return "employees";
    }
    @GetMapping("/{id}")
    public String deleteOrRestoreEmployee(RedirectAttributes redirectAttributes, @PathVariable("id") Long id, Model model, HttpSession session) {
        Store store = (Store) session.getAttribute("store");

        if (store == null) {
            return "redirect:/store";
        }

        boolean check = employeeService.getEmployeeById(id).isDeleted();

        employeeService.deleteAndRestoreEmployee(id);
        model.addAttribute("store", store);
        if (!check) {
            redirectAttributes.addFlashAttribute("success", "Xóa nhân viên thành công!");
        } else {
            redirectAttributes.addFlashAttribute("success", "Khôi phục nhân viên thành công!");
        }

        return "redirect:/employees";
    }


    //page add employee
    @GetMapping("/addEmployee")
    public String addEmployee(Model model, HttpSession session) {
        Store store = (Store) session.getAttribute("store");
        if (store == null) {
            return "redirect:/store";
        }
        model.addAttribute("newEmployee", new EmployeeDTO());
        model.addAttribute("store", store);
        return "addEmployee";
    }

    @PostMapping("/addEmployee")
    public String addEmployee (@ModelAttribute @Valid EmployeeDTO employeeDTO, BindingResult bindingResult, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Store store = (Store) session.getAttribute("store");
        if (store == null) {
            return "redirect:/store";
        }

        if (bindingResult.hasErrors()) {
            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                redirectAttributes.addFlashAttribute("error", fieldError.getDefaultMessage());
            }
            return "redirect:/employees/addEmployee";
        }

        try {
            User user = employeeService.addNewEmployee(store.getId(), employeeDTO, redirectAttributes);
            if (user == null) {
                return "redirect:/employees/addEmployee";
            }
            model.addAttribute("store", store);
            redirectAttributes.addFlashAttribute("success", "Thêm nhân viên thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra, vui lòng thử lại!");
        }
        return "redirect:/employees/addEmployee";
    }
}
