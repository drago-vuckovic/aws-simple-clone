package co.vuckovic.pegasus.model.response;

import co.vuckovic.pegasus.model.dto.Lambda;

import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class LambdasResponse {

  private List<Lambda> lambdas;
}
