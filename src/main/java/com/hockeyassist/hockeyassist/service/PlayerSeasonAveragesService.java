package com.hockeyassist.hockeyassist.service;

import com.hockeyassist.hockeyassist.dto.PlayerSeasonAveragesDTO;
import com.hockeyassist.hockeyassist.model.Player;
import com.hockeyassist.hockeyassist.model.PlayerSeasonAverages;
import com.hockeyassist.hockeyassist.model.PlayerSeasonStats;
import com.hockeyassist.hockeyassist.repository.PlayerSeasonAveragesRepository;
import com.hockeyassist.hockeyassist.repository.PlayerSeasonStatsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlayerSeasonAveragesService {

    private static final Logger logger = LoggerFactory.getLogger(PlayerSeasonAveragesService.class);

    private final PlayerSeasonAveragesRepository averagesRepository;
    private final PlayerSeasonStatsRepository statsRepository;

    public PlayerSeasonAveragesService(PlayerSeasonAveragesRepository averagesRepository,
            PlayerSeasonStatsRepository statsRepository) {
        this.averagesRepository = averagesRepository;
        this.statsRepository = statsRepository;
    }

    /**
     * Calculate averages from season totals and save to averages table
     */
    @Transactional
    public void calculateAndSaveAverages(Player player) {
        logger.info("📊 Calculating averages for player: {}", player.getName());

        List<PlayerSeasonStats> seasonStats = statsRepository.findByPlayer(player);

        if (seasonStats.isEmpty()) {
            logger.warn("No season stats found for player: {}", player.getName());
            return;
        }

        int savedCount = 0;
        int updatedCount = 0;

        for (PlayerSeasonStats stats : seasonStats) {
            String seasonId = stats.getSeasonId();
            Integer gp = stats.getGamesPlayed();

            if (gp == null || gp == 0) {
                logger.debug("Skipping season {} for {} - no games played", seasonId, player.getName());
                continue;
            }

            // ✅ Fixed: Check if averages already exist
            List<PlayerSeasonAverages> existingAverages = averagesRepository
                    .findByPlayerAndSeasonId(player, seasonId);

            PlayerSeasonAverages averages;
            if (!existingAverages.isEmpty()) {
                // Update existing
                averages = existingAverages.get(0);
                updatedCount++;
            } else {
                // Create new
                averages = new PlayerSeasonAverages(player, seasonId);
                savedCount++;
            }

            // Calculate per-game averages
            averages.setPointsPerGame((double) stats.getPoints() / gp);
            averages.setReboundsPerGame((double) stats.getRebounds() / gp);
            averages.setAssistsPerGame((double) stats.getAssists() / gp);
            averages.setStealsPerGame((double) stats.getSteals() / gp);
            averages.setBlocksPerGame((double) stats.getBlocks() / gp);
            averages.setMinutesPerGame((double) stats.getMinutes() / gp);

            // Shooting percentages (already calculated in totals table)
            averages.setFieldGoalPct(stats.getFieldGoalPct());
            averages.setThreePointPct(stats.getThreePointPct());
            averages.setFreeThrowPct(stats.getFreeThrowPct());

            // Calculate True Shooting % (TS%)
            // TS% = PTS / (2 * (FGA + 0.44 * FTA))
            Double tsPct = null;
            if (stats.getFieldGoalsAttempted() != null && stats.getFieldGoalsAttempted() > 0) {
                double denominator = 2.0 * (stats.getFieldGoalsAttempted() + 0.44 * stats.getFreeThrowsAttempted());
                if (denominator > 0) {
                    tsPct = stats.getPoints() / denominator;
                }
            }
            averages.setTrueShootingPct(tsPct);

            // Calculate Effective FG %
            // eFG% = (FGM + 0.5 * 3PM) / FGA
            Double efgPct = null;
            if (stats.getFieldGoalsAttempted() != null && stats.getFieldGoalsAttempted() > 0) {
                efgPct = (stats.getFieldGoalsMade() + 0.5 * stats.getThreePointersMade())
                        / stats.getFieldGoalsAttempted();
            }
            averages.setEffectiveFgPct(efgPct);

            // Save
            averagesRepository.save(averages);
        }

        logger.info("✅ Averages calculated for {}: {} new, {} updated",
                player.getName(), savedCount, updatedCount);
    }

    /**
     * Get averages for a player by NBA ID
     */
    @Cacheable(value = "averages", key = "#nbaPlayerId")
    public List<PlayerSeasonAveragesDTO> getAveragesByNbaPlayerId(Integer nbaPlayerId) {
        logger.info("🔍 Fetching averages for player ID: {}", nbaPlayerId);
        return averagesRepository.findByNbaPlayerId(nbaPlayerId).stream()
                .map(PlayerSeasonAveragesDTO::new)
                .collect(Collectors.toList());
    }
}