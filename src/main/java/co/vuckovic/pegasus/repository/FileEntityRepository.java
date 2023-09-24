package co.vuckovic.pegasus.repository;

import co.vuckovic.pegasus.repository.entity.FileEntity;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface FileEntityRepository extends JpaRepository<FileEntity, Integer> {

  Optional<FileEntity> findFileEntityByIdAndIsFolder(Integer id, Boolean isFolder);

  List<FileEntity> findFileEntitiesByBucketIdAndParentIdNull(Integer bucketId);

  List<FileEntity> findFileEntitiesByBucketIdAndIsFolderTrue(Integer bucketId);

  Optional<FileEntity> findByPath(String path);

  @Transactional
  void deleteByPath(String path);
}
