package com.learnplatform.service;

import com.learnplatform.entity.KbCollection;
import com.learnplatform.repository.KbCollectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class KbCollectionService {

    @Autowired
    private KbCollectionRepository collectionRepository;

    public List<KbCollection> getCollectionsByUser(String userId) {
        return collectionRepository.findByUserId(userId);
    }

    public KbCollection createCollection(String name, String description, String userId) {
        KbCollection collection = new KbCollection();
        collection.setName(name);
        collection.setDescription(description);
        collection.setUserId(userId);
        collection.setCreatedAt(LocalDateTime.now());
        return collectionRepository.save(collection);
    }

    public void deleteCollection(String id, String userId) {
        collectionRepository.findById(id).ifPresent(c -> {
            if (c.getUserId().equals(userId)) {
                collectionRepository.deleteById(id);
                // TODO: Delete all documents and chunks associated with this collection
            }
        });
    }

    public boolean existsAndBelongsToUser(String collectionId, String userId) {
        return collectionRepository.findById(collectionId)
                .map(c -> c.getUserId().equals(userId))
                .orElse(false);
    }
}
