package co.vuckovic.pegasus.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import co.vuckovic.pegasus.common.WebMvcTestConfig;
import co.vuckovic.pegasus.config.LambdaProperties;
import co.vuckovic.pegasus.repository.FileEntityRepository;
import org.mockito.InjectMocks;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@WebMvcTest(value = InvitationService.class)
@WebMvcTestConfig
@AutoConfigureMockMvc(addFilters = false)
@EnableWebMvc
class LambdaServiceTest {
  @MockBean
  private  KafkaTemplate<String, String> kafkaTemplate;
  @MockBean
  private  LambdaProperties lambdaProperties;
  @MockBean
  private  ModelMapper modelMapper;
  @MockBean
  private  RestTemplate restTemplate;
  @MockBean
  private  FileEntityRepository fileEntityRepository;
  @MockBean
  private  ObjectMapper objectMapper;
  @MockBean private JwtUserDetailsService jwtUserDetailsService;

  @MockBean private PasswordEncoder passwordEncoder;

  @MockBean private InvitationService invitationService;

  @Autowired @InjectMocks
  private LambdaService lambdaService;

  @Autowired
  private MockMvc mockMvc;
 /* @Test
  void crateLambda_withValidData_shouldReturnStatusOK()
  {
    LambdaCreationRequest lambdaCreationRequest =
        LambdaCreationRequest.builder()
            .destFolderId(1)
            .srcFolderId(2)
            .name("test")
            .triggerType(TriggerType.UPLOAD)
            .build();
    TriggerEntity triggerEntity =
        TriggerEntity.builder()
            .id(0)
            .srcFolderId(lambdaCreationRequest.getSrcFolderId())
            .triggerType(lambdaCreationRequest.getTriggerType())
            .build();

    Mockito.when(triggerEntityRepository.existsBySrcFolderIdAndTriggerType(
        lambdaCreationRequest.getSrcFolderId(), lambdaCreationRequest.getTriggerType())).thenReturn(true);



   // triggerEntityRepository.save(triggerEntity);

  }



*/


}
