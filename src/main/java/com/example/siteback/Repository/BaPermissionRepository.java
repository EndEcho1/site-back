package com.example.siteback.Repository;

import com.example.siteback.Entity.BaPermission;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface BaPermissionRepository extends MongoRepository<BaPermission, String> {
    Optional<BaPermission> findByLevel(String level);
}
