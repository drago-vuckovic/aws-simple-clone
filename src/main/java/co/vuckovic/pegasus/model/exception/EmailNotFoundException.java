package co.vuckovic.pegasus.model.exception;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@ToString
public class EmailNotFoundException extends HttpException {

  public EmailNotFoundException() {
    super(HttpStatus.NOT_FOUND, null);
  }

  public EmailNotFoundException(Object data) {
    super(HttpStatus.NOT_FOUND, data);
  }
}
