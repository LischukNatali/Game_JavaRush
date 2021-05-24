package com.game.service;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerServiceImpl(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    public Player getById(Long id) {

        return playerRepository.findById(id).orElse(null);
    }

    @Override
    public void save(Player player) {
        playerRepository.save(player);
    }

    @Override
    public Player updatePlayer(Long idPlayer, Player newPlayer) {
        if (!playerRepository
                .findById(idPlayer)
                .isPresent()) return null;

        Player updatePlayer = getById(idPlayer);

        if (newPlayer.getName() != null) {
            updatePlayer.setName(newPlayer.getName());
        }

        if (newPlayer.getTitle() != null) {
            updatePlayer.setTitle(newPlayer.getTitle());
        }

        if (newPlayer.getRace() != null) {
            updatePlayer.setRace(newPlayer.getRace());
        }

        if (newPlayer.getProfession() != null) {
            updatePlayer.setProfession(newPlayer.getProfession());
        }

        if (newPlayer.getBirthday() != null) {
            updatePlayer.setBirthday(newPlayer.getBirthday());
        }

        if (newPlayer.getBanned() != null) {
            updatePlayer.setBanned(newPlayer.getBanned());
        }

        if (newPlayer.getExperience() != null) {
            updatePlayer.setExperience(newPlayer.getExperience());
        }

        updatePlayer.setLevel(calculateCurrentLevel(updatePlayer.getExperience()));
        updatePlayer.setUntilNextLevel(calculateExperienceToNextLevel(updatePlayer.getLevel(), updatePlayer.getExperience()));

        return playerRepository.save(updatePlayer);
    }

    @Override
    public Integer calculateCurrentLevel(Integer experience) {
        double level = (Math.sqrt(2500 + 200 * experience) - 50) / 100;
        return (int) level;
    }

    @Override
    public Integer calculateExperienceToNextLevel(Integer currentLevel, Integer experience) {

        return 50 * (currentLevel + 1) * (currentLevel + 2) - experience;
    }

    @Override
    public void delete(Long id) {
        playerRepository.deleteById(id);
    }

    @Override
    public List<Player> getAll(Specification<Player> specification) {
        return playerRepository.findAll(specification);
    }

    @Override
    public Page<Player> getAll(Specification<Player> specification, Pageable pageable) {
        return playerRepository.findAll(specification, pageable);
    }

    @Override
    public Specification<Player> filterByName(String name) {
        return (root, query, builder) -> name == null
                ? null : builder.like(root.get("name"), "%" + name + "%");
    }

    @Override
    public Specification<Player> filterByTitle(String title) {
        return (root, query, builder) -> title == null
                ? null : builder.like(root.get("title"), "%" + title + "%");
    }

    @Override
    public Specification<Player> filterByRace(Race race) {
        return (root, query, builder) -> race == null
                ? null : builder.equal(root.get("race"), race);
    }

    @Override
    public Specification<Player> filterByProfession(Profession profession) {
        return (root, query, builder) -> profession == null
                ? null : builder.equal(root.get("profession"), profession);
    }

    @Override
    public Specification<Player> filterByExperience(Integer min, Integer max) {
        return (root, query, builder) -> {
            if (min == null && max == null) {
                return null;
            }
            if (min == null) {
                return builder.lessThanOrEqualTo(root.get("experience"), max);
            }
            if (max == null) {
                return builder.greaterThanOrEqualTo(root.get("experience"), min);
            }
            return builder
                    .between(root.get("experience"), min, max);
        };
    }

    @Override
    public Specification<Player> filterByLevel(Integer min, Integer max) {
        return (root, query, builder) -> {
            if (min == null && max == null) {
                return null;
            }
            if (min == null) {
                return builder.lessThanOrEqualTo(root.get("level"), max);
            }
            if (max == null) {
                return builder.greaterThanOrEqualTo(root.get("level"), min);
            }
            return builder
                    .between(root.get("level"), min, max);
        };
    }

    @Override
    public Specification<Player> filterByBirthday(Long after, Long before) {
        return (root, query, builder) -> {
            if (after == null && before == null) {
                return null;
            }

            if (after == null) {
                Date dateBefore = new Date(before);
                return builder.lessThanOrEqualTo(root.get("birthday"), dateBefore);
            }
            if (before == null) {
                Date dateAfter = new Date(after);
                return builder.greaterThanOrEqualTo(root.get("birthday"), dateAfter);
            }
            return builder
                    .between(root.get("birthday"), new Date(after), new Date(before));
        };
    }

    @Override
    public Specification<Player> filterByBanned(Boolean isBanned) {
        return (root, query, builder) -> {
            if (isBanned == null) {
                return null;
            }
            if (isBanned) {
                return builder.isTrue(root.get("banned"));
            } else {
                return builder.isFalse(root.get("banned"));
            }
        };
    }

    @Override
    public Boolean isValidPlayer(Player player) {
        return player != null
                && isValidPlayerName(player.getName())
                && isValidPlayerTitle(player.getTitle())
                && isValidPlayerRace(player.getRace())
                && isValidPlayerProfession(player.getProfession())
                && isValidPlayerBirthday(player.getBirthday())
                && isValidPlayerExperience(player.getExperience());


    }

    private Boolean isValidPlayerName(String name) {
        int maxLength = 12;
        return name != null
                && !name.isEmpty()
                && name.length() > 1
                && name.length() <= maxLength;
    }

    private Boolean isValidPlayerTitle(String title) {
        int maxLength = 30;
        return title != null
                && !title.isEmpty()
                && title.length() <= maxLength;
    }

    private Boolean isValidPlayerRace(Race race) {
        return race != null;
    }

    private Boolean isValidPlayerExperience(Integer experience) {
        final int minExp = 0;
        final int maxExp = 10_000_000;
        return experience != null
                && (experience >= minExp
                && experience <= maxExp);

    }

    private Boolean isValidPlayerProfession(Profession profession) {
        return profession != null;
    }

    private boolean isValidPlayerBirthday(Date birthday) {
        if (birthday.getTime() < 0) {
            return false;
        }
        Calendar date = Calendar.getInstance();
        date.setTime(birthday);
        return date.get(Calendar.YEAR) >= 2_000
                && date.get(Calendar.YEAR) <= 3_000;
    }

}
