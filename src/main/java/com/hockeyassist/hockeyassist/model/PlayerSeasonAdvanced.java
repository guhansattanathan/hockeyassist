package com.hockeyassist.hockeyassist.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "player_season_advanced")
public class PlayerSeasonAdvanced {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "season_id", nullable = false)
    private String seasonId;

    @Column(name = "games_played")
    private Integer gamesPlayed;

    @Column(name = "usage_rate")
    private Double usageRate;

    @Column(name = "true_shooting_pct")
    private Double trueShootingPct;

    @Column(name = "assist_rate")
    private Double assistRate;

    @Column(name = "rebound_rate")
    private Double reboundRate;

    @Column(name = "effective_fg_pct")
    private Double effectiveFgPct;

    // Constructors
    public PlayerSeasonAdvanced() {
    }

    public PlayerSeasonAdvanced(Player player, String seasonId) {
        this.player = player;
        this.seasonId = seasonId;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public String getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(String seasonId) {
        this.seasonId = seasonId;
    }

    public Integer getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(Integer gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public Double getUsageRate() {
        return usageRate;
    }

    public void setUsageRate(Double usageRate) {
        this.usageRate = usageRate;
    }

    public Double getTrueShootingPct() {
        return trueShootingPct;
    }

    public void setTrueShootingPct(Double trueShootingPct) {
        this.trueShootingPct = trueShootingPct;
    }

    public Double getAssistRate() {
        return assistRate;
    }

    public void setAssistRate(Double assistRate) {
        this.assistRate = assistRate;
    }

    public Double getReboundRate() {
        return reboundRate;
    }

    public void setReboundRate(Double reboundRate) {
        this.reboundRate = reboundRate;
    }

    public Double getEffectiveFgPct() {
        return effectiveFgPct;
    }

    public void setEffectiveFgPct(Double effectiveFgPct) {
        this.effectiveFgPct = effectiveFgPct;
    }
}