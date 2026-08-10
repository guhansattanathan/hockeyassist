package com.hockeyassist.hockeyassist.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nba_player_id", unique = true, nullable = false)
    private Integer nbaPlayerId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "position")
    private String position;

    @Column(name = "is_active")
    private Boolean isActive;

    // One Player has many PlayerSeasonStats
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<PlayerSeasonStats> seasonStats = new ArrayList<>();

    // ✅ Relationship to Team entity (instead of String team)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", referencedColumnName = "team_id")
    @JsonIgnore
    private Team team;

    // Constructors
    public Player() {
    }

    public Player(Integer nbaPlayerId, String name) {
        this.nbaPlayerId = nbaPlayerId;
        this.name = name;
        this.isActive = true;
    }

    // Helper method to add season stats
    public void addSeasonStat(PlayerSeasonStats stat) {
        seasonStats.add(stat);
        stat.setPlayer(this);
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

    public List<PlayerSeasonStats> getSeasonStats() {
        return seasonStats;
    }

    public void setSeasonStats(List<PlayerSeasonStats> seasonStats) {
        this.seasonStats = seasonStats;
    }

    // ✅ Getter and Setter for Team
    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }
}