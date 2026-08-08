package com.hockeyassist.hockeyassist.repository;

import com.hockeyassist.hockeyassist.model.PlayerSeasonStats;
import com.hockeyassist.hockeyassist.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface PlayerSeasonStatsRepository extends JpaRepository<PlayerSeasonStats, UUID> {

    // Find all stats for a player
    List<PlayerSeasonStats> findByPlayer(Player player);

    // Find stats for a player by NBA ID
    @Query("SELECT s FROM PlayerSeasonStats s WHERE s.player.nbaPlayerId = :nbaPlayerId")
    List<PlayerSeasonStats> findByNbaPlayerId(@Param("nbaPlayerId") Integer nbaPlayerId);

    // Find stats for a player, ordered by season (most recent first)
    List<PlayerSeasonStats> findByPlayerOrderBySeasonIdDesc(Player player);

    // Find stats for a player by season
    List<PlayerSeasonStats> findByPlayerAndSeasonId(Player player, String seasonId);

    // Find career totals for a player
    @Query("SELECT new map(" +
            "SUM(s.gamesPlayed) as totalGames, " +
            "SUM(s.points) as totalPoints, " +
            "SUM(s.rebounds) as totalRebounds, " +
            "SUM(s.assists) as totalAssists, " +
            "SUM(s.steals) as totalSteals, " +
            "SUM(s.blocks) as totalBlocks, " +
            "COUNT(s) as totalSeasons) " +
            "FROM PlayerSeasonStats s WHERE s.player.nbaPlayerId = :nbaPlayerId")
    java.util.Map<String, Object> findCareerTotals(@Param("nbaPlayerId") Integer nbaPlayerId);
}