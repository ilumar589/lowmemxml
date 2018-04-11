package parser;

public class TailCallTerminate implements TailCall {

	public TailCall get() { return null; }
	public boolean terminated() { return true; }
}
