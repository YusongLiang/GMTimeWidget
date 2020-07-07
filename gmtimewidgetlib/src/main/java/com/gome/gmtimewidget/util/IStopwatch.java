package com.gome.gmtimewidget.util;

/**
 * @author Felix.Liang
 */
public interface IStopwatch {

    void setElapsedTime(long millis);

    void start();

    void pause();

    void resume();

    void reset();

    void record();

    long getStopwatchTime();

    interface StopwatchListener {

        void onRecord(long recordTime);

        void onUpdate(long stopwatchTime);
    }

    void setStopwatchWatcher(StopwatchListener listener);
}
