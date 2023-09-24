package co.vuckovic.pegasus.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.vuckovic.pegasus.model.dto.File;
import co.vuckovic.pegasus.model.dto.IdNamePair;
import co.vuckovic.pegasus.model.dto.JwtUser;
import co.vuckovic.pegasus.model.dto.Permission;
import co.vuckovic.pegasus.util.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import co.vuckovic.pegasus.common.BaseUnitTest;
import co.vuckovic.pegasus.common.WebMvcTestConfig;
import co.vuckovic.pegasus.config.BucketProperties;
import co.vuckovic.pegasus.config.LambdaProperties;
import co.vuckovic.pegasus.model.enumeration.PermissionType;
import co.vuckovic.pegasus.model.enumeration.Role;
import co.vuckovic.pegasus.model.enumeration.TenantStatus;
import co.vuckovic.pegasus.model.enumeration.UserGroup;
import co.vuckovic.pegasus.model.exception.ActionNotAllowedException;
import co.vuckovic.pegasus.model.exception.NotFoundException;
import co.vuckovic.pegasus.model.request.CreateFolderRequest;
import co.vuckovic.pegasus.model.request.DeleteFolderRequest;
import co.vuckovic.pegasus.model.request.FolderGroupPermissionChangeRequest;
import co.vuckovic.pegasus.model.request.MultipartFileDeleteRequest;
import co.vuckovic.pegasus.model.response.ListFolderResponse;
import co.vuckovic.pegasus.repository.BucketEntityRepository;
import co.vuckovic.pegasus.repository.FileEntityRepository;
import co.vuckovic.pegasus.repository.PermissionEntityRepository;
import co.vuckovic.pegasus.repository.StatisticEntityRepository;
import co.vuckovic.pegasus.repository.TenantEntityRepository;
import co.vuckovic.pegasus.repository.UserEntityRepository;
import co.vuckovic.pegasus.repository.entity.BucketEntity;
import co.vuckovic.pegasus.repository.entity.FileEntity;
import co.vuckovic.pegasus.repository.entity.PermissionEntity;
import co.vuckovic.pegasus.repository.entity.StatisticEntity;
import co.vuckovic.pegasus.repository.entity.TenantEntity;
import co.vuckovic.pegasus.repository.entity.UserEntity;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@WebMvcTest(value = FileService.class)
@WebMvcTestConfig
@AutoConfigureMockMvc(addFilters = false)
@EnableWebMvc
class FileServiceTest extends BaseUnitTest {

  @Autowired @InjectMocks private FileService fileService;

  @MockBean private FileEntityRepository fileEntityRepository;

  @MockBean private TenantEntityRepository tenantEntityRepository;

  @MockBean private BucketProperties bucketProperties;

  @MockBean private ModelMapper modelMapper;

  @MockBean private PermissionEntityRepository permissionEntityRepository;

  @MockBean private BucketEntityRepository bucketEntityRepository;

  @MockBean private UserEntityRepository userEntityRepository;

  @MockBean private JwtUtil jwtUtil;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private StatisticEntityRepository statisticEntityRepository;

  @MockBean private LambdaService lambdaService;

  @MockBean private RestTemplate restTemplate;

  @MockBean private LambdaProperties lambdaProperties;

  @Test
  void checkIfPermissionExists_withPermission_shouldReturnTrue()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

    Method method =
        FileService.class.getDeclaredMethod(
            "checkIfPermissionExists", Role.class, PermissionType.class, List.class);
    method.setAccessible(true);

    PermissionEntity permission1 = new PermissionEntity(1, UserGroup.USER, PermissionType.VIEW, 1);
    PermissionEntity permission2 = new PermissionEntity(2, UserGroup.ADMIN, PermissionType.VIEW, 1);
    List<PermissionEntity> permissions = new ArrayList<>();
    permissions.add(permission1);
    permissions.add(permission2);
    Boolean result =
        (Boolean) method.invoke(fileService, Role.USER, PermissionType.VIEW, permissions);

