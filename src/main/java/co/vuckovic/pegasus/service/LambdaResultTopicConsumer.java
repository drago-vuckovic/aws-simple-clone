package co.vuckovic.pegasus.service;

import co.vuckovic.pegasus.model.dto.LambdaResultJobData;
import co.vuckovic.pegasus.repository.BucketEntityRepository;
import co.vuckovic.pegasus.repository.FileEntityRepository;
import co.vuckovic.pegasus.repository.UserEntityRepository;
import co.vuckovic.pegasus.repository.entity.BucketEntity;
import co.vuckovic.pegasus.repository.entity.FileEntity;
import co.vuckovic.pegasus.repository.entity.UserEntity;
import co.vuckovic.pegasus.model.exception.ConflictException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;

@EnableKafka
@Slf4j
@Configuration
@Service
@RequiredArgsConstructor
public class LambdaResultTopicConsumer {

  private final FileEntityRepository fileEntityRepository;
  private final BucketEntityRepository bucketEntityRepository;

  private final UserEntityRepository userEntityRepository;

  @Bean
  public NewTopic lambdaResultTopic() {
    return TopicBuilder.name("lambda-result-topic").build();
  }

  @KafkaListener(topics = "lambda-result-topic", groupId = "lambda-result")
  public void getMessages(LambdaResultJobData lambdaResultJobData) {
    try {

      Path filePath = Path.of(lambdaResultJobData.getFilePath());
      FileEntity parentDir =
          fileEntityRepository
              .findByPath(filePath.getParent().toString())
              .orElseThrow(() -> new NotFoundException("Directory not found."));

      BucketEntity bucketEntity =
          bucketEntityRepository
              .findById(parentDir.getBucketId())
              .orElseThrow(
                  () ->
                      new co.vuckovic.pegasus.model.exception.NotFoundException(
                          "Parent folder not found"));

      UserEntity userEntity =
          userEntityRepository
              .findByEmail(lambdaResultJobData.getOwnerEmail())
              .orElseThrow(() -> new NotFoundException("User not found!"));

      double fileSize = lambdaResultJobData.getFileBytes().length / 1024.0;
      if (bucketEntity.getCapacity() != null && bucketEntity.getCapacity() <= bucketEntity.getSize() + fileSize) {
          throw new ConflictException("Limit size exceeded");
      }

      if (filePath.toFile().exists()) {
        fileEntityRepository.deleteByPath(filePath.toString());
      }

      FileEntity fileEntity =
          FileEntity.builder()
              .id(0)
              .name(filePath.toFile().getName())
              .isFolder(false)
              .size(fileSize)
              .lastModified(Timestamp.from(Instant.now()))
              .parentId(parentDir.getId())
              .bucketId(parentDir.getBucketId())
              .owner(userEntity)
              .path(filePath.toString())
              .children(null)
              .permissions(null)
              .build();
      Files.write(filePath, lambdaResultJobData.getFileBytes());
      fileEntityRepository.save(fileEntity);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
