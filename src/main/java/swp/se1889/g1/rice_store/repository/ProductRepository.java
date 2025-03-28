package swp.se1889.g1.rice_store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import swp.se1889.g1.rice_store.dto.ProductZoneDTO;
import swp.se1889.g1.rice_store.entity.Product;
import swp.se1889.g1.rice_store.entity.Store;
import swp.se1889.g1.rice_store.entity.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import swp.se1889.g1.rice_store.entity.Zone;

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

    List<Product> findByNameContaining(String name);

    @Query("SELECT p FROM Product p WHERE " +
            "p.createdBy = :createdBy AND p.isDeleted = false AND " +
            "(:productName IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :productName, '%'))) AND " +
            "(:description IS NULL OR LOWER(p.description) LIKE LOWER(CONCAT('%', :description, '%'))) AND " +
            "(:priceFrom IS NULL OR p.price >= :priceFrom) AND " +
            "(:priceTo IS NULL OR p.price <= :priceTo) AND " +
            "(:createdDate IS NULL OR CAST(p.createdAt AS date) = :createdDate) AND " +
            "(:updatedDate IS NULL OR CAST(p.updatedAt AS date) = :updatedDate)")
    Page<Product> filterProducts(
            @Param("createdBy") User createdBy,
            @Param("productName") String name,
            @Param("description") String description,
            @Param("priceFrom") BigDecimal priceFrom,
            @Param("priceTo") BigDecimal priceTo,
            @Param("createdDate") LocalDate createdDate,
            @Param("updatedDate") LocalDate updatedDate,
            Pageable pageable);
    @Query("SELECT new map(p.id as productId, p.name as productName, p.description as description, " +
            "p.price as price, z.id as zoneId, z.name as zoneName, z.quantity as quantity, " +
            "s.id as storeId, s.name as storeName) " +
            "FROM Product p " +
            "JOIN Zone z ON p.id = z.product.id " +
            "JOIN Store s ON z.store.id = s.id " +
            "WHERE p.isDeleted = false AND z.isDeleted = false AND s.id = :storeId " +
            "AND (:productName IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :productName, '%'))) " +
            "AND (:description IS NULL OR LOWER(p.description) LIKE LOWER(CONCAT('%', :description, '%'))) " +
            "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
            "AND (:zoneName IS NULL OR LOWER(z.name) LIKE LOWER(CONCAT('%', :zoneName, '%'))) " +
            "AND (:minQuantity IS NULL OR z.quantity >= :minQuantity) " +
            "AND (:maxQuantity IS NULL OR z.quantity <= :maxQuantity)")
    Page<Map<String, Object>> filterProductZones(
            @Param("storeId") Long storeId,
            @Param("productName") String productName,
            @Param("description") String description,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("zoneName") String zoneName,
            @Param("minQuantity") Integer minQuantity,
            @Param("maxQuantity") Integer maxQuantity,
            Pageable pageable);



}






