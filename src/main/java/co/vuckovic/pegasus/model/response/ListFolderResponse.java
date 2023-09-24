package co.vuckovic.pegasus.model.response;

import co.vuckovic.pegasus.model.dto.File;
import co.vuckovic.pegasus.model.dto.IdNamePair;

import java.util.LinkedList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListFolderResponse {

  List<File> children;
  LinkedList<IdNamePair> pairs;
}
