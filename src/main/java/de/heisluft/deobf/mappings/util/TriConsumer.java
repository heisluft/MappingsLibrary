package de.heisluft.deobf.mappings.util;

/**
 * A consumer of three independent arguments.
 *
 * @param <T1> The first arg type
 * @param <T2> The second arg type
 * @param <T3> The third arg type
 *
 * @deprecated The Methods associated with this interface are deprecated for removal. Once they are
 * removed, this interface will also be removed
 */
@FunctionalInterface
@Deprecated
public interface TriConsumer<T1, T2, T3> {
  /**
   * Applies this function to the given arguments.
   *
   * @param t1 the first argument
   * @param t2 the second argument
   * @param t3 the third argument
   */
  void accept(T1 t1, T2 t2, T3 t3);
}
