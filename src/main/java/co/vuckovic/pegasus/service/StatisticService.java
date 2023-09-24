package co.vuckovic.pegasus.service;

import co.vuckovic.pegasus.repository.BucketEntityRepository;
import co.vuckovic.pegasus.repository.entity.BucketEntity;
import co.vuckovic.pegasus.repository.entity.StatisticEntity;
import co.vuckovic.pegasus.model.exception.NotFoundException;
import co.vuckovic.pegasus.model.response.StatisticsResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatisticService {

  private final BucketEntityRepository bucketEntityRepository;
  private final ModelMapper modelMapper;

  public StatisticsResponse getStatistics(Integer bucketId) {
    BucketEntity bucketEntity =
        bucketEntityRepository
            .findById(bucketId)
            .orElseThrow(() -> new NotFoundException("Bucket does not exist."));
    StatisticEntity statisticEntity = bucketEntity.getStatisticEntity();
    return modelMapper.map(statisticEntity, StatisticsResponse.class);
  }

}
