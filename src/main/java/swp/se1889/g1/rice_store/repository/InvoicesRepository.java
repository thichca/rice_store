package swp.se1889.g1.rice_store.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import swp.se1889.g1.rice_store.entity.Invoices;
import swp.se1889.g1.rice_store.entity.Store;
import swp.se1889.g1.rice_store.entity.User;

import java.math.BigDecimal;
import java.util.List;
@Repository
public interface InvoicesRepository extends JpaRepository<Invoices, Long> {
    List<Invoices> findByType(Invoices.InvoiceType type);
    Page<Invoices> findByStoreAndType(Store store, Invoices.InvoiceType type, Pageable pageable);
    List<Invoices> findByStoreId(Long storeID);
    Page<Invoices> findById(Long id, Pageable pageable);
    Page<Invoices> findInvoicesByStore(Store store , Pageable pageable);

    @Query("SELECT MONTH(i.createdAt) AS month, COALESCE(SUM(i.finalAmount), 0) " +
            "FROM Invoices i WHERE i.isDeleted = false " +
            "GROUP BY MONTH(i.createdAt) ORDER BY MONTH(i.createdAt)")
    List<Object[]> getRevenueByMonth();

    @Query("SELECT MONTH(i.createdAt) AS month, COALESCE(SUM(i.finalAmount), 0) " +
            "FROM Invoices i WHERE i.store.id = :storeId AND i.isDeleted = false " +
            "GROUP BY MONTH(i.createdAt) ORDER BY MONTH(i.createdAt)")
    List<Object[]> getRevenueByMonthAndStore(@Param("storeId") Long storeId);


    // Đếm tổng số hóa đơn theo User hiện tại
    @Query("SELECT COUNT(i) FROM Invoices i WHERE i.createdBy = :createdBy AND i.isDeleted = false")
    long countByCreatedBy(@Param("createdBy") User createdBy);

    // Đếm tổng số hóa đơn theo User + Store
    @Query("SELECT COUNT(i) FROM Invoices i WHERE i.createdBy = :createdBy AND i.store.id = :storeId AND i.isDeleted = false")
    long countByUserAndStore(@Param("createdBy") User createdBy, @Param("storeId") Long storeId);

    // Tính tổng doanh thu theo User hiện tại
    @Query("SELECT COALESCE(SUM(i.finalAmount), 0) FROM Invoices i WHERE i.createdBy = :createdBy AND i.isDeleted = false")
    BigDecimal getTotalRevenueByUser(@Param("createdBy") User createdBy);

    // Tính tổng doanh thu theo User + Store
    @Query("SELECT COALESCE(SUM(i.finalAmount), 0) FROM Invoices i WHERE i.createdBy = :createdBy AND i.store.id = :storeId AND i.isDeleted = false")
    BigDecimal getTotalRevenueByUserAndStore(@Param("createdBy") User createdBy, @Param("storeId") Long storeId);
}
