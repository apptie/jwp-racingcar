package racingcar.repositoryimpl;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import racingcar.dao.CarDao;
import racingcar.dao.GamesDao;
import racingcar.dao.WinnerDao;
import racingcar.dao.entity.CarEntity;
import racingcar.dao.entity.InsertGameEntity;
import racingcar.domain.Car;
import racingcar.domain.RacingGameResult;
import racingcar.repository.RacingGameRepository;

@Repository
public class RacingGameRepositoryImpl implements RacingGameRepository {

    private final GamesDao gamesDao;
    private final CarDao carDao;
    private final WinnerDao winnerDao;

    public RacingGameRepositoryImpl(final GamesDao gamesDao, final CarDao carDao, final WinnerDao winnerDao) {
        this.gamesDao = gamesDao;
        this.carDao = carDao;
        this.winnerDao = winnerDao;
    }

    @Override
    public InsertGameEntity save(final RacingGameResult racingGameResult) {
        final InsertGameEntity insertGameEntity = gamesDao.insert(InsertGameEntity.fromDomain(racingGameResult));
        final List<CarEntity> carEntities = fromCarsToEntity(racingGameResult.getTotalCars());
        final List<CarEntity> savedCarEntities = carDao.insertAll(carEntities, insertGameEntity.getGameId());
        final List<CarEntity> winnerCarEntities = findWinnerCarEntities(savedCarEntities, racingGameResult.getWinners());

        winnerDao.saveAll(winnerCarEntities, insertGameEntity.getGameId());
        return insertGameEntity;
    }

    private List<CarEntity> fromCarsToEntity(final List<Car> cars) {
        return cars.stream()
                .map(CarEntity::fromDomain)
                .collect(Collectors.toList());
    }

    private List<CarEntity> findWinnerCarEntities(final List<CarEntity> savedCarEntities, final List<Car> winner) {
        final List<String> winnerNames = winner.stream().map(Car::getCarName).collect(Collectors.toList());

        return savedCarEntities.stream()
                .filter(carEntity -> winnerNames.contains(carEntity.getName()))
                .collect(Collectors.toList());
    }
}