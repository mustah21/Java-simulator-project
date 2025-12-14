package simu.framework;

/**
 * Utility class for trace/debug output with different severity levels.
 * Allows filtering of output based on trace level.
 * 
 * @author Group 8
 * @version 1.0
 */
public class Trace {
	/**
	 * Enumeration of trace levels.
	 * Messages are filtered based on the current trace level setting.
	 */
	public enum Level {
		/** Informational messages */
		INFO,
		/** Warning messages */
		WAR,
		/** Error messages */
		ERR
	}
	
	/** Current trace level threshold */
	private static Level traceLevel;
	
	/**
	 * Sets the trace level threshold.
	 * Only messages at or above this level will be output.
	 * 
	 * @param lvl The trace level to set
	 */
	public static void setTraceLevel(Level lvl){
		traceLevel = lvl;
	}
	
	/**
	 * Outputs a trace message if its level is at or above the current trace level.
	 * 
	 * @param lvl The severity level of the message
	 * @param txt The message text to output
	 */
	public static void out(Level lvl, String txt){
		if (lvl.ordinal() >= traceLevel.ordinal()){
			System.out.println(txt);
		}
	}
}