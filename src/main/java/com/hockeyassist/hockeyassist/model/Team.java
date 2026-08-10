package com.hockeyassist.hockeyassist.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "team_id", unique = true, nullable = false)
    private Integer teamId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "abbreviation", nullable = false)
    private String abbreviation;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "conference")
    private String conference;

    @Column(name = "division")
    private String division;

    @Column(name = "arena")
    private String arena;

    @Column(name = "arena_city")
    private String arenaCity;

    @Column(name = "arena_state")
    private String arenaState;

    // Constructors
    public Team() {
    }

    public Team(Integer teamId, String fullName, String abbreviation) {
        this.teamId = teamId;
        this.fullName = fullName;
        this.abbreviation = abbreviation;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getTeamId() {
        return teamId;
    }

    public void setTeamId(Integer teamId) {
        this.teamId = teamId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getConference() {
        return conference;
    }

    public void setConference(String conference) {
        this.conference = conference;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getArena() {
        return arena;
    }

    public void setArena(String arena) {
        this.arena = arena;
    }

    public String getArenaCity() {
        return arenaCity;
    }

    public void setArenaCity(String arenaCity) {
        this.arenaCity = arenaCity;
    }

    public String getArenaState() {
        return arenaState;
    }

    public void setArenaState(String arenaState) {
        this.arenaState = arenaState;
    }
}