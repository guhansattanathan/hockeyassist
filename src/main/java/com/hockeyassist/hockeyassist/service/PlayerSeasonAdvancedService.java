package com.hockeyassist.hockeyassist.service;

import com.hockeyassist.hockeyassist.dto.PlayerSeasonAdvancedDTO;
import com.hockeyassist.hockeyassist.repository.PlayerSeasonAdvancedRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlayerSeasonAdvancedService {

    private static final Logger logger = LoggerFactory.getLogger(PlayerSeasonAdvancedService.class);

    private final PlayerSeasonAdvancedRepository advancedRepository;

    public PlayerSeasonAdvancedService(PlayerSeasonAdvancedRepository advancedRepository) {
        this.advancedRepository = advancedRepository;
    }

    /**
     * Get advanced metrics for a player by NBA ID
     */
    @Cacheable(value = "advanced", key = "#nbaPlayerId")
    public List<PlayerSeasonAdvancedDTO> getAdvancedByNbaPlayerId(Integer nbaPlayerId) {
        logger.info("🔍 Fetching advanced metrics for player ID: {}", nbaPlayerId);
        return advancedRepository.findByNbaPlayerId(nbaPlayerId).stream()
                .map(PlayerSeasonAdvancedDTO::new)
                .collect(Collectors.toList());
    }
}