package co.vuckovic.pegasus.api.v1.controller;

import co.vuckovic.pegasus.model.dto.File;
import co.vuckovic.pegasus.model.dto.FolderForLambdaCreation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import co.vuckovic.pegasus.model.request.CreateFolderRequest;
import co.vuckovic.pegasus.model.request.DeleteFolderRequest;
import co.vuckovic.pegasus.model.request.FolderGroupPermissionChangeRequest;
import co.vuckovic.pegasus.model.request.MultipartFileDeleteRequest;
import co.vuckovic.pegasus.model.response.FileResponse;
import co.vuckovic.pegasus.model.response.ListFolderResponse;
import co.vuckovic.pegasus.service.FileService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FileController {

  private final FileService fileService;

  @PostMapping("/folder")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successful request"),
        @ApiResponse(responseCode = "500", description = "Internal error"),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Not found"),
        @ApiResponse(responseCode = "409", description = "Conflict")
      })
  public ResponseEntity<File> createFolder(
      @Valid @RequestBody CreateFolderRequest createFolderRequest) {
    return ResponseEntity.ok(fileService.createFolder(createFolderRequest));
  }

  @GetMapping("/folder/{folderId}")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successful request"),
        @ApiResponse(responseCode = "500", description = "Internal error"),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Not found")
      })
  public ResponseEntity<ListFolderResponse> listFolder(@PathVariable Integer folderId) {
    return ResponseEntity.ok(fileService.listFolder(folderId));
  }

  @GetMapping("/folder/bucket")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successful request"),
        @ApiResponse(responseCode = "500", description = "Internal error"),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Not found")
      })
  public ResponseEntity<List<File>> listBucket() {
    return ResponseEntity.ok(fileService.listBucket());
  }

  @PutMapping("/folder/{folderId}/permissions")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successful request"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Not found")
      })
  public ResponseEntity<Void> changeFolderGroupPermission(
      @PathVariable Integer folderId,
      @RequestBody FolderGroupPermissionChangeRequest folderGroupPermissionChangeRequest) {
    fileService.changeFolderGroupPermission(folderId, folderGroupPermissionChangeRequest);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/file")
  public ResponseEntity<Void> deleteFile(
      @RequestBody MultipartFileDeleteRequest multipartFileDeleteRequest) {
    fileService.deleteFile(multipartFileDeleteRequest);
    return ResponseEntity.ok().build();
  }

  @PatchMapping(value = "/folder/{id}", consumes = "application/json-patch+json")
  public ResponseEntity<File> renameFolder(
      @PathVariable Integer id, @RequestBody JsonPatch folderJsonPatch)
      throws JsonPatchException, JsonProcessingException {
    File renamedFolder = fileService.renameFolder(id, folderJsonPatch);
    return ResponseEntity.ok(renamedFolder);
  }

  @DeleteMapping("/folder")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfull request"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Not found")
      })
  public ResponseEntity<Void> deleteFolder(@RequestBody DeleteFolderRequest deleteFolderRequest) {
    fileService.deleteFolder(deleteFolderRequest);
    return ResponseEntity.ok().build();
  }

  @PatchMapping(value = "/file/{id}", consumes = "application/json-patch+json")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfull request"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Not found")
      })
  public ResponseEntity<File> renameFile(
      @PathVariable Integer id, @RequestBody JsonPatch fileJsonPatch)
      throws JsonPatchException, JsonProcessingException {
    return ResponseEntity.ok(fileService.renameFile(id, fileJsonPatch));
  }

  @PostMapping("/file")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successful request"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Not found"),
        @ApiResponse(responseCode = "500", description = "Internal error")
      })
  public ResponseEntity<File> uploadFile(
      @RequestParam("file") MultipartFile file, @RequestParam Integer folderId) throws IOException {
    return ResponseEntity.ok(fileService.uploadFile(file, folderId));
  }

  @GetMapping("/file/{fileId}")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successful request"),
        @ApiResponse(responseCode = "500", description = "Internal error"),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Not found")
      })
  public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable Integer fileId)
      throws IOException {
    FileResponse response = fileService.downloadFile(fileId);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_TYPE, Files.probeContentType(Paths.get(response.getFilePath())))
        .header(
            HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + response.getName() + "\"")
        .body(new ByteArrayResource(response.getData()));
  }


  @GetMapping("/folder/create-lambda")
  public ResponseEntity<List<FolderForLambdaCreation>> listFoldersForLambdaCreation(){
    return ResponseEntity.ok(fileService.listFoldersForLambdaCreation());
  }

}