    assertThat(result).isTrue();
  }

  @Test
  void checkIfPermissionExists_withNoPermission_shouldReturnFalse()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

    Method method =
        FileService.class.getDeclaredMethod(
            "checkIfPermissionExists", Role.class, PermissionType.class, List.class);
    method.setAccessible(true);

    PermissionEntity permission1 =
        new PermissionEntity(1, UserGroup.USER, PermissionType.NO_PERMISSIONS, 1);
    PermissionEntity permission2 = new PermissionEntity(2, UserGroup.ADMIN, PermissionType.VIEW, 1);
    List<PermissionEntity> permissions = new ArrayList<>();
    permissions.add(permission1);
    permissions.add(permission2);
    Boolean result =
        (Boolean) method.invoke(fileService, Role.USER, PermissionType.VIEW, permissions);

    assertThat(result).isFalse();
  }

  @Test
  void checkIfPermissionExists_withGreaterPermission_shouldReturnTrue()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

    Method method =
        FileService.class.getDeclaredMethod(
            "checkIfPermissionExists", Role.class, PermissionType.class, List.class);
    method.setAccessible(true);

    PermissionEntity permission1 = new PermissionEntity(1, UserGroup.USER, PermissionType.VIEW, 1);
    PermissionEntity permission2 = new PermissionEntity(2, UserGroup.ADMIN, PermissionType.VIEW, 1);
    List<PermissionEntity> permissions = new ArrayList<>();
    permissions.add(permission1);
    permissions.add(permission2);
    Boolean result =
        (Boolean) method.invoke(fileService, Role.USER, PermissionType.VIEW, permissions);

    assertThat(result).isTrue();
  }

  @Test
  void getIdNamePairsRecursivelyTowardsRoot_withIdNotNull_shouldReturnListOfPairs()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Method method =
        FileService.class.getDeclaredMethod("getIdNamePairsRecursivelyTowardsRoot", Integer.class);
    method.setAccessible(true);

    Mockito.when(fileEntityRepository.getById(ArgumentMatchers.any()))
        .thenReturn(
            new FileEntity(
                4,
                "file1",
                true,
                new Timestamp(System.currentTimeMillis()),
                1.2,
                2,
                1,
                new UserEntity(
                    1,
                    "test",
                    "test",
                    "test@mail.com",
                    "password",
                    Role.OWNER,
                    "testCode1234",
                    false,
                    1),
                "path",
                null,
                null))
        .thenReturn(
            new FileEntity(
                2,
                "file2",
                true,
                new Timestamp(System.currentTimeMillis()),
                1.2,
                null,
                1,
                new UserEntity(
                    1,
                    "test",
                    "test",
                    "test@mail.com",
                    "password",
                    Role.OWNER,
                    "testCode1234",
                    false,
                    1),
                "path",
                null,
                null));

    LinkedList<IdNamePair> pairs = (LinkedList<IdNamePair>) method.invoke(fileService, 4);

    assertThat(pairs.size()).isEqualTo(2);
  }

  @Test
  void listBucket_withValidData_shouldReturnListOfFiles() {
    Authentication authentication = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    JwtUser jwtUser = new JwtUser(1, "mail@mail.com", "password", Role.USER, true);

    SecurityContextHolder.setContext(securityContext);
    when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(jwtUser);

    Mockito.when(jwtUtil.getCurrentUser()).thenReturn(jwtUser);

    UserEntity userEntity =
        new UserEntity(
            1, "firstname", "lastname", "mail@mail.com", "password", Role.USER, null, true, 1);
    Mockito.when(userEntityRepository.findById(jwtUser.getId()))
        .thenReturn(Optional.of(userEntity));
    Mockito.when(bucketEntityRepository.findByTenantId(userEntity.getTenantId()))
        .thenReturn(Optional.of(new BucketEntity(1, 1.2, 1024.0, "test", 1, null)));

    Mockito.when(fileEntityRepository.findFileEntitiesByBucketIdAndParentIdNull(1))
        .thenReturn(
            List.of(
                new FileEntity(
                    4,
                    "file1",
                    true,
                    new Timestamp(System.currentTimeMillis()),
                    1.2,
                    1,
                    1,
                    new UserEntity(
                        1,
                        "test",
                        "test",
                        "test@mail.com",
                        "password",
                        Role.OWNER,
                        "testCode1234",
                        false,
                        1),
                    "path",
                    null,
                    List.of(new PermissionEntity(1, UserGroup.USER, PermissionType.VIEW, 4))),
                new FileEntity(
                    5,
                    "file1",
                    true,
                    new Timestamp(System.currentTimeMillis()),
                    1.2,
                    1,
                    1,
                    new UserEntity(
                        1,
                        "test",
                        "test",
                        "test@mail.com",
                        "password",
                        Role.OWNER,
                        "testCode1234",
                        false,
                        1),
                    "path",
                    null,
                    List.of(new PermissionEntity(2, UserGroup.USER, PermissionType.VIEW, 5)))));

    Mockito.when(modelMapper.map(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(
            new File(
                4,
                "file1",
                true,
                new Timestamp(System.currentTimeMillis()),
                1.2,
                1,
                "test@mail.com",
                null))
        .thenReturn(
            new File(
                3,
                "file2",
                true,
                new Timestamp(System.currentTimeMillis()),
                1.2,
                1,
                "test@mail.com",
                null));

    List<File> files = fileService.listBucket();
    assertThat(files).isNotNull();
    assertThat(files.size()).isEqualTo(2);
  }

  @Test
  void listFolder_withValidData_shouldReturnListOfFiles() throws Exception {
    Authentication authentication = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    JwtUser jwtUser = new JwtUser(1, "mail@mail.com", "password", Role.USER, true);

    SecurityContextHolder.setContext(securityContext);
    when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(jwtUser);

    Mockito.when(jwtUtil.getCurrentUser()).thenReturn(jwtUser);
    Mockito.when(userEntityRepository.getById(ArgumentMatchers.any()))
        .thenReturn(
            new UserEntity(
                1,
                "test",
                "test",
                "test@mail.com",
                "password",
                Role.OWNER,
                "testCode1234",
                false,
                1));

    Mockito.when(fileEntityRepository.findFileEntityByIdAndIsFolder(1, true))
        .thenReturn(
            Optional.of(
                new FileEntity(
                    1,
                    "file1",
                    true,
                    new Timestamp(System.currentTimeMillis()),
                    1.2,
                    null,
                    1,
                    new UserEntity(
                        1,
                        "test",
                        "test",
                        "test@mail.com",
                        "password",
                        Role.OWNER,
                        "testCode1234",
                        false,
                        1),
                    "path",
                    List.of(
                        new FileEntity(
                            4,
                            "file1",
                            true,
                            new Timestamp(System.currentTimeMillis()),
                            1.2,
                            1,
                            1,
                            new UserEntity(
                                1,
                                "test",
                                "test",
                                "test@mail.com",
                                "password",
                                Role.OWNER,
                                "testCode1234",
                                false,
                                1),
                            "path",
                            null,
                            List.of(
                                new PermissionEntity(1, UserGroup.USER, PermissionType.VIEW, 4))),
                        new FileEntity(
                            5,
                            "file1",
                            true,
                            new Timestamp(System.currentTimeMillis()),
                            1.2,
                            1,
                            1,
                            new UserEntity(
                                1,
                                "test",
                                "test",
                                "test@mail.com",
                                "password",
                                Role.OWNER,
                                "testCode1234",
                                false,
                                1),
                            "path",
                            null,
                            List.of(
                                new PermissionEntity(2, UserGroup.USER, PermissionType.VIEW, 5)))),
                    List.of(new PermissionEntity(1, UserGroup.USER, PermissionType.VIEW, 4)))));
    Mockito.when(modelMapper.map(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(
            new File(
                4,
                "file1",
                true,
                new Timestamp(System.currentTimeMillis()),
                1.2,
                1,
                "test@mail.com",
                null))
        .thenReturn(
            new File(
                5,
                "file2",
                true,
                new Timestamp(System.currentTimeMillis()),
                1.2,
                1,
                "test@mail.com",
                null));

    Mockito.when(bucketEntityRepository.getById(ArgumentMatchers.any()))
        .thenReturn(new BucketEntity(1, 400.0, 1024.0, "testBucket", 1, null));

    ListFolderResponse response = fileService.listFolder(1);
    assertThat(response).isNotNull();
    assertThat(response.getChildren().size()).isEqualTo(2);
  }

  @Test
  void
      changeFolderGroupPermission_withValidData_shouldChangeFolderGroupPermissionAndReturnStatus200() {
    Authentication authentication = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    JwtUser jwtUser = new JwtUser(1, "mail@mail.com", "password", Role.USER, true);

    SecurityContextHolder.setContext(securityContext);
    when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(jwtUser);
    Mockito.when(jwtUtil.getCurrentUser()).thenReturn(jwtUser);

    FolderGroupPermissionChangeRequest folderGroupPermissionChangeRequest =
        new FolderGroupPermissionChangeRequest(UserGroup.USER, PermissionType.EDIT);

    PermissionEntity permissionEntity =
        PermissionEntity.builder()
            .id(1)
            .userGroup(UserGroup.USER)
            .permissionType(PermissionType.NO_PERMISSIONS)
            .fileId(1)
            .build();
    Mockito.when(
            permissionEntityRepository.findByFileIdAndUserGroup(
                1, folderGroupPermissionChangeRequest.getUserGroup()))
        .thenReturn(Optional.of(permissionEntity));

    Integer fileId = 1;
    Integer userId = 1;
    Integer bucketId = 1;
    Integer tenantId = 1;

    UserEntity userEntity =
        new UserEntity(
            1, "test", "test", "test@mail.com", "password", Role.OWNER, "testCode1234", false, 1);

    TenantEntity tenantEntity =
        TenantEntity.builder()
            .id(1)
            .company("LukicInvest")
            .status(TenantStatus.VERIFIED)
            .subdomain("lukicinvest.pegasus.com")
            .timestamp(new Timestamp(System.currentTimeMillis()))
            .users(List.of(userEntity))
            .invitations(null)
            .build();

    BucketEntity bucketEntity =
        new BucketEntity(1, 1.2, 1024.0, "test", tenantEntity.getId(), null);

    FileEntity fileEntity = FileEntity.builder().id(fileId).owner(userEntity).build();
    Mockito.when(fileEntityRepository.findById(fileId)).thenReturn(Optional.of(fileEntity));
    Mockito.when(userEntityRepository.findById(userId)).thenReturn(Optional.of(userEntity));
    Mockito.when(tenantEntityRepository.findById(tenantId)).thenReturn(Optional.of(tenantEntity));
    Mockito.when(bucketEntityRepository.findByTenantId((bucketId)))
        .thenReturn(Optional.of(bucketEntity));

    fileService.changeFolderGroupPermission(1, folderGroupPermissionChangeRequest);

    ArgumentCaptor<PermissionEntity> permissionCaptor =
        ArgumentCaptor.forClass(PermissionEntity.class);
    verify(permissionEntityRepository, times(1)).save(permissionCaptor.capture());

    assertThat(permissionEntity.getPermissionType()).isEqualTo(PermissionType.EDIT);
  }

  @Test
  void deleteFolder_withNoPermission_shouldThrowActionNotAllowedException() {
    Authentication authentication = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    JwtUser jwtUser = new JwtUser(4, "mail@mail.com", "password", Role.USER, true);

    SecurityContextHolder.setContext(securityContext);
    when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(jwtUser);

    Mockito.when(jwtUtil.getCurrentUser()).thenReturn(jwtUser);
    Mockito.when(userEntityRepository.getById(ArgumentMatchers.any()))
        .thenReturn(
            new UserEntity(
                1,
                "test",
                "test",
                "test@mail.com",
                "password",
                Role.OWNER,
                "testCode1234",
                false,
                1));

    Mockito.when(
            fileEntityRepository.findFileEntityByIdAndIsFolder(
                ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(
            Optional.of(
                new FileEntity(
                    4,
                    "file1",
                    true,
                    new Timestamp(System.currentTimeMillis()),
                    1.2,
                    2,
                    1,
                    new UserEntity(
                        1,
                        "test",
                        "test",
                        "test@mail.com",
                        "password",
                        Role.OWNER,
                        "testCode1234",
                        false,
                        1),
                    "path",
                    null,
                    null)));

    Mockito.when(bucketEntityRepository.getById(ArgumentMatchers.any()))
        .thenReturn(new BucketEntity(1, 400.0, 1024.0, "testBucket", 1, null));

    ArrayList<Integer> list = new ArrayList<>();
    list.add(4);
    assertThatThrownBy(() -> fileService.deleteFolder(new DeleteFolderRequest(list)))
        .isInstanceOf(ActionNotAllowedException.class);
  }

  @Test
  void deleteFolder_withFileAndNotFolder_shouldThrowActionNotAllowedException()
      throws NoSuchMethodException {
    Authentication authentication = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    JwtUser jwtUser = new JwtUser(4, "mail@mail.com", "password", Role.USER, true);

    SecurityContextHolder.setContext(securityContext);
    when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(jwtUser);

    Mockito.when(jwtUtil.getCurrentUser()).thenReturn(jwtUser);
    Mockito.when(userEntityRepository.getById(ArgumentMatchers.any()))
        .thenReturn(
            new UserEntity(
                1,
                "test",
                "test",
                "test@mail.com",
                "password",
                Role.OWNER,
                "testCode1234",
                false,
                1));

    Mockito.when(
            fileEntityRepository.findFileEntityByIdAndIsFolder(
                ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(
            Optional.of(
                new FileEntity(
                    4,
                    "file1",
                    false,
                    new Timestamp(System.currentTimeMillis()),
                    1.2,
                    2,
                    1,
                    new UserEntity(
                        1,
                        "test",
                        "test",
                        "test@mail.com",
                        "password",
                        Role.OWNER,
                        "testCode1234",
                        false,
                        1),
                    "path",
                    null,
                    null)));

    Mockito.when(bucketEntityRepository.getById(ArgumentMatchers.any()))
        .thenReturn(new BucketEntity(1, 400.0, 1024.0, "testBucket", 1, null));

    ArrayList<Integer> list = new ArrayList<>();
    list.add(4);
    assertThatThrownBy(() -> fileService.deleteFolder(new DeleteFolderRequest(list)))
        .isInstanceOf(ActionNotAllowedException.class);
  }

  @Test
  void deleteFolder_withValidData_shouldDeleteFolderFromFileSystem() {
    Authentication authentication = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    JwtUser jwtUser = new JwtUser(4, "mail@mail.com", "password", Role.OWNER, true);

    SecurityContextHolder.setContext(securityContext);
    when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(jwtUser);

    Mockito.when(jwtUtil.getCurrentUser()).thenReturn(jwtUser);
    Mockito.when(userEntityRepository.getById(ArgumentMatchers.any()))
        .thenReturn(
            new UserEntity(
                1,
                "test",
                "test",
                "test@mail.com",
                "password",
                Role.OWNER,
                "testCode1234",
                false,
                1));

    doNothing().when(fileEntityRepository).delete(ArgumentMatchers.any());

    Mockito.when(
            fileEntityRepository.findFileEntityByIdAndIsFolder(
                ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(
            Optional.of(
                new FileEntity(
                    4,
                    "test",
                    true,
                    new Timestamp(System.currentTimeMillis()),
                    1.2,
                    2,
                    1,
                    new UserEntity(
                        4,
                        "test",
                        "test",
                        "test@mail.com",
                        "password",
                        Role.OWNER,
                        "testCode1234",
                        false,
                        1),
                    "C:/root/test",
                    List.of(
                        new FileEntity(
                            5,
                            "sub",
                            true,
                            new Timestamp(System.currentTimeMillis()),
                            50.0,
                            1,
                            1,
                            new UserEntity(
                                1,
                                "test",
                                "test",
                                "test@mail.com",
                                "password",
                                Role.OWNER,
                                "testCode1234",
                                false,
                                1),
                            "C:/root/test/sub",
                            new ArrayList<>(),
                            List.of(
                                new PermissionEntity(1, UserGroup.USER, PermissionType.EDIT, 4)))),
                    null)));
    BucketEntity bucketEntity = new BucketEntity(1, 400.0, 1024.0, "testBucket", 1, null);
    Mockito.when(bucketEntityRepository.getById(1)).thenReturn(bucketEntity);

    java.io.File file = new java.io.File("C:/root/test");
    java.io.File child = new java.io.File("C:/root/test/sub");
    file.mkdirs();
    child.mkdirs();
    ArrayList<Integer> list = new ArrayList<>();
    list.add(4);
    fileService.deleteFolder(new DeleteFolderRequest(list));
    assertFalse(file.exists());
    assertFalse(child.exists());
  }

  @Test
  void renameFile_withValidData_shouldRenameFile() throws IOException, JsonPatchException {
    java.io.File parent = new java.io.File("C:/root/test2/folder11");
    FileUtils.deleteDirectory(parent);
    java.io.File file = new java.io.File(parent + java.io.File.separator + "file2.txt");
    java.io.File expectedFile = new java.io.File("C:/root/test2/folder11/noviTest.txt");

    Authentication authentication = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    JwtUser jwtUser = new JwtUser(4, "mail@mail.com", "password", Role.USER, true);

    SecurityContextHolder.setContext(securityContext);
    when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(jwtUser);

    Mockito.when(jwtUtil.getCurrentUser()).thenReturn(jwtUser);
    Mockito.when(userEntityRepository.getById(ArgumentMatchers.any()))
        .thenReturn(
            new UserEntity(
                1,
                "test",
                "test",
                "test@mail.com",
                "password",
                Role.OWNER,
                "testCode1234",
                false,
                1));
    Mockito.when(
            fileEntityRepository.findFileEntityByIdAndIsFolder(
                ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(
            Optional.of(
                new FileEntity(
                    4,
                    "file2.txt",
                    false,
                    new Timestamp(System.currentTimeMillis()),
                    1.2,
                    5,
                    1,
                    new UserEntity(
                        4,
                        "test",
                        "test",
                        "test@mail.com",
                        "password",
                        Role.OWNER,
                        "testCode1234",
                        false,
                        1),
                    "C:/root/test2/folder11/file2.txt",
                    null,
                    null)));

    Mockito.when(fileEntityRepository.getById(ArgumentMatchers.any()))
        .thenReturn(
            new FileEntity(
                5,
                "folder11",
                true,
                new Timestamp(System.currentTimeMillis()),
                1.2,
                1,
                1,
                new UserEntity(
                    1,
                    "test",
                    "test",
                    "test@mail.com",
                    "password",
                    Role.OWNER,
                    "testCode1234",
                    false,
                    1),
                "C:/root/test2/folder11",
                null,
                List.of(new PermissionEntity(2, UserGroup.USER, PermissionType.EDIT, 5))));

    Mockito.when(modelMapper.map(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(
            new File(
                4,
                "noviTest",
                true,
                new Timestamp(System.currentTimeMillis()),
                1.2,
                1,
                "test@mail.com",
                null));

    Mockito.when(bucketEntityRepository.getById(ArgumentMatchers.any()))
        .thenReturn(new BucketEntity(1, 400.0, 1024.0, "testBucket", 1, null));

    parent.mkdirs();
    file.createNewFile();
    String json = " [ {\"op\":\"replace\",\"path\":\"/name\",\"value\":\"noviTest\"} ]";

    JsonNode jsonNode = objectMapper.readTree(json);
    JsonPatch jsonPatch = JsonPatch.fromJson(jsonNode);

    File result = fileService.renameFile(4, jsonPatch);
    ArgumentCaptor<FileEntity> fileCaptor = ArgumentCaptor.forClass(FileEntity.class);
    verify(fileEntityRepository, times(1)).save(fileCaptor.capture());
    assertThat(fileCaptor.getValue().getName()).isEqualTo("noviTest.txt");
    assertThat(fileCaptor.getValue().getPath()).isEqualTo(expectedFile.getPath());
    assertFalse(file.exists());
    assertTrue(expectedFile.exists());
  }

  @Test
  void renameFile_withInvalidData_shouldThrowException() throws IOException {
    Authentication authentication = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    JwtUser jwtUser = new JwtUser(4, "mail@mail.com", "password", Role.USER, true);

    SecurityContextHolder.setContext(securityContext);
    when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(jwtUser);

    Mockito.when(jwtUtil.getCurrentUser()).thenReturn(jwtUser);
    Mockito.when(userEntityRepository.getById(ArgumentMatchers.any()))
        .thenReturn(
            new UserEntity(
                1,
                "test",
                "test",
                "test@mail.com",
                "password",
                Role.OWNER,
                "testCode1234",
                false,
                1));
    Mockito.when(
            fileEntityRepository.findFileEntityByIdAndIsFolder(
                ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(
            Optional.of(
                new FileEntity(
                    4,
                    "file3.txt",
                    false,
                    new Timestamp(System.currentTimeMillis()),
                    1.2,
                    5,
                    1,
                    new UserEntity(
                        4,
                        "test",
                        "test",
                        "test@mail.com",
                        "password",
                        Role.OWNER,
                        "testCode1234",
                        false,
                        1),
                    "C:/root/test/folder12/file3.txt",
                    null,
                    null)));

    Mockito.when(fileEntityRepository.getById(ArgumentMatchers.any()))
        .thenReturn(
            new FileEntity(
                5,
                "folder12",
                true,
                new Timestamp(System.currentTimeMillis()),
                1.2,
                1,
                1,
                new UserEntity(
                    1,
                    "test",
                    "test",
                    "test@mail.com",
                    "password",
                    Role.OWNER,
                    "testCode1234",
                    false,
                    1),
                "C:/root/test/folder12",
                null,
                List.of(new PermissionEntity(2, UserGroup.USER, PermissionType.EDIT, 5))));
    Mockito.when(modelMapper.map(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(
            new File(
                4,
                "file33",
                true,
                new Timestamp(System.currentTimeMillis()),
                1.2,
                1,
                "test@mail.com",
                null));

    Mockito.when(bucketEntityRepository.getById(ArgumentMatchers.any()))
        .thenReturn(new BucketEntity(1, 400.0, 1024.0, "testBucket", 1, null));

    java.io.File parent = new java.io.File("C:/root/test/folder12");
    if (parent.exists()) {
      for (java.io.File f : parent.listFiles()) {
        f.delete();
      }
    }
    parent.mkdirs();
    java.io.File file = new java.io.File(parent + java.io.File.separator + "file3.txt");
    java.io.File fileTwo = new java.io.File(parent + java.io.File.separator + "file33.txt");

    file.createNewFile();
    fileTwo.createNewFile();
    String json = " [ {\"op\":\"replace\",\"path\":\"/name\",\"value\":\"file33\"} ]";

    JsonNode jsonNode = objectMapper.readTree(json);
    JsonPatch jsonPatch = JsonPatch.fromJson(jsonNode);

    assertThatThrownBy(() -> fileService.renameFile(4, jsonPatch))
        .isInstanceOf(ActionNotAllowedException.class);
  }

  @Test
  void createFolder_withValidData_shouldReturnStatusCodeOK()
      throws NotFoundException, IOException, NoSuchMethodException, InvocationTargetException,
          IllegalAccessException {

    FileUtils.deleteDirectory(new java.io.File("C:/root/test/dad/son"));
    FileUtils.deleteDirectory(new java.io.File("C:/root/test/dad"));

    Authentication authentication = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    JwtUser jwtUser = new JwtUser(1, "mail@mail.com", "password", Role.USER, true);

    SecurityContextHolder.setContext(securityContext);
    when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(jwtUser);

    Mockito.when(jwtUtil.getCurrentUser()).thenReturn(jwtUser);
    Mockito.when(userEntityRepository.getById(ArgumentMatchers.any()))
        .thenReturn(
            new UserEntity(
                1,
                "test",
                "test",
                "test@mail.com",
                "password",
                Role.OWNER,
                "testCode1234",
                false,
                1));

    Method method =
        FileService.class.getDeclaredMethod(
            "checkIfPermissionIsAllowed", FileEntity.class, UserGroup.class, PermissionType.class);
    method.setAccessible(true);

    CreateFolderRequest createFolderRequest =
        new CreateFolderRequest("son", 1, 1, PermissionType.EDIT, PermissionType.EDIT);
    TenantEntity tenantEntity =
        TenantEntity.builder()
            .id(1)
            .company("LukicInvest")
            .status(TenantStatus.VERIFIED)
            .subdomain("lukicinvest.pegasus.com")
            .timestamp(new Timestamp(System.currentTimeMillis()))
            .users(
                List.of(
                    new UserEntity(
                        1,
                        "firstname",
                        "lastname",
                        "mail@mail.com",
                        "password",
                        Role.USER,
                        null,
                        true,
                        1)))
            .invitations(null)
            .build();
    Permission permissionUser = new Permission(UserGroup.USER, PermissionType.EDIT);
    Permission permissionAdmin = new Permission(UserGroup.ADMIN, PermissionType.EDIT);
    PermissionEntity permissionEntity =
        PermissionEntity.builder()
            .userGroup(UserGroup.ADMIN)
            .permissionType(PermissionType.EDIT)
            .fileId(1)
            .build();
    PermissionEntity permissionEntityUser =
        PermissionEntity.builder()
            .userGroup(UserGroup.USER)
            .permissionType(PermissionType.EDIT)
            .fileId(1)
            .build();

    FileEntity fileEntity =
        FileEntity.builder()
            .isFolder(true)
            .bucketId(1)
            .children(null)
            .name("dad")
            .path("C:/root/test/dad")
            .id(1)
            .size(1.0)
            .owner(
                new UserEntity(
                    1,
                    "test",
                    "test",
                    "test@mail.com",
                    "password",
                    Role.OWNER,
                    "testCode1234",
                    false,
                    1))
            .lastModified(new Timestamp(System.currentTimeMillis()))
            .parentId(null)
            .permissions(List.of(permissionEntity, permissionEntityUser))
            .build();
    BucketEntity bucketEntity =
        BucketEntity.builder().id(1).name("bucket").capacity(1024.0).size(0.0).tenantId(1).build();

    Mockito.when(userEntityRepository.existsById(1)).thenReturn(true);
    Mockito.when(fileEntityRepository.existsById(1)).thenReturn(true);
    Mockito.when(bucketEntityRepository.findById(1)).thenReturn(Optional.of(bucketEntity));
    Mockito.when(fileEntityRepository.findById(1)).thenReturn(Optional.of(fileEntity));

    Mockito.when(fileEntityRepository.getById(1)).thenReturn(fileEntity);

    FileEntity entity =
        FileEntity.builder()
            .isFolder(true)
            .bucketId(1)
            .children(null)
            .name("son")
            .path("C:/root/test/dad/son")
            .id(2)
            .size(1.0)
            .owner(
                new UserEntity(
                    1,
                    "test",
                    "test",
                    "test@mail.com",
                    "password",
                    Role.OWNER,
                    "testCode1234",
                    false,
                    1))
            .lastModified(new Timestamp(System.currentTimeMillis()))
            .parentId(1)
            .permissions(
                List.of(
                    PermissionEntity.builder()
                        .userGroup(UserGroup.USER)
                        .permissionType(PermissionType.EDIT)
                        .fileId(1)
                        .build()))
            .build();
    Mockito.when(modelMapper.map(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(entity)
        .thenReturn(
            new File(
                2,
                "son",
                true,
                new Timestamp(System.currentTimeMillis()),
                1.2,
                1,
                "test@mail.com",
                List.of(permissionUser, permissionAdmin)));

    Mockito.when(fileEntityRepository.save(Mockito.any(FileEntity.class))).thenReturn(entity);
    Boolean result =
        (Boolean) method.invoke(fileService, fileEntity, UserGroup.USER, PermissionType.VIEW);
    File newFolder = fileService.createFolder(createFolderRequest);

    assertThat(result).isTrue();
    ArgumentCaptor<FileEntity> fileCaptor = ArgumentCaptor.forClass(FileEntity.class);
    verify(fileEntityRepository, times(1)).save(fileCaptor.capture());
    assertThat(fileCaptor.getValue().getName()).isEqualTo("son");
  }

  @Test
  void deleteFile_withValidData_shouldDeleteFileFromFileSystemAndReturnStatus200()
      throws IOException {
    Authentication authentication = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    JwtUser jwtUser = new JwtUser(1, "mail@mail.com", "password", Role.OWNER, true);

    SecurityContextHolder.setContext(securityContext);
    when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(jwtUser);

    Mockito.when(jwtUtil.getCurrentUser()).thenReturn(jwtUser);
    Mockito.when(userEntityRepository.getById(ArgumentMatchers.any()))
        .thenReturn(
            new UserEntity(
                1,
                "test",
                "test",
                "test@mail.com",
                "password",
                Role.OWNER,
                "testCode1234",
                false,
                1));

    doNothing().when(fileEntityRepository).delete(ArgumentMatchers.any());

    FileEntity fileEntity =
        new FileEntity(
            2,
            "file",
            false,
            new Timestamp(System.currentTimeMillis()),
            1.2,
            1,
            1,
            new UserEntity(
                1,
                "test",
                "test",
                "test@mail.com",
                "password",
                Role.OWNER,
                "testCode1234",
                false,
                1),
            "C:/root/file",
            null,
            null);
    Mockito.when(fileEntityRepository.findFileEntityByIdAndIsFolder(2, false))
        .thenReturn(Optional.of(fileEntity));

    Mockito.when(fileEntityRepository.getById(1))
        .thenReturn(
            new FileEntity(
                1,
                "root",
                true,
                new Timestamp(System.currentTimeMillis()),
                1.2,
                null,
                1,
                new UserEntity(
                    1,
                    "test",
                    "test",
                    "test@mail.com",
                    "password",
                    Role.OWNER,
                    "testCode1234",
                    false,
                    1),
                "C:/root",
                null,
                null));

    BucketEntity bucketEntity =
        new BucketEntity(1, 10.2, 15.0, "testBucket", 1, new StatisticEntity(1, 3, 4, 5));
    Mockito.when(bucketEntityRepository.getById(1)).thenReturn(bucketEntity);

    Double prevBucketSize = bucketEntity.getSize();
    java.io.File root = new java.io.File("C:/root");
    root.mkdirs();
    java.io.File file = new java.io.File(root + java.io.File.separator + "file");
    file.createNewFile();
    ArrayList list = new ArrayList();
    list.add(2);
    MultipartFileDeleteRequest multipartFileDeleteRequest = new MultipartFileDeleteRequest(list);
    fileService.deleteFile(multipartFileDeleteRequest);
    assertFalse(file.exists());
  }

  @Test
  void renameFolder_withWalidData_shouldChangeFolderNameAndUpdateChildPathsAndReturnStatus200()
      throws IOException, JsonPatchException {
    Authentication authentication = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    JwtUser jwtUser = new JwtUser(1, "mail@mail.com", "password", Role.OWNER, true);

    SecurityContextHolder.setContext(securityContext);
    when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(jwtUser);

    Mockito.when(jwtUtil.getCurrentUser()).thenReturn(jwtUser);
    Mockito.when(userEntityRepository.getById(ArgumentMatchers.any()))
        .thenReturn(
            new UserEntity(
                1,
                "test",
                "test",
                "test@mail.com",
                "password",
                Role.OWNER,
                "testCode1234",
                false,
                1));

    String json = " [ {\"op\":\"replace\",\"path\":\"/name\",\"value\":\"updateName\"} ]";

    JsonNode jsonNode = objectMapper.readTree(json);

    JsonPatch jsonPatch = JsonPatch.fromJson(jsonNode);

    FileEntity child =
        new FileEntity(
            2,
            "sub",
            false,
            new Timestamp(System.currentTimeMillis()),
            1.2,
            1,
            1,
            new UserEntity(
                1,
                "test",
                "test",
                "test@mail.com",
                "password",
                Role.OWNER,
                "testCode1234",
                false,
                1),
            "C:/root/test/sub",
            null,
            null);
    FileEntity root =
        new FileEntity(
            1,
            "test",
            true,
            new Timestamp(System.currentTimeMillis()),
            1.2,
            null,
            1,
            new UserEntity(
                1,
                "test",
                "test",
                "test@mail.com",
                "password",
                Role.OWNER,
                "testCode1234",
                false,
                1),
            "C:/root/test",
            null,
            null);

    Mockito.when(bucketEntityRepository.getById(ArgumentMatchers.any()))
        .thenReturn(new BucketEntity(1, 400.0, 1024.0, "testBucket", 1, null));

    root.setChildren(Arrays.asList(child));

    Mockito.when(fileEntityRepository.findFileEntityByIdAndIsFolder(1, true))
        .thenReturn(Optional.of(root));

    java.io.File rootDir = new java.io.File("C:\\root\\test");
    java.io.File childFile = new java.io.File("C:\\root\\test\\sub");
    rootDir.mkdirs();
    childFile.mkdirs();

    java.io.File expectedRootDir = new java.io.File("C:\\root\\updateName");
    java.io.File expectedChildFile = new java.io.File("C:\\root\\updateName\\sub");

    if (expectedRootDir.exists()) {
      FileUtils.deleteDirectory(expectedRootDir);
    }

    Mockito.when(modelMapper.map(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(
            new File(
                2,
                "sub",
                false,
                new Timestamp(System.currentTimeMillis()),
                1.2,
                1,
                "test@mail.com",
                null));

    fileService.renameFolder(1, jsonPatch);

    ArgumentCaptor<FileEntity> fileCaptor = ArgumentCaptor.forClass(FileEntity.class);
    verify(fileEntityRepository, times(2)).save(fileCaptor.capture());

    List<FileEntity> capturedFiles = fileCaptor.getAllValues();

    assertThat(capturedFiles.get(1).getName()).isEqualTo("updateName");
    assertThat(Paths.get(capturedFiles.get(1).getPath()))
        .isEqualTo(Paths.get(expectedRootDir.getPath()));
    assertFalse(rootDir.exists());
    assertTrue(expectedRootDir.exists());

    assertThat(Paths.get(capturedFiles.get(0).getPath()))
        .isEqualTo(Paths.get(expectedChildFile.getPath()));
    assertFalse(childFile.exists());
    assertTrue(expectedChildFile.exists());
  }
}
