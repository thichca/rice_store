package swp.se1889.g1.rice_store.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import swp.se1889.g1.rice_store.entity.Store;

import java.util.List;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    List<Store> findByCreatedBy(String username);

    Store findByNameAndCreatedBy(String name, String createdBy);

    Store findByEmail(String email);

    Store findByPhone(String phone);

    Store findByAddressAndCreatedBy(String address, String createdBy);

    Page<Store> findByCreatedByAndIsDeletedFalse(String createdBy, Pageable pageable);

    @Query("SELECT s FROM Store s WHERE " +
            "(COALESCE(:fromId, '') = '' OR s.id >= :fromId) AND " +
            "(COALESCE(:toId, '') = '' OR s.id <= :toId) AND " +
            "(COALESCE(:name, '') = '' OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(COALESCE(:address, '') = '' OR LOWER(s.address) LIKE LOWER(CONCAT('%', :address, '%'))) AND " +
            "(COALESCE(:phone, '') = '' OR s.phone LIKE CONCAT('%', :phone, '%')) AND " +
            "(COALESCE(:email, '') = '' OR LOWER(s.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
            "(COALESCE(:fromCreated, '') = '' OR s.createdAt >= :fromCreated) AND " +
            "(COALESCE(:toCreated, '') = '' OR s.createdAt <= :toCreated) AND " +
            "(COALESCE(:fromUpdated, '') = '' OR s.updatedAt >= :fromUpdated) AND " +
            "(COALESCE(:toUpdated, '') = '' OR s.updatedAt <= :toUpdated)")
    List<Store> findStoresWithFilters(
            @Param("fromId") String fromId,
            @Param("toId") String toId,
            @Param("name") String name,
            @Param("address") String address,
            @Param("phone") String phone,
            @Param("email") String email,
            @Param("fromCreated") String fromCreated,
            @Param("toCreated") String toCreated,
            @Param("fromUpdated") String fromUpdated,
            @Param("toUpdated") String toUpdated);
}

