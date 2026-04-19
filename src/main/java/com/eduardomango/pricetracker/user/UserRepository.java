package com.eduardomango.pricetracker.user;

import com.eduardomango.pricetracker.user.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByExternalId(UUID externalId);
    void deleteByExternalId(UUID externalId);

    Optional<UserEntity> findByExternalId(UUID userId);
}
