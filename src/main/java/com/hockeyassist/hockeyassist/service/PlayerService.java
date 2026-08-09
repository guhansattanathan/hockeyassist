package com.hockeyassist.hockeyassist.service;

import com.hockeyassist.hockeyassist.model.Player;
import com.hockeyassist.hockeyassist.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    // ==========================================
    // BASIC CRUD OPERATIONS
    // ==========================================

    // Get all players
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    // Get player by UUID
    public Optional<Player> getPlayerById(UUID id) {
        return playerRepository.findById(id);
    }

    // Get player by NBA ID
    public Optional<Player> getPlayerByNbaId(Integer nbaPlayerId) {
        return playerRepository.findByNbaPlayerId(nbaPlayerId);
    }

    // Get player by name
    public Optional<Player> getPlayerByName(String name) {
        return playerRepository.findByName(name);
    }

    // Create or update player
    @Transactional
    public Player savePlayer(Player player) {
        return playerRepository.save(player);
    }

    // Delete player
    @Transactional
    public void deletePlayer(UUID id) {
        playerRepository.deleteById(id);
    }

    // Check if player exists
    public boolean playerExists(Integer nbaPlayerId) {
        return playerRepository.existsByNbaPlayerId(nbaPlayerId);
    }

    // ==========================================
    // SEARCH AND FILTER METHODS
    // ==========================================

    // Search players by name (partial match, case insensitive)
    public List<Player> searchPlayers(String query) {
        return playerRepository.findByNameContainingIgnoreCase(query);
    }

    // Get players by team
    public List<Player> getPlayersByTeam(String team) {
        return playerRepository.findByTeam(team);
    }

    // Get players by position
    public List<Player> getPlayersByPosition(String position) {
        return playerRepository.findByPosition(position);
    }

    // Get all active players
    public List<Player> getActivePlayers() {
        return playerRepository.findByIsActiveTrue();
    }

    // Get all inactive players
    public List<Player> getInactivePlayers() {
        return playerRepository.findByIsActiveFalse();
    }

    // ==========================================
    // TEAM METHODS
    // ==========================================

    // Get all distinct teams
    public List<String> getAllTeams() {
        return playerRepository.findDistinctTeams();
    }

    // Get all players on a team (alias for getPlayersByTeam)
    public List<Player> getPlayersOnTeam(String team) {
        return getPlayersByTeam(team);
    }

    // ==========================================
    // POSITION METHODS
    // ==========================================

    // Get all distinct positions
    public List<String> getAllPositions() {
        return playerRepository.findDistinctPositions();
    }

    // ==========================================
    // STATISTICS METHODS
    // ==========================================

    // Get total player count
    public long getTotalPlayerCount() {
        return playerRepository.count();
    }

    // Get active player count
    public long getActivePlayerCount() {
        return playerRepository.countByIsActiveTrue();
    }

    // Get team player count
    public long getTeamPlayerCount(String team) {
        return playerRepository.countByTeam(team);
    }
}