package org.jenkinsci.plugins.qywechat;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;

public enum ResultCondition {

	ALWAYS("Always") {
		boolean isMet(Result result, Run<?, ?> run, TaskListener listener) {
			return true;
		}
	},
	ABORTED("Aborted") {
		boolean isMet(Result result, Run<?, ?> run, TaskListener listener) {
			return result == Result.ABORTED;
		}
	},
	SUCCESS("Success") {
		boolean isMet(Result result, Run<?, ?> run, TaskListener listener) {
			return result == Result.SUCCESS;
		}
	},
	FAILED_ANY("Failure - Any") {
		boolean isMet(Result result, Run<?, ?> run, TaskListener listener) {
			return result == Result.FAILURE;
		}
	},
	FAILURE_1ST("Failure - 1st") {
		boolean isMet(Result result, Run<?, ?> run, TaskListener listener) {
			return trigger(run, listener, 1);
		}
	},
	FAILURE_2ND("Failure - 2nd") {
		boolean isMet(Result result, Run<?, ?> run, TaskListener listener) {
			return trigger(run, listener, 2);
		}
	},
	FAILURE_3LY("Failure - 3ly") {
		boolean isMet(Result result, Run<?, ?> run, TaskListener listener) {
			return trigger(run, listener, 3);
		}
	},
	FAILURE_STILL("Failure - Still") {
		boolean isMet(Result result, Run<?, ?> run, TaskListener listener) {

			if (result == Result.FAILURE) {
				Run<?, ?> prevRun = getPreviousRun(run, listener);
				if (prevRun != null && prevRun.getResult() == Result.FAILURE) {
					return true;
				}
			}
			return false;
		}
	},
	UNSTABLE("Unstable (Test Failures)") {
		boolean isMet(Result result, Run<?, ?> run, TaskListener listener) {
			return result == Result.UNSTABLE;
		}
	},
	UNSTABLE_1ST("Unstable (Test Failures) - 1st") {
		boolean isMet(Result result, Run<?, ?> run, TaskListener listener) {
			Run<?, ?> previousRun = getPreviousRun(run, listener);
			return previousRun != null ? previousRun.getResult() != Result.UNSTABLE && run.getResult() == Result.UNSTABLE : run.getResult() == Result.UNSTABLE;
		}
	},
	UNSTABLE_STILL("Unstable (Test Failures) - Still") {
		boolean isMet(Result result, Run<?, ?> run, TaskListener listener) {
			if (result == Result.UNSTABLE) {
				Run<?, ?> prevRun = getPreviousRun(run, listener);
				if (prevRun != null && prevRun.getResult() == Result.UNSTABLE) {
					return true;
				}
			}
			return false;
		}
	},
	UNSTABLE_FAILURE_SUCCESS("Unstable (Test Failures)/Failure -> Success") {
		boolean isMet(Result result, Run<?, ?> run, TaskListener listener) {

			if (result == Result.SUCCESS) {
				Run<?, ?> prevBuild = getPreviousRun2(run, listener);
				if (prevBuild != null && (prevBuild.getResult() == Result.UNSTABLE || prevBuild.getResult() == Result.FAILURE)) {
					return true;
				}
			}

			return false;
		}
	},
	FAILED_OR_BETTER("Success, unstable or failed, but not aborted") {
		boolean isMet(Result result, Run<?, ?> build, TaskListener listener) {
			return result.isBetterOrEqualTo(Result.FAILURE);
		}
	},
	UNSTABLE_OR_BETTER("Success or unstable but not failed") {
		boolean isMet(Result result, Run<?, ?> run, TaskListener listener) {
			return result.isBetterOrEqualTo(Result.UNSTABLE);
		}
	},
	UNSTABLE_OR_WORSE("Unstable or Failed but not Success") {
		boolean isMet(Result result, Run<?, ?> run, TaskListener listener) {
			return result.isWorseOrEqualTo(Result.UNSTABLE);
		}
	};

	private ResultCondition(String displayName) {
		this.displayName = displayName;
	}

	private final String displayName;

	public String getDisplayName() {
		return displayName;
	}

	abstract boolean isMet(Result result, Run<?, ?> run, TaskListener listener);

	/**
	 * 
	 * @param run
	 * @param listener
	 * @param count
	 * @return
	 */
	public boolean trigger(Run<?, ?> run, TaskListener listener, int count) {
		for (int i = 0; i < count; i++) {
			if (run == null) {
				return false;
			}
			Result buildResult = run.getResult();
			if (buildResult != Result.FAILURE) {
				return false;
			}
			run = getPreviousRun(run, listener);
		}
		return run == null || run.getResult() == Result.SUCCESS || run.getResult() == Result.UNSTABLE;
	}

	/**
	 * 
	 * @param run
	 * @param listener
	 * @return
	 */
	public static @CheckForNull Run<?, ?> getPreviousRun(@Nonnull Run<?, ?> run, TaskListener listener) {
		Run<?, ?> previousRun = run.getPreviousBuild();
		if (previousRun != null && previousRun.isBuilding()) {
			listener.getLogger().println(run.getDisplayName());
			return null;
		} else {
			return previousRun;
		}
	}

	/**
	 * 
	 * @param run
	 * @param listener
	 * @return
	 */
	public static Run<?, ?> getPreviousRun2(Run<?, ?> run, TaskListener listener) {

		Run<?, ?> prevBuild = getPreviousRun(run, listener);
		// Skip ABORTED builds
		if (prevBuild != null && prevBuild.getResult() == Result.ABORTED) {
			return getPreviousRun(prevBuild, listener);
		}
		return prevBuild;
	}

}
