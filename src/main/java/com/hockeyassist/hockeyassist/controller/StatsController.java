package com.hockeyassist.hockeyassist.controller;

import com.hockeyassist.hockeyassist.model.Player;
import com.hockeyassist.hockeyassist.model.PlayerSeasonStats;
import com.hockeyassist.hockeyassist.service.PlayerService;
import com.hockeyassist.hockeyassist.service.PlayerSeasonStatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final PlayerService playerService;
    private final PlayerSeasonStatsService statsService;

    public StatsController(PlayerService playerService, PlayerSeasonStatsService statsService) {
        this.playerService = playerService;
        this.statsService = statsService;
    }

    // ==========================================
    // 1. PLAYER ENDPOINTS
    // ==========================================

    // GET /api/stats/players - Get all players
    @GetMapping("/players")
    public ResponseEntity<List<Player>> getAllPlayers() {
        return ResponseEntity.ok(playerService.getAllPlayers());
    }

    // GET /api/stats/players/{nbaPlayerId} - Get player by NBA ID
    @GetMapping("/players/{nbaPlayerId}")
    public ResponseEntity<Player> getPlayerByNbaId(@PathVariable Integer nbaPlayerId) {
        return playerService.getPlayerByNbaId(nbaPlayerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/stats/players/search?query=LeBron - Search players by name
    @GetMapping("/players/search")
    public ResponseEntity<List<Player>> searchPlayers(@RequestParam String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<Player> players = playerService.searchPlayers(query.trim());
        return ResponseEntity.ok(players);
    }

    // GET /api/stats/players/team/{team} - Get players by team
    @GetMapping("/players/team/{team}")
    public ResponseEntity<List<Player>> getPlayersByTeam(@PathVariable String team) {
        List<Player> players = playerService.getPlayersByTeam(team.toUpperCase());
        if (players.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(players);
    }

    // GET /api/stats/players/position/{position} - Get players by position
    @GetMapping("/players/position/{position}")
    public ResponseEntity<List<Player>> getPlayersByPosition(@PathVariable String position) {
        List<Player> players = playerService.getPlayersByPosition(position.toUpperCase());
        if (players.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(players);
    }

    // GET /api/stats/players/active - Get all active players
    @GetMapping("/players/active")
    public ResponseEntity<List<Player>> getActivePlayers() {
        return ResponseEntity.ok(playerService.getActivePlayers());
    }

    // ==========================================
    // 2. SEASON STATS ENDPOINTS
    // ==========================================

    // GET /api/stats/players/{nbaPlayerId}/seasons - Get all seasons for a player
    @GetMapping("/players/{nbaPlayerId}/seasons")
    public ResponseEntity<List<PlayerSeasonStats>> getPlayerSeasons(@PathVariable Integer nbaPlayerId) {
        List<PlayerSeasonStats> stats = statsService.getStatsByNbaPlayerId(nbaPlayerId);
        if (stats.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(stats);
    }

    // GET /api/stats/players/{nbaPlayerId}/seasons/{seasonId} - Get specific season
    @GetMapping("/players/{nbaPlayerId}/seasons/{seasonId}")
    public ResponseEntity<PlayerSeasonStats> getPlayerSeason(
            @PathVariable Integer nbaPlayerId,
            @PathVariable String seasonId) {
        return playerService.getPlayerByNbaId(nbaPlayerId)
                .flatMap(player -> {
                    List<PlayerSeasonStats> stats = statsService.getStatsByPlayerAndSeason(player, seasonId);
                    return stats.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(stats.get(0));
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/stats/players/{nbaPlayerId}/career - Get career totals
    @GetMapping("/players/{nbaPlayerId}/career")
    public ResponseEntity<Map<String, Object>> getCareerTotals(@PathVariable Integer nbaPlayerId) {
        Map<String, Object> totals = statsService.getCareerTotals(nbaPlayerId);
        if (totals == null || totals.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(totals);
    }

    // GET /api/stats/players/{nbaPlayerId}/averages - Get career averages
    @GetMapping("/players/{nbaPlayerId}/averages")
    public ResponseEntity<Map<String, Double>> getCareerAverages(@PathVariable Integer nbaPlayerId) {
        Map<String, Double> averages = statsService.getCareerAverages(nbaPlayerId);
        if (averages == null || averages.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(averages);
    }

    // GET /api/stats/players/{nbaPlayerId}/best/{stat} - Get best season for a stat
    @GetMapping("/players/{nbaPlayerId}/best/{stat}")
    public ResponseEntity<PlayerSeasonStats> getBestSeason(
            @PathVariable Integer nbaPlayerId,
            @PathVariable String stat) {
        return statsService.getBestSeason(nbaPlayerId, stat)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================================
    // 3. LEAGUE LEADERS ENDPOINTS
    // ==========================================

    // GET /api/stats/leaders/{season}/{stat}?limit=10 - Get league leaders
    @GetMapping("/leaders/{season}/{stat}")
    public ResponseEntity<List<PlayerSeasonStats>> getLeagueLeaders(
            @PathVariable String season,
            @PathVariable String stat,
            @RequestParam(defaultValue = "10") Integer limit) {
        List<PlayerSeasonStats> leaders = statsService.getLeagueLeaders(season, stat, limit);
        if (leaders.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(leaders);
    }

    // GET /api/stats/leaders/all-time/{stat}?limit=10 - Get all-time leaders
    @GetMapping("/leaders/all-time/{stat}")
    public ResponseEntity<List<Map<String, Object>>> getAllTimeLeaders(
            @PathVariable String stat,
            @RequestParam(defaultValue = "10") Integer limit) {
        List<Map<String, Object>> leaders = statsService.getAllTimeLeaders(stat, limit);
        if (leaders.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(leaders);
    }

    // ==========================================
    // 4. TEAM ANALYTICS ENDPOINTS
    // ==========================================

    // GET /api/stats/teams - Get all teams
    @GetMapping("/teams")
    public ResponseEntity<List<String>> getAllTeams() {
        return ResponseEntity.ok(playerService.getAllTeams());
    }

    // GET /api/stats/teams/{team}/players - Get players on a team
    @GetMapping("/teams/{team}/players")
    public ResponseEntity<List<Player>> getTeamPlayers(@PathVariable String team) {
        List<Player> players = playerService.getPlayersByTeam(team.toUpperCase());
        if (players.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(players);
    }

    // GET /api/stats/teams/{team}/averages/{season} - Get team averages for a
    // season
    @GetMapping("/teams/{team}/averages/{season}")
    public ResponseEntity<Map<String, Double>> getTeamAverages(
            @PathVariable String team,
            @PathVariable String season) {
        Map<String, Double> averages = statsService.getTeamAverages(team, season);
        if (averages == null || averages.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(averages);
    }

    // ==========================================
    // 5. SEASON ANALYTICS ENDPOINTS
    // ==========================================

    // GET /api/stats/seasons - Get all seasons
    @GetMapping("/seasons")
    public ResponseEntity<List<String>> getAllSeasons() {
        return ResponseEntity.ok(statsService.getAllSeasons());
    }

    // GET /api/stats/seasons/{season}/players - Get all players in a season
    @GetMapping("/seasons/{season}/players")
    public ResponseEntity<List<PlayerSeasonStats>> getSeasonPlayers(@PathVariable String season) {
        List<PlayerSeasonStats> stats = statsService.getSeasonPlayers(season);
        if (stats.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(stats);
    }

    // ==========================================
    // 6. COMPARISON ENDPOINTS
    // ==========================================

    // GET /api/stats/compare?player1=203999&player2=2544 - Compare two players
    @GetMapping("/compare")
    public ResponseEntity<Map<String, Object>> comparePlayers(
            @RequestParam Integer player1,
            @RequestParam Integer player2) {
        Map<String, Object> comparison = statsService.comparePlayers(player1, player2);
        if (comparison == null || comparison.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(comparison);
    }
}