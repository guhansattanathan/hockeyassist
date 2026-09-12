package com.hockeyassist.hockeyassist.dto;

import java.io.Serializable;

public class PlayerVennDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private Integer nbaPlayerId;
    private String team;
    private String position;
    private String headshotUrl;

    // Stats
    private Double pointsPerGame;
    private Double reboundsPerGame;
    private Double assistsPerGame;

    // Membership flags
    private boolean isScorer; // PPG >= threshold
    private boolean isRebounder; // RPG >= threshold
    private boolean isPlaymaker; // APG >= threshold

    // Region classification
    // e.g. "scorer-only", "scorer+rebounder", "all-three"
    private String vennRegion;

    public PlayerVennDTO() {
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getNbaPlayerId() {
        return nbaPlayerId;
    }

    public void setNbaPlayerId(Integer nbaPlayerId) {
        this.nbaPlayerId = nbaPlayerId;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getHeadshotUrl() {
        return headshotUrl;
    }

    public void setHeadshotUrl(String headshotUrl) {
        this.headshotUrl = headshotUrl;
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

    public boolean isScorer() {
        return isScorer;
    }

    public void setScorer(boolean scorer) {
        isScorer = scorer;
    }

    public boolean isRebounder() {
        return isRebounder;
    }

    public void setRebounder(boolean rebounder) {
        isRebounder = rebounder;
    }

    public boolean isPlaymaker() {
        return isPlaymaker;
    }

    public void setPlaymaker(boolean playmaker) {
        isPlaymaker = playmaker;
    }

    public String getVennRegion() {
        return vennRegion;
    }

    public void setVennRegion(String vennRegion) {
        this.vennRegion = vennRegion;
    }
}
