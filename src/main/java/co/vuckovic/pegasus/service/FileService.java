package co.vuckovic.pegasus.service;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import co.vuckovic.pegasus.util.FileVisitorUtil;
import co.vuckovic.pegasus.util.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import co.vuckovic.pegasus.config.BucketProperties;
import co.vuckovic.pegasus.config.LambdaProperties;
import co.vuckovic.pegasus.model.dto.File;
import co.vuckovic.pegasus.model.dto.FolderForLambdaCreation;
import co.vuckovic.pegasus.model.dto.IdNamePair;
import co.vuckovic.pegasus.model.dto.JwtUser;
import co.vuckovic.pegasus.model.dto.LambdaJobData;
import co.vuckovic.pegasus.model.enumeration.PermissionType;
import co.vuckovic.pegasus.model.enumeration.Role;
import co.vuckovic.pegasus.model.enumeration.TriggerType;
import co.vuckovic.pegasus.model.enumeration.UserGroup;
import co.vuckovic.pegasus.model.exception.ActionNotAllowedException;
import co.vuckovic.pegasus.model.exception.ConflictException;
import co.vuckovic.pegasus.model.exception.FolderExistsException;
import co.vuckovic.pegasus.model.exception.HttpException;
import co.vuckovic.pegasus.model.exception.NotFoundException;
import co.vuckovic.pegasus.model.request.ChangeLambdaFolderRequest;
import co.vuckovic.pegasus.model.request.CreateFolderRequest;
import co.vuckovic.pegasus.model.request.DeleteFolderRequest;
import co.vuckovic.pegasus.model.request.FolderGroupPermissionChangeRequest;
import co.vuckovic.pegasus.model.request.MultipartFileDeleteRequest;
import co.vuckovic.pegasus.model.request.UpdateCompanyNameRequest;
import co.vuckovic.pegasus.model.response.FileResponse;
import co.vuckovic.pegasus.model.response.ListFolderResponse;
import co.vuckovic.pegasus.repository.BucketEntityRepository;
import co.vuckovic.pegasus.repository.FileEntityRepository;
import co.vuckovic.pegasus.repository.PermissionEntityRepository;
import co.vuckovic.pegasus.repository.StatisticEntityRepository;
import co.vuckovic.pegasus.repository.UserEntityRepository;
import co.vuckovic.pegasus.repository.entity.BucketEntity;
import co.vuckovic.pegasus.repository.entity.FileEntity;
import co.vuckovic.pegasus.repository.entity.PermissionEntity;
import co.vuckovic.pegasus.repository.entity.StatisticEntity;
import co.vuckovic.pegasus.repository.entity.UserEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class FileService {

  private final BucketProperties bucketProperties;

  private final FileEntityRepository fileEntityRepository;

  private final ModelMapper modelMapper;

  private final PermissionEntityRepository permissionEntityRepository;

  private final BucketEntityRepository bucketEntityRepository;

  private final UserEntityRepository userEntityRepository;

  private final StatisticEntityRepository statisticEntityRepository;

  private final JwtUtil jwtUtil;

  private final ObjectMapper objectMapper;

  private final LambdaService lambdaService;

  private final RestTemplate restTemplate;

  private final LambdaProperties lambdaProperties;

  public boolean createBucketFolder(String companyName) {
    Path bucketFolderPath = Paths.get(bucketProperties.getFolderPath()).resolve(companyName);
    return bucketFolderPath.toFile().mkdirs();
  }

  public File createFolder(CreateFolderRequest createFolderRequest) {
    JwtUser currentUser = jwtUtil.getCurrentUser();
    UserEntity userEntity = userEntityRepository.getById(currentUser.getId());

    if (createFolderRequest.getParentId() != null
        && !fileEntityRepository.existsById(createFolderRequest.getParentId())) {
      throw new NotFoundException("Parent does not exist.");
    }

    BucketEntity bucketEntity =
        bucketEntityRepository
            .findById(createFolderRequest.getBucketId())
            .orElseThrow(() -> new NotFoundException("Bucket not found"));

    if (Boolean.FALSE.equals(
        checkIfUserBelongsToTenant(bucketEntity.getTenantId(), userEntity.getTenantId()))) {
      throw new ActionNotAllowedException("User doesn't belong to this tenant");
    }

    Path path;
    if (createFolderRequest.getParentId() == null) {
      String bucketPath =
          bucketProperties.getFolderPath() + bucketEntity.getName() + java.io.File.separator;
      path = Paths.get(bucketPath).resolve(createFolderRequest.getName());
    } else {
      FileEntity parent =
          fileEntityRepository
              .findById(createFolderRequest.getParentId())
              .orElseThrow(() -> new NotFoundException("Parent not found"));

      if (Boolean.FALSE.equals(checkIfPermissionIsAllowed(
              parent, UserGroup.ADMIN, createFolderRequest.getPermissionTypeAdmin()))
          && Boolean.TRUE.equals(checkIfPermissionIsAllowed(
              parent, UserGroup.USER, createFolderRequest.getPermissionTypeUser()))) {
        throw new ActionNotAllowedException(
            "The parent directory has more restrictive permissions");
      }

      if (!parent.getOwner().getId().equals(currentUser.getId())
          && Boolean.FALSE.equals(
              checkIfPermissionExists(
                  currentUser.getRole(), PermissionType.EDIT, parent.getPermissions()))) {
        throw new ActionNotAllowedException(
            "Current user does not have permission to edit this file");
      }

      path = Paths.get(parent.getPath()).resolve(createFolderRequest.getName());
    }

    FileEntity newFolder;
    if (path.toFile().mkdirs()) {
      newFolder = modelMapper.map(createFolderRequest, FileEntity.class);
      newFolder.setBucketId(bucketEntity.getId());
      newFolder.setOwner(userEntity);
      newFolder.setSize(0.0);
      newFolder.setIsFolder(true);
      newFolder.setPath(path.toString());
      newFolder.setLastModified(new Timestamp(System.currentTimeMillis()));
      newFolder = fileEntityRepository.save(newFolder);
      permissionEntityRepository.save(
          new PermissionEntity(
              0, UserGroup.ADMIN, createFolderRequest.getPermissionTypeAdmin(), newFolder.getId()));
      permissionEntityRepository.save(
          new PermissionEntity(
              0, UserGroup.USER, createFolderRequest.getPermissionTypeUser(), newFolder.getId()));
      newFolder = fileEntityRepository.getById(newFolder.getId());
    } else {
      throw new FolderExistsException("Folder already exists.");
    }
    return modelMapper.map(newFolder, File.class);
  }

  public void changeFolderGroupPermission(
      Integer folderId, FolderGroupPermissionChangeRequest folderGroupPermissionChangeRequest) {

    JwtUser currentUser = jwtUtil.getCurrentUser();

    UserEntity userEntity =
        userEntityRepository
            .findById(currentUser.getId())
            .orElseThrow(() -> new NotFoundException("User not found"));
    BucketEntity bucketEntity =
        bucketEntityRepository
            .findByTenantId(userEntity.getTenantId())
            .orElseThrow(() -> new NotFoundException("Bucket doesn't exist"));

    if (Boolean.FALSE.equals(
        checkIfUserBelongsToTenant(bucketEntity.getTenantId(), userEntity.getTenantId()))) {
      throw new ActionNotAllowedException("User doesn't belong to this tenant");
    }

    FileEntity fileEntity =
        fileEntityRepository
            .findById(folderId)
            .orElseThrow(() -> new NotFoundException("Folder doesn't exist"));

    if (fileEntity.getParentId() != null) {
      FileEntity parentFileEntity =
          fileEntityRepository
              .findById(fileEntity.getParentId())
              .orElseThrow(() -> new NotFoundException("Parent directory missing"));

      if (parentFileEntity != null
          && !checkIfPermissionIsAllowed(
              parentFileEntity,
              folderGroupPermissionChangeRequest.getUserGroup(),
              folderGroupPermissionChangeRequest.getPermissionType())) {
        throw new ActionNotAllowedException(
            "The parent directory has more restrictive permissions");
      }

      if (Boolean.FALSE.equals(checkIfUserIsBucketOwner(userEntity, bucketEntity))
          && Boolean.FALSE.equals(checkIfUserIsFolderOwner(userEntity.getId(), fileEntity))) {
        throw new NotFoundException("Only owner of the file can change permissions");
      }
    }
    PermissionEntity permissionEntity =
        permissionEntityRepository
            .findByFileIdAndUserGroup(folderId, folderGroupPermissionChangeRequest.getUserGroup())
            .orElseThrow(() -> new NotFoundException("Permission not found."));

    permissionEntity.setPermissionType(folderGroupPermissionChangeRequest.getPermissionType());
    permissionEntityRepository.save(permissionEntity);
  }

  public ListFolderResponse listFolder(Integer folderId) {
    JwtUser currentUser = jwtUtil.getCurrentUser();
    UserEntity userEntity = userEntityRepository.getById(currentUser.getId());

    FileEntity fileEntity =
        fileEntityRepository
            .findFileEntityByIdAndIsFolder(folderId, true)
            .orElseThrow(() -> new NotFoundException("Folder not found"));

    BucketEntity bucketEntity = bucketEntityRepository.getById(fileEntity.getBucketId());

    if (Boolean.FALSE.equals(
        checkIfUserBelongsToTenant(bucketEntity.getTenantId(), userEntity.getTenantId()))) {
      throw new ActionNotAllowedException("User doesn't belong to this tenant");
    }

    Boolean checkPermissionsResult =
        checkIfPermissionExists(
            currentUser.getRole(), PermissionType.VIEW, fileEntity.getPermissions());
    if (!fileEntity.getOwner().getId().equals(currentUser.getId())
        && !Boolean.TRUE.equals(checkPermissionsResult)) {
      throw new ActionNotAllowedException(
          "Current user does not have permission to list this folder");
    }

    List<File> children =
        fileEntity.getChildren().stream()
            .filter(
                child -> {
                  if (Boolean.FALSE.equals(child.getIsFolder())) {
                    return true;
                  } else {
                    return child.getOwner().getId().equals(currentUser.getId())
                        || checkIfPermissionExists(
                            currentUser.getRole(), PermissionType.VIEW, child.getPermissions());
                  }
                })
            .map(entity -> modelMapper.map(entity, File.class))
            .toList();

    LinkedList<IdNamePair> pairs = getIdNamePairsRecursivelyTowardsRoot(fileEntity.getParentId());
    pairs.addLast(new IdNamePair(fileEntity.getId(), fileEntity.getName()));

    return new ListFolderResponse(children, pairs);
  }

  public FileResponse downloadFile(Integer fileId) {
    JwtUser currentUser = jwtUtil.getCurrentUser();
    UserEntity userEntity = userEntityRepository.getById(currentUser.getId());

    FileEntity fileEntity =
        fileEntityRepository
            .findFileEntityByIdAndIsFolder(fileId, false)
            .orElseThrow(() -> new NotFoundException("File not found!"));
    FileEntity parent = fileEntityRepository.getById(fileEntity.getParentId());

    BucketEntity bucketEntity = bucketEntityRepository.getById(parent.getBucketId());

    if (Boolean.FALSE.equals(
        checkIfUserBelongsToTenant(bucketEntity.getTenantId(), userEntity.getTenantId()))) {
      throw new ActionNotAllowedException("User doesn't belong to this tenant");
    }

    Boolean checkPermissionsResult =
        checkIfPermissionExists(
            currentUser.getRole(), PermissionType.VIEW, parent.getPermissions());
    if (!parent.getOwner().getId().equals(currentUser.getId())
        && !Boolean.TRUE.equals(checkPermissionsResult)) {
      throw new ActionNotAllowedException(
          "Current user does not have permission to download this file");
    }

    byte[] fileData;
    String type = getFileExtension(fileEntity);
    try {
      fileData = Files.readAllBytes(Paths.get(fileEntity.getPath()));
    } catch (IOException e) {
      log.error(String.format("An error occurred while downloading file: %s", e.getMessage()));
      throw new ActionNotAllowedException(e.getMessage());
    }

    incrementNumberOfDownloads(bucketEntityRepository.getById(fileEntity.getBucketId()));

    return new FileResponse(
        fileEntity.getName(), fileEntity.getSize(), type, fileEntity.getPath(), fileData);
  }

  public List<File> listBucket() {
    JwtUser currentUser = jwtUtil.getCurrentUser();

    UserEntity userEntity =
        userEntityRepository
            .findById(currentUser.getId())
            .orElseThrow(() -> new NotFoundException("User not found"));
    BucketEntity bucketEntity =
        bucketEntityRepository
            .findByTenantId(userEntity.getTenantId())
            .orElseThrow(() -> new NotFoundException("Bucket doesn't exist"));

    if (Boolean.FALSE.equals(
        checkIfUserBelongsToTenant(bucketEntity.getTenantId(), userEntity.getTenantId()))) {
      throw new ActionNotAllowedException("User doesn't belong to this tenant");
    }

    if (Boolean.FALSE.equals(
        checkIfUserBelongsToTenant(bucketEntity.getTenantId(), userEntity.getTenantId()))) {
      throw new ActionNotAllowedException("User doesn't belong to this tenant");
    }

    List<FileEntity> folders =
        fileEntityRepository.findFileEntitiesByBucketIdAndParentIdNull(bucketEntity.getId());

    return folders.stream()
        .filter(
            folder ->
                folder.getOwner().getId().equals(currentUser.getId())
                    || Boolean.TRUE.equals(
                        checkIfPermissionExists(
                            currentUser.getRole(), PermissionType.VIEW, folder.getPermissions())))
        .map(entity -> modelMapper.map(entity, File.class))
        .toList();
  }

  public void deleteFolder(DeleteFolderRequest deleteFolderRequest) {
    JwtUser currentUser = jwtUtil.getCurrentUser();
    UserEntity userEntity = userEntityRepository.getById(currentUser.getId());

    for (int folderId : deleteFolderRequest.getArray()) {
      FileEntity fileEntity =
          fileEntityRepository
              .findFileEntityByIdAndIsFolder(folderId, true)
              .orElseThrow(() -> new NotFoundException("Folder not found"));
      BucketEntity bucketEntity = bucketEntityRepository.getById(fileEntity.getBucketId());

      if (Boolean.FALSE.equals(
          checkIfUserBelongsToTenant(bucketEntity.getTenantId(), userEntity.getTenantId()))) {
        throw new ActionNotAllowedException("User doesn't belong to this tenant");
      }

      if (!(fileEntity.getOwner().getId().equals(currentUser.getId())
          || currentUser.getRole().equals(Role.OWNER))) {
        throw new ActionNotAllowedException(
            "Current user does not have permission to delete folder");
      }

      try {
        fileEntityRepository.delete(fileEntity);
        deleteFolderOnFS(fileEntity.getPath());
        disableLambdasWithSrcOrDestPath(fileEntity.getPath());
      } catch (IOException e) {
        log.error(String.format("An error occurred while deleting folder: %s", e.getMessage()));
        throw new NotFoundException(e.getMessage());
      }
    }
  }

  public void deleteFile(MultipartFileDeleteRequest multipartFileDeleteRequest) {
    JwtUser currentUser = jwtUtil.getCurrentUser();
    UserEntity userEntity = userEntityRepository.getById(currentUser.getId());

    for (int fileId : multipartFileDeleteRequest.getArray()) {
      FileEntity fileEntity =
          fileEntityRepository
              .findFileEntityByIdAndIsFolder(fileId, false)
              .orElseThrow(() -> new NotFoundException("File does not exist."));

      BucketEntity bucketEntity = bucketEntityRepository.getById(fileEntity.getBucketId());

      if (Boolean.FALSE.equals(
          checkIfUserBelongsToTenant(bucketEntity.getTenantId(), userEntity.getTenantId()))) {
        throw new ActionNotAllowedException("User doesn't belong to this tenant");
      }

      FileEntity parentDir = fileEntityRepository.getById(fileEntity.getParentId());

      if (!parentDir.getOwner().getId().equals(currentUser.getId())
          && Boolean.FALSE.equals(
              checkIfPermissionExists(
                  currentUser.getRole(), PermissionType.EDIT, parentDir.getPermissions()))) {
        throw new ActionNotAllowedException(
            "Current user does not have permission to delete this file");
      }

      try {
        byte[] fileBytes = Files.readAllBytes(Paths.get(fileEntity.getPath()));

        fileEntityRepository.delete(fileEntity);

        if (lambdaService.checkIfAnyLambdaExists(parentDir.getPath(), TriggerType.DELETE)) {
          LambdaJobData lambdaJobData =
              new LambdaJobData(
                  parentDir.getPath(), TriggerType.DELETE, fileEntity.getName(), fileBytes);
          lambdaService.sendMessage(lambdaJobData);
        }

        Files.delete(Paths.get(fileEntity.getPath()));
      } catch (IOException e) {
        log.error(String.format("An error occurred while deleting file: %s", e.getMessage()));
        throw new NotFoundException(e.getMessage());
      }
    }
  }

  public File renameFile(Integer id, JsonPatch fileJsonPatch)
      throws JsonPatchException, JsonProcessingException {
    JwtUser currentUser = jwtUtil.getCurrentUser();
    UserEntity userEntity = userEntityRepository.getById(currentUser.getId());

    FileEntity fileBeforePatch =
        fileEntityRepository
            .findFileEntityByIdAndIsFolder(id, false)
            .orElseThrow(() -> new NotFoundException("File not found"));

    BucketEntity bucketEntity = bucketEntityRepository.getById(fileBeforePatch.getBucketId());
    if (Boolean.FALSE.equals(
        checkIfUserBelongsToTenant(bucketEntity.getTenantId(), userEntity.getTenantId()))) {
      throw new ActionNotAllowedException("User doesn't belong to this tenant");
    }

    FileEntity parent = fileEntityRepository.getById(fileBeforePatch.getParentId());

    if (!parent.getOwner().getId().equals(currentUser.getId())
        && Boolean.FALSE.equals(
            checkIfPermissionExists(
                currentUser.getRole(), PermissionType.EDIT, parent.getPermissions()))) {
      throw new ActionNotAllowedException("Current user does not have permission to rename file");
    }

    FileEntity fileAfterPatch = applyPatchToFile(fileJsonPatch, fileBeforePatch);
    try {
      java.io.File fileOnFS = new java.io.File(fileBeforePatch.getPath());
      if (fileAfterPatch.getName().contains(".")) {
        throw new ActionNotAllowedException("New file name is not valid, it contains .");
      }
      Path path =
          Paths.get(fileOnFS.getParent())
              .resolve(
                  fileAfterPatch.getName() + "." + FilenameUtils.getExtension(fileOnFS.getName()));
      if (!path.toFile().exists() && fileOnFS.renameTo(path.toFile())) {
        fileAfterPatch.setPath(path.toString());
        fileAfterPatch.setName(
            fileAfterPatch.getName() + "." + FilenameUtils.getExtension(fileOnFS.getName()));
        fileEntityRepository.save(fileAfterPatch);
      } else {
        throw new ActionNotAllowedException("File already exists");
      }
    } catch (Exception e) {
      log.error(String.format("An error occurred while renaming file: %s", e.getMessage()));
      throw e;
    }

    return modelMapper.map(fileAfterPatch, File.class);
  }


  public File renameFolder(Integer id, JsonPatch folderJsonPatch)
      throws JsonPatchException, JsonProcessingException {
    JwtUser currentUser = jwtUtil.getCurrentUser();
    UserEntity userEntity = userEntityRepository.getById(currentUser.getId());

    FileEntity folderBeforePatch =
        fileEntityRepository
            .findFileEntityByIdAndIsFolder(id, true)
            .orElseThrow(() -> new NotFoundException("Folder not found!"));

    BucketEntity bucketEntity = bucketEntityRepository.getById(folderBeforePatch.getBucketId());

    ChangeLambdaFolderRequest changeLambdaFolderRequest=ChangeLambdaFolderRequest.builder().oldPath(folderBeforePatch.getPath()).build();

    if (Boolean.FALSE.equals(
        checkIfUserBelongsToTenant(bucketEntity.getTenantId(), userEntity.getTenantId()))) {
      throw new ActionNotAllowedException("User doesn't belong to this tenant");
    }

    if (!folderBeforePatch.getOwner().getId().equals(currentUser.getId())
        && Boolean.FALSE.equals(
            checkIfPermissionExists(
                currentUser.getRole(), PermissionType.EDIT, folderBeforePatch.getPermissions()))) {
      throw new ActionNotAllowedException("Current user does not have permission to rename folder");
    }

    FileEntity folderAfterPatch = applyPatchToFile(folderJsonPatch, folderBeforePatch);
    folderAfterPatch.setPath(
        folderBeforePatch
            .getPath().substring(0, folderAfterPatch.getPath().lastIndexOf(folderBeforePatch.getName()))+folderAfterPatch.getName());

    try {
      java.io.File folder = new java.io.File(folderAfterPatch.getPath());
      if (folder.isDirectory() && folder.exists()) {
        throw new ActionNotAllowedException("A folder with the same name already exists.");
      }
      Files.move(
          Paths.get(folderBeforePatch.getPath()),
          Paths.get(folderAfterPatch.getPath()),
          REPLACE_EXISTING);
      updateChildPaths(folderAfterPatch);
      fileEntityRepository.save(folderAfterPatch);


      changeLambdaFolderRequest.setNewPath(folderAfterPatch.getPath());

      restTemplate.put(
          lambdaProperties.getBaseUrl() + "/change-folder", changeLambdaFolderRequest);

      return modelMapper.map(folderAfterPatch, File.class);
    } catch (IOException e) {
      log.error(String.format("An error occurred while renaming folder: %s", e.getMessage()));
      throw new ActionNotAllowedException(e.getMessage());
    }
  }

  public File uploadFile(MultipartFile file, Integer folderId) throws IOException {
    JwtUser currentUser = jwtUtil.getCurrentUser();
    UserEntity userEntity = userEntityRepository.getById(currentUser.getId());

    FileEntity parent =
        fileEntityRepository
            .findById(folderId)
            .orElseThrow(() -> new NotFoundException("Parent folder not found"));

    Path checkIfExist =
        Paths.get(parent.getPath() + java.io.File.separator + file.getOriginalFilename());
    java.io.File f = new java.io.File(checkIfExist.toUri());
    if (f.exists()) {
      throw new ConflictException(String.format("File %s already exists", f.getName()));
    }
    if (!parent.getOwner().getId().equals(currentUser.getId())
        && Boolean.FALSE.equals(
            checkIfPermissionExists(
                currentUser.getRole(), PermissionType.EDIT, parent.getPermissions()))) {
      throw new ActionNotAllowedException("Current user does not have permission to upload file");
    }
    BucketEntity bucketEntity =
        bucketEntityRepository
            .findById(parent.getBucketId())
            .orElseThrow(() -> new NotFoundException("Parent folder not found"));

    if (Boolean.FALSE.equals(
        checkIfUserBelongsToTenant(bucketEntity.getTenantId(), userEntity.getTenantId()))) {
      throw new ActionNotAllowedException("User doesn't belong to this tenant");
    }

    byte[] bytes = file.getBytes();
    Path path =
        Paths.get(parent.getPath()).resolve(Objects.requireNonNull(file.getOriginalFilename()));
    double fileSize = bytes.length / 1024.0;
    if (bucketEntity.getCapacity() != null) {
      if (bucketEntity.getCapacity() <= bucketEntity.getSize() + fileSize) {
        throw new ConflictException("Limit size exceeded");
      }
    }

    FileEntity newFile =
        new FileEntity(
            0,
            file.getOriginalFilename(),
            false,
            new Timestamp(System.currentTimeMillis()),
            fileSize,
            folderId,
            parent.getBucketId(),
            userEntity,
            path.toString(),
            null,
            null);
    newFile = fileEntityRepository.save(newFile);

    if (lambdaService.checkIfAnyLambdaExists(parent.getPath(), TriggerType.UPLOAD)) {

      LambdaJobData lambdaJobData =
          new LambdaJobData(parent.getPath(), TriggerType.UPLOAD, newFile.getName(), bytes);
      lambdaService.sendMessage(lambdaJobData);
    }
    Files.write(path, bytes);

    return modelMapper.map(newFile, File.class);
  }

  public void updateBucketName(BucketEntity bucketEntity, UpdateCompanyNameRequest request) {
    JwtUser currentUser = jwtUtil.getCurrentUser();
    UserEntity userEntity = userEntityRepository.getById(currentUser.getId());

    if (Boolean.FALSE.equals(
        checkIfUserBelongsToTenant(bucketEntity.getTenantId(), userEntity.getTenantId()))) {
      throw new ActionNotAllowedException("User doesn't belong to this tenant");
    }

    String oldBucketName = bucketEntity.getName();
    bucketEntity.setName(request.getCompany());
    bucketEntityRepository.save(bucketEntity);
    String oldBucketPath =
        bucketProperties.getFolderPath() + oldBucketName + java.io.File.separator;
    String newBucketPath =
        bucketProperties.getFolderPath() + bucketEntity.getName() + java.io.File.separator;
    try {
      Files.move(Paths.get(oldBucketPath), Paths.get(newBucketPath), REPLACE_EXISTING);
    } catch (Exception e) {
      throw new ActionNotAllowedException("Company name can't be changed");
    }
    List<FileEntity> rootFolders =
        fileEntityRepository.findFileEntitiesByBucketIdAndParentIdNull(bucketEntity.getId());
    rootFolders.forEach(
        f -> {
          f.setPath(Paths.get(newBucketPath).resolve(f.getName()).toString());
          f = fileEntityRepository.save(f);
          updateChildPaths(f);
        });
  }

  public List<FolderForLambdaCreation> listFoldersForLambdaCreation() {
    JwtUser currentUser = jwtUtil.getCurrentUser();

    UserEntity userEntity = userEntityRepository.getById(currentUser.getId());
    BucketEntity bucketEntity =
        bucketEntityRepository
            .findByTenantId(userEntity.getTenantId())
            .orElseThrow(() -> new NotFoundException("Bucket doesn't exist"));

    if (Boolean.FALSE.equals(
        checkIfUserBelongsToTenant(bucketEntity.getTenantId(), userEntity.getTenantId()))) {
      throw new ActionNotAllowedException("User doesn't belong to this tenant");
    }

    return fileEntityRepository
        .findFileEntitiesByBucketIdAndIsFolderTrue(bucketEntity.getId())
        .stream()
        .filter(
            folder ->
                folder.getOwner().getId().equals(currentUser.getId())
                    || Boolean.TRUE.equals(
                        checkIfPermissionExists(
                            currentUser.getRole(), PermissionType.EDIT, folder.getPermissions())))
        .map(
            entity ->
                FolderForLambdaCreation.builder()
                    .id(entity.getId())
                    .name(entity.getName())
                    .path(
                        entity
                            .getPath()
                            .substring(bucketProperties.getFolderPath().length())
                            .replace("\\", "/"))
                    .build())
        .toList();
  }

  private void deleteFolderOnFS(String folderPath) throws IOException {
    Path directory = Paths.get(folderPath);

    if (Files.exists(directory)) {
      Files.walkFileTree(directory, new FileVisitorUtil());
    }
  }

  private void updateChildPaths(FileEntity parent) {
    parent
        .getChildren()
        .forEach(
            f -> {
              f.setPath(Paths.get(parent.getPath()).resolve(f.getName()).toString());
              fileEntityRepository.save(f);
              if (Boolean.TRUE.equals(f.getIsFolder())) {
                updateChildPaths(f);
              }
            });
  }

  private Boolean checkIfPermissionExists(
      Role targetRole, PermissionType targetType, List<PermissionEntity> permissions) {
    if (targetRole.equals(Role.OWNER)) {
      return true;
    }
    long matches =
        permissions.stream()
            .filter(
                permissionEntity ->
                    permissionEntity.getUserGroup().toString().equals(targetRole.toString())
                        && permissionEntity.getPermissionType().ordinal() >= targetType.ordinal())
            .count();
    return matches == 1;
  }

  private Boolean checkIfUserIsFolderOwner(Integer userId, FileEntity fileEntity) {
    return fileEntity.getOwner().getId().equals(userId);
  }

  private Boolean checkIfUserIsBucketOwner(UserEntity userEntity, BucketEntity bucketEntity) {
    return userEntity.getRole().equals(Role.OWNER)
        && userEntity.getTenantId().equals(bucketEntity.getTenantId());
  }

  private Boolean checkIfPermissionIsAllowed(
      FileEntity parentFileEntity, UserGroup userGroup, PermissionType permissionType) {
    PermissionEntity permissionEntity =
        parentFileEntity.getPermissions().stream()
            .filter(p -> p.getUserGroup().equals(userGroup))
            .findFirst()
            .orElse(null);
    return permissionEntity == null
        || !permissionEntity.getPermissionType().equals(PermissionType.NO_PERMISSIONS)
        || permissionType.equals(PermissionType.NO_PERMISSIONS);
  }

  private LinkedList<IdNamePair> getIdNamePairsRecursivelyTowardsRoot(Integer parentId) {
    LinkedList<IdNamePair> list = new LinkedList<>();
    FileEntity parent;
    while (parentId != null) {
      parent = fileEntityRepository.getById(parentId);
      list.addFirst(new IdNamePair(parent.getId(), parent.getName()));
      parentId = parent.getParentId();
    }
    return list;
  }

  private FileEntity applyPatchToFile(JsonPatch fileJsonPatch, FileEntity targetFile)
      throws JsonPatchException, JsonProcessingException {
    JsonNode patched = fileJsonPatch.apply(objectMapper.convertValue(targetFile, JsonNode.class));
    return objectMapper.treeToValue(patched, FileEntity.class);
  }

  private void incrementNumberOfDownloads(BucketEntity bucketEntity) {
    StatisticEntity statisticEntity = bucketEntity.getStatisticEntity();
    statisticEntity.setNumOfDownloads(statisticEntity.getNumOfDownloads() + 1);
    statisticEntityRepository.save(statisticEntity);
  }

  private String getFileExtension(FileEntity file) {
    String name = file.getName();
    int lastIndexOf = name.lastIndexOf(".");
    if (lastIndexOf == -1) {
      return "";
    }
    return name.substring(lastIndexOf);
  }

  private boolean checkIfUserBelongsToTenant(Integer tenantIdFromBucket, Integer tenantIdFromUser) {
    return tenantIdFromBucket.equals(tenantIdFromUser);
  }

  private void disableLambdasWithSrcOrDestPath(String dirPath) {
    try {

      restTemplate.postForObject(
          lambdaProperties.getBaseUrl() + "/disable-by-dir-path", dirPath, Void.class);

    } catch (HttpStatusCodeException e) {
      e.printStackTrace();
      throw new HttpException("Lambda creation failed. Check if your file has compilation errors.");
    }
  }
}
