package swp.se1889.g1.rice_store.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import swp.se1889.g1.rice_store.dto.ChangePasswordDTO;
import swp.se1889.g1.rice_store.dto.UserDTO;
import swp.se1889.g1.rice_store.entity.Store;
import swp.se1889.g1.rice_store.entity.User;
import swp.se1889.g1.rice_store.service.StoreService;
import swp.se1889.g1.rice_store.service.UserServiceIpml;

@Controller
public class ProfileController {

    @Autowired
    private UserServiceIpml userServiceIpml;

    @Autowired
    private StoreService storeService;

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        User user = userServiceIpml.getCurrentUser();
        Store store = (Store) session.getAttribute("store");
        model.addAttribute("store", store);
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/updateInfo")
    public String updateInfo(@Valid UserDTO userDTO,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             HttpSession session,
                             Model model) {
        if (bindingResult.hasErrors()) {
            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                redirectAttributes.addFlashAttribute("error", fieldError.getDefaultMessage());
            }
            return "redirect:/profile";
        }

        try {
            User currentUser = userServiceIpml.getCurrentUser();
            currentUser.setStoreName(userDTO.getStoreName());
            currentUser.setAddress(userDTO.getAddress());
            currentUser.setPhone(userDTO.getPhone());
            currentUser.setEmail(userDTO.getEmail());
            currentUser.setNote(userDTO.getNote());
            userServiceIpml.updateUser(currentUser);

            if (currentUser.getRole().equals("ROLE_OWNER")) {
                Store store = (Store) session.getAttribute("store");
                store.setName(userDTO.getStoreName());
                store.setAddress(userDTO.getAddress());
                store.setPhone(userDTO.getPhone());
                store.setEmail(userDTO.getEmail());
                store.setNote(userDTO.getNote());
                storeService.updateStore(store);
                session.setAttribute("store", store);
                model.addAttribute("store", store);
            }

            redirectAttributes.addFlashAttribute("success", "Cập nhật thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra, vui lòng thử lại!");
        }
        return "redirect:/profile";
    }

    @PostMapping("/updatePassword")
    public String updatePassword(@Valid ChangePasswordDTO changePasswordDTO,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {

        User user = userServiceIpml.getCurrentUser();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        if (!passwordEncoder.matches(changePasswordDTO.getCurrentPassword(), user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không đúng");
            return "redirect:/profile";
        }

        if (bindingResult.hasErrors()) {
            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                redirectAttributes.addFlashAttribute("error", fieldError.getDefaultMessage());
            }
            return "redirect:/profile";
        }

        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmNewPassword())) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu nhập lại không khớp");
            return "redirect:/profile";
        }

        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        userServiceIpml.updateUser(user);

        redirectAttributes.addFlashAttribute("success", "Cập nhật mật khẩu thành công!");
        return "redirect:/profile";
    }
}
