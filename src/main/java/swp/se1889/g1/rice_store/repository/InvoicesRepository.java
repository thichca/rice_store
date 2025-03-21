package swp.se1889.g1.rice_store.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import swp.se1889.g1.rice_store.entity.Invoices;
import swp.se1889.g1.rice_store.entity.Store;

import java.util.List;
@Repository
public interface InvoicesRepository extends JpaRepository<Invoices, Long> {
    List<Invoices> findByType(Invoices.InvoiceType type);
    Page<Invoices> findByStoreAndType(Store store, Invoices.InvoiceType type, Pageable pageable);
    List<Invoices> findByStoreId(Long storeID);
    Page<Invoices> findById(Long id, Pageable pageable);
    Page<Invoices> findInvoicesByStore(Store store , Pageable pageable);


}
