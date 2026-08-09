package com.hockeyassist.hockeyassist.service;

import com.hockeyassist.hockeyassist.dto.PlayerDTO;
import com.hockeyassist.hockeyassist.model.Player;
import com.hockeyassist.hockeyassist.repository.PlayerRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
    @Cacheable(value = "players", key = "'all'")
    public List<PlayerDTO> getAllPlayers() {
        System.out.println("🔍 Fetching ALL players from database");
        return playerRepository.findAll().stream()
                .map(PlayerDTO::new)
                .collect(Collectors.toList());
    }

    // Get player by UUID
    public Optional<PlayerDTO> getPlayerById(UUID id) {
        return playerRepository.findById(id)
                .map(PlayerDTO::new);
    }

    // Get player by NBA ID
    @Cacheable(value = "players", key = "#nbaPlayerId")
    public Optional<PlayerDTO> getPlayerByNbaId(Integer nbaPlayerId) {
        System.out.println("🔍 Fetching player from database: " + nbaPlayerId);
        return playerRepository.findByNbaPlayerId(nbaPlayerId)
                .map(PlayerDTO::new);
    }

    // Get player by name
    public Optional<PlayerDTO> getPlayerByName(String name) {
        return playerRepository.findByName(name)
                .map(PlayerDTO::new);
    }

    // Create or update player (returns entity for internal use)
    @CacheEvict(value = "players", allEntries = true)
    @Transactional
    public Player savePlayer(Player player) {
        return playerRepository.save(player);
    }

    // Delete player
    @CacheEvict(value = "players", allEntries = true)
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
    public List<PlayerDTO> searchPlayers(String query) {
        return playerRepository.findByNameContainingIgnoreCase(query).stream()
                .map(PlayerDTO::new)
                .collect(Collectors.toList());
    }

    // Get players by team
    public List<PlayerDTO> getPlayersByTeam(String team) {
        return playerRepository.findByTeam(team).stream()
                .map(PlayerDTO::new)
                .collect(Collectors.toList());
    }

    // Get players by position
    public List<PlayerDTO> getPlayersByPosition(String position) {
        return playerRepository.findByPosition(position).stream()
                .map(PlayerDTO::new)
                .collect(Collectors.toList());
    }

    // Get all active players
    public List<PlayerDTO> getActivePlayers() {
        return playerRepository.findByIsActiveTrue().stream()
                .map(PlayerDTO::new)
                .collect(Collectors.toList());
    }

    // Get all inactive players
    public List<PlayerDTO> getInactivePlayers() {
        return playerRepository.findByIsActiveFalse().stream()
                .map(PlayerDTO::new)
                .collect(Collectors.toList());
    }

    // Get all players on a team (alias for getPlayersByTeam)
    public List<PlayerDTO> getPlayersOnTeam(String team) {
        return getPlayersByTeam(team);
    }

    // ==========================================
    // TEAM METHODS
    // ==========================================

    // Get all distinct teams
    public List<String> getAllTeams() {
        return playerRepository.findDistinctTeams();
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

    // Get player entity by nba id
    public Optional<Player> findPlayerEntityByNbaId(Integer nbaPlayerId) {
        return playerRepository.findByNbaPlayerId(nbaPlayerId);
    }
}