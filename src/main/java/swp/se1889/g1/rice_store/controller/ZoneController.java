package swp.se1889.g1.rice_store.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import swp.se1889.g1.rice_store.dto.ProductDTO;
import swp.se1889.g1.rice_store.dto.ZoneDTO;
import swp.se1889.g1.rice_store.entity.Product;
import swp.se1889.g1.rice_store.entity.Store;
import swp.se1889.g1.rice_store.entity.Zone;
import swp.se1889.g1.rice_store.repository.ProductRepository;
import swp.se1889.g1.rice_store.service.ProductService;
import swp.se1889.g1.rice_store.service.ZoneService;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/zone")
public class ZoneController {
    @Autowired
    private ZoneService zoneService;
@Autowired
private ProductRepository productRepository;
@Autowired
private ProductService productService;
    @GetMapping
    public String listZones(Model model, HttpSession session) {
        Store store = (Store) session.getAttribute("store");
        if (store == null) return "redirect:/login";

        model.addAttribute("store", store);
        model.addAttribute("zones", zoneService.getZonesByStoreId(store));
        return "zones";
    }
    @GetMapping("/add")
    public String AddZone(Model model , HttpSession session) {
        Store store = (Store) session.getAttribute("store");
        model.addAttribute("store", store);
        model.addAttribute("zone", new Zone());
        return "addZone";
    }
    @PostMapping("/add")
    public String createZone(@ModelAttribute("zone") ZoneDTO zone , HttpSession session , Model model) {
        Store store = (Store) session.getAttribute("store");
        model.addAttribute("store", store);
        zoneService.createZone(zone , store);
        return "redirect:/zone";
    }
    @GetMapping("/edit/{id}")
    public String EditZone(Model model , HttpSession session, @PathVariable Long id) {
        Store store = (Store) session.getAttribute("store");
        model.addAttribute("store", store);
        Zone zone = zoneService.getZoneById(id);
        model.addAttribute("zone", zone);
        return "editZone";

    }
    @PostMapping("/edit/{id}")
    public String updateZone(@ModelAttribute("zone") ZoneDTO zone , HttpSession session , Model model , @PathVariable Long id) {
        Store store = (Store) session.getAttribute("store");
        model.addAttribute("store", store);
        zoneService.updateZone(zone , store);
        return "redirect:/zone";
    }
    @GetMapping("/delete/{id}")
    public String deleteZone(@PathVariable Long id) {
        zoneService.deleteZone(id);
        return "redirect:/zone";
    }
    @GetMapping("/detail/{id}")
    public String detailZone(Model model, HttpSession session, @PathVariable Long id) {
        Store store = (Store) session.getAttribute("store");
        if (store == null) return "redirect:/login";

        Zone zone = zoneService.getZoneById(id);
        ProductDTO productDTO = (zone.getProduct() != null) ? productService.getProductById(zone.getProduct().getId()) : null;

        model.addAttribute("store", store);
        model.addAttribute("zone", zone);
        model.addAttribute("product", productDTO);
        return "zoneDetail";
    }
    @GetMapping("/addInventory")
    public String showAddInventoryForm(Model model, HttpSession session) {
        Store store = (Store) session.getAttribute("store");
        if (store == null) return "redirect:/login";

        // Fetch all zones for the store
        List<Zone> zones = zoneService.getZonesByStoreId(store);
        // Fetch all products (assuming ProductService has a getAllProducts method)
        List<Product> products = productRepository.findAll(); // Adjust based on your ProductService

        model.addAttribute("store", store);
        model.addAttribute("zones", zones);
        model.addAttribute("products", products);
        return "addInventory";
    }
    @PostMapping("/addInventory")
    public String addInventory(
            @RequestParam("zoneId") Long zoneId,
            @RequestParam("productId") Long productId,
            @RequestParam("quantity") int quantity,
            HttpSession session,
            Model model) {
        Store store = (Store) session.getAttribute("store");
        if (store == null) return "redirect:/login";

        Zone zone = zoneService.getZoneById(zoneId);
        if (zone == null) {
            model.addAttribute("error", "Zone không tồn tại");
            return "addInventory"; // Return to form with error
        }

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            model.addAttribute("error", "Sản phẩm không tồn tại");
            return "addInventory"; // Return to form with error
        }

        // Add inventory using a simplified ZoneService method

        zoneService.addInventory(zone, product, quantity);

        return "redirect:/zone/detail/" + zoneId;
    }
}