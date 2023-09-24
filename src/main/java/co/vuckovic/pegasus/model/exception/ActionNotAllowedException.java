package co.vuckovic.pegasus.model.exception;

import org.springframework.http.HttpStatus;

public class ActionNotAllowedException extends HttpException{

  public ActionNotAllowedException(Object data) {
    super(HttpStatus.NOT_FOUND, data);
  }

}
