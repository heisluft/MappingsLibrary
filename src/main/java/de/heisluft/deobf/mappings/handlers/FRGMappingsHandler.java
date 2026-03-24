package de.heisluft.deobf.mappings.handlers;

import de.heisluft.deobf.mappings.Mappings;
import de.heisluft.deobf.mappings.MappingsBuilder;
import de.heisluft.deobf.mappings.MappingsHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * A mappings handler that is able to read and write FRG(2) mappings.
 * These contain the following entries:
 * CL: Class Mappings
 * FD: Field Mappings
 * MD: Method Mappings
 * FRG2 only:
 * DF: Field Mappings with descriptors
 */
public final class FRGMappingsHandler implements MappingsHandler {

  private static final int FRG_MAPPING_TYPE_INDEX = 0;
  private static final int FRG_ENTITY_CLASS_NAME_INDEX = 1;
  private static final int FRG_MAPPED_CLASS_NAME_INDEX = 2;
  private static final int FRG_DESC_NAME_INDEX = 2;
  private static final int FRG_MAPPED_FIELD_NAME_INDEX = 3;
  private static final int FRG_DESC_INDEX = 3;
  private static final int FRG_DESC_MAPPED_NAME_INDEX = 4;

  private static final int FRG_CLASS_MAPPING_LEN = 3;
  private static final int FRG_FIELD_MAPPING_LEN = 4;
  private static final int FRG_MIN_DESC_MAPPING_LEN = 5;

  @Override
  public Mappings parseMappings(Path input) throws IOException {
    MappingsBuilder builder = new MappingsBuilder();
    List<String> lines = Files.readAllLines(input);
    for(String line : lines) {
      String[] split = line.split(" ");
      String mappingType = split[FRG_MAPPING_TYPE_INDEX];
      switch(mappingType) {
        case "MD:":
          if(split.length < FRG_MIN_DESC_MAPPING_LEN) throw new IllegalArgumentException(
              "Not enough arguments supplied. (" + line + "), expected at least 4 got" + (split.length - 1)
          );
          String clsName = split[FRG_ENTITY_CLASS_NAME_INDEX];
          String obfName = split[FRG_DESC_NAME_INDEX];
          String obfDesc = split[FRG_DESC_INDEX];
          String rName = split[FRG_DESC_MAPPED_NAME_INDEX];
          builder.addMethodMapping(clsName, obfName, obfDesc, rName);
          if(split.length > FRG_MIN_DESC_MAPPING_LEN) builder.addExceptions(clsName, obfName, obfDesc,
              Arrays.asList(split).subList(FRG_MIN_DESC_MAPPING_LEN, split.length));
          break;
        case "FD:":
          if(split.length != FRG_FIELD_MAPPING_LEN) throw new IllegalArgumentException(
              "Illegal amount of Arguments supplied. (" + line + "), expected 3 got" + (split.length - 1)
          );
          builder.addFieldMapping(split[FRG_ENTITY_CLASS_NAME_INDEX], split[FRG_DESC_NAME_INDEX],
              split[FRG_MAPPED_FIELD_NAME_INDEX]);
          break;
        case "CL:":
          if(split.length != FRG_CLASS_MAPPING_LEN) throw new IllegalArgumentException(
              "Illegal amount of Arguments supplied. (" + line + "), expected 2 got" + (split.length - 1)
          );
          builder.addClassMapping(split[FRG_ENTITY_CLASS_NAME_INDEX],
              split[FRG_MAPPED_CLASS_NAME_INDEX]);
          break;
        default:
          System.out.print("Not operating on line '" + line + "'!");
          break;
      }
    }
    return builder.build();
  }

  @Override
  public String fileExt() {
    return "frg";
  }

  @Override
  public boolean supportsExceptionData() {
    return true;
  }

  @Override
  public void writeMappings(Mappings mappings, Path to) throws IOException {
    List<String> lines = new ArrayList<>();
    mappings.forAllClasses((k, v) -> lines.add("CL: " + k + " " + v));
    mappings.forAllFields((clsName, obfName, obfDesc, deobfName) ->
      lines.add("FD: " + clsName + " " + obfName + " " + deobfName)
    );
    mappings.forAllMethods((clsName, obfName, obfDesc, deobfName) -> {
      StringBuilder line = new StringBuilder("MD: " + clsName + " " + obfName + " " + obfDesc + " " + deobfName);
      mappings.getExceptions(clsName, obfName, obfDesc).stream().sorted().forEach(s -> line.append(" ").append(s));
      lines.add(line.toString());
    });
    lines.sort(Comparator.naturalOrder());
    Files.write(to, lines);
  }
}
