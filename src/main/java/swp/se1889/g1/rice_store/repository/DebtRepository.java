package swp.se1889.g1.rice_store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import swp.se1889.g1.rice_store.entity.Customer;
import swp.se1889.g1.rice_store.entity.DebtRecord;

import java.util.List;
import java.util.Optional;


@Repository
public interface DebtRepository extends JpaRepository<DebtRecord, Long> {
    List<DebtRecord> findByCustomerId(Long customerId);
    List<DebtRecord> findByCustomerIdAndStoreId(Long customerId, Long storeId);
    List<DebtRecord> findByCustomerIdOrderByCreatedAtAsc(Long customerId);
}



