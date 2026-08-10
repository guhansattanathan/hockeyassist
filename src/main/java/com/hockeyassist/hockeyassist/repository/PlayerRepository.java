package com.hockeyassist.hockeyassist.repository;

import com.hockeyassist.hockeyassist.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerRepository extends JpaRepository<Player, UUID> {

    // ==========================================
    // FIND BY FIELD
    // ==========================================

    Optional<Player> findByNbaPlayerId(Integer nbaPlayerId);

    Optional<Player> findByName(String name);

    List<Player> findByNameContainingIgnoreCase(String name);

    List<Player> findByTeam(String team);

    List<Player> findByPosition(String position);

    List<Player> findByIsActiveTrue();

    List<Player> findByIsActiveFalse();

    // ==========================================
    // EXISTENCE CHECKS
    // ==========================================

    boolean existsByNbaPlayerId(Integer nbaPlayerId);

    // ==========================================
    // DISTINCT VALUES
    // ==========================================

    @Query("SELECT DISTINCT p.team FROM Player p WHERE p.team IS NOT NULL ORDER BY p.team")
    List<String> findDistinctTeams();

    @Query("SELECT DISTINCT p.position FROM Player p WHERE p.position IS NOT NULL ORDER BY p.position")
    List<String> findDistinctPositions();

    // ==========================================
    // COUNT METHODS
    // ==========================================

    long countByTeam(String team);

    long countByIsActiveTrue();

    long countByIsActiveFalse();

    // ==========================================
    // COMPLEX QUERIES
    // ==========================================

    List<Player> findByTeamAndPosition(String team, String position);

    List<Player> findByTeamAndIsActiveTrue(String team);

    // ✅ Add this if you need to search by team entity
    @Query("SELECT p FROM Player p WHERE p.team.abbreviation = :teamAbbreviation")
    List<Player> findByTeamAbbreviation(@Param("teamAbbreviation") String teamAbbreviation);
}