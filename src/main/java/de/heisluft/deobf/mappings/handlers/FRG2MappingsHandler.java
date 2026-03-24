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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A mappings handler that is able to read and write FRG2 mappings.
 * These contain the following entries:
 * Class Mappings
 * Field Mappings with Descriptors
 * Method Mappings
 * Method Exception Data
 */
public final class FRG2MappingsHandler implements MappingsHandler {

  private static final int FRG2_ENTITY_CLASS_NAME_INDEX = 0;
  private static final int FRG2_MAPPED_CLASS_NAME_INDEX = 1;
  private static final int FRG2_MEMBER_NAME_INDEX = 1;
  private static final int FRG2_MEMBER_DESC_INDEX = 2;
  private static final int FRG2_MEMBER_MAPPED_NAME_INDEX = 3;
  private static final int FRG2_METHOD_EXCEPTIONS_BEGIN_INDEX = 4;

  @Override
  public Mappings parseMappings(Path input) throws IOException {
    MappingsBuilder builder = new MappingsBuilder();
    List<String> lines = Files.readAllLines(input);
    for(String line : lines) {
      String[] split = line.split(" ");
      int len = split.length;
      if(len == 2) builder.addClassMapping(split[FRG2_ENTITY_CLASS_NAME_INDEX], split[FRG2_MAPPED_CLASS_NAME_INDEX]);
      else if(len > FRG2_MEMBER_MAPPED_NAME_INDEX) {
        String clsName = split[FRG2_ENTITY_CLASS_NAME_INDEX];
        String obfName = split[FRG2_MEMBER_NAME_INDEX];
        String obfDesc = split[FRG2_MEMBER_DESC_INDEX];
        String rName = split[FRG2_MEMBER_MAPPED_NAME_INDEX];
        if(obfDesc.charAt(0) != '(') {
          builder.addFieldMapping(clsName, obfName, obfDesc, rName);
        } else {
          if(!";".equals(rName)) builder.addMethodMapping(clsName, obfName, obfDesc, rName);
          if(split.length > FRG2_METHOD_EXCEPTIONS_BEGIN_INDEX) {
            builder.addExceptions(
                clsName,
                obfName,
                obfDesc,
                Arrays.asList(split).subList(FRG2_METHOD_EXCEPTIONS_BEGIN_INDEX, split.length)
            );
          }
        }
      } else {
        System.out.print("Not operating on line '" + line + "'!");
      }
    }
    return builder.build();
  }

  @Override
  public boolean supportsFieldDescriptors() {
    return true;
  }

  @Override
  public String fileExt() {
    return "frg2";
  }

  @Override
  public boolean supportsExceptionData() {
    return true;
  }

  @Override
  public void writeMappings(Mappings mappings, Path to) throws IOException {
    List<String> lines = new ArrayList<>();
    mappings.forAllClasses((k, v) -> lines.add(k + " " + v));
    lines.sort(Comparator.naturalOrder());
    List<String> fLines = new ArrayList<>();
    mappings.forAllFields((clsName, obfName, obfDesc, deobfName) ->
      fLines.add(clsName + " " + obfName + " " + obfDesc + " " + deobfName)
    );
    fLines.sort(Comparator.naturalOrder());
    lines.addAll(fLines);
    List<String> mLines = new ArrayList<>();
    Set<String> addedExceptions = new HashSet<>();
    mappings.forAllMethods((clsName, obfName, obfDesc, deobfName) -> {
      StringBuilder line = new StringBuilder(clsName + " " + obfName + " " + obfDesc + " " + deobfName);
      if(mappings.hasExceptionsFor(clsName, obfName, obfDesc)) addedExceptions.add(clsName + obfName + obfDesc);
      mappings.getExceptions(clsName, obfName, obfDesc).stream().sorted().forEach(s -> line.append(" ").append(s));
      mLines.add(line.toString());
    });
    mappings.forAllExceptions((clsName, obfName, obfDesc, data) -> {
      if(addedExceptions.contains(clsName + obfName + obfDesc)) return;
      StringBuilder line = new StringBuilder(clsName + " " + obfName + " " + obfDesc + " ;");
      data.stream().sorted().forEach(s -> line.append(" ").append(s));
      mLines.add(line.toString());
    });
    mLines.sort(Comparator.naturalOrder());
    lines.addAll(mLines);

    Files.write(to, lines);
  }
}
