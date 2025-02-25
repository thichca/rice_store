package swp.se1889.g1.rice_store.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import swp.se1889.g1.rice_store.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeManagementRepository extends JpaRepository<User, Long>{

    Optional<User> findByUsername(String username);

    Page<User> findByStoreIdAndRole(Long storeId, String role, Pageable pageable);

    Page<User> findByStoreIdAndRoleAndUsernameContainingIgnoreCase(Long storeId, String role, String username, Pageable pageable);

    List<User> findByStoreIdAndRole(Long storeId, String role);


}
