package swp.se1889.g1.rice_store.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import swp.se1889.g1.rice_store.dto.MailBodyDTO;
import swp.se1889.g1.rice_store.entity.ForgotPassword;
import swp.se1889.g1.rice_store.entity.User;
import swp.se1889.g1.rice_store.repository.ForgotPasswordRepository;
import swp.se1889.g1.rice_store.repository.UserRepository;
import swp.se1889.g1.rice_store.service.EmailService;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

@Controller
@RequestMapping("/forgotPassword")
public class ForgotPasswordController {
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final ForgotPasswordRepository forgotPasswordRepository;
    private final PasswordEncoder passwordEncoder;

    public ForgotPasswordController(UserRepository userRepository, EmailService emailService, ForgotPasswordRepository forgotPasswordRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.forgotPasswordRepository = forgotPasswordRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Show forgot password page
    @GetMapping
    public String showForgotPasswordPage() {
        return "forgotPassword/forgotPassword";
    }

    // Handle form submission for sending OTP
    @PostMapping("/sendOtp")
    public String sendOtp(@RequestParam String email, RedirectAttributes redirectAttributes) {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            redirectAttributes.addAttribute("error", "Email not found");
            return "redirect:/forgotPassword";
        }

        int otp = otpGenerator();
        MailBodyDTO mailBodyDTO = new MailBodyDTO(email, "OTP for Forgot Password request", "This is the OTP for your Forgot Password request: " + otp);

        ForgotPassword fp = new ForgotPassword(otp, new Date(System.currentTimeMillis() + 60 * 1000), user.getId());
        emailService.sendSimpleMessage(mailBodyDTO);
        forgotPasswordRepository.save(fp);

        return "redirect:/forgotPassword/verify?email=" + email;
    }

    // Show OTP verification page
    @GetMapping("/verify")
    public String showVerifyOtpPage(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "forgotPassword/verifyOTP";
    }

    // Handle form submission for verifying OTP
    @PostMapping("/verify")
    public String validateOtp(@RequestParam Integer otp, @RequestParam String email, RedirectAttributes redirectAttributes) {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            redirectAttributes.addAttribute("error", "User not found");
            return "redirect:/forgotPassword/verify?email=" + email;
        }

        Optional<ForgotPassword> fpOptional = forgotPasswordRepository.findByOtpAndUser(otp, user.getId());

        if (fpOptional.isEmpty()) {
            redirectAttributes.addAttribute("error", "Invalid OTP");
            return "redirect:/forgotPassword/verify?email=" + email;
        }

        ForgotPassword fp = fpOptional.get();
        if (fp.getExpirationTime().before(Date.from(Instant.now()))) {
            forgotPasswordRepository.deleteById(fp.getFpid());
            redirectAttributes.addAttribute("error", "OTP has expired");
            return "redirect:/forgotPassword/verify?email=" + email;
        }

        // OTP is valid, redirect to change password page
        return "redirect:/forgotPassword/changePassword?email=" + email;
    }

    // Show change password page
    @GetMapping("/changePassword")
    public String showChangePasswordPage(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "forgotPassword/changePassword";
    }

    // Handle form submission for changing password
    @PostMapping("/updatePassword")
    public String updatePassword(@RequestParam String email, @RequestParam String password, @RequestParam String repeatPassword, RedirectAttributes redirectAttributes) {
        if (!Objects.equals(password, repeatPassword)) {
            redirectAttributes.addAttribute("error", "Passwords don't match");
            return "redirect:/forgotPassword/changePassword?email=" + email;
        }

        User user = userRepository.findByEmail(email);
        if (user == null) {
            redirectAttributes.addAttribute("error", "User not found");
            return "redirect:/forgotPassword/changePassword?email=" + email;
        }

        userRepository.updatePassword(email, passwordEncoder.encode(password));
        forgotPasswordRepository.deleteByUserId(user.getId());

        redirectAttributes.addAttribute("success", "Password has been changed successfully");
        return "redirect:/login";
    }

    // Handle resending OTP
    @PostMapping("/resendOtp")
    public String resendOtp(@RequestParam String email, RedirectAttributes redirectAttributes) {
        return sendOtp(email, redirectAttributes);
    }

    private Integer otpGenerator() {
        Random random = new Random();
        return random.nextInt(100_000, 1_000_000);
    }
}
