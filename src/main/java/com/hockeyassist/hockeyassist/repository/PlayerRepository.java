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

    List<Player> findByPosition(String position);

    List<Player> findByIsActiveTrue();

    List<Player> findByIsActiveFalse();

    // ==========================================
    // TEAM QUERIES
    // ==========================================

    @Query("""
                SELECT p
                FROM Player p
                WHERE p.team.abbreviation = :team
            """)
    List<Player> findByTeamAbbreviation(
            @Param("team") String team);

    @Query("""
                SELECT p
                FROM Player p
                WHERE p.team.abbreviation = :team
                AND p.position = :position
            """)
    List<Player> findByTeamAndPosition(
            @Param("team") String team,
            @Param("position") String position);

    @Query("""
                SELECT p
                FROM Player p
                WHERE p.team.abbreviation = :team
                AND p.isActive = true
            """)
    List<Player> findByTeamAndIsActiveTrue(
            @Param("team") String team);

    @Query("""
                SELECT COUNT(p)
                FROM Player p
                WHERE p.team.abbreviation = :team
            """)
    long countByTeamAbbreviation(
            @Param("team") String team);

    // ==========================================
    // EXISTENCE CHECKS
    // ==========================================

    boolean existsByNbaPlayerId(Integer nbaPlayerId);

    // ==========================================
    // DISTINCT VALUES
    // ==========================================

    @Query("""
                SELECT DISTINCT p.team.abbreviation
                FROM Player p
                WHERE p.team IS NOT NULL
                ORDER BY p.team.abbreviation
            """)
    List<String> findDistinctTeams();

    @Query("""
                SELECT DISTINCT p.position
                FROM Player p
                WHERE p.position IS NOT NULL
                ORDER BY p.position
            """)
    List<String> findDistinctPositions();

    // ==========================================
    // COUNT METHODS
    // ==========================================

    long countByIsActiveTrue();

    long countByIsActiveFalse();

    // ==========================================
    // CACHE-FRIENDLY PLAYER QUERY
    // ==========================================

    @Query("""
                SELECT p
                FROM Player p
                LEFT JOIN FETCH p.team
            """)
    List<Player> findAllWithTeam();

    @Query("""
                SELECT p
                FROM Player p
                LEFT JOIN FETCH p.team
                WHERE p.nbaPlayerId = :nbaPlayerId
            """)
    Optional<Player> findByNbaPlayerIdWithTeam(
            @Param("nbaPlayerId") Integer nbaPlayerId);
}
