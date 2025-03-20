package swp.se1889.g1.rice_store.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import swp.se1889.g1.rice_store.dto.ProductDTO;
import swp.se1889.g1.rice_store.entity.Product;
import swp.se1889.g1.rice_store.entity.Store;
import swp.se1889.g1.rice_store.entity.User;
import swp.se1889.g1.rice_store.service.ProductService;

import jakarta.validation.Valid;
import swp.se1889.g1.rice_store.service.UserServiceIpml;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserServiceIpml userService;


    @GetMapping("owner/products")
    public String getProducts(@RequestParam(required = false) String searchType,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "5") int size,
                              Model model,
                              HttpSession session) {

        Page<Product> productPage;


        if (keyword != null && !keyword.trim().isEmpty()) {
            productPage = productService.searchProducts(searchType, keyword, page, size);
        } else {
            productPage = productService.getProductsByCurrentUser(page, size);
        }


        if (!model.containsAttribute("newProduct")) {
            model.addAttribute("newProduct", new ProductDTO());
        }

        Store store = (Store) session.getAttribute("store");
        model.addAttribute("store", store);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        // Lưu lại thông tin tìm kiếm để Thymeleaf sử dụng
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", keyword);
        model.addAttribute("editProduct", new ProductDTO());

        User user = userService.getCurrentUser();
        model.addAttribute("user", user);

        return "products";
    }



    @GetMapping("/owner/zones")
    public String getProductsWithZones(Model model, HttpSession session,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "5") int size,
                                       @RequestParam(required = false) String searchType,
                                       @RequestParam(required = false) String keyword) {
        Store store = (Store) session.getAttribute("store");
        if (store == null) return "redirect:/login";

        model.addAttribute("store", store);

        Page<Map<String, Object>> productWithZones;

        if (keyword != null && !keyword.trim().isEmpty()) {
            productWithZones = productService.searchProductsWithZones(store.getId(), searchType, keyword, page, size);
        } else {
            productWithZones = productService.getAllProductsWithZones(store.getId(), page, size);
        }

        model.addAttribute("productWithZones", productWithZones);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productWithZones.getTotalPages());
        model.addAttribute("totalItems", productWithZones.getTotalElements());

        User user = userService.getCurrentUser();
        model.addAttribute("user", user);

        return "sellProducts";
    }



    @PostMapping("/products/add")
    @ResponseBody
    public ResponseEntity<?> addProduct(@Valid @ModelAttribute("newProduct") ProductDTO productDTO,
                                        BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);  // Trả về lỗi dưới dạng JSON
        }

        try {
            productService.createProduct(productDTO);
            return ResponseEntity.ok().body(Collections.singletonMap("message", "Thêm sản phẩm thành công!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("errorMessage", e.getMessage()));
        }
    }


    @GetMapping("/edit-product/{id}")
    @ResponseBody
    public ProductDTO getProductForEdit(@PathVariable Long id) {
        return productService.getProductById(id);
    }


    @PostMapping("/products/update")
    public String updateProduct(@Valid @ModelAttribute("editProduct") ProductDTO productDTO, BindingResult result, Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "5") int size) {
        if (result.hasErrors()) {
            Page<Product> productPage = productService.getProductsByCurrentUser(page, size);
            model.addAttribute("products", productPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", productPage.getTotalPages());
            model.addAttribute("totalItems", productPage.getTotalElements());
            return "products";
        }
        productService.updateProduct(productDTO);
        return "redirect:/owner/products?page=" + page + "&size=" + size;
    }


    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "5") int size) {
        productService.deleteProduct(id);
        return "redirect:/owner/products?page=" + page + "&size=" + size;
    }


}


