package com.hockeyassist.hockeyassist.dto;

import com.hockeyassist.hockeyassist.model.PlayerSeasonAdvanced;
import java.io.Serializable;
import java.util.UUID;

public class PlayerSeasonAdvancedDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID id;
    private String seasonId;
    private Integer gamesPlayed;
    private Double usageRate;
    private Double trueShootingPct;
    private Double assistRate;
    private Double reboundRate;
    private Double effectiveFgPct;

    public PlayerSeasonAdvancedDTO() {}

    public PlayerSeasonAdvancedDTO(PlayerSeasonAdvanced advanced) {
        this.id = advanced.getId();
        this.seasonId = advanced.getSeasonId();
        this.gamesPlayed = advanced.getGamesPlayed();
        this.usageRate = advanced.getUsageRate();
        this.trueShootingPct = advanced.getTrueShootingPct();
        this.assistRate = advanced.getAssistRate();
        this.reboundRate = advanced.getReboundRate();
        this.effectiveFgPct = advanced.getEffectiveFgPct();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getSeasonId() { return seasonId; }
    public void setSeasonId(String seasonId) { this.seasonId = seasonId; }

    public Integer getGamesPlayed() { return gamesPlayed; }
    public void setGamesPlayed(Integer gamesPlayed) { this.gamesPlayed = gamesPlayed; }

    public Double getUsageRate() { return usageRate; }
    public void setUsageRate(Double usageRate) { this.usageRate = usageRate; }

    public Double getTrueShootingPct() { return trueShootingPct; }
    public void setTrueShootingPct(Double trueShootingPct) { this.trueShootingPct = trueShootingPct; }

    public Double getAssistRate() { return assistRate; }
    public void setAssistRate(Double assistRate) { this.assistRate = assistRate; }

    public Double getReboundRate() { return reboundRate; }
    public void setReboundRate(Double reboundRate) { this.reboundRate = reboundRate; }

    public Double getEffectiveFgPct() { return effectiveFgPct; }
    public void setEffectiveFgPct(Double effectiveFgPct) { this.effectiveFgPct = effectiveFgPct; }
}