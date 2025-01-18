package de.heisluft.deobf.mappings.handlers;

import de.heisluft.deobf.mappings.Mappings;
import de.heisluft.deobf.mappings.MappingsBuilder;
import de.heisluft.deobf.mappings.MappingsHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A MappingsHandler capable of reading SRG files.
 */
//TODO: Implement Packages
public final class SRGMappingsHandler implements MappingsHandler {

  private static final int SRG_MAPPING_TYPE_INDEX = 0;
  private static final int SRG_OBFED_INDEX = 1;
  private static final int SRG_DEOBFED_INDEX = 2;
  private static final int SRG_MD_DESCRIPTOR_INDEX = 2;
  private static final int SRG_MD_DEOBFED_INDEX = 3;

  private static final int SRG_CLASS_MAPPING_LEN = 3;
  private static final int SRG_FIELD_MAPPING_LEN = 3;
  private static final int SRG_METHOD_MAPPING_LEN = 5;

  @Override
  public Mappings parseMappings(Path input) throws IOException {
    MappingsBuilder builder = new MappingsBuilder();
    List<String> lines = Files.readAllLines(input);
    int lCounter = 0;
    for (String line : lines) {
      if(!line.contains(" ")) throw parseError(lCounter, "Line does not contain command");
      String[] split = line.split(" ");
      String cmd = split[SRG_MAPPING_TYPE_INDEX];
      switch (cmd) {
        case "CL:":
          if (split.length != SRG_CLASS_MAPPING_LEN)
            throw parseError(lCounter, "Class mappings need 2 arguments, " + (split.length - 1) + " given");
          builder.addClassMapping(split[SRG_OBFED_INDEX], split[SRG_DEOBFED_INDEX]);
          break;
        case "MD:":
          if (split.length != SRG_METHOD_MAPPING_LEN)
            throw parseError(lCounter, "Method mappings need 3 arguments, " + (split.length - 1) + " given");
          String obfName = split[SRG_OBFED_INDEX];
          String deobfName = split[SRG_MD_DEOBFED_INDEX];
          if(!obfName.contains("/") || !deobfName.contains("/"))
            throw parseError(lCounter, "Class member names must contain slash");
          int lastSlash = obfName.lastIndexOf('/');
          builder.addMethodMapping(
              obfName.substring(0, lastSlash),
              obfName.substring(lastSlash + 1),
              split[SRG_MD_DESCRIPTOR_INDEX],
              deobfName.substring(deobfName.lastIndexOf('/') + 1)
          );
          break;
        case "FD:":
          if (split.length != SRG_FIELD_MAPPING_LEN)
            throw parseError(lCounter, "Field mappings need 3 arguments, " + (split.length - 1) + " given");
          obfName = split[SRG_OBFED_INDEX];
          deobfName = split[SRG_DEOBFED_INDEX];
          if(!obfName.contains("/") || !deobfName.contains("/"))
            throw parseError(lCounter, "Class member names must contain slash");
          lastSlash = obfName.lastIndexOf('/');
          builder.addFieldMapping(
              obfName.substring(0, lastSlash),
              obfName.substring(lastSlash + 1),
              deobfName.substring(deobfName.lastIndexOf('/') + 1)
          );
          break;
        case "PK:":
          break;
        default: throw parseError(lCounter, "Unknown entry '" + cmd + "'");
      }
      lCounter++;
    }
    return builder.build();
  }

  private IOException parseError(int lCounter, String message) {
    return new IOException("Error reading line " + lCounter + ": " + message);
  }

  @Override
  public void writeMappings(Mappings mappings, Path output) throws IOException {
    MappingsHandler.super.writeMappings(mappings, output);
  }

  @Override
  public Collection<String> fileExts() {
    return Collections.singleton("srg");
  }
}
