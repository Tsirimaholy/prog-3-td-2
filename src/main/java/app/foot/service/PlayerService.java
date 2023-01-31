package app.foot.service;

import app.foot.exception.BadRequestException;
import app.foot.model.Player;
import app.foot.repository.PlayerRepository;
import app.foot.repository.entity.PlayerEntity;
import app.foot.repository.mapper.PlayerMapper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PlayerService {
    private final PlayerRepository repository;
    private final PlayerMapper mapper;

    public List<Player> getPlayers() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toUnmodifiableList());
    }

    public List<Player> createPlayers(List<Player> toCreate) {
        return repository.saveAll(toCreate.stream()
                        .map(mapper::toEntity)
                        .collect(Collectors.toUnmodifiableList())).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toUnmodifiableList());
    }

    @Transactional
    public List<Player> updatePlayers(List<Player> players) {
        List<Player> updatedPlayers = new ArrayList<>();
        players.forEach(player -> {
            Optional<PlayerEntity> dbPlayer = repository.findById(player.getId());
            if (dbPlayer.isEmpty()) throw new BadRequestException("The user " + player + "Does not exist");

            PlayerEntity playerEntity = dbPlayer.get();
            playerEntity.setGuardian(player.getIsGuardian());
            playerEntity.setName(player.getName());
            repository.save(playerEntity);

            updatedPlayers.add(repository.findById(player.getId()).map(mapper::toDomain).get());
        });

        return updatedPlayers;
    }
}
