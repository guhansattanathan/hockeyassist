package com.hockeyassist.hockeyassist.dto;

import com.hockeyassist.hockeyassist.model.PlayerSeasonStats;
import java.io.Serializable;
import java.util.UUID;

public class PlayerSeasonStatsDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID id;
    private Integer nbaPlayerId;
    private String playerName;
    private String seasonId;
    private String teamAbbreviation;
    private Double playerAge;
    private Integer gamesPlayed;
    private Integer gamesStarted;
    private Integer minutes;
    private Integer fieldGoalsMade;
    private Integer fieldGoalsAttempted;
    private Double fieldGoalPct;
    private Integer threePointersMade;
    private Integer threePointersAttempted;
    private Double threePointPct;
    private Integer freeThrowsMade;
    private Integer freeThrowsAttempted;
    private Double freeThrowPct;
    private Integer offensiveRebounds;
    private Integer defensiveRebounds;
    private Integer rebounds;
    private Integer assists;
    private Integer steals;
    private Integer blocks;
    private Integer turnovers;
    private Integer personalFouls;
    private Integer points;

    // ✅ Default constructor (required for Jackson deserialization)
    public PlayerSeasonStatsDTO() {
    }

    // ✅ Constructor from entity
    public PlayerSeasonStatsDTO(PlayerSeasonStats stats) {
        this.id = stats.getId();
        this.seasonId = stats.getSeasonId();
        this.teamAbbreviation = stats.getTeamAbbreviation();
        this.playerAge = stats.getPlayerAge();
        this.gamesPlayed = stats.getGamesPlayed();
        this.gamesStarted = stats.getGamesStarted();
        this.minutes = stats.getMinutes();
        this.fieldGoalsMade = stats.getFieldGoalsMade();
        this.fieldGoalsAttempted = stats.getFieldGoalsAttempted();
        this.fieldGoalPct = stats.getFieldGoalPct();
        this.threePointersMade = stats.getThreePointersMade();
        this.threePointersAttempted = stats.getThreePointersAttempted();
        this.threePointPct = stats.getThreePointPct();
        this.freeThrowsMade = stats.getFreeThrowsMade();
        this.freeThrowsAttempted = stats.getFreeThrowsAttempted();
        this.freeThrowPct = stats.getFreeThrowPct();
        this.offensiveRebounds = stats.getOffensiveRebounds();
        this.defensiveRebounds = stats.getDefensiveRebounds();
        this.rebounds = stats.getRebounds();
        this.assists = stats.getAssists();
        this.steals = stats.getSteals();
        this.blocks = stats.getBlocks();
        this.turnovers = stats.getTurnovers();
        this.personalFouls = stats.getPersonalFouls();
        this.points = stats.getPoints();

        if (stats.getPlayer() != null) {
            this.nbaPlayerId = stats.getPlayer().getNbaPlayerId();
            this.playerName = stats.getPlayer().getName();
        }
    }

    // ==========================================
    // GETTERS AND SETTERS
    // ==========================================

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

    public String getTeamAbbreviation() {
        return teamAbbreviation;
    }

    public void setTeamAbbreviation(String teamAbbreviation) {
        this.teamAbbreviation = teamAbbreviation;
    }

    public Double getPlayerAge() {
        return playerAge;
    }

    public void setPlayerAge(Double playerAge) {
        this.playerAge = playerAge;
    }

    public Integer getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(Integer gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public Integer getGamesStarted() {
        return gamesStarted;
    }

    public void setGamesStarted(Integer gamesStarted) {
        this.gamesStarted = gamesStarted;
    }

    public Integer getMinutes() {
        return minutes;
    }

    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }

    public Integer getFieldGoalsMade() {
        return fieldGoalsMade;
    }

    public void setFieldGoalsMade(Integer fieldGoalsMade) {
        this.fieldGoalsMade = fieldGoalsMade;
    }

    public Integer getFieldGoalsAttempted() {
        return fieldGoalsAttempted;
    }

    public void setFieldGoalsAttempted(Integer fieldGoalsAttempted) {
        this.fieldGoalsAttempted = fieldGoalsAttempted;
    }

    public Double getFieldGoalPct() {
        return fieldGoalPct;
    }

    public void setFieldGoalPct(Double fieldGoalPct) {
        this.fieldGoalPct = fieldGoalPct;
    }

    public Integer getThreePointersMade() {
        return threePointersMade;
    }

    public void setThreePointersMade(Integer threePointersMade) {
        this.threePointersMade = threePointersMade;
    }

    public Integer getThreePointersAttempted() {
        return threePointersAttempted;
    }

    public void setThreePointersAttempted(Integer threePointersAttempted) {
        this.threePointersAttempted = threePointersAttempted;
    }

    public Double getThreePointPct() {
        return threePointPct;
    }

    public void setThreePointPct(Double threePointPct) {
        this.threePointPct = threePointPct;
    }

    public Integer getFreeThrowsMade() {
        return freeThrowsMade;
    }

    public void setFreeThrowsMade(Integer freeThrowsMade) {
        this.freeThrowsMade = freeThrowsMade;
    }

    public Integer getFreeThrowsAttempted() {
        return freeThrowsAttempted;
    }

    public void setFreeThrowsAttempted(Integer freeThrowsAttempted) {
        this.freeThrowsAttempted = freeThrowsAttempted;
    }

    public Double getFreeThrowPct() {
        return freeThrowPct;
    }

    public void setFreeThrowPct(Double freeThrowPct) {
        this.freeThrowPct = freeThrowPct;
    }

    public Integer getOffensiveRebounds() {
        return offensiveRebounds;
    }

    public void setOffensiveRebounds(Integer offensiveRebounds) {
        this.offensiveRebounds = offensiveRebounds;
    }

    public Integer getDefensiveRebounds() {
        return defensiveRebounds;
    }

    public void setDefensiveRebounds(Integer defensiveRebounds) {
        this.defensiveRebounds = defensiveRebounds;
    }

    public Integer getRebounds() {
        return rebounds;
    }

    public void setRebounds(Integer rebounds) {
        this.rebounds = rebounds;
    }

    public Integer getAssists() {
        return assists;
    }

    public void setAssists(Integer assists) {
        this.assists = assists;
    }

    public Integer getSteals() {
        return steals;
    }

    public void setSteals(Integer steals) {
        this.steals = steals;
    }

    public Integer getBlocks() {
        return blocks;
    }

    public void setBlocks(Integer blocks) {
        this.blocks = blocks;
    }

    public Integer getTurnovers() {
        return turnovers;
    }

    public void setTurnovers(Integer turnovers) {
        this.turnovers = turnovers;
    }

    public Integer getPersonalFouls() {
        return personalFouls;
    }

    public void setPersonalFouls(Integer personalFouls) {
        this.personalFouls = personalFouls;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }
}