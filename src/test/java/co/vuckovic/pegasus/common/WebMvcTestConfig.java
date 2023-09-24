package co.vuckovic.pegasus.common;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@Retention(RetentionPolicy.RUNTIME)
@AutoConfigureMockMvc
public @interface WebMvcTestConfig {

}
