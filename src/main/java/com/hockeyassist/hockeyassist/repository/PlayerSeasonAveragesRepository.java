package com.hockeyassist.hockeyassist.repository;

import com.hockeyassist.hockeyassist.model.Player;
import com.hockeyassist.hockeyassist.model.PlayerSeasonAverages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerSeasonAveragesRepository extends JpaRepository<PlayerSeasonAverages, UUID> {

    // ✅ Changed: Returns List, not Optional
    List<PlayerSeasonAverages> findByPlayerAndSeasonId(Player player, String seasonId);

    // Find all averages for a player, ordered by season
    List<PlayerSeasonAverages> findByPlayerOrderBySeasonIdAsc(Player player);

    // Find averages for a player by NBA ID, ordered by season
    @Query("SELECT a FROM PlayerSeasonAverages a WHERE a.player.nbaPlayerId = :nbaPlayerId ORDER BY a.seasonId ASC")
    List<PlayerSeasonAverages> findByNbaPlayerId(@Param("nbaPlayerId") Integer nbaPlayerId);

    // Find averages for a player by NBA ID and season
    @Query("SELECT a FROM PlayerSeasonAverages a WHERE a.player.nbaPlayerId = :nbaPlayerId AND a.seasonId = :seasonId")
    Optional<PlayerSeasonAverages> findByNbaPlayerIdAndSeasonId(@Param("nbaPlayerId") Integer nbaPlayerId,
            @Param("seasonId") String seasonId);

    // Check if season averages exist for a player
    boolean existsByPlayerAndSeasonId(Player player, String seasonId);
}