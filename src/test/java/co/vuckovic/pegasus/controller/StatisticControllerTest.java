package co.vuckovic.pegasus.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.vuckovic.pegasus.api.v1.controller.StatisticController;
import co.vuckovic.pegasus.common.BaseUnitTest;
import co.vuckovic.pegasus.common.WebMvcTestConfig;
import co.vuckovic.pegasus.model.response.StatisticsResponse;
import co.vuckovic.pegasus.repository.entity.BucketEntity;
import co.vuckovic.pegasus.repository.entity.StatisticEntity;
import co.vuckovic.pegasus.service.StatisticService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(value = StatisticController.class)
@WebMvcTestConfig
@AutoConfigureMockMvc(addFilters = false)
class StatisticControllerTest extends BaseUnitTest {

  @MockBean
  private StatisticService statisticService;

  @Autowired
  private MockMvc mockMvc;

  @Test
  void getStatistics_withValidData_shouldReturnStatus200() throws Exception {
    StatisticEntity statisticEntity = new StatisticEntity(1, 3, 4, 5);
    BucketEntity bucketEntity = new BucketEntity(1, 1.2, 1024.0, "test", 1, statisticEntity);

    StatisticsResponse response = new StatisticsResponse(statisticEntity.getNumOfDownloads(), statisticEntity.getNumOfUploads(), statisticEntity.getNumOfFiles());

    Mockito.when(statisticService.getStatistics(bucketEntity.getId())).thenReturn(response);

    mockMvc.perform(get("/api/statistics/1")).andExpect(status().isOk())
        .andExpect(
            MockMvcResultMatchers.jsonPath("$.numOfDownloads",
                Matchers.is(statisticEntity.getNumOfDownloads())))
        .andExpect(
        MockMvcResultMatchers.jsonPath("$.numOfUploads",
            Matchers.is(statisticEntity.getNumOfUploads())));
  }
}
