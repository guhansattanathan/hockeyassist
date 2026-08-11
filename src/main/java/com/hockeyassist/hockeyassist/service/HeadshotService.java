package com.hockeyassist.hockeyassist.service;

import com.hockeyassist.hockeyassist.model.PlayerHeadshot;
import com.hockeyassist.hockeyassist.repository.PlayerHeadshotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class HeadshotService {

    private static final Logger logger = LoggerFactory.getLogger(HeadshotService.class);

    private final PlayerHeadshotRepository headshotRepository;

    public HeadshotService(PlayerHeadshotRepository headshotRepository) {
        this.headshotRepository = headshotRepository;
    }

    /**
     * Get headshot URL for a player (generates URL on the fly)
     */
    public String getHeadshotUrl(Integer playerId) {
        if (playerId == null)
            return null;
        return String.format("https://cdn.nba.com/headshots/nba/latest/260x190/%d.png", playerId);
    }

    /**
     * Get headshot info from database (if stored)
     */
    public Optional<PlayerHeadshot> getHeadshotByPlayerId(Integer playerId) {
        return headshotRepository.findByPlayerId(playerId);
    }

    /**
     * Save or update headshot record
     */
    @Transactional
    public PlayerHeadshot saveHeadshot(Integer playerId, byte[] imageData) {
        Optional<PlayerHeadshot> existing = headshotRepository.findByPlayerId(playerId);

        PlayerHeadshot headshot;
        if (existing.isPresent()) {
            headshot = existing.get();
            headshot.setImageData(imageData);
            headshot.setLastFetched(LocalDateTime.now());
            logger.info("🔄 Updated headshot for player ID: {}", playerId);
        } else {
            headshot = new PlayerHeadshot(playerId);
            headshot.setImageData(imageData);
            headshot.setImageUrl(getHeadshotUrl(playerId));
            logger.info("✅ Added new headshot for player ID: {}", playerId);
        }

        return headshotRepository.save(headshot);
    }

    /**
     * Check if headshot exists for player
     */
    public boolean headshotExists(Integer playerId) {
        return headshotRepository.existsByPlayerId(playerId);
    }
}