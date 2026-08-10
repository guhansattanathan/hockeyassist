package com.hockeyassist.hockeyassist.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hockeyassist.hockeyassist.model.Team;
import com.hockeyassist.hockeyassist.repository.TeamRepository;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

@Service
public class TeamDataLoaderService {

    private static final Logger logger = LoggerFactory.getLogger(TeamDataLoaderService.class);

    private final TeamRepository teamRepository;

    public TeamDataLoaderService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @PostConstruct
    public void loadTeamData() {
        try {
            logger.info("🏀 Starting team data load...");

            ObjectMapper mapper = new ObjectMapper();
            File jsonFile = new File("data/all_teams_data.json");

            if (!jsonFile.exists()) {
                logger.warn("❌ Team data file not found at: {}", jsonFile.getAbsolutePath());
                logger.warn("   Run 'python scripts/python/fetch_team_data.py' to fetch team data.");
                return;
            }

            JsonNode teams = mapper.readTree(jsonFile);
            int loadedCount = 0;

            for (JsonNode teamNode : teams) {
                Integer teamId = teamNode.get("team_id").asInt();

                // Check if team already exists
                if (teamRepository.existsByTeamId(teamId)) {
                    logger.debug("⏭️ Team {} already loaded. Skipping.", teamId);
                    continue;
                }

                Team team = new Team();
                team.setTeamId(teamId);
                team.setFullName(teamNode.get("full_name").asText());
                team.setAbbreviation(teamNode.get("abbreviation").asText());

                // Optional fields
                if (teamNode.has("city") && !teamNode.get("city").isNull()) {
                    team.setCity(teamNode.get("city").asText());
                }
                if (teamNode.has("state") && !teamNode.get("state").isNull()) {
                    team.setState(teamNode.get("state").asText());
                }
                if (teamNode.has("nickname") && !teamNode.get("nickname").isNull()) {
                    team.setNickname(teamNode.get("nickname").asText());
                }
                if (teamNode.has("conference") && !teamNode.get("conference").isNull()) {
                    team.setConference(teamNode.get("conference").asText());
                }
                if (teamNode.has("division") && !teamNode.get("division").isNull()) {
                    team.setDivision(teamNode.get("division").asText());
                }
                if (teamNode.has("arena") && !teamNode.get("arena").isNull()) {
                    team.setArena(teamNode.get("arena").asText());
                }
                if (teamNode.has("arena_city") && !teamNode.get("arena_city").isNull()) {
                    team.setArenaCity(teamNode.get("arena_city").asText());
                }
                if (teamNode.has("arena_state") && !teamNode.get("arena_state").isNull()) {
                    team.setArenaState(teamNode.get("arena_state").asText());
                }

                teamRepository.save(team);
                loadedCount++;
                logger.info("✅ Saved team: {} ({})", team.getFullName(), team.getAbbreviation());
            }

            logger.info("🎉 Team data load complete! Loaded {} teams.", loadedCount);

        } catch (Exception e) {
            logger.error("❌ Error loading team data: {}", e.getMessage(), e);
        }
    }
}