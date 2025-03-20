package swp.se1889.g1.rice_store.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import swp.se1889.g1.rice_store.entity.Product;
import swp.se1889.g1.rice_store.entity.Store;
import swp.se1889.g1.rice_store.entity.Zone;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long> {

    @Modifying
    @Transactional
    @Query(value = "UPDATE zones SET is_deleted = 0 WHERE created_at < :date", nativeQuery = true)
    int updateIsDeletedBeforeDate(@Param("date") LocalDateTime date);
    Page<Zone> findByStoreId(Long storeId, Pageable pageable);

    Optional<Zone> findByIdAndIsDeletedFalse(Long id);
    Page<Zone> findByStoreAndIsDeletedFalse(Store store , Pageable pageable);
   // Tìm khu vực theo tên và chỉ lấy những khu vực chưa bị xóa
   List<Zone> findByNameContainingIgnoreCaseAndStoreAndIsDeletedFalse(String name, Store store);
   List<Zone> findByStore(Store store);
}
