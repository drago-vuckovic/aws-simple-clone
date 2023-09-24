package co.vuckovic.pegasus.model.exception;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@ToString
public class InvalidRoleException extends HttpException {

  public InvalidRoleException() {
    super(HttpStatus.BAD_REQUEST, null);
  }

  public InvalidRoleException(Object data) {
    super(HttpStatus.BAD_REQUEST, data);
  }
}
