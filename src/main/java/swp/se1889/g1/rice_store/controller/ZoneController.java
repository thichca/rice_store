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
import java.util.*;
import java.util.stream.Collectors;

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
        model.addAttribute("store" , store);
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
        if(zone.getQuantity() != null && zone.getQuantity() > 0) {
            model.addAttribute("error", "Kho đã chứa một sản phẩm khác. Không thể thay đổi sản phẩm.");
            return "addInventory"; // Quay lại form với lỗi
        }
        zoneService.addInventory(zone, product, quantity);
        return "redirect:/zone";
    }
    // Tìm kiếm khu vực theo tên (dựa theo store từ session)
    @GetMapping("/api/zones/search")
    @ResponseBody
    public List<Map<String, Object>> searchZones(@RequestParam("query") String query, HttpSession session) {
        Store store = (Store) session.getAttribute("store");
        if (store == null) return new ArrayList<>();
        return zoneService.getZonesByStoreId(store).stream()
                .filter(zone -> zone.getName().toLowerCase().contains(query.toLowerCase()))
                .map(zone -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", zone.getId());
                    map.put("text", zone.getName());
                    return map;
                })
                .collect(Collectors.toList());
    }

    // Tìm kiếm sản phẩm theo tên
    @GetMapping("/api/products/search")
    @ResponseBody
    public List<Map<String, Object>> searchProducts(@RequestParam("query") String query) {
        return productRepository.findAll().stream()
                .filter(product -> product.getName().toLowerCase().contains(query.toLowerCase()))
                .map(product -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", product.getId());
                    map.put("text", product.getName());
                    return map;
                })
                .collect(Collectors.toList());
    }
}