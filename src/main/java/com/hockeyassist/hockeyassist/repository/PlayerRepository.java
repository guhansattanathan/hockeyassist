package com.hockeyassist.hockeyassist.repository;

import com.hockeyassist.hockeyassist.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerRepository extends JpaRepository<Player, UUID> {

    // ==========================================
    // FIND BY FIELD
    // ==========================================

    // Find player by NBA ID
    Optional<Player> findByNbaPlayerId(Integer nbaPlayerId);

    // Find player by name (exact match)
    Optional<Player> findByName(String name);

    // Find players by name containing (case insensitive)
    List<Player> findByNameContainingIgnoreCase(String name);

    // Find players by team
    List<Player> findByTeam(String team);

    // Find players by position
    List<Player> findByPosition(String position);

    // Find active players
    List<Player> findByIsActiveTrue();

    // Find inactive players
    List<Player> findByIsActiveFalse();

    // ==========================================
    // EXISTENCE CHECKS
    // ==========================================

    // Check if player exists by NBA ID
    boolean existsByNbaPlayerId(Integer nbaPlayerId);

    // ==========================================
    // DISTINCT VALUES
    // ==========================================

    // Get all distinct teams
    @Query("SELECT DISTINCT p.team FROM Player p WHERE p.team IS NOT NULL ORDER BY p.team")
    List<String> findDistinctTeams();

    // Get all distinct positions
    @Query("SELECT DISTINCT p.position FROM Player p WHERE p.position IS NOT NULL ORDER BY p.position")
    List<String> findDistinctPositions();

    // ==========================================
    // COUNT METHODS
    // ==========================================

    // Count players by team
    long countByTeam(String team);

    // Count active players
    long countByIsActiveTrue();

    // Count inactive players
    long countByIsActiveFalse();

    // ==========================================
    // COMPLEX QUERIES
    // ==========================================

    // Find players by team and position
    List<Player> findByTeamAndPosition(String team, String position);

    // Find active players on a team
    List<Player> findByTeamAndIsActiveTrue(String team);
}