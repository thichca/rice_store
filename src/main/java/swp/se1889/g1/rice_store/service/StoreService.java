package swp.se1889.g1.rice_store.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import swp.se1889.g1.rice_store.entity.Store;
import swp.se1889.g1.rice_store.repository.StoreRepository;

import java.util.List;

@Service
public class StoreService {

    @Autowired
    private StoreRepository storeRepository;

    public List<Store> getStoresByUser(Long userId) {
        return storeRepository.findByUserId(userId);
    }

    public void createStore(Store store) {
        storeRepository.save(store);
    }

    public String getStoreNameByUserId(Long userId) {
        Store store = storeRepository.findByUserId(userId).stream().findFirst().orElse(null);
        return store != null ? store.getName() : null;
    }

    public Store getStoreById(Long userId) {
        return storeRepository.findByUserId(userId).stream().findFirst().orElse(null);
    }
}
