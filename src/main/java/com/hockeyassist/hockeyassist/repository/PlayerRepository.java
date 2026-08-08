package com.hockeyassist.hockeyassist.repository;

import com.hockeyassist.hockeyassist.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PlayerRepository extends JpaRepository<Player, UUID> {

    // Find player by NBA ID
    Optional<Player> findByNbaPlayerId(Integer nbaPlayerId);

    // Find player by name (exact match)
    Optional<Player> findByName(String name);

    // Find players by team
    java.util.List<Player> findByTeam(String team);

    // Check if player exists
    boolean existsByNbaPlayerId(Integer nbaPlayerId);
}