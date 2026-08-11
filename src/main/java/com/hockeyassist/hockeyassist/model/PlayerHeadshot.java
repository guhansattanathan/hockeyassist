package com.hockeyassist.hockeyassist.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "player_headshots")
public class PlayerHeadshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "player_id", nullable = false)
    private Integer playerId;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "image_data")
    @Lob
    private byte[] imageData;

    @Column(name = "last_fetched")
    private LocalDateTime lastFetched;

    @Column(name = "headshot_size")
    private String headshotSize = "260x190";

    // Constructors
    public PlayerHeadshot() {
    }

    public PlayerHeadshot(Integer playerId) {
        this.playerId = playerId;
        this.lastFetched = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Integer playerId) {
        this.playerId = playerId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public LocalDateTime getLastFetched() {
        return lastFetched;
    }

    public void setLastFetched(LocalDateTime lastFetched) {
        this.lastFetched = lastFetched;
    }

    public String getHeadshotSize() {
        return headshotSize;
    }

    public void setHeadshotSize(String headshotSize) {
        this.headshotSize = headshotSize;
    }
}