package com.hockeyassist.hockeyassist.repository;

import com.hockeyassist.hockeyassist.model.PlayerSeasonAdvanced;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface PlayerSeasonAdvancedRepository extends JpaRepository<PlayerSeasonAdvanced, UUID> {

    @Query("SELECT a FROM PlayerSeasonAdvanced a WHERE a.player.nbaPlayerId = :nbaPlayerId ORDER BY a.seasonId ASC")
    List<PlayerSeasonAdvanced> findByNbaPlayerId(@Param("nbaPlayerId") Integer nbaPlayerId);

    @Query("SELECT a FROM PlayerSeasonAdvanced a WHERE a.player.nbaPlayerId = :nbaPlayerId AND a.seasonId = :seasonId")
    PlayerSeasonAdvanced findByNbaPlayerIdAndSeasonId(@Param("nbaPlayerId") Integer nbaPlayerId,
            @Param("seasonId") String seasonId);
}