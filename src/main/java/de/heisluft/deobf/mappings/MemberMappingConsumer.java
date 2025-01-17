package de.heisluft.deobf.mappings;

/**
 * A consumer of a class member mapping, either a field or method.
 */
@FunctionalInterface
public interface MemberMappingConsumer {
  /**
   * Applies this function to a given mapping.
   *
   * @param cName the obfuscated class name in binary form
   * @param memberName the member name
   * @param memberDesc the members descriptor
   * @param rName the remapped name
   */
  void accept(String cName, String memberName, String memberDesc, String rName);
}
