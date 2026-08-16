package com.hockeyassist.hockeyassist.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hockeyassist.hockeyassist.model.Player;
import com.hockeyassist.hockeyassist.model.PlayerShot;
import com.hockeyassist.hockeyassist.repository.PlayerRepository;
import com.hockeyassist.hockeyassist.repository.PlayerShotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class KafkaShotConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaShotConsumer.class);

    private final PlayerRepository playerRepository;
    private final PlayerShotRepository shotRepository;
    private final ObjectMapper objectMapper;

    public KafkaShotConsumer(PlayerRepository playerRepository,
            PlayerShotRepository shotRepository) {
        this.playerRepository = playerRepository;
        this.shotRepository = shotRepository;
        this.objectMapper = new ObjectMapper();
    }

    @KafkaListener(topics = "player_shot_data", groupId = "hockey-assist-group", containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void consumeShotData(byte[] messageBytes, Acknowledgment ack) {
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

            PlayerShot shot = new PlayerShot(player, seasonId);
            shot.setGameId(root.has("GAME_ID") ? root.get("GAME_ID").asText() : null);
            shot.setShotMade(root.has("SHOT_MADE_FLAG") && root.get("SHOT_MADE_FLAG").asInt() == 1);
            shot.setLocX(root.has("LOC_X") ? root.get("LOC_X").asDouble() : null);
            shot.setLocY(root.has("LOC_Y") ? root.get("LOC_Y").asDouble() : null);
            shot.setShotZoneBasic(root.has("SHOT_ZONE_BASIC") ? root.get("SHOT_ZONE_BASIC").asText() : null);
            shot.setShotZoneArea(root.has("SHOT_ZONE_AREA") ? root.get("SHOT_ZONE_AREA").asText() : null);
            shot.setShotZoneRange(root.has("SHOT_ZONE_RANGE") ? root.get("SHOT_ZONE_RANGE").asText() : null);
            shot.setShotDistance(root.has("SHOT_DISTANCE") ? root.get("SHOT_DISTANCE").asInt() : null);

            shotRepository.save(shot);
            logger.debug("✅ Saved shot for {} season {}", player.getName(), seasonId);

            ack.acknowledge();

        } catch (Exception e) {
            logger.error("❌ Error processing shot data: {}", e.getMessage(), e);
        }
    }
}