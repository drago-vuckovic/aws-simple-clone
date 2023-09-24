package co.vuckovic.pegasus.model.exception;

import org.springframework.http.HttpStatus;

public class BucketException extends HttpException{

  public BucketException(Object data) {
    super(HttpStatus.CONFLICT, data);
  }
}
