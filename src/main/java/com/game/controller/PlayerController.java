package com.game.controller;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.service.PlayerService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
//@GetMapping
@RequestMapping("/rest/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @RequestMapping(value = "{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Player> getPlayer(@PathVariable("id") Long playerId) {
        if (playerId == null || playerId <= 0) {
            return new ResponseEntity<Player>(HttpStatus.BAD_REQUEST);
        }
        Player player = this.playerService.getById(playerId);

        if (player == null) {
            return new ResponseEntity<Player>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<Player>(player, HttpStatus.OK);
    }

    @RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Player> createPlayer(@RequestBody Player player) {

        if (!playerService.isValidPlayer(player)) {
            return new ResponseEntity<Player>(HttpStatus.BAD_REQUEST);
        }
        if (player.getBanned() == null) {
            player.setBanned(false);
        }
        player.setExperience(player.getExperience());

        final int currentLevel = playerService.calculateCurrentLevel(player.getExperience());
        player.setLevel(currentLevel);

        player.setUntilNextLevel(player.getUntilNextLevel());
        final int nextLevel = playerService.calculateExperienceToNextLevel(player.getLevel(), player.getExperience());
        player.setUntilNextLevel(nextLevel);

        this.playerService.save(player);
        return new ResponseEntity<Player>(player, HttpStatus.OK);
    }

    @RequestMapping(value = "{id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Player> updatePlayer(@PathVariable("id") Long playerId, @RequestBody Player newPlayer) {

        if (playerId <= 0 || invalidParameters(newPlayer)) {
            return new ResponseEntity<Player>(HttpStatus.BAD_REQUEST);
        }

        Player responsePlayer = playerService.updatePlayer(playerId, newPlayer);
        if (responsePlayer == null) {
            return new ResponseEntity<Player>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<Player>(responsePlayer, HttpStatus.OK);
        }
    }

    private boolean invalidParameters(Player player) {
        return (player.getExperience() != null && (player.getExperience() < 0 || player.getExperience() > 10000000))
                || (player.getBirthday() != null && player.getBirthday().getTime() < 0);
    }

    @RequestMapping(value = "{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Player> deletePlayer(@PathVariable("id") Long playerId) {
        if (playerId == null || playerId <= 0) {
            return new ResponseEntity<Player>(HttpStatus.BAD_REQUEST);
        }
        Player player = this.playerService.getById(playerId);

        if (player == null) {
            return new ResponseEntity<Player>(HttpStatus.NOT_FOUND);
        }

        this.playerService.delete(playerId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public List<Player> getAllPlayers(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "race", required = false) Race race,
            @RequestParam(value = "profession", required = false) Profession profession,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "banned", required = false) Boolean banned,
            @RequestParam(value = "minExperience", required = false) Integer minExperience,
            @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
            @RequestParam(value = "minLevel", required = false) Integer minLevel,
            @RequestParam(value = "maxLevel", required = false) Integer maxLevel,
            @RequestParam(value = "order", required = false, defaultValue = "ID") PlayerOrder order,
            @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(value = "pageSize", required = false, defaultValue = "3") Integer pageSize) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(order.getFieldName()));

        return playerService.getAll(
                Specification.where(
                        playerService.filterByName(name)
                                .and(playerService.filterByTitle(title)))
                        .and(playerService.filterByRace(race))
                        .and(playerService.filterByProfession(profession))
                        .and(playerService.filterByBirthday(after, before))
                        .and(playerService.filterByBanned(banned))
                        .and(playerService.filterByExperience(minExperience, maxExperience))
                        .and(playerService.filterByLevel(minLevel, maxLevel)), pageable)
                .getContent();
    }

    @RequestMapping(value = "/count", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Integer getPlayersCount(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "race", required = false) Race race,
            @RequestParam(value = "profession", required = false) Profession profession,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "banned", required = false) Boolean banned,
            @RequestParam(value = "minExperience", required = false) Integer minExperience,
            @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
            @RequestParam(value = "minLevel", required = false) Integer minLevel,
            @RequestParam(value = "maxLevel", required = false) Integer maxLevel) {

        return playerService.getAll(
                Specification.where(
                        playerService.filterByName(name)
                                .and(playerService.filterByTitle(title)))
                        .and(playerService.filterByRace(race))
                        .and(playerService.filterByProfession(profession))
                        .and(playerService.filterByBirthday(after, before))
                        .and(playerService.filterByBanned(banned))
                        .and(playerService.filterByExperience(minExperience, maxExperience))
                        .and(playerService.filterByLevel(minLevel, maxLevel))).size();
    }
}


