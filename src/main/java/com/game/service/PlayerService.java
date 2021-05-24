package com.game.service;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface PlayerService {

    Player getById(Long id);

    void save (Player player);

    Player updatePlayer(Long idPlayer, Player newPlayer);

    void delete(Long id);

    List<Player> getAll(Specification<Player> specification);

    Page<Player> getAll(Specification<Player> specification, Pageable pageable);

    Specification<Player> filterByName(String name);

    Specification<Player> filterByTitle(String title);

    Specification<Player> filterByRace(Race race);

    Specification<Player> filterByProfession(Profession profession);

    Specification<Player> filterByExperience(Integer min, Integer max);

    Specification<Player> filterByLevel(Integer min, Integer max);

    Specification<Player> filterByBirthday(Long after, Long before);

    Specification<Player> filterByBanned(Boolean isBanned);

    Boolean isValidPlayer(Player player);

    Integer calculateCurrentLevel(Integer experience);

    Integer calculateExperienceToNextLevel(Integer levelRating, Integer experience);

}
