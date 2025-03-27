package swp.se1889.g1.rice_store.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import swp.se1889.g1.rice_store.entity.Store;

import java.util.List;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    @Modifying
    @Transactional
    @Query("UPDATE Store s SET s.isDeleted = :isDeleted WHERE s.id = :storeId")
    int updateStoreStatus(@Param("storeId") Long storeId, @Param("isDeleted") boolean isDeleted);

    List<Store> findByCreatedBy(String username);

    Store findByNameAndCreatedBy(String name, String createdBy);

    Store findByEmail(String email);

    Store findByPhone(String phone);

    Store findByAddressAndCreatedBy(String address, String createdBy);

    Page<Store> findByCreatedByAndIsDeletedFalse(String createdBy, Pageable pageable);

    Store findById(long id);
}

