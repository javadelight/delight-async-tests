package one.async.tests.jre;

import delight.concurrency.Concurrency;

import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import one.async.tests.AsyncTest;
import one.async.tests.AsyncTestFactory;
import one.utils.jre.OneUtilsJre;



/**
 * 
 * @author mx
 * @see <a href="http://eyalsch.wordpress.com/2010/07/13/multithreaded-tests/">A
 *      utility for multithreaded unit tests</a>
 */
public class AsyncTestJre extends AsyncTest {

	public static AsyncTest newAsyncTest() {
		return new AsyncTestJre(OneUtilsJre.newJreConcurrency());
	}

	public static AsyncTestFactory newAsyncTestFactory() {
		return new AsyncTestFactory() {

			@Override
			public AsyncTest newAsyncTest() {
				return AsyncTestJre.newAsyncTest();
			}

		};
	}

	/**
	 * 
	 */
	@Override
	public void runLater(final Runnable step) {
		// System.out.println("run later!");
		final ArrayList<Throwable> exceptions = new ArrayList<Throwable>();
		final Thread t = new Thread() {

			@Override
			public void run() {
				try {
					Thread.yield();
					Thread.sleep(50);
					Thread.yield();
					step.run();
				} catch (final Throwable _t) {
					exceptions.add(_t);
					throw new RuntimeException(_t);
				}

			}

		};
		t.start();

		new Verifyer() {

			@Override
			public void join() {
				try {
					t.join();
				} catch (final InterruptedException e) {
					throw new RuntimeException(e);
				}
				for (final Throwable _t : exceptions) {
					throw new RuntimeException(_t);
				}
			}

		};

	}

	@Override
	public void delayTestFinish(final int duration) {
		delayed = true;
		timeout = false;

		newTimer().scheduleOnce(duration, new Runnable() {

			@Override
			public void run() {
				if (delayed) {
					timeout = true;
				}
			}

		});

		// System.out.println("wait" +this);
		while (delayed && !failed) {
			Thread.yield();
			try {
				Thread.sleep(10);
			} catch (final InterruptedException e) {
				throw new RuntimeException(e);
			}
			if (failed) {
				throw new RuntimeException(cause);
			}

			if (timeout) {
				throw new RuntimeException(new TimeoutException(
						"finishTest() not called in time."));
			}
		}

	}

	public AsyncTestJre(final Concurrency con) {
		super(con);
	}

}
