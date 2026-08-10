package com.hockeyassist.hockeyassist.controller;

import com.hockeyassist.hockeyassist.dto.PlayerDTO;
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
    // PLAYER ENDPOINTS
    // ==========================================

    @GetMapping("/players")
    public ResponseEntity<List<PlayerDTO>> getAllPlayers() {
        return ResponseEntity.ok(playerService.getAllPlayers());
    }

    @GetMapping("/players/{nbaPlayerId}")
    public ResponseEntity<PlayerDTO> getPlayerByNbaId(@PathVariable Integer nbaPlayerId) {
        return playerService.getPlayerByNbaId(nbaPlayerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/players/search")
    public ResponseEntity<List<PlayerDTO>> searchPlayers(@RequestParam String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<PlayerDTO> players = playerService.searchPlayers(query.trim());
        return ResponseEntity.ok(players);
    }

    @GetMapping("/players/team/{team}")
    public ResponseEntity<List<PlayerDTO>> getPlayersByTeam(@PathVariable String team) {
        List<PlayerDTO> players = playerService.getPlayersByTeam(team.toUpperCase());
        if (players.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(players);
    }

    @GetMapping("/players/position/{position}")
    public ResponseEntity<List<PlayerDTO>> getPlayersByPosition(@PathVariable String position) {
        List<PlayerDTO> players = playerService.getPlayersByPosition(position.toUpperCase());
        if (players.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(players);
    }

    @GetMapping("/players/active")
    public ResponseEntity<List<PlayerDTO>> getActivePlayers() {
        return ResponseEntity.ok(playerService.getActivePlayers());
    }

    // ==========================================
    // SEASON STATS ENDPOINTS
    // ==========================================

    @GetMapping("/players/{nbaPlayerId}/seasons")
    public ResponseEntity<List<PlayerSeasonStats>> getPlayerSeasons(@PathVariable Integer nbaPlayerId) {
        List<PlayerSeasonStats> stats = statsService.getStatsByNbaPlayerId(nbaPlayerId);
        if (stats.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/players/{nbaPlayerId}/seasons/{seasonId}")
    public ResponseEntity<PlayerSeasonStats> getPlayerSeason(
            @PathVariable Integer nbaPlayerId,
            @PathVariable String seasonId) {
        return playerService.getPlayerByNbaId(nbaPlayerId)
                .flatMap(playerDTO -> {
                    // Need to get the actual Player entity for season stats
                    List<PlayerSeasonStats> stats = statsService.getStatsByNbaPlayerId(nbaPlayerId);
                    return stats.stream()
                            .filter(s -> seasonId.equals(s.getSeasonId()))
                            .findFirst();
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/players/{nbaPlayerId}/career")
    public ResponseEntity<Map<String, Object>> getCareerTotals(@PathVariable Integer nbaPlayerId) {
        Map<String, Object> totals = statsService.getCareerTotals(nbaPlayerId);
        if (totals == null || totals.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(totals);
    }

    @GetMapping("/players/{nbaPlayerId}/averages")
    public ResponseEntity<Map<String, Double>> getCareerAverages(@PathVariable Integer nbaPlayerId) {
        Map<String, Double> averages = statsService.getCareerAverages(nbaPlayerId);
        if (averages == null || averages.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(averages);
    }

    @GetMapping("/players/{nbaPlayerId}/best/{stat}")
    public ResponseEntity<PlayerSeasonStats> getBestSeason(
            @PathVariable Integer nbaPlayerId,
            @PathVariable String stat) {
        return statsService.getBestSeason(nbaPlayerId, stat)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================================
    // LEAGUE LEADERS ENDPOINTS
    // ==========================================

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
    // TEAM ANALYTICS ENDPOINTS
    // ==========================================

    @GetMapping("/teams")
    public ResponseEntity<List<String>> getAllTeams() {
        return ResponseEntity.ok(playerService.getAllTeams());
    }

    @GetMapping("/teams/{team}/players")
    public ResponseEntity<List<PlayerDTO>> getTeamPlayers(@PathVariable String team) {
        List<PlayerDTO> players = playerService.getPlayersByTeam(team.toUpperCase());
        if (players.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(players);
    }

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
    // SEASON ANALYTICS ENDPOINTS
    // ==========================================

    @GetMapping("/seasons")
    public ResponseEntity<List<String>> getAllSeasons() {
        return ResponseEntity.ok(statsService.getAllSeasons());
    }

    @GetMapping("/seasons/{season}/players")
    public ResponseEntity<List<PlayerSeasonStats>> getSeasonPlayers(@PathVariable String season) {
        List<PlayerSeasonStats> stats = statsService.getSeasonPlayers(season);
        if (stats.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(stats);
    }

    // ==========================================
    // COMPARISON ENDPOINTS
    // ==========================================

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