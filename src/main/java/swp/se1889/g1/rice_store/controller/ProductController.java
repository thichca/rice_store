package swp.se1889.g1.rice_store.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import swp.se1889.g1.rice_store.dto.ProductDTO;
import swp.se1889.g1.rice_store.entity.Store;
import swp.se1889.g1.rice_store.service.ProductService;

import jakarta.validation.Valid;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 📌 Hiển thị danh sách sản phẩm của người dùng đăng nhập
     */
    @GetMapping("/products")
    public String getProducts(Model model, HttpSession session) {
        if (!model.containsAttribute("newProduct")) {
            model.addAttribute("newProduct", new ProductDTO());  // Tránh null
        }
        Store store = (Store) session.getAttribute("store");
        model.addAttribute("store", store);
        model.addAttribute("products", productService.getProductsByCurrentUser());
        model.addAttribute("editProduct", new ProductDTO());

        return "products"; // Trả về trang Thymeleaf `products.html`
    }

    /**
     * 📌 Xử lý thêm sản phẩm mới
     */
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

    /**
     * 📌 API lấy thông tin sản phẩm để chỉnh sửa
     */
    @GetMapping("/edit-product/{id}")
    @ResponseBody
    public ProductDTO getProductForEdit(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    /**
     * 📌 Cập nhật sản phẩm (tương tự Customer)
     */
    @PostMapping("/products/update")
    public String updateProduct(@Valid @ModelAttribute("editProduct") ProductDTO productDTO, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("products", productService.getProductsByCurrentUser());
            return "products";
        }
        productService.updateProduct(productDTO);
        return "redirect:/products";
    }
}
