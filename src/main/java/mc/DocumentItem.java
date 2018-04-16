package mc;

import java.util.Set;

/**
 * In-memory representation of a row/part of an import file (see orders or catalog import).
 *
 */
public interface DocumentItem {

	/**
	 * Retrieves data associated with a given key/column name.
	 * Depending on the format of the file, the key is composed differently,
	 * but it should be always described in a ProjectConfiguration object.
	 *
	 */
	Object resolve(String key);

	/**
	 * Retrieves the keys that were never accessed via resolve.
	 * <p>
	 * Clients that do not support this feature may return empty set.
	 * </p>
	 *
	 * @see #resolve(String)
	 */
	Set<String> getUnusedKeys();
}
