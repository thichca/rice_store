package swp.se1889.g1.rice_store.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import swp.se1889.g1.rice_store.entity.Store;
import swp.se1889.g1.rice_store.service.StoreService;
import swp.se1889.g1.rice_store.service.UserServiceIpml;

@Controller
public class HomeController {

    @Autowired
    private StoreService storeService;

    @Autowired
    private UserServiceIpml userService;

    @PostMapping("/home")
    public String selectStore(@RequestParam String name,
                              @RequestParam Long user_id,
                              Model model) {

        Store store = storeService.getStoreById(user_id);

        model.addAttribute("store", store);

        return "redirect:/home";
    }


    @GetMapping("/home")
    public String getHome(Model model) {
        Long userId = userService.getCurrentUserId();
        String name = storeService.getStoreNameByUserId(userId);
        model.addAttribute("storeName", name);
        model.addAttribute("userId", userId);
        return "home";
    }
}
