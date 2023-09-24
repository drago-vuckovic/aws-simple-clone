package co.vuckovic.pegasus.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import co.vuckovic.pegasus.common.BaseUnitTest;
import co.vuckovic.pegasus.common.WebMvcTestConfig;
import co.vuckovic.pegasus.model.response.StatisticsResponse;
import co.vuckovic.pegasus.repository.BucketEntityRepository;
import co.vuckovic.pegasus.repository.entity.BucketEntity;
import co.vuckovic.pegasus.repository.entity.StatisticEntity;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@WebMvcTest(value = StatisticService.class)
@WebMvcTestConfig
@AutoConfigureMockMvc(addFilters = false)
@EnableWebMvc
class StatisticsServiceTest extends BaseUnitTest {

  @InjectMocks
  @Autowired
  private StatisticService statisticService;

  @MockBean
  private BucketEntityRepository bucketEntityRepository;

  @MockBean
  private ModelMapper modelMapper;

  @Test
  void getStatistics_withValidData_shouldReturnStatisticResponseAndStatusOk() {

    StatisticEntity statisticEntity = new StatisticEntity(1, 3, 4, 5);
    BucketEntity bucketEntity = new BucketEntity(1, 1.2, 1024.0, "test", 1, statisticEntity);

    Mockito.when(bucketEntityRepository.findById(ArgumentMatchers.any()))
        .thenReturn(Optional.of(bucketEntity));

    StatisticsResponse statisticsResponse = statisticService.getStatistics(bucketEntity.getId());

    assertThat(statisticsResponse)
        .isEqualTo(modelMapper.map(statisticEntity, StatisticsResponse.class));
  }
}
