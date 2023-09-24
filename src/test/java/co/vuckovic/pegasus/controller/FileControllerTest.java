package co.vuckovic.pegasus.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.vuckovic.pegasus.api.v1.controller.FileController;
import co.vuckovic.pegasus.model.dto.File;
import co.vuckovic.pegasus.model.dto.IdNamePair;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import co.vuckovic.pegasus.common.BaseUnitTest;
import co.vuckovic.pegasus.common.WebMvcTestConfig;
import co.vuckovic.pegasus.model.enumeration.PermissionType;
import co.vuckovic.pegasus.model.enumeration.UserGroup;
import co.vuckovic.pegasus.model.request.DeleteFolderRequest;
import co.vuckovic.pegasus.model.request.FolderGroupPermissionChangeRequest;
import co.vuckovic.pegasus.model.request.MultipartFileDeleteRequest;
import co.vuckovic.pegasus.model.response.ListFolderResponse;
import co.vuckovic.pegasus.service.FileService;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(value = FileController.class)
@WebMvcTestConfig
@AutoConfigureMockMvc(addFilters = false)
class FileControllerTest extends BaseUnitTest {

  @MockBean
  private FileService fileService;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void listBucket_withValidData_ShouldReturnStatus200AndListBucket() throws Exception {
    File file1 = new File(1, "file1", true, new Timestamp(System.currentTimeMillis()), 1.2, null,
        "mail@mail.com", null);
    File file2 = new File(2, "file2", true, new Timestamp(System.currentTimeMillis()), 1.2, null,
        "mail@mail.com", null);
    File file3 = new File(3, "file3", true, new Timestamp(System.currentTimeMillis()), 1.2, null,
        "mail@mail.com", null);

    List<File> files = new ArrayList<>();
    files.add(file1);
    files.add(file2);
    files.add(file3);
    Mockito.when(fileService.listBucket()).thenReturn(files);
    MvcResult mvcResult = mockMvc.perform(get("/api/folder/bucket")).andExpect(status().isOk())
        .andReturn();

    String actualResponseBody = mvcResult.getResponse().getContentAsString();
    assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
        objectMapper.writeValueAsString(files));
  }

  @Test
  void listFolder_withValidData_ShouldReturnStatus200AndListFolder() throws Exception {
    File file1 = new File(4, "file1", true, new Timestamp(System.currentTimeMillis()), 1.2, 1,
        "mail@mail.com", null);
    File file2 = new File(2, "file2", true, new Timestamp(System.currentTimeMillis()), 1.2, 1,
        "mail@mail.com", null);
    File file3 = new File(3, "file3", true, new Timestamp(System.currentTimeMillis()), 1.2, 1,
        "mail@mail.com", null);

    List<File> files = new ArrayList<>();
    files.add(file1);
    files.add(file2);
    files.add(file3);
    LinkedList<IdNamePair> list = new LinkedList<>();
    list.addFirst(new IdNamePair(7, "test"));
    list.addFirst(new IdNamePair(1, "test2"));
    ListFolderResponse response = new ListFolderResponse(files, list);
    Integer id = 1;
    Mockito.when(fileService.listFolder(id)).thenReturn(response);
    MvcResult mvcResult = mockMvc.perform(get("/api/folder/" + id)).andExpect(status().isOk())
        .andReturn();

    String actualResponseBody = mvcResult.getResponse().getContentAsString();
    assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
        objectMapper.writeValueAsString(response));
  }

  @Test
  void changeFolderGroupPermission_withValidData_shouldReturnStatus200() throws Exception {
    FolderGroupPermissionChangeRequest folderGroupPermissionChangeRequest =
        new FolderGroupPermissionChangeRequest(UserGroup.USER, PermissionType.VIEW);
    Integer folderId = 1;

    doNothing()
        .when(fileService)
        .changeFolderGroupPermission(folderId, folderGroupPermissionChangeRequest);

    mockMvc
        .perform(
            put("/api/folder/" + folderId + "/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    new org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper()
                        .writeValueAsString(folderGroupPermissionChangeRequest)))
        .andExpect(status().isOk());
  }

  @Test
  void deleteFile_withValidData_shouldReturnStatus200() throws Exception {
    ArrayList<Integer> list =new ArrayList<>();
    list.add(1);
    MultipartFileDeleteRequest multipartFileDeleteRequest =new MultipartFileDeleteRequest(
        list );
    doNothing().when(fileService).deleteFile(multipartFileDeleteRequest);

    mockMvc.perform(delete("/api/file")
        .contentType(MediaType.APPLICATION_JSON)
        .content(new org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper()
            .writeValueAsString(multipartFileDeleteRequest))).andExpect(status().isOk());
  }

  @Test
  void renameFolder_withValidData_shouldReturnStatus200() throws Exception {
    String json = " [ {\"op\":\"replace\",\"path\":\"/name\",\"value\":\"testName\"} ]";

    JsonNode jsonNode = objectMapper.readTree(json);

    JsonPatch jsonPatch = JsonPatch.fromJson(jsonNode);

    Mockito.when(fileService.renameFolder(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(
            new File(
                2,
                "sub",
                false,
                new Timestamp(System.currentTimeMillis()),
                1.2,
                1,
                "test@mail.com", null));

    mockMvc
        .perform(
            patch("/api/folder/{id}", 1)
                .contentType(MediaType.valueOf("application/json-patch+json"))
                .content(" [ {\"op\":\"replace\",\"path\":\"/name\",\"value\":\"testName\"} ]"))
        .andExpect(status().isOk());
  }

  @Test
  void deleteFolder_withPermissions_shouldReturnStatus200() throws Exception {
    ArrayList<Integer> list =new ArrayList<>();
    list.add(1);
    DeleteFolderRequest deleteFolderRequest=new DeleteFolderRequest(list);
    doNothing().when(fileService).deleteFolder(deleteFolderRequest);

    mockMvc.perform(delete("/api/folder").contentType(MediaType.APPLICATION_JSON)
        .content(new org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper()
            .writeValueAsString(deleteFolderRequest))).andExpect(status().isOk());
  }

  @Test
  void renameFile_withValidData_shouldReturnStatus200() throws Exception {
    String json = " [ {\"op\":\"replace\",\"path\":\"/name\",\"value\":\"testName\"} ]";
    JsonNode jsonNode = objectMapper.readTree(json);
    JsonPatch jsonPatch = JsonPatch.fromJson(jsonNode);

    Integer fileId = 1;

    Mockito.when(fileService.renameFile(fileId, jsonPatch))
        .thenReturn(new File(4, "testName", true, new Timestamp(System.currentTimeMillis()), 1.2, 1,
            "test@mail.com", null));

    mockMvc

        .perform(
            patch("/api/folder/{id}", 1)
                .contentType(MediaType.valueOf("application/json-patch+json"))
                .content(" [ {\"op\":\"replace\",\"path\":\"/name\",\"value\":\"testName\"} ]"))
        .andExpect(status().isOk());
  }
}
