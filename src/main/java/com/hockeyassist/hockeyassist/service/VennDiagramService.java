package com.hockeyassist.hockeyassist.service;

import com.hockeyassist.hockeyassist.dto.PlayerVennDTO;
import com.hockeyassist.hockeyassist.repository.PlayerSeasonStatsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class VennDiagramService {

    private static final Logger logger = LoggerFactory.getLogger(VennDiagramService.class);

    // Default thresholds
    private static final double PPG_THRESHOLD = 20.0;
    private static final double RPG_THRESHOLD = 8.0;
    private static final double APG_THRESHOLD = 6.0;

    private final PlayerSeasonStatsRepository statsRepository;

    public VennDiagramService(PlayerSeasonStatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    public List<PlayerVennDTO> getTwoWayVenn(String seasonId,
            Double ppgThreshold,
            Double rpgThreshold,
            Double apgThreshold) {
        double ppgT = ppgThreshold != null ? ppgThreshold : PPG_THRESHOLD;
        double rpgT = rpgThreshold != null ? rpgThreshold : RPG_THRESHOLD;
        double apgT = apgThreshold != null ? apgThreshold : APG_THRESHOLD;

        logger.info("🔍 Building Venn: PPG>={}, RPG>={}, APG>={} for season {}",
                ppgT, rpgT, apgT, seasonId);

        List<Map<String, Object>> candidates = statsRepository.findTwoWayCandidates(seasonId);

        List<PlayerVennDTO> result = new ArrayList<>();

        for (Map<String, Object> row : candidates) {
            Double ppg = toDouble(row.get("ppg"));
            Double rpg = toDouble(row.get("rpg"));
            Double apg = toDouble(row.get("apg"));

            if (ppg == null || rpg == null || apg == null)
                continue;

            boolean isScorer = ppg >= ppgT;
            boolean isRebounder = rpg >= rpgT;
            boolean isPlaymaker = apg >= apgT;

            // Skip players in zero regions
            if (!isScorer && !isRebounder && !isPlaymaker)
                continue;

            PlayerVennDTO dto = new PlayerVennDTO();
            dto.setName((String) row.get("name"));
            dto.setNbaPlayerId((Integer) row.get("nbaPlayerId"));
            dto.setTeam((String) row.get("team"));
            dto.setPosition((String) row.get("position"));
            dto.setPointsPerGame(round(ppg));
            dto.setReboundsPerGame(round(rpg));
            dto.setAssistsPerGame(round(apg));
            dto.setScorer(isScorer);
            dto.setRebounder(isRebounder);
            dto.setPlaymaker(isPlaymaker);
            dto.setVennRegion(classifyRegion(isScorer, isRebounder, isPlaymaker));
            dto.setHeadshotUrl("https://cdn.nba.com/headshots/nba/latest/260x190/"
                    + dto.getNbaPlayerId() + ".png");

            result.add(dto);
        }

        logger.info("✅ Venn built with {} players", result.size());
        return result;
    }

    private String classifyRegion(boolean s, boolean r, boolean p) {
        if (s && r && p)
            return "all-three";
        if (s && r)
            return "scorer+rebounder";
        if (s && p)
            return "scorer+playmaker";
        if (r && p)
            return "rebounder+playmaker";
        if (s)
            return "scorer-only";
        if (r)
            return "rebounder-only";
        if (p)
            return "playmaker-only";
        return "none";
    }

    private Double toDouble(Object o) {
        return o == null ? null : ((Number) o).doubleValue();
    }

    private double round(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}