package com.hockeyassist.hockeyassist.dto;

import com.hockeyassist.hockeyassist.model.PlayerSeasonAverages;
import java.io.Serializable;
import java.util.UUID;

public class PlayerSeasonAveragesDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID id;
    private Integer nbaPlayerId;
    private String playerName;
    private String seasonId;
    private Double pointsPerGame;
    private Double reboundsPerGame;
    private Double assistsPerGame;
    private Double stealsPerGame;
    private Double blocksPerGame;
    private Double minutesPerGame;
    private Double fieldGoalPct;
    private Double threePointPct;
    private Double freeThrowPct;
    private Double trueShootingPct;
    private Double effectiveFgPct;

    public PlayerSeasonAveragesDTO() {
    }

    public PlayerSeasonAveragesDTO(PlayerSeasonAverages averages) {
        this.id = averages.getId();
        this.seasonId = averages.getSeasonId();
        this.pointsPerGame = averages.getPointsPerGame();
        this.reboundsPerGame = averages.getReboundsPerGame();
        this.assistsPerGame = averages.getAssistsPerGame();
        this.stealsPerGame = averages.getStealsPerGame();
        this.blocksPerGame = averages.getBlocksPerGame();
        this.minutesPerGame = averages.getMinutesPerGame();
        this.fieldGoalPct = averages.getFieldGoalPct();
        this.threePointPct = averages.getThreePointPct();
        this.freeThrowPct = averages.getFreeThrowPct();
        this.trueShootingPct = averages.getTrueShootingPct();
        this.effectiveFgPct = averages.getEffectiveFgPct();

        if (averages.getPlayer() != null) {
            this.nbaPlayerId = averages.getPlayer().getNbaPlayerId();
            this.playerName = averages.getPlayer().getName();
        }
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getNbaPlayerId() {
        return nbaPlayerId;
    }

    public void setNbaPlayerId(Integer nbaPlayerId) {
        this.nbaPlayerId = nbaPlayerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(String seasonId) {
        this.seasonId = seasonId;
    }

    public Double getPointsPerGame() {
        return pointsPerGame;
    }

    public void setPointsPerGame(Double pointsPerGame) {
        this.pointsPerGame = pointsPerGame;
    }

    public Double getReboundsPerGame() {
        return reboundsPerGame;
    }

    public void setReboundsPerGame(Double reboundsPerGame) {
        this.reboundsPerGame = reboundsPerGame;
    }

    public Double getAssistsPerGame() {
        return assistsPerGame;
    }

    public void setAssistsPerGame(Double assistsPerGame) {
        this.assistsPerGame = assistsPerGame;
    }

    public Double getStealsPerGame() {
        return stealsPerGame;
    }

    public void setStealsPerGame(Double stealsPerGame) {
        this.stealsPerGame = stealsPerGame;
    }

    public Double getBlocksPerGame() {
        return blocksPerGame;
    }

    public void setBlocksPerGame(Double blocksPerGame) {
        this.blocksPerGame = blocksPerGame;
    }

    public Double getMinutesPerGame() {
        return minutesPerGame;
    }

    public void setMinutesPerGame(Double minutesPerGame) {
        this.minutesPerGame = minutesPerGame;
    }

    public Double getFieldGoalPct() {
        return fieldGoalPct;
    }

    public void setFieldGoalPct(Double fieldGoalPct) {
        this.fieldGoalPct = fieldGoalPct;
    }

    public Double getThreePointPct() {
        return threePointPct;
    }

    public void setThreePointPct(Double threePointPct) {
        this.threePointPct = threePointPct;
    }

    public Double getFreeThrowPct() {
        return freeThrowPct;
    }

    public void setFreeThrowPct(Double freeThrowPct) {
        this.freeThrowPct = freeThrowPct;
    }

    public Double getTrueShootingPct() {
        return trueShootingPct;
    }

    public void setTrueShootingPct(Double trueShootingPct) {
        this.trueShootingPct = trueShootingPct;
    }

    public Double getEffectiveFgPct() {
        return effectiveFgPct;
    }

    public void setEffectiveFgPct(Double effectiveFgPct) {
        this.effectiveFgPct = effectiveFgPct;
    }
}