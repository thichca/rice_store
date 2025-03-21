package swp.se1889.g1.rice_store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import swp.se1889.g1.rice_store.entity.Product;
import swp.se1889.g1.rice_store.entity.Store;
import swp.se1889.g1.rice_store.entity.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCreatedBy(User createdBy);

    List<Product> findAllByName(String name);
    boolean existsByCreatedByAndNameAndIdNot(User createdBy, String name, Long id);


    Optional<Product> findById(Long id);

    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.createdBy = :createdBy AND p.isDeleted = false AND LOWER(p.name) = LOWER(:name)")
    boolean existsByCreatedByAndName(@Param("createdBy") User createdBy, @Param("name") String name);

    //
    Page<Product> findByCreatedByAndIsDeletedFalse(User createdBy, Pageable pageable);

    @Query("SELECT new map(p.id as productId, p.name as productName, p.description as description, " +
            "p.price as price, z.id as zoneId, z.name as zoneName, z.quantity as quantity, " +
            "s.id as storeId, s.name as storeName) " +
            "FROM Product p " +
            "JOIN Zone z ON p.id = z.product.id " +
            "JOIN Store s ON z.store.id = s.id " +
            "WHERE p.isDeleted = false AND z.isDeleted = false AND s.id = :storeId")
    Page<Map<String, Object>> findAllProductsWithZones(@Param("storeId") Long storeId, Pageable pageable);

    @Query("SELECT new map(p.id as productId, p.name as productName, p.description as description, " +
            "p.price as price, z.id as zoneId, z.name as zoneName, z.quantity as quantity, " +
            "s.id as storeId, s.name as storeName) " +
            "FROM Product p " +
            "JOIN Zone z ON p.id = z.product.id " +
            "JOIN Store s ON z.store.id = s.id " +
            "WHERE p.isDeleted = false AND z.isDeleted = false AND s.id = :storeId " +
            "AND LOWER(p.description) LIKE LOWER(:keyword)")
    Page<Map<String, Object>> findProductsByDescription(@Param("storeId") Long storeId, @Param("keyword") String keyword, Pageable pageable);


    @Query("SELECT p FROM Product p WHERE " +
            "p.createdBy = :createdBy AND p.isDeleted = false AND " +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:description IS NULL OR LOWER(p.description) LIKE LOWER(CONCAT('%', :description, '%'))) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> searchProductsByUser(
            @Param("createdBy") User createdBy,
            @Param("name") String name,
            @Param("description") String description,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);


    List<Product> findByIsDeletedFalseAndNameContainingIgnoreCase(String name);
    @Query("SELECT new map(p.id as productId, p.name as productName, p.description as description, " +
            "p.price as price, z.id as zoneId, z.name as zoneName, z.quantity as quantity, " +
            "s.id as storeId, s.name as storeName) " +
            "FROM Product p " +
            "JOIN Zone z ON p.id = z.product.id " +
            "JOIN Store s ON z.store.id = s.id " +
            "WHERE p.isDeleted = false AND z.isDeleted = false AND s.id = :storeId " +
            "AND LOWER(p.name) LIKE LOWER(:keyword)")
    Page<Map<String, Object>> findProductsByName(@Param("storeId") Long storeId,
                                                 @Param("keyword") String keyword,
                                                 Pageable pageable);

    @Query("SELECT new map(p.id as productId, p.name as productName, p.description as description, " +
            "p.price as price, z.id as zoneId, z.name as zoneName, z.quantity as quantity, " +
            "s.id as storeId, s.name as storeName) " +
            "FROM Product p " +
            "JOIN Zone z ON p.id = z.product.id " +
            "JOIN Store s ON z.store.id = s.id " +
            "WHERE p.isDeleted = false AND z.isDeleted = false AND s.id = :storeId " +
            "AND LOWER(p.description) LIKE LOWER(:keyword) " +
            "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Map<String, Object>> findProductsByDescriptionAndPrice(@Param("storeId") Long storeId,
                                                                @Param("keyword") String keyword,
                                                                @Param("minPrice") BigDecimal minPrice,
                                                                @Param("maxPrice") BigDecimal maxPrice,
                                                                Pageable pageable);
    @Query("SELECT COUNT(p) FROM Product p WHERE p.createdBy = :createdBy AND p.isDeleted = false")
    long countByCreatedBy(@Param("createdBy") User createdBy);


}






