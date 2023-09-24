package co.vuckovic.pegasus.model.exception;

import org.springframework.http.HttpStatus;

public class FolderExistsException extends HttpException {

  public FolderExistsException(Object data) {
    super(HttpStatus.CONFLICT, data);
  }
}
