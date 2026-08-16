package com.hockeyassist.hockeyassist.dto;

import com.hockeyassist.hockeyassist.model.PlayerShot;
import java.io.Serializable;
import java.util.UUID;

public class PlayerShotDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID id;
    private String seasonId;
    private Boolean shotMade;
    private Double locX;
    private Double locY;
    private String shotZoneBasic;
    private String shotZoneArea;
    private String shotZoneRange;
    private Integer shotDistance;

    public PlayerShotDTO() {
    }

    public PlayerShotDTO(PlayerShot shot) {
        this.id = shot.getId();
        this.seasonId = shot.getSeasonId();
        this.shotMade = shot.getShotMade();
        this.locX = shot.getLocX();
        this.locY = shot.getLocY();
        this.shotZoneBasic = shot.getShotZoneBasic();
        this.shotZoneArea = shot.getShotZoneArea();
        this.shotZoneRange = shot.getShotZoneRange();
        this.shotDistance = shot.getShotDistance();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(String seasonId) {
        this.seasonId = seasonId;
    }

    public Boolean getShotMade() {
        return shotMade;
    }

    public void setShotMade(Boolean shotMade) {
        this.shotMade = shotMade;
    }

    public Double getLocX() {
        return locX;
    }

    public void setLocX(Double locX) {
        this.locX = locX;
    }

    public Double getLocY() {
        return locY;
    }

    public void setLocY(Double locY) {
        this.locY = locY;
    }

    public String getShotZoneBasic() {
        return shotZoneBasic;
    }

    public void setShotZoneBasic(String shotZoneBasic) {
        this.shotZoneBasic = shotZoneBasic;
    }

    public String getShotZoneArea() {
        return shotZoneArea;
    }

    public void setShotZoneArea(String shotZoneArea) {
        this.shotZoneArea = shotZoneArea;
    }

    public String getShotZoneRange() {
        return shotZoneRange;
    }

    public void setShotZoneRange(String shotZoneRange) {
        this.shotZoneRange = shotZoneRange;
    }

    public Integer getShotDistance() {
        return shotDistance;
    }

    public void setShotDistance(Integer shotDistance) {
        this.shotDistance = shotDistance;
    }
}