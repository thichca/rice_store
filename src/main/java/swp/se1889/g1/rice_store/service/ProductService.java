package swp.se1889.g1.rice_store.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import swp.se1889.g1.rice_store.dto.ProductDTO;
import swp.se1889.g1.rice_store.entity.Product;
import swp.se1889.g1.rice_store.entity.User;
import swp.se1889.g1.rice_store.repository.ProductRepository;
import swp.se1889.g1.rice_store.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Lấy danh sách sản phẩm của người dùng hiện tại
     */
    public List<Product> getProductsByCurrentUser() {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            return productRepository.findByCreatedByAndIsDeletedFalse(currentUser);
        }
        return List.of();
    }

    public List<Product> searchProductsByName(String name) {
        return productRepository.findByIsDeletedFalseAndNameContainingIgnoreCase(name);
    }

    public Product getProductToDelete(Long id){
        return  productRepository.findById(id).orElse(null);
}

    /**
     * Lấy thông tin chi tiết sản phẩm theo ID
     */
    public Product getProduct(Long id){
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            return productOpt.get();
        }
        throw new RuntimeException("Không tìm thấy sản phẩm có ID: " + id);
    }
    public ProductDTO getProductById(Long id) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            return new ProductDTO(product); // Dùng constructor DTO thay vì set thủ công
        }
        throw new RuntimeException("Không tìm thấy sản phẩm có ID: " + id);
    }
    public List<Product> getAllProduct(){
        return productRepository.findByIsDeletedFalse();
    }

    /**
     * Thêm sản phẩm mới
     */
    public void createProduct(ProductDTO productDTO) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Không thể xác định người dùng hiện tại.");
        }

        if (productDTO.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Giá sản phẩm phải lớn hơn 0.");
        }

        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setCreatedBy(currentUser);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        productRepository.save(product);
    }

    /**
     * Cập nhật sản phẩm
     */
    public void updateProduct(ProductDTO productDTO) {
        Optional<Product> productOpt = productRepository.findById(productDTO.getId());
        if (productOpt.isPresent()) {
            Product product = productOpt.get();

            if (productDTO.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Giá sản phẩm phải lớn hơn 0.");
            }

            product.setName(productDTO.getName());
            product.setDescription(productDTO.getDescription());
            product.setPrice(productDTO.getPrice());
            product.setUpdatedAt(LocalDateTime.now());
            productRepository.save(product);
        } else {
            throw new RuntimeException("Không tìm thấy sản phẩm để cập nhật!");
        }
    }

    /**
     * Xóa sản phẩm theo ID
     */
    public void deleteProduct(Long id) {
        Product product = getProductToDelete(id);
        product.setDeleted(true);
        productRepository.save(product);
    }

    /**
     * Lấy người dùng hiện tại từ SecurityContextHolder
     */
    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByUsername(username);
        }
        return null;
    }


    public List<Map<String, Object>> getAllProductsWithZones() {
        return productRepository.findAllProductsWithZones();
    }
}
