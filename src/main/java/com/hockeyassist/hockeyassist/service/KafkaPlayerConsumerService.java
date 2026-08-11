package com.hockeyassist.hockeyassist.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hockeyassist.hockeyassist.model.Player;
import com.hockeyassist.hockeyassist.model.PlayerHeadshot;
import com.hockeyassist.hockeyassist.model.PlayerSeasonStats;
import com.hockeyassist.hockeyassist.model.Team;
import com.hockeyassist.hockeyassist.repository.PlayerHeadshotRepository;
import com.hockeyassist.hockeyassist.repository.PlayerRepository;
import com.hockeyassist.hockeyassist.repository.PlayerSeasonStatsRepository;
import com.hockeyassist.hockeyassist.repository.TeamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class KafkaPlayerConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaPlayerConsumerService.class);

    private final PlayerRepository playerRepository;
    private final PlayerSeasonStatsRepository statsRepository;
    private final TeamRepository teamRepository;
    private final PlayerHeadshotRepository headshotRepository; // ✅ Added
    private final ObjectMapper objectMapper;

    public KafkaPlayerConsumerService(PlayerRepository playerRepository,
            PlayerSeasonStatsRepository statsRepository,
            TeamRepository teamRepository,
            PlayerHeadshotRepository headshotRepository) { // ✅ Added to constructor
        this.playerRepository = playerRepository;
        this.statsRepository = statsRepository;
        this.teamRepository = teamRepository;
        this.headshotRepository = headshotRepository;
        this.objectMapper = new ObjectMapper();
    }

    @KafkaListener(topics = "player-stats", groupId = "hockey-assist-group", containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void consumePlayerStats(byte[] messageBytes, Acknowledgment ack) {
        try {
            JsonNode root = objectMapper.readTree(messageBytes);

            JsonNode playerInfoNode = root.get("player_info");
            if (playerInfoNode == null) {
                logger.warn("Received message with no player_info. Skipping.");
                ack.acknowledge();
                return;
            }

            Integer nbaPlayerId = playerInfoNode.get("id").asInt();
            String fullName = playerInfoNode.get("full_name").asText();

            logger.info("📥 Processing: {} (ID: {})", fullName, nbaPlayerId);

            // ✅ Get team info
            String teamIdStr = getString(playerInfoNode, "team");
            Team team = null;
            if (teamIdStr != null && !teamIdStr.isEmpty()) {
                try {
                    Integer teamId = Integer.parseInt(teamIdStr);
                    team = teamRepository.findByTeamId(teamId).orElse(null);
                    if (team == null) {
                        logger.warn("⚠️ Team with ID {} not found for player {}", teamId, fullName);
                    }
                } catch (NumberFormatException e) {
                    team = teamRepository.findByAbbreviation(teamIdStr).orElse(null);
                    if (team == null) {
                        logger.warn("⚠️ Team with abbreviation {} not found for player {}", teamIdStr, fullName);
                    }
                }
            }

            String position = getString(playerInfoNode, "position");
            Boolean isActive = playerInfoNode.has("is_active") ? playerInfoNode.get("is_active").asBoolean() : true;

            // ✅ UPSERT Player
            Optional<Player> existingPlayer = playerRepository.findByNbaPlayerId(nbaPlayerId);
            Player player;

            if (existingPlayer.isPresent()) {
                // 🔄 UPDATE existing player
                player = existingPlayer.get();
                player.setName(fullName);
                player.setFirstName(getString(playerInfoNode, "first_name"));
                player.setLastName(getString(playerInfoNode, "last_name"));
                player.setIsActive(isActive);
                player.setTeam(team);
                player.setPosition(position);
                player = playerRepository.save(player);
                logger.info("🔄 Updated player: {} ({})", player.getName(),
                        team != null ? team.getAbbreviation() : "N/A");
            } else {
                // ✅ INSERT new player
                player = new Player();
                player.setNbaPlayerId(nbaPlayerId);
                player.setName(fullName);
                player.setFirstName(getString(playerInfoNode, "first_name"));
                player.setLastName(getString(playerInfoNode, "last_name"));
                player.setIsActive(isActive);
                player.setTeam(team);
                player.setPosition(position);
                player = playerRepository.save(player);
                logger.info("✅ Added new player: {} ({})", player.getName(),
                        team != null ? team.getAbbreviation() : "N/A");
            }

            // ✅ Save Headshot URL
            String headshotUrl = getString(playerInfoNode, "headshot_url");
            if (headshotUrl != null) {
                Optional<PlayerHeadshot> existingHeadshot = headshotRepository.findByPlayerId(nbaPlayerId);
                PlayerHeadshot headshot;
                if (existingHeadshot.isPresent()) {
                    headshot = existingHeadshot.get();
                    headshot.setImageUrl(headshotUrl);
                    logger.debug("🔄 Updated headshot URL for player {}", fullName);
                } else {
                    headshot = new PlayerHeadshot(nbaPlayerId);
                    headshot.setImageUrl(headshotUrl);
                    logger.debug("✅ Added headshot URL for player {}", fullName);
                }
                headshotRepository.save(headshot);
            }

            // ✅ UPSERT Season Stats
            JsonNode resultSets = root.get("stats").get("resultSets");
            if (resultSets != null && resultSets.isArray()) {
                List<PlayerSeasonStats> statsList = new ArrayList<>();

                for (JsonNode resultSet : resultSets) {
                    if ("SeasonTotalsRegularSeason".equals(resultSet.get("name").asText())) {
                        JsonNode rows = resultSet.get("rowSet");
                        if (rows != null && rows.isArray()) {
                            for (JsonNode row : rows) {
                                String seasonId = getStringFromRow(row, 1);

                                // ✅ Check if season already exists
                                List<PlayerSeasonStats> existing = statsRepository
                                        .findByPlayerAndSeasonId(player, seasonId);

                                PlayerSeasonStats seasonStats;

                                if (!existing.isEmpty()) {
                                    // 🔄 UPDATE existing season
                                    seasonStats = existing.get(0);
                                    logger.debug("🔄 Updating season {} for {}", seasonId, player.getName());
                                } else {
                                    // ✅ INSERT new season
                                    seasonStats = new PlayerSeasonStats();
                                    seasonStats.setPlayer(player);
                                    seasonStats.setSeasonId(seasonId);
                                    logger.debug("✅ Adding new season {} for {}", seasonId, player.getName());
                                }

                                // Update all fields (works for both insert and update)
                                seasonStats.setTeamAbbreviation(getStringFromRow(row, 4));
                                seasonStats.setPlayerAge(getDoubleFromRow(row, 5));
                                seasonStats.setGamesPlayed(getIntFromRow(row, 6));
                                seasonStats.setGamesStarted(getIntFromRow(row, 7));
                                seasonStats.setMinutes(getIntFromRow(row, 8));
                                seasonStats.setFieldGoalsMade(getIntFromRow(row, 9));
                                seasonStats.setFieldGoalsAttempted(getIntFromRow(row, 10));
                                seasonStats.setFieldGoalPct(getDoubleFromRow(row, 11));
                                seasonStats.setThreePointersMade(getIntFromRow(row, 12));
                                seasonStats.setThreePointersAttempted(getIntFromRow(row, 13));
                                seasonStats.setThreePointPct(getDoubleFromRow(row, 14));
                                seasonStats.setFreeThrowsMade(getIntFromRow(row, 15));
                                seasonStats.setFreeThrowsAttempted(getIntFromRow(row, 16));
                                seasonStats.setFreeThrowPct(getDoubleFromRow(row, 17));
                                seasonStats.setOffensiveRebounds(getIntFromRow(row, 18));
                                seasonStats.setDefensiveRebounds(getIntFromRow(row, 19));
                                seasonStats.setRebounds(getIntFromRow(row, 20));
                                seasonStats.setAssists(getIntFromRow(row, 21));
                                seasonStats.setSteals(getIntFromRow(row, 22));
                                seasonStats.setBlocks(getIntFromRow(row, 23));
                                seasonStats.setTurnovers(getIntFromRow(row, 24));
                                seasonStats.setPersonalFouls(getIntFromRow(row, 25));
                                seasonStats.setPoints(getIntFromRow(row, 26));

                                statsList.add(seasonStats);
                            }
                        }
                        break;
                    }
                }

                if (!statsList.isEmpty()) {
                    statsRepository.saveAll(statsList);
                    logger.info("✅ Saved {} seasons for {}", statsList.size(), player.getName());
                } else {
                    logger.info("ℹ️ No seasons to save for {}", player.getName());
                }
            }

            ack.acknowledge();
            logger.info("✅ Successfully processed: {}", fullName);

        } catch (Exception e) {
            logger.error("❌ Error processing message: {}", e.getMessage(), e);
            // Don't acknowledge - message will be retried
        }
    }

    // Helper methods for extracting values from JSON nodes
    private String getString(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value != null && !value.isNull() ? value.asText() : null;
    }

    private String getStringFromRow(JsonNode row, int index) {
        JsonNode node = row.get(index);
        return node.isNull() ? null : node.asText();
    }

    private Integer getIntFromRow(JsonNode row, int index) {
        JsonNode node = row.get(index);
        if (node.isNull() || "NR".equals(node.asText())) {
            return null;
        }
        try {
            return node.asInt();
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double getDoubleFromRow(JsonNode row, int index) {
        JsonNode node = row.get(index);
        if (node.isNull() || "NR".equals(node.asText())) {
            return null;
        }
        try {
            return node.asDouble();
        } catch (NumberFormatException e) {
            return null;
        }
    }
}