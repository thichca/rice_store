package swp.se1889.g1.rice_store.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public String employee(@RequestParam(value = "trangthai", required = false) String trangthai,
                           @RequestParam(value = "page", defaultValue = "0") int page,
                           @RequestParam(value = "size", defaultValue = "5") int size,
                           Model model, HttpSession session) {

        Store store = (Store) session.getAttribute("store");
        if (store == null) {
            return "redirect:/owner/store";
        }

        // Create a Pageable object with the requested page and size
        Pageable pageable = PageRequest.of(page, size);

        // Get a Page of employees instead of a List
        Page<User> employeesPage;
        if (trangthai == null || trangthai.equals("working")) {
            trangthai="working";
            employeesPage = employeeService.getEmployeesActivePage(store.getId(), "ROLE_EMPLOYEE", false, pageable);
        } else {
            employeesPage = employeeService.getEmployeesActivePage(store.getId(), "ROLE_EMPLOYEE", true, pageable);
        }

        model.addAttribute("employees", employeesPage.getContent());
        model.addAttribute("currentPage", employeesPage.getNumber());
        model.addAttribute("totalPages", employeesPage.getTotalPages());
        model.addAttribute("totalItems", employeesPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("store", store);
        model.addAttribute("trangthai", trangthai);
        model.addAttribute("employeeDTO", new EmployeeDTO());
        return "employees";
    }

    @PostMapping("")
    public String updateEmployee(@RequestParam("id") Long employeeId,
                                 @ModelAttribute @Valid EmployeeDTO employeeDTO,
                                 BindingResult bindingResult,
                                 Model model,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        Store store = (Store) session.getAttribute("store");
        if (store == null) {
            return "redirect:/owner/store";
        }

        if (bindingResult.hasErrors()) {
            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                redirectAttributes.addFlashAttribute("error", fieldError.getDefaultMessage());
            }
            return "redirect:/owner/employees";
        }

        try {
            boolean result = employeeService.updateEmployee(employeeId, employeeDTO);
            if (result) {
                redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin nhân viên thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy nhân viên cần cập nhật!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/owner/employees";
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
        model.addAttribute("employeeDTO", new EmployeeDTO());
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
            model.addAttribute("employeeDTO", employeeDTO);
            model.addAttribute("store", store);
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
