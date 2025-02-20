package swp.se1889.g1.rice_store.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import swp.se1889.g1.rice_store.entity.User;
import swp.se1889.g1.rice_store.service.UserServiceIpml;

@Controller
public class ProfileController {

    @Autowired
    private UserServiceIpml userServiceIpml;

    @GetMapping("/profile")
    public String profile(Model model) {
        User user = userServiceIpml.getCurrentUser();
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/updateInfo")
    public String updateInfo(@RequestParam("nameStore") String nameStore,
                             @RequestParam("address") String address,
                             @RequestParam("phone") String phone,
                             @RequestParam("email") String email,
                             @RequestParam("note") String note,
                             RedirectAttributes redirectAttributes) {
        User user = userServiceIpml.getCurrentUser();

        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            redirectAttributes.addFlashAttribute("error", "Email không hợp lệ.");
            return "redirect:/profile";
        }

        if (phone != null && !phone.isEmpty() && !phone.matches("^\\d{10,15}$")) {
            redirectAttributes.addFlashAttribute("error", "Số điện thoại không hợp lệ.");
            return "redirect:/profile";
        }

        try {
            user.setStoreName(nameStore);
            user.setAddress(address);
            user.setPhone(phone);
            user.setEmail(email);
            user.setNote(note);
            userServiceIpml.updateUser(user);

            redirectAttributes.addFlashAttribute("success", "Cập nhật thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra, vui lòng thử lại!");
        }
        return "redirect:/profile";
    }

    @PostMapping("/updatePassword")
    public String updatePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmNewPassword") String confirmNewPassword,
                                 RedirectAttributes redirectAttributes) {
        User user = userServiceIpml.getCurrentUser();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không đúng");
            return "redirect:/profile";
        }

        if (newPassword.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu mới không được để trống");
            return "redirect:/profile";
        }

        if (!newPassword.equals(confirmNewPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu nhập lại không khớp");
            return "redirect:/profile";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userServiceIpml.updateUser(user);

        redirectAttributes.addFlashAttribute("success", "Cập nhật mật khẩu thành công!");
        return "redirect:/profile";
    }
}
