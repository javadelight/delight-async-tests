package one.async.tests;

import delight.concurrency.Concurrency;
import delight.concurrency.factories.TimerFactory;

/**
 * Base interface for helper classes to support asynchronous unit tests.
 * 
 * @author mx
 * 
 */
public abstract class AsyncTest {

	protected final Concurrency con;

	protected volatile boolean delayed = false;
	protected volatile boolean failed = false;
	protected volatile Throwable cause = null;
	protected volatile boolean timeout = false;

	public interface TestDef {
		public void run(AsyncTest at);
	}

	/**
	 * Enters the thread into a loop, which is executed for the specified
	 * duration. If the time specified in the duration is passed, an exception
	 * will be thrown. To prevent this {@link #finishTest()} should be called
	 * within the specified time frame.<br />
	 * <br />
	 * {@link #delayTestFinish(int)} should sensibly be called as the last
	 * instruction in a test case.
	 * 
	 * @param duration
	 *            Duration in ms for which the test case waits
	 */
	public abstract void delayTestFinish(int duration);

	public static void newTest(final AsyncTestFactory testFactory,
			final TestDef def) {
		final AsyncTest at = testFactory.newAsyncTest();
		at.runLater(new Runnable() {

			@Override
			public void run() {
				try {
					def.run(at);
				} catch (final Throwable t) {
					at.failTest(t);
				}
			}
		});

		at.delayTestFinish();
	}

	public interface Verifyer {
		public void join();
	}

	/**
	 * Executes the defined runnable with some delay. Asynchronous tests should
	 * sensibly be contained within such a method.
	 * 
	 * @param step
	 */
	public void runLater(final Runnable step) {
		con.newTimer().scheduleOnce(1, step);
	}

	public void finishTest() {
		delayed = false;
	}

	boolean printLog = false;

	public void printLog() {
		printLog = true;
	}

	public void will(final String doWhat) {
		if (printLog) {
			System.out.println("Will '" + doWhat + "'");
		}
	}

	public void has(final String doneWhat) {
		if (printLog) {
			System.out.println("Has '" + doneWhat + "'");
		}
	}

	public void failTest(final Throwable t) {
		cause = t;
		failed = true;

	}

	public void failTest(final String message) {
		failTest(new Exception(message));
	}

	/**
	 * Creates a new timer, which can be executed with a timeout.
	 * 
	 * @param timer
	 * @return
	 */

	public TimerFactory newTimer() {
		return con.newTimer();
	}

	/**
	 * Delays the termination of a test process by a default duration (for
	 * instance 10000 ms). Otherwise works like {@link #delayTestFinish(int)}.
	 */
	public void delayTestFinish() {
		this.delayTestFinish(15000);
	}

	public void printLog(final boolean enabled) {
		if (enabled) {
			printLog();
		} else {
			printLog = false;
		}
	}

	public void assertThat(final boolean test, final String message) {
		if (!test) {
			final String renderedMessage;
			if (message == null) {
				renderedMessage = "Expected <true> but got <false>.";
			} else {
				renderedMessage = message;
			}
			failTest(renderedMessage);
		}
	}

	public void assertTrue(final boolean test) {
		assertThat(test, null);
	}

	public void assertEquals(final Object expected, final Object actual,
			final String message) {

		if (expected == null) {
			if (actual != null) {
				final String renderedMessage = "Expected [" + expected + "] "
						+ "but got [" + actual + "]";
				if (message != null) {
					failTest(message + " " + renderedMessage);
				} else {
					failTest(renderedMessage);
				}

				return;
			} else {
				return;
			}
		}

		if (!expected.equals(actual)) {
			if (message != null) {
				failTest(message + " Expected [" + expected + "] but got ["
						+ actual + "]");
			} else {
				failTest("Expected [" + expected + "] but got [" + actual + "]");
			}
		}
	}

	public void assertEquals(final Object expected, final Object actual) {
		assertEquals(expected, actual, null);
	}

	public AsyncTest(final Concurrency con) {
		super();
		this.con = con;
	}

}
