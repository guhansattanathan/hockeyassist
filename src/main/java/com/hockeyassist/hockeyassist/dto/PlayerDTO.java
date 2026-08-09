package com.hockeyassist.hockeyassist.dto;

import com.hockeyassist.hockeyassist.model.Player;
import java.io.Serializable;
import java.util.UUID;

public class PlayerDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID id;
    private Integer nbaPlayerId;
    private String name;
    private String firstName;
    private String lastName;
    private String team;
    private String position;
    private Boolean isActive;

    public PlayerDTO() {
    }

    public PlayerDTO(Player player) {
        this.id = player.getId();
        this.nbaPlayerId = player.getNbaPlayerId();
        this.name = player.getName();
        this.firstName = player.getFirstName();
        this.lastName = player.getLastName();
        this.team = player.getTeam();
        this.position = player.getPosition();
        this.isActive = player.getIsActive();
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}