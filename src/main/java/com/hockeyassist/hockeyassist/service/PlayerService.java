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
    // BASIC CRUD OPERATIONS (Return DTOs)
    // ==========================================

    @Cacheable(value = "players", key = "'all'")
    public List<PlayerDTO> getAllPlayers() {
        System.out.println("🔍 Fetching ALL players from database");
        return playerRepository.findAll().stream()
                .map(PlayerDTO::new)
                .collect(Collectors.toList());
    }

    public Optional<PlayerDTO> getPlayerById(UUID id) {
        return playerRepository.findById(id)
                .map(PlayerDTO::new);
    }

    @Cacheable(value = "players", key = "#nbaPlayerId")
    public Optional<PlayerDTO> getPlayerByNbaId(Integer nbaPlayerId) {
        System.out.println("🔍 Fetching player from database: " + nbaPlayerId);
        return playerRepository.findByNbaPlayerId(nbaPlayerId)
                .map(PlayerDTO::new);
    }

    public Optional<PlayerDTO> getPlayerByName(String name) {
        return playerRepository.findByName(name)
                .map(PlayerDTO::new);
    }

    @CacheEvict(value = "players", allEntries = true)
    @Transactional
    public Player savePlayer(Player player) {
        return playerRepository.save(player);
    }

    @CacheEvict(value = "players", allEntries = true)
    @Transactional
    public void deletePlayer(UUID id) {
        playerRepository.deleteById(id);
    }

    public boolean playerExists(Integer nbaPlayerId) {
        return playerRepository.existsByNbaPlayerId(nbaPlayerId);
    }

    // ==========================================
    // SEARCH AND FILTER METHODS (Return DTOs)
    // ==========================================

    public List<PlayerDTO> searchPlayers(String query) {
        return playerRepository.findByNameContainingIgnoreCase(query).stream()
                .map(PlayerDTO::new)
                .collect(Collectors.toList());
    }

    public List<PlayerDTO> getPlayersByTeam(String team) {
        return playerRepository.findByTeam(team).stream()
                .map(PlayerDTO::new)
                .collect(Collectors.toList());
    }

    public List<PlayerDTO> getPlayersByPosition(String position) {
        return playerRepository.findByPosition(position).stream()
                .map(PlayerDTO::new)
                .collect(Collectors.toList());
    }

    public List<PlayerDTO> getActivePlayers() {
        return playerRepository.findByIsActiveTrue().stream()
                .map(PlayerDTO::new)
                .collect(Collectors.toList());
    }

    public List<PlayerDTO> getInactivePlayers() {
        return playerRepository.findByIsActiveFalse().stream()
                .map(PlayerDTO::new)
                .collect(Collectors.toList());
    }

    public List<PlayerDTO> getPlayersOnTeam(String team) {
        return getPlayersByTeam(team);
    }

    // ==========================================
    // TEAM METHODS
    // ==========================================

    public List<String> getAllTeams() {
        return playerRepository.findDistinctTeams();
    }

    public List<String> getAllPositions() {
        return playerRepository.findDistinctPositions();
    }

    // ==========================================
    // STATISTICS METHODS
    // ==========================================

    public long getTotalPlayerCount() {
        return playerRepository.count();
    }

    public long getActivePlayerCount() {
        return playerRepository.countByIsActiveTrue();
    }

    public long getTeamPlayerCount(String team) {
        return playerRepository.countByTeam(team);
    }
}