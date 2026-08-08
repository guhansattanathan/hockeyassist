package com.hockeyassist.hockeyassist.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hockeyassist.hockeyassist.model.Player;
import com.hockeyassist.hockeyassist.model.PlayerSeasonStats;
import com.hockeyassist.hockeyassist.repository.PlayerRepository;
import com.hockeyassist.hockeyassist.repository.PlayerSeasonStatsRepository;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class DataLoaderService {

    private final PlayerRepository playerRepository;

    private final PlayerSeasonStatsRepository statsRepository;

    DataLoaderService(PlayerRepository playerRepository, PlayerSeasonStatsRepository statsRepository) {
        this.playerRepository = playerRepository;
        this.statsRepository = statsRepository;
    }

    @PostConstruct
    public void loadData() {
        try {
            System.out.println("Starting data load...");

            // 1. Read the JSON file
            ObjectMapper mapper = new ObjectMapper();
            File jsonFile = new File("data/player_data.json");

            if (!jsonFile.exists()) {
                System.out.println("JSON file not found at: " + jsonFile.getAbsolutePath());
                System.out.println("Skipping data load.");
                return;
            }

            JsonNode root = mapper.readTree(jsonFile);

            // 2. Get player info
            JsonNode playerInfo = root.get("player_info");
            if (playerInfo == null) {
                System.out.println("No player_info found in JSON.");
                return;
            }

            Integer nbaPlayerId = playerInfo.get("id").asInt();
            String fullName = playerInfo.get("full_name").asText();
            String firstName = playerInfo.get("first_name").asText();
            String lastName = playerInfo.get("last_name").asText();
            Boolean isActive = playerInfo.get("is_active").asBoolean();

            // 3. Check if player already exists
            if (playerRepository.findByNbaPlayerId(nbaPlayerId).isPresent()) {
                System.out.println("Player " + fullName + " already loaded. Skipping.");
                return;
            }

            // 4. Create and save Player
            Player player = new Player();
            player.setNbaPlayerId(nbaPlayerId);
            player.setName(fullName);
            player.setFirstName(firstName);
            player.setLastName(lastName);
            player.setIsActive(isActive);
            // You can set team and position later when you have that data
            player = playerRepository.save(player);
            System.out.println("✅ Saved player: " + player.getName());

            // 5. Find the SeasonTotalsRegularSeason result set
            JsonNode resultSets = root.get("resultSets");
            JsonNode seasonStats = null;
            for (JsonNode resultSet : resultSets) {
                if ("SeasonTotalsRegularSeason".equals(resultSet.get("name").asText())) {
                    seasonStats = resultSet;
                    break;
                }
            }

            if (seasonStats == null) {
                System.out.println("No season stats found.");
                return;
            }

            // 6. Parse and save each season
            JsonNode rows = seasonStats.get("rowSet");
            List<PlayerSeasonStats> statsList = new ArrayList<>();

            for (JsonNode row : rows) {
                PlayerSeasonStats stats = new PlayerSeasonStats();
                stats.setPlayer(player);

                // Map each column by index
                // Headers: PLAYER_ID, SEASON_ID, LEAGUE_ID, TEAM_ID, TEAM_ABBREVIATION,
                // PLAYER_AGE, GP, GS, MIN, FGM, FGA, FG_PCT, FG3M, FG3A, FG3_PCT,
                // FTM, FTA, FT_PCT, OREB, DREB, REB, AST, STL, BLK, TOV, PF, PTS

                stats.setSeasonId(getString(row, 1));
                stats.setTeamAbbreviation(getString(row, 4));
                stats.setPlayerAge(getDouble(row, 5));
                stats.setGamesPlayed(getInt(row, 6));
                stats.setGamesStarted(getInt(row, 7));
                stats.setMinutes(getInt(row, 8));
                stats.setFieldGoalsMade(getInt(row, 9));
                stats.setFieldGoalsAttempted(getInt(row, 10));
                stats.setFieldGoalPct(getDouble(row, 11));
                stats.setThreePointersMade(getInt(row, 12));
                stats.setThreePointersAttempted(getInt(row, 13));
                stats.setThreePointPct(getDouble(row, 14));
                stats.setFreeThrowsMade(getInt(row, 15));
                stats.setFreeThrowsAttempted(getInt(row, 16));
                stats.setFreeThrowPct(getDouble(row, 17));
                stats.setOffensiveRebounds(getInt(row, 18));
                stats.setDefensiveRebounds(getInt(row, 19));
                stats.setRebounds(getInt(row, 20));
                stats.setAssists(getInt(row, 21));
                stats.setSteals(getInt(row, 22));
                stats.setBlocks(getInt(row, 23));
                stats.setTurnovers(getInt(row, 24));
                stats.setPersonalFouls(getInt(row, 25));
                stats.setPoints(getInt(row, 26));

                statsList.add(stats);
            }

            // 7. Save all season stats
            statsRepository.saveAll(statsList);
            System.out.println("✅ Saved " + statsList.size() + " seasons for " + player.getName());

        } catch (Exception e) {
            System.err.println("❌ Error loading data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper methods to safely extract values from JSON
    private String getString(JsonNode row, int index) {
        JsonNode node = row.get(index);
        return node.isNull() ? null : node.asText();
    }

    private Integer getInt(JsonNode row, int index) {
        JsonNode node = row.get(index);
        if (node.isNull() || node.asText().equals("NR")) {
            return null;
        }
        try {
            return node.asInt();
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double getDouble(JsonNode row, int index) {
        JsonNode node = row.get(index);
        if (node.isNull() || node.asText().equals("NR")) {
            return null;
        }
        try {
            return node.asDouble();
        } catch (NumberFormatException e) {
            return null;
        }
    }
}