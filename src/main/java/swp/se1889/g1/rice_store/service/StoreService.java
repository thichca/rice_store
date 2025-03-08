package swp.se1889.g1.rice_store.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

        if (storeRepository.findByEmail(storeDTO.getEmail()) != null) {
            redirectAttributes.addFlashAttribute("error", "Email cửa hàng đã tồn tại");
            hasError = true;
        }

        if (storeRepository.findByPhone(storeDTO.getPhone()) != null) {
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

    public Store updateStore(Long storeId, StoreDTO storeDTO, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = (authentication != null) ? authentication.getName() : "Unknown";

        boolean hasError = false;
        Store existingStore = storeRepository.findById(storeId).orElse(null);

        if (existingStore == null) {
            redirectAttributes.addFlashAttribute("error", "Cửa hàng không tồn tại");
            return null;
        }

        if (storeRepository.findByNameAndCreatedBy(storeDTO.getStoreName(), username) != null &&
                !existingStore.getName().equals(storeDTO.getStoreName())) {
            redirectAttributes.addFlashAttribute("error", "Tên cửa hàng đã tồn tại");
            hasError = true;
        }

        if (storeRepository.findByEmail(storeDTO.getEmail()) != null &&
                !existingStore.getEmail().equals(storeDTO.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "Email cửa hàng đã tồn tại");
            hasError = true;
        }

        if (storeRepository.findByPhone(storeDTO.getPhone()) != null &&
                !existingStore.getPhone().equals(storeDTO.getPhone())) {
            redirectAttributes.addFlashAttribute("error", "Số điện thoại cửa hàng đã tồn tại");
            hasError = true;
        }

        if (storeRepository.findByAddressAndCreatedBy(storeDTO.getAddress(), username) != null &&
                !existingStore.getAddress().equals(storeDTO.getAddress())) {
            redirectAttributes.addFlashAttribute("error", "Địa chỉ cửa hàng đã tồn tại");
            hasError = true;
        }

        if (hasError) {
            return null;
        }

        existingStore.setName(storeDTO.getStoreName());
        existingStore.setEmail(storeDTO.getEmail());
        existingStore.setPhone(storeDTO.getPhone());
        existingStore.setAddress(storeDTO.getAddress());

        try {
            storeRepository.save(existingStore);
        } catch (Exception e) {
            throw new RuntimeException("Error while updating store: " + e.getMessage());
        }

        return existingStore;
    }


    public Page<Store> getStoresByUserName(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return storeRepository.findByCreatedByAndIsDeletedFalse(username, pageable);
    }

    public void deleteStore(Long storeId) {
        Store store = storeRepository.findById(storeId).orElse(null);
        if (store != null) {
            store.setDelete(true);
            storeRepository.save(store);
        }
    }

}
