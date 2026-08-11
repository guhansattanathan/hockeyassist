package com.hockeyassist.hockeyassist.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hockeyassist.hockeyassist.model.Player;
import com.hockeyassist.hockeyassist.model.PlayerSeasonStats;
import com.hockeyassist.hockeyassist.model.Team;
import com.hockeyassist.hockeyassist.repository.PlayerRepository;
import com.hockeyassist.hockeyassist.repository.PlayerSeasonStatsRepository;
import com.hockeyassist.hockeyassist.repository.TeamRepository;
import org.springframework.stereotype.Service;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DataLoaderService {

    private final PlayerRepository playerRepository;
    private final PlayerSeasonStatsRepository statsRepository;
    private final TeamRepository teamRepository;

    DataLoaderService(PlayerRepository playerRepository,
            PlayerSeasonStatsRepository statsRepository,
            TeamRepository teamRepository) {
        this.playerRepository = playerRepository;
        this.statsRepository = statsRepository;
        this.teamRepository = teamRepository;
    }

    public void loadData() {
        try {
            System.out.println("🚀 Starting data load...");

            ObjectMapper mapper = new ObjectMapper();
            File jsonFile = new File("data/all_players_data.json");

            if (!jsonFile.exists()) {
                System.out.println("❌ all_players_data.json not found. Trying player_data.json...");
                jsonFile = new File("data/player_data.json");
                if (!jsonFile.exists()) {
                    System.out.println("❌ No data file found. Skipping data load.");
                    return;
                }
            }

            JsonNode root = mapper.readTree(jsonFile);

            int totalPlayers = 0;
            int totalSeasons = 0;

            if (root.isArray()) {
                System.out.println("📦 Loading multiple players from all_players_data.json...");
                for (JsonNode playerNode : root) {
                    int count = loadSinglePlayer(playerNode);
                    if (count > 0) {
                        totalPlayers++;
                        totalSeasons += count;
                    }
                }
            } else {
                System.out.println("📦 Loading single player from player_data.json...");
                int count = loadSinglePlayer(root);
                if (count > 0) {
                    totalPlayers = 1;
                    totalSeasons = count;
                }
            }

            System.out.println("✅ Data load complete!");
            System.out.println("   Players loaded: " + totalPlayers);
            System.out.println("   Seasons loaded: " + totalSeasons);

        } catch (Exception e) {
            System.err.println("❌ Error loading data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int loadSinglePlayer(JsonNode node) {
        try {
            JsonNode playerInfo = node.get("player_info");
            if (playerInfo == null) {
                System.out.println("⚠️ No player_info found, skipping...");
                return 0;
            }

            Integer nbaPlayerId = playerInfo.get("id").asInt();
            String fullName = playerInfo.get("full_name").asText();
            String firstName = playerInfo.get("first_name").asText();
            String lastName = playerInfo.get("last_name").asText();
            Boolean isActive = playerInfo.get("is_active").asBoolean();

            // Get team from player_info
            String teamIdStr = playerInfo.has("team") && !playerInfo.get("team").isNull()
                    ? playerInfo.get("team").asText()
                    : null;

            Team team = null;
            if (teamIdStr != null && !teamIdStr.isEmpty()) {
                try {
                    Integer teamId = Integer.parseInt(teamIdStr);
                    team = teamRepository.findByTeamId(teamId).orElse(null);
                    if (team == null) {
                        System.out.println("   ⚠️ Team with ID " + teamId + " not found for " + fullName);
                    }
                } catch (NumberFormatException e) {
                    team = teamRepository.findByAbbreviation(teamIdStr).orElse(null);
                    if (team == null) {
                        System.out.println("   ⚠️ Team with abbreviation " + teamIdStr + " not found for " + fullName);
                    }
                }
            }

            String position = playerInfo.has("position") && !playerInfo.get("position").isNull()
                    ? playerInfo.get("position").asText()
                    : null;

            // ✅ UPSERT Player
            Optional<Player> existingPlayer = playerRepository.findByNbaPlayerId(nbaPlayerId);
            Player player;

            if (existingPlayer.isPresent()) {
                // 🔄 UPDATE existing player
                player = existingPlayer.get();
                player.setName(fullName);
                player.setFirstName(firstName);
                player.setLastName(lastName);
                player.setIsActive(isActive);
                player.setTeam(team);
                player.setPosition(position);
                player = playerRepository.save(player);
                System.out.println("   🔄 Updated player: " + player.getName() +
                        (team != null ? " (" + team.getAbbreviation() + ")" : ""));
            } else {
                // ✅ INSERT new player
                player = new Player();
                player.setNbaPlayerId(nbaPlayerId);
                player.setName(fullName);
                player.setFirstName(firstName);
                player.setLastName(lastName);
                player.setIsActive(isActive);
                player.setTeam(team);
                player.setPosition(position);
                player = playerRepository.save(player);
                System.out.println("   ✅ Added new player: " + player.getName() +
                        (team != null ? " (" + team.getAbbreviation() + ")" : ""));
            }

            // Find season stats
            JsonNode resultSets = node.get("resultSets");
            JsonNode seasonStats = null;
            for (JsonNode resultSet : resultSets) {
                if ("SeasonTotalsRegularSeason".equals(resultSet.get("name").asText())) {
                    seasonStats = resultSet;
                    break;
                }
            }

            if (seasonStats == null) {
                System.out.println("   ⚠️ No season stats found for " + fullName);
                return 0;
            }

            // ✅ UPSERT Seasons
            JsonNode rows = seasonStats.get("rowSet");
            List<PlayerSeasonStats> statsList = new ArrayList<>();

            for (JsonNode row : rows) {
                String seasonId = getString(row, 1);

                // Check if season already exists
                List<PlayerSeasonStats> existing = statsRepository
                        .findByPlayerAndSeasonId(player, seasonId);

                PlayerSeasonStats stats;

                if (!existing.isEmpty()) {
                    // 🔄 UPDATE existing season
                    stats = existing.get(0);
                } else {
                    // ✅ INSERT new season
                    stats = new PlayerSeasonStats();
                    stats.setPlayer(player);
                    stats.setSeasonId(seasonId);
                }

                // Update all fields
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

            if (!statsList.isEmpty()) {
                statsRepository.saveAll(statsList);
                System.out.println("   ✅ Saved " + statsList.size() + " seasons for " + player.getName());
            }

            return statsList.size();

        } catch (Exception e) {
            System.err.println("   ❌ Error loading player: " + e.getMessage());
            return 0;
        }
    }

    private String getString(JsonNode row, int index) {
        JsonNode node = row.get(index);
        return node.isNull() ? null : node.asText();
    }

    private Integer getInt(JsonNode row, int index) {
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

    private Double getDouble(JsonNode row, int index) {
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