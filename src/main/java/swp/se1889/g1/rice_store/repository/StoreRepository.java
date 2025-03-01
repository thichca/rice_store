package swp.se1889.g1.rice_store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import swp.se1889.g1.rice_store.entity.Store;

import java.util.List;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    List<Store> findByCreatedBy(String username);

    Store findByNameAndCreatedBy(String name, String createdBy);

    Store findByEmailAndCreatedBy(String email, String createdBy);

    Store findByPhoneAndCreatedBy(String phone, String createdBy);

    Store findByAddressAndCreatedBy(String address, String createdBy);

}