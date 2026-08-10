package com.hockeyassist.hockeyassist.dto;

import com.hockeyassist.hockeyassist.model.PlayerSeasonStats;

public class PlayerSeasonStatsDTO {

    private String seasonId;
    private Integer nbaPlayerId;
    private String playerName;
    private String teamAbbreviation;

    private Integer gamesPlayed;
    private Integer points;
    private Integer rebounds;
    private Integer assists;
    private Integer steals;
    private Integer blocks;

    private Double fieldGoalPct;
    private Double threePointPct;
    private Double freeThrowPct;

    public PlayerSeasonStatsDTO(PlayerSeasonStats stats) {

        this.seasonId = stats.getSeasonId();

        if (stats.getPlayer() != null) {
            this.nbaPlayerId = stats.getPlayer().getNbaPlayerId();
            this.playerName = stats.getPlayer().getName();
        }

        this.teamAbbreviation = stats.getTeamAbbreviation();

        this.gamesPlayed = stats.getGamesPlayed();
        this.points = stats.getPoints();
        this.rebounds = stats.getRebounds();
        this.assists = stats.getAssists();
        this.steals = stats.getSteals();
        this.blocks = stats.getBlocks();

        this.fieldGoalPct = stats.getFieldGoalPct();
        this.threePointPct = stats.getThreePointPct();
        this.freeThrowPct = stats.getFreeThrowPct();
    }

    public String getSeasonId() {
        return seasonId;
    }

    public Integer getNbaPlayerId() {
        return nbaPlayerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getTeamAbbreviation() {
        return teamAbbreviation;
    }

    public Integer getGamesPlayed() {
        return gamesPlayed;
    }

    public Integer getPoints() {
        return points;
    }

    public Integer getRebounds() {
        return rebounds;
    }

    public Integer getAssists() {
        return assists;
    }

    public Integer getSteals() {
        return steals;
    }

    public Integer getBlocks() {
        return blocks;
    }

    public Double getFieldGoalPct() {
        return fieldGoalPct;
    }

    public Double getThreePointPct() {
        return threePointPct;
    }

    public Double getFreeThrowPct() {
        return freeThrowPct;
    }
}