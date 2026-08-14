package com.hockeyassist.hockeyassist.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "player_season_averages")
public class PlayerSeasonAverages {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "season_id", nullable = false)
    private String seasonId;

    // Per-game averages
    @Column(name = "points_per_game")
    private Double pointsPerGame;

    @Column(name = "rebounds_per_game")
    private Double reboundsPerGame;

    @Column(name = "assists_per_game")
    private Double assistsPerGame;

    @Column(name = "steals_per_game")
    private Double stealsPerGame;

    @Column(name = "blocks_per_game")
    private Double blocksPerGame;

    @Column(name = "minutes_per_game")
    private Double minutesPerGame;

    // Percentages
    @Column(name = "field_goal_pct")
    private Double fieldGoalPct;

    @Column(name = "three_point_pct")
    private Double threePointPct;

    @Column(name = "free_throw_pct")
    private Double freeThrowPct;

    // Shooting efficiency
    @Column(name = "true_shooting_pct")
    private Double trueShootingPct;  // Advanced metric

    @Column(name = "effective_fg_pct")
    private Double effectiveFgPct;   // Advanced metric

    // Constructors
    public PlayerSeasonAverages() {}

    public PlayerSeasonAverages(Player player, String seasonId) {
        this.player = player;
        this.seasonId = seasonId;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    public String getSeasonId() { return seasonId; }
    public void setSeasonId(String seasonId) { this.seasonId = seasonId; }

    public Double getPointsPerGame() { return pointsPerGame; }
    public void setPointsPerGame(Double pointsPerGame) { this.pointsPerGame = pointsPerGame; }

    public Double getReboundsPerGame() { return reboundsPerGame; }
    public void setReboundsPerGame(Double reboundsPerGame) { this.reboundsPerGame = reboundsPerGame; }

    public Double getAssistsPerGame() { return assistsPerGame; }
    public void setAssistsPerGame(Double assistsPerGame) { this.assistsPerGame = assistsPerGame; }

    public Double getStealsPerGame() { return stealsPerGame; }
    public void setStealsPerGame(Double stealsPerGame) { this.stealsPerGame = stealsPerGame; }

    public Double getBlocksPerGame() { return blocksPerGame; }
    public void setBlocksPerGame(Double blocksPerGame) { this.blocksPerGame = blocksPerGame; }

    public Double getMinutesPerGame() { return minutesPerGame; }
    public void setMinutesPerGame(Double minutesPerGame) { this.minutesPerGame = minutesPerGame; }

    public Double getFieldGoalPct() { return fieldGoalPct; }
    public void setFieldGoalPct(Double fieldGoalPct) { this.fieldGoalPct = fieldGoalPct; }

    public Double getThreePointPct() { return threePointPct; }
    public void setThreePointPct(Double threePointPct) { this.threePointPct = threePointPct; }

    public Double getFreeThrowPct() { return freeThrowPct; }
    public void setFreeThrowPct(Double freeThrowPct) { this.freeThrowPct = freeThrowPct; }

    public Double getTrueShootingPct() { return trueShootingPct; }
    public void setTrueShootingPct(Double trueShootingPct) { this.trueShootingPct = trueShootingPct; }

    public Double getEffectiveFgPct() { return effectiveFgPct; }
    public void setEffectiveFgPct(Double effectiveFgPct) { this.effectiveFgPct = effectiveFgPct; }
}