package com.hockeyassist.hockeyassist.dto;

import com.hockeyassist.hockeyassist.model.Player;
import com.hockeyassist.hockeyassist.model.Team;
import java.io.Serializable;
import java.util.UUID;

public class PlayerDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID id;
    private Integer nbaPlayerId;
    private String name;
    private String firstName;
    private String lastName;
    private String team; // Team abbreviation (String)
    private String teamName; // Full team name (optional)
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
        this.position = player.getPosition();
        this.isActive = player.getIsActive();

        // ✅ Handle Team entity
        Team team = player.getTeam();
        if (team != null) {
            this.team = team.getAbbreviation();
            this.teamName = team.getFullName();
        } else {
            this.team = null;
            this.teamName = null;
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

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
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