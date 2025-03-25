package swp.se1889.g1.rice_store.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import swp.se1889.g1.rice_store.dto.StoreDTO;
import swp.se1889.g1.rice_store.entity.Store;
import swp.se1889.g1.rice_store.entity.User;
import swp.se1889.g1.rice_store.service.StoreService;
import swp.se1889.g1.rice_store.service.UserServiceIpml;

@Controller
public class StoreController {
    @Autowired
    private StoreService storeService;

    @Autowired
    private UserServiceIpml userService;

    @GetMapping("owner/store")
    public String getStores(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "5") int size,
                            Model model) {
        String username = userService.getCurrentUsername();
        Page<Store> storePage = storeService.getStoresByUserName(username, page, size);
        model.addAttribute("stores", storePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", storePage.getTotalPages());
        model.addAttribute("totalItems", storePage.getTotalElements());
        model.addAttribute("recordsPerPage", size);

        return "store";
    }

    @GetMapping("owner/createStore")
    public String createStoreForm(Model model) {
        model.addAttribute("page", "createStore");
        return "createStore";
    }

    @GetMapping("owner/manageStores")
    public String manageStores(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               Model model) {
        String username = userService.getCurrentUsername();
        Page<Store> storePage = storeService.getStoresByUserName(username, page, size);

        model.addAttribute("stores", storePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", storePage.getTotalPages());
        model.addAttribute("totalItems", storePage.getTotalElements());
        model.addAttribute("recordsPerPage", size);

        return "manageStores";
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
            return "redirect:/owner/createStore";
        }

        try {
            Store store = storeService.createStore(storeDTO, redirectAttributes);
            if (store == null) {
                return "redirect:/owner/createStore";
            }
            model.addAttribute("store", store);
            redirectAttributes.addFlashAttribute("success", "Tạo cửa hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra, vui lòng thử lại!");
        }
        return "redirect:/owner/store";
    }

    @GetMapping("/deleteStore/{storeId}")
    public String deleteStore(@PathVariable Long storeId, RedirectAttributes redirectAttributes) {
        try {
            storeService.deleteStore(storeId);
            redirectAttributes.addFlashAttribute("success", "Cửa hàng đã được xóa!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi xóa cửa hàng!");
        }
        return "redirect:/owner/manageStores";
    }

    @GetMapping("owner/updateStore/{id}")
    public String showUpdateForm(@PathVariable Long id, Model model) {
        Store store = storeService.findStoreByStoreId(id);
        if (store == null) {
            return "redirect:/manageStores";
        }
        model.addAttribute("store", store);
        return "updateStore";
    }

    @PostMapping("/updateStore")
    public String updateStore(@RequestParam Long storeId,
                              @Valid StoreDTO storeDTO,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                redirectAttributes.addFlashAttribute("error", fieldError.getDefaultMessage());
            }
            return "redirect:/owner/updateStore/" + storeId;
        }

        try {
            Store store = storeService.updateStore(storeId, storeDTO, redirectAttributes);
            if (store == null) {
                return "redirect:/owner/updateStore/" + storeId;
            }
            redirectAttributes.addFlashAttribute("success", "Cập nhật cửa hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra, vui lòng thử lại!");
        }
        return "redirect:/owner/manageStores";
    }

    @GetMapping("/restore-data")
    public String GetViewManageUser(Model model,
                                    HttpSession session) {
        User user = userService.getCurrentUser();
        model.addAttribute("user", user);

        Store store = (Store) session.getAttribute("store");
        model.addAttribute("store", store);
        return "manage-store-restore-data";
    }


}
