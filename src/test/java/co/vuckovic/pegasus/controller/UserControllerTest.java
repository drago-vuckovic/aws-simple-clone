package co.vuckovic.pegasus.controller;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.vuckovic.pegasus.api.v1.controller.UserController;
import co.vuckovic.pegasus.model.dto.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import co.vuckovic.pegasus.common.BaseUnitTest;
import co.vuckovic.pegasus.common.WebMvcTestConfig;
import co.vuckovic.pegasus.model.enumeration.Role;
import co.vuckovic.pegasus.model.enumeration.TenantStatus;
import co.vuckovic.pegasus.model.request.ChangePasswordRequest;
import co.vuckovic.pegasus.model.request.UpdateCompanyNameRequest;
import co.vuckovic.pegasus.model.request.UpdateProfileDetailsRequest;
import co.vuckovic.pegasus.repository.entity.TenantEntity;
import co.vuckovic.pegasus.repository.entity.UserEntity;
import co.vuckovic.pegasus.service.UserService;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = UserController.class)
@WebMvcTestConfig
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest extends BaseUnitTest {

  @MockBean
  private UserService userService;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private ModelMapper mapper;

  @Test
  void getAllUsersById_withValidData_shouldReturnStatus200() throws Exception {
    TenantEntity tenantEntity =
        new TenantEntity(
            1,
            "test",
            "test.pegasus.com",
            TenantStatus.VERIFIED,
            new Timestamp(System.currentTimeMillis()),
            null,
            null, null,null);

    UserEntity userEntity =
        new UserEntity(
            1,
            "test",
            "test",
            "mail@mail.com",
            "$2a$10$ZRekLsIaysJsfF9v.YU0wOCmJr0pWMs25MDq0C0dN7Fazacz7/Gc.",
            Role.OWNER,
            null,
            true,
            tenantEntity.getId());

    UserEntity userEntity2 =
        new UserEntity(
            2,
            "test2",
            "test2",
            "mail2@mail.com",
            "$1a$10$ZRekLsIaysJsfF9v.YU0wOCmJr0pWMs25MDq0C0dN7Fazacz7/Gc.",
            Role.OWNER,
            null,
            true,
            tenantEntity.getId());

    User user = mapper.map(userEntity, User.class);
    User user2 = mapper.map(userEntity2, User.class);
    List<User> users = new ArrayList<>();
    users.add(user);
    users.add(user2);
    Integer id = 1;
    Mockito.when(userService.getAllUsersByTenantId(id)).thenReturn(users);
    mockMvc.perform(get("/api/users/1")).andExpect(status().isOk());
  }

  @Test
  void changeUserStatus_withValidData_shouldReturnStatusCode200() throws Exception {

    String json = " [ {\"op\":\"replace\",\"path\":\"/enabled\",\"value\":\"false\"} ]";

    JsonNode jsonNode = objectMapper.readTree(json);

    JsonPatch jsonPatch = JsonPatch.fromJson(jsonNode);

    doNothing().when(userService).changeUserStatus(1, jsonPatch);

    mockMvc
        .perform(
            patch("/api/users/{id}/status", 1)
                .contentType(MediaType.valueOf("application/json-patch+json"))
                .content(
                    "[\n"
                        + "    {\"op\":\"replace\",\"path\":\"/enabled\",\"value\":\"false\"}\n"
                        + "]"))
        .andExpect(status().isOk());
  }

  @Test
  void changeUserRole_withValidData_shouldReturnStatusCode200() throws Exception {

    String json = " [ {\"op\":\"replace\",\"path\":\"/role\",\"value\":\"USER\"} ]";

    JsonNode jsonNode = objectMapper.readTree(json);

    JsonPatch jsonPatch = JsonPatch.fromJson(jsonNode);

    doNothing().when(userService).changeUserRole(1, jsonPatch);

    mockMvc
        .perform(
            patch("/api/users/{id}/role", 1)
                .contentType(MediaType.valueOf("application/json-patch+json"))
                .content(
                    "[\n"
                        + "    {\"op\":\"replace\",\"path\":\"/role\",\"value\":\"USER\"}\n"
                        + "]"))
        .andExpect(status().isOk());
  }

  @Test
  void changePassword_withValidData_shouldReturnStatusCode200() throws Exception {
    Integer userId = 1;
    ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest("oldPassword",
        "newPaasword1#");

    doNothing().when(userService).changePassword(1, changePasswordRequest);

    mockMvc
        .perform(
            put("/api/users/" + userId + "/password")
                .content(new ObjectMapper().writeValueAsString(changePasswordRequest))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andReturn();
  }

  @Test
  void updateProfileDetails_withValidData_shouldReturnStatus200() throws Exception {

    UpdateProfileDetailsRequest updateProfileDetailsRequest =
        UpdateProfileDetailsRequest.builder().firstname("FirstnameUpdate")
            .lastname("LastnameUpdate").build();

    Integer userId = 1;

    doNothing()
        .when(userService)
        .updateProfileDetails(ArgumentMatchers.any(), ArgumentMatchers.any());

    mockMvc
        .perform(
            put("/api/users/" + userId + "/profile-details")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    new org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper()
                        .writeValueAsString(updateProfileDetailsRequest)))
        .andExpect(status().isOk());
  }

  @Test
  void updateCompanyName_withValidData_shouldReturnStatusOk() throws Exception {
    UpdateCompanyNameRequest request =
        UpdateCompanyNameRequest.builder().company("novoIme").build();

    Integer userId = 1;

    Mockito.doNothing()
        .when(userService)
        .updateCompanyName(ArgumentMatchers.any(), ArgumentMatchers.any());

    mockMvc
        .perform(
            put("/api/users/" + userId + "/company-name")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    new org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper()
                        .writeValueAsString(request)))
        .andExpect(status().isOk());
  }
}
