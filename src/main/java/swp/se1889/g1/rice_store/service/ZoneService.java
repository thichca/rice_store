package swp.se1889.g1.rice_store.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import swp.se1889.g1.rice_store.dto.ZoneDTO;
import swp.se1889.g1.rice_store.entity.Store;
import swp.se1889.g1.rice_store.entity.User;
import swp.se1889.g1.rice_store.entity.Zone;
import swp.se1889.g1.rice_store.repository.StoreRepository;
import swp.se1889.g1.rice_store.repository.UserRepository;
import swp.se1889.g1.rice_store.repository.ZoneRepository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ZoneService {
    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Zone> getAllZones() {
        return zoneRepository.findAll();
    }

    public Zone getZoneById(Long id) {
        return zoneRepository.findByIdAndIsDeletedFalse(id).orElse(null);

    }

    public List<Zone> getZonesByStoreId(Long storeId) {
        Store store = storeRepository.findById(storeId).orElse(null);

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
}
