package swp.se1889.g1.rice_store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import swp.se1889.g1.rice_store.entity.Product;
import swp.se1889.g1.rice_store.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCreatedBy(User createdBy);
    List<Product> findAllByName(String name);
    Optional<Product> findById(Long id);
   // List<Product> findByStoreId(Long storeId);
  // List<Product> findByNameContainingIgnoreCase(String keyword);

}
