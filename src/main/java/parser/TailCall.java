package parser;

@FunctionalInterface
public interface TailCall {
	TailCall get();
	default boolean terminated() { return false; }
}
