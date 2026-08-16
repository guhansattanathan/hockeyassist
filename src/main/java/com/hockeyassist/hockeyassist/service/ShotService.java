package com.hockeyassist.hockeyassist.service;

import com.hockeyassist.hockeyassist.dto.PlayerShotDTO;
import com.hockeyassist.hockeyassist.repository.PlayerShotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShotService {

    private static final Logger logger = LoggerFactory.getLogger(ShotService.class);

    private final PlayerShotRepository shotRepository;

    public ShotService(PlayerShotRepository shotRepository) {
        this.shotRepository = shotRepository;
    }

    public List<PlayerShotDTO> getShotsByPlayerId(Integer nbaPlayerId) {
        logger.info("🔍 Fetching shots for player ID: {}", nbaPlayerId);
        return shotRepository.findByNbaPlayerId(nbaPlayerId).stream()
                .map(PlayerShotDTO::new)
                .collect(Collectors.toList());
    }

    public List<PlayerShotDTO> getShotsByPlayerIdAndSeason(Integer nbaPlayerId, String seasonId) {
        logger.info("🔍 Fetching shots for player {} season {}", nbaPlayerId, seasonId);
        return shotRepository.findByNbaPlayerIdAndSeason(nbaPlayerId, seasonId).stream()
                .map(PlayerShotDTO::new)
                .collect(Collectors.toList());
    }

    public List<String> getDistinctSeasonsByPlayerId(Integer nbaPlayerId) {
        logger.info("🔍 Fetching distinct seasons for player ID: {}", nbaPlayerId);
        return shotRepository.findDistinctSeasonsByNbaPlayerId(nbaPlayerId);
    }
}