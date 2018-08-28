package com.mohamed.mario.worker.customClasses;

/**
 * Created by Mohamed on 8/28/2018.
 *
 * MCH -> is abbreviation of Multiple Classes Holder
 * var -> is abbreviation of variable
 */
public class MCHRunnableAndBoolean {

    private Runnable runnable;
    private boolean varBoolean;

    public MCHRunnableAndBoolean(Runnable runnable, boolean varBoolean) {
        this.runnable = runnable;
        this.varBoolean = varBoolean;
    }

    public Runnable getRunnable() {
        return runnable;
    }

    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    public boolean isVarBoolean() {
        return varBoolean;
    }

    public void setVarBoolean(boolean varBoolean) {
        this.varBoolean = varBoolean;
    }
}
