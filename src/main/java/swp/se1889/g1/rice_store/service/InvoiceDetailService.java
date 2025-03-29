package swp.se1889.g1.rice_store.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import swp.se1889.g1.rice_store.entity.InvoiceDetail;
import swp.se1889.g1.rice_store.repository.InvoiceDetailRepository;
import swp.se1889.g1.rice_store.repository.InvoiceSaleRepository;

import java.util.List;

@Service
public class InvoiceDetailService {

    @Autowired
    private InvoiceDetailRepository invoiceDetailRepository;


}
