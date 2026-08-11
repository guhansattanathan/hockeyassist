package com.hockeyassist.hockeyassist.repository;

import com.hockeyassist.hockeyassist.model.PlayerHeadshot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PlayerHeadshotRepository extends JpaRepository<PlayerHeadshot, UUID> {

    Optional<PlayerHeadshot> findByPlayerId(Integer playerId);

    boolean existsByPlayerId(Integer playerId);
}