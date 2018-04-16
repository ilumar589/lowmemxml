package mc;

public abstract class AbstractDocumentItem implements DocumentItem {

	public Object resolve(String key) {
		if (isQuoted(key)) {
			return extractQuotedValue(key);
		}
		if (isTemplate(key)) {
			return resolveTemplate(key);
		}
		return resolveValue(key);
	}

	protected abstract Object resolveValue(String value);

	private boolean isQuoted(String key) {
		return key.startsWith("'") && key.endsWith("'");
	}

	private String extractQuotedValue(String constant) {
		return constant.substring(1, constant.length() - 1);
	}

	protected boolean isTemplate(String key) {
		return false;
	}

	protected Object resolveTemplate(String key) {
		throw new UnsupportedOperationException();
	}

}