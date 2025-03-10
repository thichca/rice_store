package swp.se1889.g1.rice_store.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import swp.se1889.g1.rice_store.entity.Store;
import swp.se1889.g1.rice_store.service.StoreService;

@Controller
public class HomeController {

    @Autowired
    private StoreService storeService;

    @PostMapping("/home")
    public String storeSelection(@RequestParam("storeName") String name,
                                 @RequestParam("createdBy") String createdBy,
                                 HttpSession session,
                                 Model model) {
        Store store = storeService.getStoreByNameAndCreatedBy(name, createdBy);
        session.setAttribute("store", store);
        model.addAttribute("store", store);
        return "home";
    }

    @GetMapping("/home")
    public String getHome(HttpSession session, Model model) {
        Store store = (Store) session.getAttribute("store");
        model.addAttribute("store", store);
        return "home";
    }

    @GetMapping("/baseFE")
    public String getBaseFE(HttpSession session, Model model) {
        Store store = (Store) session.getAttribute("store");
        model.addAttribute("store", store);
        return "baseFE";
    }
}
