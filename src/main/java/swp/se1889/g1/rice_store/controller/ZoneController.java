package swp.se1889.g1.rice_store.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
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
    public String createZone(@ModelAttribute("zone") ZoneDTO zone , HttpSession session , Model model , RedirectAttributes redirectAttributes) {
        Store store = (Store) session.getAttribute("store");
        model.addAttribute("store", store);

        // Lấy danh sách khu vực trong cửa hàng
        List<Zone> existingZones = zoneService.getZonesByStoreId(store);

        // Kiểm tra nếu tên khu vực đã tồn tại
        boolean isDuplicate = existingZones.stream()
                .anyMatch(z -> z.getName().equalsIgnoreCase(zone.getName()));

        if (isDuplicate) {
            redirectAttributes.addFlashAttribute("error", "Tên khu vực đã tồn tại. Vui lòng chọn tên khác.");
            return "redirect:/zone/add"; // Quay lại trang thêm khu vực
        }
        boolean isDuplicate1 = existingZones.stream()
                .anyMatch(z -> z.getAddress().equalsIgnoreCase(zone.getAddress()));

        if (isDuplicate1) {
            redirectAttributes.addFlashAttribute("error", "Địa chỉ khu vực đã tồn tại. Vui lòng chọn tên khác.");
            return "redirect:/zone/add"; // Quay lại trang thêm khu vực
        }
        Zone zone2 =  zoneService.createZone(zone , store);
      if(zone2 != null){
          redirectAttributes.addFlashAttribute("success", "Thêm khu vực thành công");
      }
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
    public String updateZone(@ModelAttribute("zone") ZoneDTO zone , HttpSession session , Model model , @PathVariable Long id , RedirectAttributes redirectAttributes) {
        Store store = (Store) session.getAttribute("store");
        model.addAttribute("store", store);

        // Lấy danh sách tất cả khu vực trong store
        List<Zone> zones = zoneService.getZonesByStoreId(store);

        // Lấy khu vực cũ
        Zone existingZone = zoneService.getZoneById(id);
        if (existingZone == null) {
            redirectAttributes.addFlashAttribute("error", "Khu vực không tồn tại.");
            return "redirect:/zone";
        }
        // Kiểm tra nếu tên mới đã tồn tại trong danh sách khu vực (trừ chính nó)
        boolean isDuplicate = zones.stream()
                .anyMatch(z -> !z.getId().equals(id) && z.getName().equalsIgnoreCase(zone.getName()));

        if (isDuplicate) {
            redirectAttributes.addFlashAttribute("error", "Tên khu vực đã tồn tại. Vui lòng chọn tên khác.");
            return "redirect:/zone/edit/" + id;
        }


        Zone zone1 =   zoneService.updateZone(zone , store);
      if(zone1 != null){
          redirectAttributes.addFlashAttribute("success", "Sửa khu vực thành công");
      }
        return "redirect:/zone";
    }
    @GetMapping("/delete/{id}")
    public String deleteZone(@PathVariable Long id , RedirectAttributes redirectAttributes) {
      Zone zone =  zoneService.deleteZone(id);
      if(zone != null){
          redirectAttributes.addFlashAttribute("success", "Xoá khu vực thành công");
      }
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
            Model model , RedirectAttributes redirectAttributes) {
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

        if (quantity <= 0) {
            redirectAttributes.addFlashAttribute("error", "Số lượng nhập vào phải lớn hơn 0.");
            return "redirect:/zone/addInventory";
        }
        if (zone.getProduct() != null && !zone.getProduct().getId().equals(product.getId())) {
            if (!zone.getProduct().isDeleted()) {
                redirectAttributes.addFlashAttribute("error", "Kho đã chứa một sản phẩm khác. Không thể thay đổi sản phẩm.");
                return "redirect:/zone/addInventory";
            }
        }

        Zone zone1 = zoneService.addInventory(zone, product, quantity);
        if (zone1 != null){
            redirectAttributes.addFlashAttribute("success", "Nhập  kho thành công");
        }
        return "redirect:/zone";
    }
    // Tìm kiếm khu vực theo tên (dựa theo store từ session)
    @GetMapping("/api/zones/search")
    @ResponseBody
    public List<Map<String, Object>> searchZones(@RequestParam("query") String query, HttpSession session) {
        Store store = (Store) session.getAttribute("store");
        if (store == null) return new ArrayList<>();

        return zoneService.searchZonesByName(store, query).stream()
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
        return productService.searchProductsByName(query).stream()
                .map(product -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", product.getId());
                    map.put("text", product.getName());
                    return map;
                })
                .collect(Collectors.toList());
    }
}