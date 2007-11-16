package magellan.library.utils.logging;

/** 
 * A Log Listener will be informed about logging informations
 */
public interface LogListener {
	public void log(int aLevel, Object aObj, Throwable aThrowable);
}
