package co.vuckovic.pegasus.common.util;

import lombok.NonNull;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.PostgreSQLContainer;

public class ContainerBaseTest {

  protected static final PostgreBaseContainer POSTGRESQL_CONTAINER;

  static {
    POSTGRESQL_CONTAINER = new PostgreBaseContainer();
    POSTGRESQL_CONTAINER.start();
  }

  public static class ContainerBaseTestInitializer
      implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(@NonNull ConfigurableApplicationContext configurableApplicationContext) {
      TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
          configurableApplicationContext,
          "spring.datasource.url=" + POSTGRESQL_CONTAINER.getJdbcUrl(),
          "spring.datasource.username=" + POSTGRESQL_CONTAINER.getUsername(),
          "spring.datasource.password=" + POSTGRESQL_CONTAINER.getPassword()
      );
    }
  }

  private static class PostgreBaseContainer extends PostgreSQLContainer<PostgreBaseContainer> {

    private static final String POSTGRESQL_IMAGE = "postgres:11.9";

    public PostgreBaseContainer() {
      super(POSTGRESQL_IMAGE);
    }
  }

}
