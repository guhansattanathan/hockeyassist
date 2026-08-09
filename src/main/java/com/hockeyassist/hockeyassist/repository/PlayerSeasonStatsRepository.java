package com.hockeyassist.hockeyassist.repository;

import com.hockeyassist.hockeyassist.model.Player;
import com.hockeyassist.hockeyassist.model.PlayerSeasonStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PlayerSeasonStatsRepository extends JpaRepository<PlayerSeasonStats, UUID> {

    // ==========================================
    // BASIC QUERIES
    // ==========================================

    List<PlayerSeasonStats> findByPlayer(Player player);

    @Query("SELECT s FROM PlayerSeasonStats s WHERE s.player.nbaPlayerId = :nbaPlayerId")
    List<PlayerSeasonStats> findByNbaPlayerId(@Param("nbaPlayerId") Integer nbaPlayerId);

    List<PlayerSeasonStats> findByPlayerOrderBySeasonIdDesc(Player player);

    List<PlayerSeasonStats> findByPlayerAndSeasonId(Player player, String seasonId);

    // ==========================================
    // CAREER TOTALS
    // ==========================================

    @Query("SELECT new map(" +
            "SUM(s.gamesPlayed) as totalGames, " +
            "SUM(s.points) as totalPoints, " +
            "SUM(s.rebounds) as totalRebounds, " +
            "SUM(s.assists) as totalAssists, " +
            "SUM(s.steals) as totalSteals, " +
            "SUM(s.blocks) as totalBlocks, " +
            "COUNT(s) as totalSeasons) " +
            "FROM PlayerSeasonStats s WHERE s.player.nbaPlayerId = :nbaPlayerId")
    Map<String, Object> findCareerTotals(@Param("nbaPlayerId") Integer nbaPlayerId);

    // ==========================================
    // LEAGUE LEADERS
    // ==========================================

    @Query("SELECT s FROM PlayerSeasonStats s " +
            "WHERE s.seasonId = :season " +
            "ORDER BY " +
            "CASE WHEN :stat = 'points' THEN s.points " +
            "     WHEN :stat = 'rebounds' THEN s.rebounds " +
            "     WHEN :stat = 'assists' THEN s.assists " +
            "     WHEN :stat = 'steals' THEN s.steals " +
            "     WHEN :stat = 'blocks' THEN s.blocks " +
            "END DESC")
    List<PlayerSeasonStats> findLeagueLeadersBySeason(
            @Param("season") String season,
            @Param("stat") String stat,
            @Param("limit") Integer limit);

    @Query("SELECT new map(" +
            "s.player.name as name, " +
            "s.player.nbaPlayerId as playerId, " +
            "SUM(s.points) as totalPoints) " +
            "FROM PlayerSeasonStats s " +
            "GROUP BY s.player.name, s.player.nbaPlayerId " +
            "ORDER BY totalPoints DESC")
    List<Map<String, Object>> findAllTimeLeaders(
            @Param("stat") String stat,
            @Param("limit") Integer limit);

    // ==========================================
    // TEAM ANALYTICS
    // ==========================================

    @Query("SELECT s FROM PlayerSeasonStats s WHERE s.seasonId = :season AND s.teamAbbreviation = :team")
    List<PlayerSeasonStats> findBySeasonAndTeam(
            @Param("season") String season,
            @Param("team") String team);

    // ==========================================
    // SEASON ANALYTICS
    // ==========================================

    @Query("SELECT DISTINCT s.seasonId FROM PlayerSeasonStats s ORDER BY s.seasonId DESC")
    List<String> findAllSeasons();

    @Query("SELECT s FROM PlayerSeasonStats s WHERE s.seasonId = :season")
    List<PlayerSeasonStats> findSeasonPlayers(@Param("season") String season);

    // ==========================================
    // STATS BY POSITION
    // ==========================================

    @Query("SELECT s FROM PlayerSeasonStats s WHERE s.player.position = :position AND s.seasonId = :season")
    List<PlayerSeasonStats> findByPositionAndSeason(
            @Param("position") String position,
            @Param("season") String season);
}