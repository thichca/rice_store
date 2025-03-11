package swp.se1889.g1.rice_store.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import swp.se1889.g1.rice_store.dto.ProductDTO;
import swp.se1889.g1.rice_store.entity.Product;
import swp.se1889.g1.rice_store.entity.Store;
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
     * 📌 Lấy danh sách sản phẩm theo user với phân trang
     */
    public Page<Product> getProductsByCurrentUser(int page, int size) {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            Pageable pageable = PageRequest.of(page, size);
            return productRepository.findByCreatedByAndIsDeletedFalse(currentUser, pageable);
        }
        return Page.empty();
    }



    public Product getProductToDelete(Long id){
        return  productRepository.findById(id).orElse(null);
    }

    /**
     * Lấy thông tin chi tiết sản phẩm theo ID
     */
    public ProductDTO getProductById(Long id) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            return new ProductDTO(product); // Dùng constructor DTO thay vì set thủ công
        }
        throw new RuntimeException("Không tìm thấy sản phẩm có ID: " + id);
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
            // ✅ Cập nhật thông tin "Người sửa"
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                product.setUpdatedBy(currentUser.getUsername());
            }
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


    public Page<Map<String, Object>> getAllProductsWithZones(Long storeId,int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findAllProductsWithZones(storeId, pageable );
    }
    public Page<Map<String, Object>> searchProductsWithZones(Long storeId, String searchType, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        if ("description".equals(searchType)) {
            return productRepository.findProductsByDescription(storeId, "%" + keyword + "%", pageable);
        } else {
            return productRepository.findProductsByName(storeId, "%" + keyword + "%", pageable);
        }
    }
    public Page<Product> searchProducts(String searchType, String keyword, int page, int size) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Không thể xác định người dùng hiện tại.");
        }

        Pageable pageable = PageRequest.of(page, size);

        String name = null;
        String description = null;
        BigDecimal price = null;

        if ("name".equals(searchType)) {
            name = keyword;
        } else if ("description".equals(searchType)) {
            description = keyword;
        } else if ("price".equals(searchType)) {
            try {
                price = new BigDecimal(keyword);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Giá phải là số hợp lệ.");
            }
        }

        return productRepository.searchProductsByUser(currentUser, name, description, price, pageable);
    }



}
