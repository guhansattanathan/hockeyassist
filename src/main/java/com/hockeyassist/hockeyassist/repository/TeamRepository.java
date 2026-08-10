package com.hockeyassist.hockeyassist.repository;

import com.hockeyassist.hockeyassist.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface TeamRepository extends JpaRepository<Team, UUID> {

    Optional<Team> findByTeamId(Integer teamId);

    Optional<Team> findByAbbreviation(String abbreviation);

    boolean existsByTeamId(Integer teamId);
}