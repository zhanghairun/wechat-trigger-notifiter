package com.plugins.wechattrigger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;

public enum ResultCondition {

	SUCCESS("Stable") {
		boolean isMet(Result result,Run<?, ?> build, TaskListener listener) {
			return result == Result.SUCCESS;
		}
	},
	UNSTABLE("Unstable") {
		boolean isMet(Result result,Run<?, ?> build, TaskListener listener) {
			return result == Result.UNSTABLE;
		}
	},
	FAILED_OR_BETTER("Stable, unstable or failed, but not aborted") {
		boolean isMet(Result result,Run<?, ?> build, TaskListener listener) {
			return result.isBetterOrEqualTo(Result.FAILURE);
		}
	},
	UNSTABLE_OR_BETTER("Stable or unstable but not failed") {
		boolean isMet(Result result,Run<?, ?> build, TaskListener listener) {
			return result.isBetterOrEqualTo(Result.UNSTABLE);
		}
	},
	UNSTABLE_OR_WORSE("Unstable or Failed but not stable") {
		boolean isMet(Result result,Run<?, ?> build, TaskListener listener) {
			return result.isWorseOrEqualTo(Result.UNSTABLE);
		}
	},
	FAILED("Failed") {
		boolean isMet(Result result,Run<?, ?> build, TaskListener listener) {
			return result == Result.FAILURE;
		}
	},
	FAILURE_1ST("Failure - 1st") {
		boolean isMet(Result result,Run<?, ?> build, TaskListener listener) {
			return trigger(build,listener,1);
		}
	},
	ALWAYS("Complete (always trigger)") {
		boolean isMet(Result result,Run<?, ?> build, TaskListener listener) {
			return true;
		}
	};

	private ResultCondition(String displayName) {
		this.displayName = displayName;
	}

	private final String displayName;

	public String getDisplayName() {
		return displayName;
	}

	abstract boolean isMet(Result result,Run<?, ?> build, TaskListener listener);
	//abstract boolean isMet(Result result);
	//abstract boolean trigger(AbstractBuild<?, ?> build, TaskListener listener);
	
	public boolean trigger(Run<?, ?> build, TaskListener listener, int count) {
        //Run<?,?> run = build;
        //int count = 1 ;//getRequiredFailureCount();
        // Work back through the failed builds.
        for (int i = 0; i < count; i++) {
            if (build == null) {
                // We don't have enough history to have reached the failure count.
                return false;
            }

            Result buildResult = build.getResult();
            if (buildResult != Result.FAILURE) {
                return false;
            }

            build = getPreviousRun(build, listener);
        }

        return build == null || build.getResult() == Result.SUCCESS || build.getResult() == Result.UNSTABLE;
    }
	
	 public static @CheckForNull
	    Run<?, ?> getPreviousRun(@Nonnull Run<?, ?> run, TaskListener listener) {
	        Run<?, ?> previousRun = run.getPreviousBuild();
	        if (previousRun != null && previousRun.isBuilding()) {
	            //listener.getLogger().println(Messages.ExtendedEmailPublisher__is_still_in_progress_ignoring_for_purpo(previousRun.getDisplayName()));
	            return null;
	        } else {
	            return previousRun;
	        }
	    }

}
