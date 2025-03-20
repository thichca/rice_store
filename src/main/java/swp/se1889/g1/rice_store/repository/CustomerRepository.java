package swp.se1889.g1.rice_store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import swp.se1889.g1.rice_store.dto.CustomerDTO;
import swp.se1889.g1.rice_store.entity.Customer;
import swp.se1889.g1.rice_store.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    @Query("SELECT new swp.se1889.g1.rice_store.dto.CustomerDTO(c.id, c.name, c.phone, c.address, c.email, c.debtBalance, c.createdBy.username, c.updatedBy) FROM Customer c WHERE c.createdBy = :currentUser")
    List<CustomerDTO> findCustomersByCurrentUser(@Param("currentUser") User currentUser);
    Optional<Customer> findById(Long id);
    List<Customer> findByPhone(String phone);
    List<Customer> findByemail(String email);

}
