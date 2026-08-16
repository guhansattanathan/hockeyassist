package com.hockeyassist.hockeyassist.repository;

import com.hockeyassist.hockeyassist.model.PlayerShot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface PlayerShotRepository extends JpaRepository<PlayerShot, UUID> {

    @Query("SELECT s FROM PlayerShot s WHERE s.player.nbaPlayerId = :nbaPlayerId AND s.seasonId = :seasonId")
    List<PlayerShot> findByNbaPlayerIdAndSeason(@Param("nbaPlayerId") Integer nbaPlayerId,
            @Param("seasonId") String seasonId);

    @Query("SELECT s FROM PlayerShot s WHERE s.player.nbaPlayerId = :nbaPlayerId")
    List<PlayerShot> findByNbaPlayerId(@Param("nbaPlayerId") Integer nbaPlayerId);

    @Query("SELECT DISTINCT s.seasonId FROM PlayerShot s WHERE s.player.nbaPlayerId = :nbaPlayerId ORDER BY s.seasonId DESC")
    List<String> findDistinctSeasonsByNbaPlayerId(@Param("nbaPlayerId") Integer nbaPlayerId);
}