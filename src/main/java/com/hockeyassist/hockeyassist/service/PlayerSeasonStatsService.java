package com.hockeyassist.hockeyassist.service;

import com.hockeyassist.hockeyassist.model.Player;
import com.hockeyassist.hockeyassist.model.PlayerSeasonStats;
import com.hockeyassist.hockeyassist.repository.PlayerSeasonStatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.cache.annotation.Cacheable;

import java.util.*;

@Service
public class PlayerSeasonStatsService {

    private final PlayerSeasonStatsRepository statsRepository;

    PlayerSeasonStatsService(PlayerSeasonStatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    // ==========================================
    // BASIC CRUD OPERATIONS
    // ==========================================

    // Get all season stats
    public List<PlayerSeasonStats> getAllStats() {
        return statsRepository.findAll();
    }

    // Get stats by player
    public List<PlayerSeasonStats> getStatsByPlayer(Player player) {
        return statsRepository.findByPlayer(player);
    }

    // Get stats by NBA player ID
    @Cacheable(value = "seasons", key = "#nbaPlayerId")
    public List<PlayerSeasonStats> getStatsByNbaPlayerId(Integer nbaPlayerId) {
        return statsRepository.findByNbaPlayerId(nbaPlayerId);
    }

    // Get stats by player, most recent first
    public List<PlayerSeasonStats> getStatsByPlayerRecentFirst(Player player) {
        return statsRepository.findByPlayerOrderBySeasonIdDesc(player);
    }

    // Get stats for a specific season
    public List<PlayerSeasonStats> getStatsByPlayerAndSeason(Player player, String seasonId) {
        return statsRepository.findByPlayerAndSeasonId(player, seasonId);
    }

    // ==========================================
    // CAREER STATISTICS
    // ==========================================

    // Get career totals for a player
    @Cacheable(value = "seasons", key = "'career_' + #nbaPlayerId")
    public Map<String, Object> getCareerTotals(Integer nbaPlayerId) {
        return statsRepository.findCareerTotals(nbaPlayerId);
    }

    // Get career averages for a player
    public Map<String, Double> getCareerAverages(Integer nbaPlayerId) {
        List<PlayerSeasonStats> stats = statsRepository.findByNbaPlayerId(nbaPlayerId);
        if (stats.isEmpty())
            return null;

        Map<String, Double> averages = new HashMap<>();
        int seasons = stats.size();

        averages.put("points",
                stats.stream().mapToDouble(s -> s.getPoints() != null ? s.getPoints() : 0).sum() / seasons);
        averages.put("rebounds",
                stats.stream().mapToDouble(s -> s.getRebounds() != null ? s.getRebounds() : 0).sum() / seasons);
        averages.put("assists",
                stats.stream().mapToDouble(s -> s.getAssists() != null ? s.getAssists() : 0).sum() / seasons);
        averages.put("gamesPlayed",
                stats.stream().mapToDouble(s -> s.getGamesPlayed() != null ? s.getGamesPlayed() : 0).sum() / seasons);

        return averages;
    }

    // Get best season for a specific stat
    public Optional<PlayerSeasonStats> getBestSeason(Integer nbaPlayerId, String stat) {
        List<PlayerSeasonStats> stats = statsRepository.findByNbaPlayerId(nbaPlayerId);
        if (stats.isEmpty())
            return Optional.empty();

        return stats.stream()
                .max((s1, s2) -> {
                    Double v1 = getStatValue(s1, stat);
                    Double v2 = getStatValue(s2, stat);
                    return v1.compareTo(v2);
                });
    }

    // ==========================================
    // LEAGUE LEADERS
    // ==========================================

    // Get league leaders for a season and stat
    @Cacheable(value = "leaders", key = "#season + '_' + #stat + '_' + #limit")
    public List<PlayerSeasonStats> getLeagueLeaders(String season, String stat, Integer limit) {
        return statsRepository.findLeagueLeadersBySeason(season, stat, limit);
    }

    // Get all-time leaders for a stat
    public List<Map<String, Object>> getAllTimeLeaders(String stat, Integer limit) {
        return statsRepository.findAllTimeLeaders(stat, limit);
    }

    // ==========================================
    // TEAM ANALYTICS
    // ==========================================

    // Get team averages for a season
    public Map<String, Double> getTeamAverages(String team, String season) {
        List<PlayerSeasonStats> stats = statsRepository.findBySeasonAndTeam(season, team);
        if (stats.isEmpty())
            return null;

        Map<String, Double> averages = new LinkedHashMap<>();
        int count = stats.size();

        averages.put("points",
                stats.stream().mapToDouble(s -> s.getPoints() != null ? s.getPoints() : 0).sum() / count);
        averages.put("rebounds",
                stats.stream().mapToDouble(s -> s.getRebounds() != null ? s.getRebounds() : 0).sum() / count);
        averages.put("assists",
                stats.stream().mapToDouble(s -> s.getAssists() != null ? s.getAssists() : 0).sum() / count);
        averages.put("gamesPlayed",
                stats.stream().mapToDouble(s -> s.getGamesPlayed() != null ? s.getGamesPlayed() : 0).sum() / count);

        return averages;
    }

    // ==========================================
    // SEASON ANALYTICS
    // ==========================================

    // Get all distinct seasons (most recent first)
    public List<String> getAllSeasons() {
        return statsRepository.findAllSeasons();
    }

    // Get all players in a season
    public List<PlayerSeasonStats> getSeasonPlayers(String season) {
        return statsRepository.findSeasonPlayers(season);
    }

    // ==========================================
    // PLAYER COMPARISON
    // ==========================================

    // Compare two players
    public Map<String, Object> comparePlayers(Integer player1Id, Integer player2Id) {
        List<PlayerSeasonStats> p1Stats = statsRepository.findByNbaPlayerId(player1Id);
        List<PlayerSeasonStats> p2Stats = statsRepository.findByNbaPlayerId(player2Id);

        if (p1Stats.isEmpty() || p2Stats.isEmpty())
            return null;

        Map<String, Object> comparison = new LinkedHashMap<>();

        // Get player info
        Player p1 = p1Stats.get(0).getPlayer();
        Player p2 = p2Stats.get(0).getPlayer();

        Map<String, Object> player1Info = new LinkedHashMap<>();
        player1Info.put("name", p1.getName());
        player1Info.put("id", p1.getNbaPlayerId());
        player1Info.put("team", p1.getTeam());
        player1Info.put("position", p1.getPosition());

        Map<String, Object> player2Info = new LinkedHashMap<>();
        player2Info.put("name", p2.getName());
        player2Info.put("id", p2.getNbaPlayerId());
        player2Info.put("team", p2.getTeam());
        player2Info.put("position", p2.getPosition());

        comparison.put("player1", player1Info);
        comparison.put("player2", player2Info);

        // Compare career totals
        Map<String, Map<String, Double>> totals = new LinkedHashMap<>();
        totals.put("player1", getCareerTotalsMap(p1Stats));
        totals.put("player2", getCareerTotalsMap(p2Stats));
        comparison.put("careerTotals", totals);

        // Compare per-game averages
        Map<String, Map<String, Double>> averages = new LinkedHashMap<>();
        averages.put("player1", getCareerAveragesMap(p1Stats));
        averages.put("player2", getCareerAveragesMap(p2Stats));
        comparison.put("careerAverages", averages);

        // Compare best seasons
        Map<String, Map<String, Object>> bestSeasons = new LinkedHashMap<>();
        bestSeasons.put("player1", getBestSeasonMap(p1Stats));
        bestSeasons.put("player2", getBestSeasonMap(p2Stats));
        comparison.put("bestSeasons", bestSeasons);

        // Compare total seasons
        Map<String, Integer> seasons = new LinkedHashMap<>();
        seasons.put("player1", p1Stats.size());
        seasons.put("player2", p2Stats.size());
        comparison.put("totalSeasons", seasons);

        return comparison;
    }

    // Helper method to calculate career totals map
    private Map<String, Double> getCareerTotalsMap(List<PlayerSeasonStats> stats) {
        Map<String, Double> totals = new LinkedHashMap<>();
        totals.put("points", stats.stream().mapToDouble(s -> s.getPoints() != null ? s.getPoints() : 0).sum());
        totals.put("rebounds", stats.stream().mapToDouble(s -> s.getRebounds() != null ? s.getRebounds() : 0).sum());
        totals.put("assists", stats.stream().mapToDouble(s -> s.getAssists() != null ? s.getAssists() : 0).sum());
        totals.put("steals", stats.stream().mapToDouble(s -> s.getSteals() != null ? s.getSteals() : 0).sum());
        totals.put("blocks", stats.stream().mapToDouble(s -> s.getBlocks() != null ? s.getBlocks() : 0).sum());
        totals.put("gamesPlayed",
                stats.stream().mapToDouble(s -> s.getGamesPlayed() != null ? s.getGamesPlayed() : 0).sum());
        return totals;
    }

    // Helper method to calculate career averages map
    private Map<String, Double> getCareerAveragesMap(List<PlayerSeasonStats> stats) {
        Map<String, Double> averages = new LinkedHashMap<>();
        int seasons = stats.size();
        if (seasons == 0)
            return averages;

        averages.put("points",
                stats.stream().mapToDouble(s -> s.getPoints() != null ? s.getPoints() : 0).sum() / seasons);
        averages.put("rebounds",
                stats.stream().mapToDouble(s -> s.getRebounds() != null ? s.getRebounds() : 0).sum() / seasons);
        averages.put("assists",
                stats.stream().mapToDouble(s -> s.getAssists() != null ? s.getAssists() : 0).sum() / seasons);
        averages.put("gamesPlayed",
                stats.stream().mapToDouble(s -> s.getGamesPlayed() != null ? s.getGamesPlayed() : 0).sum() / seasons);
        return averages;
    }

    // Helper method to get best season map
    private Map<String, Object> getBestSeasonMap(List<PlayerSeasonStats> stats) {
        if (stats.isEmpty())
            return Map.of();

        PlayerSeasonStats best = stats.stream()
                .max((s1, s2) -> {
                    int p1 = s1.getPoints() != null ? s1.getPoints() : 0;
                    int p2 = s2.getPoints() != null ? s2.getPoints() : 0;
                    return Integer.compare(p1, p2);
                })
                .orElse(null);

        if (best == null)
            return Map.of();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("season", best.getSeasonId());
        result.put("points", best.getPoints());
        result.put("rebounds", best.getRebounds());
        result.put("assists", best.getAssists());
        result.put("gamesPlayed", best.getGamesPlayed());
        return result;
    }

    // Helper method to get stat value
    private Double getStatValue(PlayerSeasonStats stats, String stat) {
        return switch (stat.toLowerCase()) {
            case "points" -> stats.getPoints() != null ? stats.getPoints().doubleValue() : 0;
            case "rebounds" -> stats.getRebounds() != null ? stats.getRebounds().doubleValue() : 0;
            case "assists" -> stats.getAssists() != null ? stats.getAssists().doubleValue() : 0;
            case "steals" -> stats.getSteals() != null ? stats.getSteals().doubleValue() : 0;
            case "blocks" -> stats.getBlocks() != null ? stats.getBlocks().doubleValue() : 0;
            case "field_goal_pct", "fg_pct" -> stats.getFieldGoalPct() != null ? stats.getFieldGoalPct() : 0;
            case "three_point_pct", "fg3_pct" -> stats.getThreePointPct() != null ? stats.getThreePointPct() : 0;
            case "free_throw_pct", "ft_pct" -> stats.getFreeThrowPct() != null ? stats.getFreeThrowPct() : 0;
            default -> 0.0;
        };
    }

    // ==========================================
    // SAVE OPERATIONS
    // ==========================================

    // Save season stats
    @Transactional
    public PlayerSeasonStats saveStats(PlayerSeasonStats stats) {
        return statsRepository.save(stats);
    }

    // Save multiple season stats
    @Transactional
    public List<PlayerSeasonStats> saveAllStats(List<PlayerSeasonStats> statsList) {
        return statsRepository.saveAll(statsList);
    }

    // Delete stats
    @Transactional
    public void deleteStats(UUID id) {
        statsRepository.deleteById(id);
    }

    // Delete stats by player
    @Transactional
    public void deleteStatsByPlayer(Player player) {
        List<PlayerSeasonStats> stats = statsRepository.findByPlayer(player);
        statsRepository.deleteAll(stats);
    }
}