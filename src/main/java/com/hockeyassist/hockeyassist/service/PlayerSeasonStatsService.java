package com.hockeyassist.hockeyassist.service;

import com.hockeyassist.hockeyassist.model.Player;
import com.hockeyassist.hockeyassist.model.PlayerSeasonStats;
import com.hockeyassist.hockeyassist.repository.PlayerSeasonStatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PlayerSeasonStatsService {

    private final PlayerSeasonStatsRepository statsRepository;

    PlayerSeasonStatsService(PlayerSeasonStatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    // Get all season stats
    public List<PlayerSeasonStats> getAllStats() {
        return statsRepository.findAll();
    }

    // Get stats by player
    public List<PlayerSeasonStats> getStatsByPlayer(Player player) {
        return statsRepository.findByPlayer(player);
    }

    // Get stats by NBA player ID
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

    // Get career totals for a player
    public Map<String, Object> getCareerTotals(Integer nbaPlayerId) {
        return statsRepository.findCareerTotals(nbaPlayerId);
    }

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
}