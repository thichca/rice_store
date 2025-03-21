package swp.se1889.g1.rice_store.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import swp.se1889.g1.rice_store.entity.Invoices;
import swp.se1889.g1.rice_store.entity.InvoicesDetails;
import swp.se1889.g1.rice_store.entity.Store;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceDetailRepository extends JpaRepository<InvoicesDetails, Long> {
    Page<InvoicesDetails> findAllByIsDeletedFalse(Pageable pageable);

    Optional<InvoicesDetails> findById(Long id);
}
