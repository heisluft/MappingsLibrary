package de.heisluft.deobf.mappings.handlers;

import de.heisluft.deobf.mappings.Mappings;
import de.heisluft.deobf.mappings.MappingsBuilder;
import de.heisluft.deobf.mappings.MappingsHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * RGS Files are mappings for RetroGuard.
 * They have way more options than just mappings, providing accessModifiers and such things.
 * 4 Directives are relevant to us: .class_map, .field_map, .method_map and .class.
 * While the first three are self-explanatory
 * the .class allows package relocations and access modifiers.
 * We only parse in the former.
 *
 */
public final class RGSMappingsHandler implements MappingsHandler {

  private static final int RG_COMMAND_TYPE_INDEX = 0;
  private static final int RG_OBF_NAME_INDEX = 1;
  private static final int RG_DEOBF_NAME_INDEX = 2;
  private static final int RG_METHOD_DESC_INDEX = 2;
  private static final int RG_METHOD_DEOBF_NAME_INDEX = 3;

  private static final int RG_CLASS_LEN = 2;
  private static final int RG_CLASS_MAPPING_LEN = 3;
  private static final int RG_FIELD_MAPPING_LEN = 3;
  private static final int RG_METHOD_MAPPING_LEN = 4;

  @Override
  public Collection<String> fileExts() {
    return Collections.singleton("rgs");
  }

  @Override
  public Mappings parseMappings(Path path) throws IOException {
    MappingsBuilder mappings = new MappingsBuilder();
    List<String> lines = Files.readAllLines(path);
    List<String> globs = new ArrayList<>();
    for(String line : lines) {
      if(line.startsWith("#") || line.isEmpty()) continue;
      String[] words = line.split(" ");
      if(words.length < RG_CLASS_LEN)
        throw new IllegalArgumentException("Directive given with no arguments! (line '" + line + "')");
      switch(words[RG_COMMAND_TYPE_INDEX]) {
        case ".class":
          if(words.length > RG_CLASS_LEN) break;
          globs.add(words[RG_OBF_NAME_INDEX]);
          break;
        case ".class_map":
          if(words.length < RG_CLASS_MAPPING_LEN)
            throw new IllegalArgumentException(argMismatch(line, RG_CLASS_MAPPING_LEN - 1, words.length - 1));
          mappings.addClassMapping(words[RG_OBF_NAME_INDEX], words[RG_DEOBF_NAME_INDEX]);
          break;
        case ".field_map":
          if(words.length < RG_FIELD_MAPPING_LEN)
            throw new IllegalArgumentException(argMismatch(line, RG_FIELD_MAPPING_LEN - 1, words.length - 1));
          String[] fd = splitAt(words[RG_OBF_NAME_INDEX], words[RG_OBF_NAME_INDEX].lastIndexOf('/'));
          mappings.addFieldMapping(fd[0], fd[1], words[RG_DEOBF_NAME_INDEX]);
          break;
        case ".method_map":
          if(words.length < RG_METHOD_MAPPING_LEN)
            throw new IllegalArgumentException(argMismatch(line, RG_METHOD_MAPPING_LEN - 1, words.length - 1));
          String[] md = splitAt(words[RG_OBF_NAME_INDEX], words[RG_OBF_NAME_INDEX].lastIndexOf('/'));
          mappings.addMethodMapping(md[0], md[1], words[RG_METHOD_DESC_INDEX], words[RG_METHOD_DEOBF_NAME_INDEX]);
          break;
        default: //explicit continue
      }
    }
    for(int i = 0; i < globs.size(); i++) {
      String regex = "^" + globs.get(i).substring(0, globs.get(i).lastIndexOf('*')) + "[^\\/]+$";
      String newPackage = globs.get(++i).replace("**", "");
      mappings.addPackageRelocation(regex, newPackage);
    }
    return mappings.build();
  }

  private String argMismatch(String line, int expected, int actual) {
    return "Error on line '" + line + "'. Expected at least " + expected + " arguments, got " + actual;
  }

  /**
   * Splits a String at the given index.
   *
   * @param toSplit
   *     the String to be split
   * @param index
   *     the index on which to split on
   *
   * @return the pair of split halves
   */
  private static String[] splitAt(String toSplit, int index) {
    return new String[]{toSplit.substring(0, index), toSplit.substring(index + 1)};
  }
}
