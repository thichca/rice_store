package swp.se1889.g1.rice_store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swp.se1889.g1.rice_store.entity.DebtRecords;

import java.util.List;
import java.util.Optional;

public interface DebtRecordRepository extends JpaRepository<DebtRecords, Long> {
   //List<DebtRecords> findByUserId(User createdBy);
    Optional<DebtRecords> findById(Long id);
    List<DebtRecords> findByCustomerId(Long customerId);


}
