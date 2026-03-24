package de.heisluft.deobf.mappings;

/**
 * A consumer of a class member mapping, either a field or method.
 *
 * @param <T> the type of data received.
 */
@FunctionalInterface
public interface MemberMappingConsumer<T> {
  /**
   * Applies this function to a given mapping.
   *
   * @param cName the obfuscated class name in binary form
   * @param memberName the member name
   * @param memberDesc the members descriptor
   * @param data the returned data
   */
  void accept(String cName, String memberName, String memberDesc, T data);
}
