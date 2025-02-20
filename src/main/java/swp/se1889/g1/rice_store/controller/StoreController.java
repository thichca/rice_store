package swp.se1889.g1.rice_store.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
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
        Long userId = userService.getCurrentUserId();
        List<Store> stores = storeService.getStoresByUser(userId);
        model.addAttribute("stores", stores);
        return "store";
    }

    @GetMapping("/createStore")
    public String createStoreForm(Model model) {
        model.addAttribute("store", new Store());
        return "createStore";
    }

    @PostMapping("/createStore")
    public String createStore(@ModelAttribute Store store) {
        Long userId = userService.getCurrentUserId();
        store.setUserId(userId);
        storeService.createStore(store);
        return "redirect:/store";
    }
}
