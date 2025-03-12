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
import swp.se1889.g1.rice_store.service.UserServiceIpml;

import java.util.List;

@Controller
@RequestMapping("/owner/employees")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private UserServiceIpml userService;

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
        User user = userService.getCurrentUser();
        model.addAttribute("user", user);

        model.addAttribute("employees", employees);
        model.addAttribute("store", store);
        model.addAttribute("trangthai", trangthai);
        return "employees";
    }

    @GetMapping("/{id}")
    public String deleteOrRestoreEmployee(RedirectAttributes redirectAttributes, @PathVariable("id") Long id, Model model, HttpSession session) {
        Store store = (Store) session.getAttribute("store");

        if (store == null) {
            return "redirect:/owner/store";
        }

        boolean check = employeeService.getEmployeeById(id).isDeleted();

        employeeService.deleteAndRestoreEmployee(id);
        model.addAttribute("store", store);
        if (!check) {
            redirectAttributes.addFlashAttribute("success", "Xóa nhân viên thành công!");
        } else {
            redirectAttributes.addFlashAttribute("success", "Khôi phục nhân viên thành công!");
        }

        return "redirect:/owner/employees";
    }


    //page add employee
    @GetMapping("/addEmployee")
    public String addEmployee(Model model, HttpSession session) {
        Store store = (Store) session.getAttribute("store");
        if (store == null) {
            return "redirect:/owner/store";
        }
        model.addAttribute("newEmployee", new EmployeeDTO());
        model.addAttribute("store", store);

        User user = userService.getCurrentUser();
        model.addAttribute("user", user);
        return "addEmployee";
    }

    @PostMapping("/addEmployee")
    public String addEmployee(@ModelAttribute @Valid EmployeeDTO employeeDTO, BindingResult bindingResult, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Store store = (Store) session.getAttribute("store");
        if (store == null) {
            return "redirect:/owner/store";
        }

        if (bindingResult.hasErrors()) {
            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                redirectAttributes.addFlashAttribute("error", fieldError.getDefaultMessage());
            }
            return "redirect:/owner/employees/addEmployee";
        }

        try {
            User user = employeeService.addNewEmployee(store.getId(), employeeDTO, redirectAttributes);
            if (user == null) {
                return "redirect:/owner/employees/addEmployee";
            }
            model.addAttribute("store", store);
            redirectAttributes.addFlashAttribute("success", "Thêm nhân viên thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra, vui lòng thử lại!");
        }
        return "redirect:/owner/employees/addEmployee";
    }
}
