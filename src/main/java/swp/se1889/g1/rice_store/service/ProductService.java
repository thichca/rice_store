
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
    // Lấy tổng số sản phẩm theo user hiện tại
    public long countProductsByCurrentUser() {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            return productRepository.countByCreatedBy(currentUser);
        }
        return 0;
    }


    public Page<Product> getProductsByCurrentUser(int page, int size) {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            Pageable pageable = PageRequest.of(page, size);
            return productRepository.findByCreatedByAndIsDeletedFalse(currentUser, pageable);
        }
        return Page.empty();
    }

    public List<Product> searchProductsByName(String name) {
        return productRepository.findByIsDeletedFalseAndNameContainingIgnoreCase(name);
    }


    public Product getProductToDelete(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public ProductDTO getProductById(Long id) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            return new ProductDTO(product); // Dùng constructor DTO thay vì set thủ công
        }
        throw new RuntimeException("Không tìm thấy sản phẩm có ID: " + id);
    }

    public void createProduct(ProductDTO productDTO) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Không thể xác định người dùng hiện tại.");
        }

        if (productDTO.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Giá sản phẩm phải lớn hơn 0.");
        }
        // Kiểm tra xem sản phẩm có tên trùng với sản phẩm cũ hay không
        boolean isProductNameExists = productRepository.existsByCreatedByAndName(currentUser, productDTO.getName());
        if (isProductNameExists) {
            throw new RuntimeException("Sản phẩm với tên này đã tồn tại.");
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

    public void updateProduct(ProductDTO productDTO) {
        Optional<Product> productOpt = productRepository.findById(productDTO.getId());

        if (productOpt.isPresent()) {
            Product product = productOpt.get();

            boolean isDuplicateName = productRepository.existsByCreatedByAndNameAndIdNot(
                    product.getCreatedBy(), productDTO.getName(), productDTO.getId()
            );

            if (isDuplicateName) {
                throw new RuntimeException("Tên sản phẩm đã tồn tại, vui lòng chọn tên khác.");
            }

            // Kiểm tra giá phải lớn hơn 0
            if (productDTO.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Giá sản phẩm phải lớn hơn 0.");
            }

            // Cập nhật thông tin sản phẩm
            product.setName(productDTO.getName());
            product.setDescription(productDTO.getDescription());
            product.setPrice(productDTO.getPrice());
            product.setUpdatedAt(LocalDateTime.now());

            // Cập nhật người sửa
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                product.setUpdatedBy(currentUser.getUsername());
            }

            productRepository.save(product);
        } else {
            throw new RuntimeException("Không tìm thấy sản phẩm để cập nhật!");
        }
    }


    public void deleteProduct(Long id) {
        Product product = getProductToDelete(id);
        product.setDeleted(true);
        productRepository.save(product);
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByUsername(username);
        }
        return null;
    }


    public Page<Map<String, Object>> getAllProductsWithZones(Long storeId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findAllProductsWithZones(storeId, pageable);
    }

    public Page<Map<String, Object>> searchProductsWithZones(Long storeId, String searchType, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        if ("description".equals(searchType)) {
            return productRepository.findProductsByDescription(storeId, "%" + keyword + "%", pageable);
        } else {
            return productRepository.findProductsByName(storeId, "%" + keyword + "%", pageable);
        }
    }

    public Page<Product> searchProducts(String searchType, String keyword, BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Không thể xác định người dùng hiện tại.");
        }

        Pageable pageable = PageRequest.of(page, size);

        String name = null;
        String description = null;

        if ("name".equals(searchType)) {
            name = keyword;
        } else if ("description".equals(searchType)) {
            description = keyword;
        }

        return productRepository.searchProductsByUser(currentUser, name, description, minPrice, maxPrice, pageable);
    }

    public Page<Map<String, Object>> searchProductsByName(Long storeId, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findProductsByName(storeId, "%" + keyword + "%", pageable);
    }

    public Page<Map<String, Object>> searchProductsWithDescriptionAndPrice(Long storeId, String keyword,
                                                                           BigDecimal minPrice, BigDecimal maxPrice,
                                                                           int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findProductsByDescriptionAndPrice(storeId, "%" + keyword + "%", minPrice, maxPrice, pageable);
    }



}