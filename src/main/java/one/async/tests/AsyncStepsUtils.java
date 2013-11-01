package one.async.tests;

import java.util.Arrays;
import java.util.List;

public class AsyncStepsUtils {

	public static interface StepCallback {
		public void onStepCompleted();

		public void onFailure(Throwable t);
	}

	public static interface TestStep {

		/**
		 * Every test <b>must</b> call either callback.onStepCompleted or
		 * callback.onFailure.
		 * 
		 * @param data
		 * @param callback
		 */
		public void process(AsyncTest test, StepCallback callback);
	};

	public static interface TestMonitor {

		public void onFailure(Throwable t);

		public void onLastStepCompleted();
	}

	/**
	 * Process a number of asynchronous steps with various guards and
	 * boilerplate around them
	 * 
	 * @param data
	 * @param testFactory
	 * @param steps
	 */
	public static void processSteps(final AsyncTest test,
			final TestMonitor monitor, final TestStep... steps) {
		if (steps.length == 0) {
			return;
		}

		executeSteps(Arrays.asList(steps), 0, test, new ExecuteStepsCallback() {

			@Override
			public void onCompleted() {
				monitor.onLastStepCompleted();
				// ce.finishTest();
			}

			@Override
			public void onFailure(final Throwable t) {
				monitor.onFailure(t);
				// ce.failTest(t);
			}
		});

		test.delayTestFinish(15000);

	}

	private interface ExecuteStepsCallback {
		public void onCompleted();

		public void onFailure(Throwable t);
	}

	private static <StepData> void executeSteps(final List<TestStep> steps,
			final int index, final AsyncTest test,
			final ExecuteStepsCallback callback) {
		try {

			if (index >= steps.size() - 1) {
				callback.onCompleted();
			}

			final TestStep step = steps.get(index);
			// final AsyncTest ce = ceFactory.newAsyncTest();

			test.runLater(new Runnable() {

				@Override
				public void run() {
					try {
						step.process(test, new StepCallback() {

							@Override
							public void onStepCompleted() {
								test.runLater(new Runnable() {

									@Override
									public void run() {

										executeSteps(steps, index + 1, test,
												callback);

									}

								});

							}

							@Override
							public void onFailure(final Throwable t) {
								test.failTest(t);
							}
						});

					} catch (final Throwable t) {
						test.failTest(t);
					}
				}

			});

		} catch (final Throwable t) {
			callback.onFailure(t);
		}
	}
}
