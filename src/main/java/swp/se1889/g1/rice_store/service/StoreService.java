package swp.se1889.g1.rice_store.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import swp.se1889.g1.rice_store.dto.StoreDTO;
import swp.se1889.g1.rice_store.entity.Store;
import swp.se1889.g1.rice_store.repository.StoreRepository;

import java.util.List;

@Service
public class StoreService {

    @Autowired
    private StoreRepository storeRepository;

    public Store findStoreByStoreId(Long storeId) {
        return storeRepository.findById(storeId).orElse(null);
    }

    public List<Store> getStoresByUserName(String username) {
        return storeRepository.findByCreatedBy(username);
    }

    public Store getStoreByNameAndCreatedBy(String storeName, String username) {
        return storeRepository.findByNameAndCreatedBy(storeName, username);
    }

    public Store createStore(StoreDTO storeDTO, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = (authentication != null) ? authentication.getName() : "Unknown";

        boolean hasError = false;

        if (storeRepository.findByNameAndCreatedBy(storeDTO.getStoreName(), username) != null) {
            redirectAttributes.addFlashAttribute("error", "Tên cửa hàng đã tồn tại");
            hasError = true;
        }

        if (storeRepository.findByEmailAndCreatedBy(storeDTO.getEmail(), username) != null) {
            redirectAttributes.addFlashAttribute("error", "Email cửa hàng đã tồn tại");
            hasError = true;
        }

        if (storeRepository.findByPhoneAndCreatedBy(storeDTO.getPhone(), username) != null) {
            redirectAttributes.addFlashAttribute("error", "Số điện thoại cửa hàng đã tồn tại");
            hasError = true;
        }

        if (storeRepository.findByAddressAndCreatedBy(storeDTO.getAddress(), username) != null) {
            redirectAttributes.addFlashAttribute("error", "Địa chỉ cửa hàng đã tồn tại");
            hasError = true;
        }

        if (hasError) {
            return null;
        }

        Store store = new Store();
        store.setName(storeDTO.getStoreName());
        store.setEmail(storeDTO.getEmail());
        store.setPhone(storeDTO.getPhone());
        store.setAddress(storeDTO.getAddress());
        store.setCreatedBy(username);

        try {
            storeRepository.save(store);
        } catch (Exception e) {
            throw new RuntimeException("Error while saving store: " + e.getMessage());
        }
        return store;
    }

    public void updateStore(Store store) {
        storeRepository.save(store);
    }
}
