package co.vuckovic.pegasus.repository;

import co.vuckovic.pegasus.repository.entity.StatisticEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatisticEntityRepository extends JpaRepository<StatisticEntity, Integer> {

}
