package swp.se1889.g1.rice_store.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import swp.se1889.g1.rice_store.dto.ProductDTO;
import swp.se1889.g1.rice_store.dto.ZoneDTO;
import swp.se1889.g1.rice_store.entity.Product;
import swp.se1889.g1.rice_store.entity.Store;
import swp.se1889.g1.rice_store.entity.User;
import swp.se1889.g1.rice_store.entity.Zone;
import swp.se1889.g1.rice_store.repository.ProductRepository;
import swp.se1889.g1.rice_store.repository.StoreRepository;
import swp.se1889.g1.rice_store.repository.UserRepository;
import swp.se1889.g1.rice_store.repository.ZoneRepository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ZoneService {
    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductService productService;


    public List<Zone> getAllZones() {
        return zoneRepository.findAll();
    }

    public Zone getZoneById(Long id) {
        return zoneRepository.findByIdAndIsDeletedFalse(id).orElse(null);

    }


    public List<Zone> getZonesByStoreId(Store store) {
        return zoneRepository.findByStoreAndIsDeletedFalse(store);
    }

    public void createZone(ZoneDTO zoneDTO, Store store) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Không tìm thấy thông tin người dùng");
        }

        Zone zone = new Zone();
        zone.setName(zoneDTO.getName());
        zone.setStore(store);
        zone.setAddress(zoneDTO.getAddress());
        zone.setCreatedBy(currentUser);
        zone.setQuantity(0);
        zone.setQuantity(zoneDTO.getQuantity());
        zone.setCreatedAt(LocalDateTime.now());
        zone.setUpdatedAt(LocalDateTime.now());
        zoneRepository.save(zone);
    }

    public void updateZone(ZoneDTO zoneDTO, Store store) {
        Zone zone = getZoneById(zoneDTO.getId());
        zone.setName(zoneDTO.getName());
        zone.setAddress(zoneDTO.getAddress());
        zone.setStore(store);
        zone.setUpdatedAt(LocalDateTime.now());
        zoneRepository.save(zone);
    }

    public void deleteZone(Long id) {
        Zone zone = getZoneById(id);
        zone.setIsDeleted(true);
        zoneRepository.save(zone);
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByUsername(username);
        }
        return null;
    }

    public void addInventory(Zone zone, Product product, int quantity ) {
        if (zone == null || product == null) {
            throw new RuntimeException("Zone hoặc Product không hợp lệ");
        }
        if (zone.getProduct() == null) {
            zone.setProduct(product);
            zone.setQuantity(quantity);
        } else if (zone.getProduct().getId().equals(product.getId())) {
            zone.setQuantity(zone.getQuantity() + quantity);
        }
        zone.setUpdatedAt(LocalDateTime.now());
        zoneRepository.save(zone);
    }
}
