package com.hockeyassist.hockeyassist.controller;

import com.hockeyassist.hockeyassist.dto.PlayerDTO;
import com.hockeyassist.hockeyassist.dto.PlayerSeasonAdvancedDTO;
import com.hockeyassist.hockeyassist.dto.PlayerSeasonAveragesDTO;
import com.hockeyassist.hockeyassist.dto.PlayerSeasonStatsDTO;
import com.hockeyassist.hockeyassist.dto.PlayerShotDTO;
import com.hockeyassist.hockeyassist.model.PlayerHeadshot;
import com.hockeyassist.hockeyassist.model.PlayerSeasonStats;
import com.hockeyassist.hockeyassist.service.PlayerService;
import com.hockeyassist.hockeyassist.service.ShotService;
import com.hockeyassist.hockeyassist.service.PlayerSeasonStatsService;
import com.hockeyassist.hockeyassist.service.HeadshotService;
import com.hockeyassist.hockeyassist.service.PlayerSeasonAdvancedService;
import com.hockeyassist.hockeyassist.service.PlayerSeasonAveragesService; // ✅ Added import
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final PlayerService playerService;
    private final PlayerSeasonStatsService statsService;
    private final HeadshotService headshotService;
    private final PlayerSeasonAveragesService averagesService; // ✅ Added
    private final PlayerSeasonAdvancedService advancedService;
    private final ShotService shotService;

    public StatsController(PlayerService playerService,
            PlayerSeasonStatsService statsService,
            HeadshotService headshotService,
            PlayerSeasonAveragesService averagesService,
            PlayerSeasonAdvancedService advancedService,
            ShotService shotService) {
        this.playerService = playerService;
        this.statsService = statsService;
        this.headshotService = headshotService;
        this.averagesService = averagesService;
        this.advancedService = advancedService;
        this.shotService = shotService;
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
    public ResponseEntity<List<PlayerSeasonStatsDTO>> getPlayerSeasons(@PathVariable Integer nbaPlayerId) {
        List<PlayerSeasonStatsDTO> stats = statsService.getStatsByNbaPlayerId(nbaPlayerId);
        if (stats.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/players/{nbaPlayerId}/seasons/{seasonId}")
    public ResponseEntity<PlayerSeasonStatsDTO> getPlayerSeason(
            @PathVariable Integer nbaPlayerId,
            @PathVariable String seasonId) {
        return playerService.getPlayerByNbaId(nbaPlayerId)
                .flatMap(playerDTO -> {
                    List<PlayerSeasonStatsDTO> stats = statsService.getStatsByNbaPlayerId(nbaPlayerId);
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
    public ResponseEntity<List<PlayerSeasonStatsDTO>> getLeagueLeaders(
            @PathVariable String season,
            @PathVariable String stat,
            @RequestParam(defaultValue = "10") Integer limit) {
        List<PlayerSeasonStatsDTO> leaders = statsService.getLeagueLeaders(season, stat, limit);
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

    // ==========================================
    // HEADSHOT ENDPOINTS
    // ==========================================

    @GetMapping("/players/{nbaPlayerId}/headshot")
    public ResponseEntity<?> getPlayerHeadshot(@PathVariable Integer nbaPlayerId) {
        String headshotUrl = headshotService.getHeadshotUrl(nbaPlayerId);
        if (headshotUrl == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(headshotUrl);
    }

    @GetMapping("/players/{nbaPlayerId}/headshot-image")
    public ResponseEntity<byte[]> getPlayerHeadshotImage(@PathVariable Integer nbaPlayerId) {
        Optional<PlayerHeadshot> headshot = headshotService.getHeadshotByPlayerId(nbaPlayerId);

        if (headshot.isPresent() && headshot.get().getImageData() != null) {
            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.IMAGE_PNG)
                    .body(headshot.get().getImageData());
        }

        return ResponseEntity.notFound().build();
    }

    // ==========================================
    // SEASON AVERAGES ENDPOINTS
    // ==========================================

    @GetMapping("/players/{nbaPlayerId}/averages/seasons")
    public ResponseEntity<List<PlayerSeasonAveragesDTO>> getPlayerAverages(@PathVariable Integer nbaPlayerId) {
        List<PlayerSeasonAveragesDTO> averages = averagesService.getAveragesByNbaPlayerId(nbaPlayerId);
        if (averages.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(averages);
    }

    // ==========================================
    // ADVANCED METRICS ENDPOINTS
    // ==========================================

    @GetMapping("/players/{nbaPlayerId}/advanced/seasons")
    public ResponseEntity<List<PlayerSeasonAdvancedDTO>> getPlayerAdvancedMetrics(@PathVariable Integer nbaPlayerId) {
        List<PlayerSeasonAdvancedDTO> advanced = advancedService.getAdvancedByNbaPlayerId(nbaPlayerId);
        if (advanced.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(advanced);
    }

    // ==========================================
    // SHOT CHART ENDPOINTS
    // ==========================================

    @GetMapping("/players/{nbaPlayerId}/shots")
    public ResponseEntity<List<PlayerShotDTO>> getPlayerShots(
            @PathVariable Integer nbaPlayerId,
            @RequestParam(required = false) String season) {

        List<PlayerShotDTO> shots;
        if (season != null && !season.isEmpty()) {
            shots = shotService.getShotsByPlayerIdAndSeason(nbaPlayerId, season);
        } else {
            shots = shotService.getShotsByPlayerId(nbaPlayerId);
        }

        if (shots.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(shots);
    }

    @GetMapping("/players/{nbaPlayerId}/shots/seasons")
    public ResponseEntity<List<String>> getShotSeasons(@PathVariable Integer nbaPlayerId) {
        List<String> seasons = shotService.getDistinctSeasonsByPlayerId(nbaPlayerId);
        if (seasons.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(seasons);
    }
}