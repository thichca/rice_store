package swp.se1889.g1.rice_store.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import swp.se1889.g1.rice_store.dto.StoreDTO;
import swp.se1889.g1.rice_store.entity.Store;
import swp.se1889.g1.rice_store.service.StoreService;
import swp.se1889.g1.rice_store.service.UserServiceIpml;

import java.util.List;

@Controller
public class StoreController {
    @Autowired
    private StoreService storeService;

    @Autowired
    private UserServiceIpml userService;

    @GetMapping({"/", "/store"})
    public String getStores(Model model) {
        String username = userService.getCurrentUsername();
        List<Store> stores = storeService.getStoresByUserName(username);

        if (userService.getCurrentStoreId() != null) {
            Store store = storeService.findStoreByStoreId(userService.getCurrentStoreId());
            if (store != null && !stores.contains(store)) {
                stores.add(store);
            }
        }
        model.addAttribute("stores", stores);
        return "store";
    }

    @GetMapping("/createStore")
    public String createStoreForm() {
        return "createStore";
    }

    @PostMapping("/createStore")
    public String createStore(@Valid StoreDTO storeDTO,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                redirectAttributes.addFlashAttribute("error", fieldError.getDefaultMessage());
            }
            return "redirect:/createStore";
        }

        try {
            Store store = storeService.createStore(storeDTO, redirectAttributes);
            if (store == null) {
                return "redirect:/createStore";
            }
            model.addAttribute("store", store);
            redirectAttributes.addFlashAttribute("success", "Tạo cửa hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra, vui lòng thử lại!");
        }
        return "redirect:/store";
    }
}
