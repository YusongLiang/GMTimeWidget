package com.gome.gmtimewidget.util;

/**
 * @author Felix.Liang
 */
public interface ITimer {

    void setTotalTime(long time);

    void start();

    void pause();

    void resume();

    void reset();
}
