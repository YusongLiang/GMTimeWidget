package com.gome.gmtimewidget.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.gome.gmtimewidget.util.SizeTransformer;
import com.gome.gmtimewidget.util.SvgPathParser;
import com.gome.gmtimewidget.util.ViewConfig;

import java.text.ParseException;

/**
 * @author Felix.Liang
 */
public abstract class AbsTimeView extends View {

    private static final long UPDATE_DELAY = 30;
    protected float mShadowRadius = dp2px(1);
    protected float mShadowOffset = dp2px(2.5f);
    private boolean mVisible;
    private boolean mRunning;
    private boolean mStarted;
    private boolean mPaused;

    protected Paint mDialPaint;
    protected Paint mSubLinePaint;
    protected float mBoundRadius;
    protected AnimatedCallback mAnimatedCallback;
    private AnimatorSet mShowAnimSet;
    private AnimatorSet mHideAnimSet;

    public AbsTimeView(Context context) {
        this(context, null);
    }

    public AbsTimeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AbsTimeView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AbsTimeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initBasePaint();
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    private void initBasePaint() {
        mDialPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mDialPaint.setStyle(Paint.Style.STROKE);
        mSubLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mSubLinePaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBoundRadius = Math.min(w, h) * 0.5f;
    }

    protected int dp2px(float dpValue) {
        return SizeTransformer.dp2px(getContext(), dpValue);
    }

    protected int sp2px(float spValue) {
        return SizeTransformer.sp2px(getContext(), spValue);
    }

    protected String getString(int resId) {
        return getContext().getString(resId);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        mVisible = visibility == VISIBLE;
        updateRunning();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mVisible = false;
        if (mShowAnimSet != null) mShowAnimSet.removeAllListeners();
        if (mHideAnimSet != null) mHideAnimSet.removeAllListeners();
    }

    private void updateRunning() {
        boolean running = mStarted && mVisible && !mPaused;
        if (mRunning != running) {
            mRunning = running;
            if (running) {
                postDelayed(mTicker, UPDATE_DELAY);
            } else {
                removeCallbacks(mTicker);
            }
        }
    }

    protected void setStarted(boolean started) {
        if (mStarted != started) {
            mStarted = started;
            if (!mStarted) {
                mPaused = false;
            }
            updateRunning();
        }
    }

    public boolean isRunning() {
        return mRunning;
    }

    public boolean isStarted() {
        return mStarted;
    }

    public boolean isPaused() {
        return mPaused;
    }

    protected void setPaused(boolean paused) {
        if (mPaused != paused) {
            mPaused = paused;
            updateRunning();
        }
    }

    private final Runnable mTicker = new Runnable() {
        @Override
        public void run() {
            if (mRunning) {
                onTimeChanged();
                postDelayed(mTicker, UPDATE_DELAY);
            }
        }
    };

    protected abstract void onTimeChanged();

    protected Path parseSVG(String data) {
        try {
            return SvgPathParser.parsePath(data);
        } catch (ParseException e) {
            return null;
        }
    }

    void setAnimatedCallback(AnimatedCallback callback) {
        mAnimatedCallback = callback;
    }

    /**
     * Start an animation to show current time widget
     */
    void animatedShow() {
        if (mShowAnimSet == null) {
            mShowAnimSet = getShowAnimSet();
            if (mShowAnimSet != null) {
                mShowAnimSet.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (mAnimatedCallback != null) mAnimatedCallback.onStart();
                        setLayerType(LAYER_TYPE_HARDWARE, null);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (mAnimatedCallback != null) mAnimatedCallback.onEnd();
                        setLayerType(LAYER_TYPE_SOFTWARE, null);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        setLayerType(LAYER_TYPE_SOFTWARE, null);
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });
            }
        }
        if (mShowAnimSet != null && mShowAnimSet.isRunning()) mShowAnimSet.cancel();
        if (mShowAnimSet != null && !mShowAnimSet.isStarted()) {
            onInitShowAnimState();
            mShowAnimSet.start();
        }
    }

    protected void onInitShowAnimState() {
    }

    /**
     * Start an animation to hide current time widget
     */
    void animatedHide() {
        if (mShowAnimSet != null && mShowAnimSet.isRunning()) mShowAnimSet.cancel();
        if (ViewConfig.SHOW_HIDE_ANIMATION) {
            if (mHideAnimSet == null) {
                mHideAnimSet = getHideAnimSet();
                if (mHideAnimSet != null) {
                    mHideAnimSet.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            if (mAnimatedCallback != null) mAnimatedCallback.onStart();
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (mAnimatedCallback != null) mAnimatedCallback.onEnd();
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {
                        }
                    });
                }
            }
            if (mHideAnimSet != null) {
                if (mHideAnimSet.isRunning()) mHideAnimSet.cancel();
                mHideAnimSet.start();
            }
        } else {
            if (mAnimatedCallback != null) mAnimatedCallback.onEnd();
        }
    }

    protected AnimatorSet getShowAnimSet() {
        return new AnimatorSet();
    }

    protected AnimatorSet getHideAnimSet() {
        return new AnimatorSet();
    }

    interface AnimatedCallback {

        void onStart();

        void onEnd();
    }

    protected void setPaintShadow(float degree, Paint paint, int color) {
        float rad = (float) Math.toRadians(degree);
        paint.setShadowLayer(mShadowRadius, (float) (mShadowOffset * Math.sin(rad)),
                (float) (mShadowOffset * Math.cos(rad)), color);
    }
}
