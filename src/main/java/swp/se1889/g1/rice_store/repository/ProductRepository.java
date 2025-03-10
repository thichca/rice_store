package swp.se1889.g1.rice_store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import swp.se1889.g1.rice_store.entity.Product;
import swp.se1889.g1.rice_store.entity.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCreatedBy(User createdBy);
    List<Product> findAllByName(String name);
    Optional<Product> findById(Long id);
    List<Product> findByIsDeletedFalse();
    List<Product> findByCreatedByAndIsDeletedFalse(User createdBy);
    @Query("SELECT new map(p.id as productId, p.name as productName, p.description as description, " +
            "p.price as price, z.id as zoneId, z.name as zoneName, z.quantity as quantity) " +
            "FROM Product p JOIN Zone z ON p.id = z.product.id " +
            "WHERE p.isDeleted = false AND z.isDeleted = false")
    List<Map<String, Object>> findAllProductsWithZones();


}
