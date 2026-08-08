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

    PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

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

    // Get players by team
    public List<Player> getPlayersByTeam(String team) {
        return playerRepository.findByTeam(team);
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
}