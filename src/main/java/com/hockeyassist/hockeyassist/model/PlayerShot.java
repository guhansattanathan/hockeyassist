package com.hockeyassist.hockeyassist.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "player_shots")
public class PlayerShot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "season_id", nullable = false)
    private String seasonId;

    @Column(name = "game_id")
    private String gameId;

    @Column(name = "shot_made")
    private Boolean shotMade;

    @Column(name = "loc_x")
    private Double locX;

    @Column(name = "loc_y")
    private Double locY;

    @Column(name = "shot_zone_basic")
    private String shotZoneBasic;

    @Column(name = "shot_zone_area")
    private String shotZoneArea;

    @Column(name = "shot_zone_range")
    private String shotZoneRange;

    @Column(name = "shot_distance")
    private Integer shotDistance;

    // Constructors
    public PlayerShot() {
    }

    public PlayerShot(Player player, String seasonId) {
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

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
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