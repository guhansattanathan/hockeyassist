package com.hockeyassist.hockeyassist.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayerStatsMessage {

    @JsonProperty("player_info")
    private PlayerInfoDTO playerInfo;

    @JsonProperty("stats")
    private Map<String, Object> stats;

    // Getters and Setters
    public PlayerInfoDTO getPlayerInfo() {
        return playerInfo;
    }

    public void setPlayerInfo(PlayerInfoDTO playerInfo) {
        this.playerInfo = playerInfo;
    }

    public Map<String, Object> getStats() {
        return stats;
    }

    public void setStats(Map<String, Object> stats) {
        this.stats = stats;
    }
}