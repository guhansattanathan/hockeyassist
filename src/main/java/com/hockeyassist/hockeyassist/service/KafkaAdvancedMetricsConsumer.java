package com.hockeyassist.hockeyassist.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hockeyassist.hockeyassist.model.Player;
import com.hockeyassist.hockeyassist.model.PlayerSeasonAdvanced;
import com.hockeyassist.hockeyassist.repository.PlayerRepository;
import com.hockeyassist.hockeyassist.repository.PlayerSeasonAdvancedRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class KafkaAdvancedMetricsConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaAdvancedMetricsConsumer.class);

    private final PlayerRepository playerRepository;
    private final PlayerSeasonAdvancedRepository advancedRepository;
    private final ObjectMapper objectMapper;

    public KafkaAdvancedMetricsConsumer(PlayerRepository playerRepository,
            PlayerSeasonAdvancedRepository advancedRepository) {
        this.playerRepository = playerRepository;
        this.advancedRepository = advancedRepository;
        this.objectMapper = new ObjectMapper();
    }

    @KafkaListener(topics = "player_advanced_metrics", groupId = "hockey-assist-group", containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void consumeAdvancedMetrics(byte[] messageBytes, Acknowledgment ack) {
        try {
            JsonNode root = objectMapper.readTree(messageBytes);

            Integer nbaPlayerId = root.get("PLAYER_ID").asInt();
            String seasonId = root.has("SEASON_ID") ? root.get("SEASON_ID").asText() : "2025-26";

            Optional<Player> playerOpt = playerRepository.findByNbaPlayerId(nbaPlayerId);
            if (playerOpt.isEmpty()) {
                logger.warn("⚠️ Player with ID {} not found in database. Skipping.", nbaPlayerId);
                ack.acknowledge();
                return;
            }

            Player player = playerOpt.get();

            PlayerSeasonAdvanced advanced = advancedRepository
                    .findByNbaPlayerIdAndSeasonId(nbaPlayerId, seasonId);

            if (advanced == null) {
                advanced = new PlayerSeasonAdvanced(player, seasonId);
            }

            // Map available advanced metrics
            advanced.setGamesPlayed(getInt(root, "GP"));
            advanced.setUsageRate(getDouble(root, "USG_PCT"));
            advanced.setTrueShootingPct(getDouble(root, "TS_PCT"));
            advanced.setAssistRate(getDouble(root, "AST_PCT"));
            advanced.setReboundRate(getDouble(root, "REB_PCT"));
            advanced.setEffectiveFgPct(getDouble(root, "EFG_PCT"));

            advanced.setPlayer(player);
            advancedRepository.save(advanced);
            logger.debug("✅ Saved advanced metrics for {} season {}", player.getName(), seasonId);

            ack.acknowledge();

        } catch (Exception e) {
            logger.error("❌ Error processing advanced metrics message: {}", e.getMessage(), e);
        }
    }

    private Integer getInt(JsonNode node, String field) {
        if (node.has(field) && !node.get(field).isNull()) {
            return node.get(field).asInt();
        }
        return null;
    }

    private Double getDouble(JsonNode node, String field) {
        if (node.has(field) && !node.get(field).isNull()) {
            return node.get(field).asDouble();
        }
        return null;
    }
}