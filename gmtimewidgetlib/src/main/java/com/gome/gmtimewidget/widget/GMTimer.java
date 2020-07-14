package com.gome.gmtimewidget.widget;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.os.SystemClock;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.gome.gmtimewidget.R;
import com.gome.gmtimewidget.util.ITimer;
import com.gome.gmtimewidget.util.TimeFormatter;

/**
 * @author Felix.Liang
 */
public class GMTimer extends AbsTimeView implements ITimer {

    private long mTotalTime = -1;
    private float mDividerLength;
    private float mInnerRadius;
    private float mOuterRadius;
    private float mDialRadius;
    private Path mDstOuterPath = new Path();
    private Path mDstInnerPath = new Path();
    private Path mDstTrianglePath = new Path();
    private Path mOuterPath;
    private Path mInnerPath;
    private Paint mBorderLinePaint;
    private Paint mNumberTextPaint;
    private Paint mScaleTextPaint;
    private int mColorBorder;
    private int mColorDialStart;
    private int mColorDialEnd;
    private int mBorderLineWidth;
    private long mBase;
    private int mNumberTextSize;
    private int mColorNumberText;
    private int mScaleTextSize;
    private int mColorScaleText;
    private String mTimeText;
    private int mTextVerticalOffset;
    private int mTextPadding;
    private float mCheckedDegree;
    private final String mHourScale = getString(R.string.hour);
    private final String mMinScale = getString(R.string.minute);
    private final String mSecScale = getString(R.string.second);
    private float[] mDash = new float[2];
    private int mDialWidth;
    private int mDialLength;
    private long mRemainingTime = -1;
    private Path mTrianglePath;
    private Paint mBorderFillPaint;
    private long mPauseTime;
    private Matrix mMatrix = new Matrix();
    private float mOuterScale = 1;
    private float mInnerScale = 1;
    private float mNumberTextScale = 1;
    private int mInnerAlpha = 255;
    private int mOuterAlpha = 255;
    private int mScaleTextAlpha = 255;
    private int mNumberTextAlpha = 255;
    private int mDialAlpha = 255;
    private float mOffsetDegree;

    public GMTimer(Context context) {
        this(context, null);
    }

    public GMTimer(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.GMTimer);
        mDividerLength = array.getDimensionPixelSize(R.styleable.GMTimer_timerDividerLength, dp2px(12));
        mBorderLineWidth = array.getDimensionPixelSize(R.styleable.GMTimer_timerBorderLineWidth, dp2px(0.8f));
        mColorBorder = array.getColor(R.styleable.GMTimer_timerBorderColor, 0xFF8C8C8C);
        mColorDialStart = array.getColor(R.styleable.GMTimer_timerDialStartColor, 0xFF0754FF);
        mColorDialEnd = array.getColor(R.styleable.GMTimer_timerDialEndColor, 0xFF455DFF);
        mColorNumberText = array.getColor(R.styleable.GMTimer_timerNumberTextColor, 0xCC000000);
        mNumberTextSize = array.getDimensionPixelSize(R.styleable.GMTimer_timerNumberTextSize, sp2px(40));
        mScaleTextSize = array.getDimensionPixelSize(R.styleable.GMTimer_timerScaleTextSize, sp2px(11));
        mColorScaleText = array.getColor(R.styleable.GMTimer_timerScaleTextColor, 0xFF8B887C);
        mTextVerticalOffset = array.getDimensionPixelSize(R.styleable.GMTimer_timerTextVerticalOffset, dp2px(10));
        mTextPadding = array.getDimensionPixelSize(R.styleable.GMTimer_timerTextPadding, dp2px(6));
        mDialWidth = array.getDimensionPixelSize(R.styleable.GMTimer_timerDialWidth, dp2px(0.8f));
        mDialLength = array.getDimensionPixelSize(R.styleable.GMTimer_timerDialLength, dp2px(7f));
        array.recycle();
        initPaint();
        setTotalTime(0);
    }

    private void initPaint() {
        mBorderLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mBorderLinePaint.setStyle(Paint.Style.STROKE);
        mBorderLinePaint.setColor(mColorBorder);
        mBorderLinePaint.setStrokeWidth(mBorderLineWidth);
        mBorderFillPaint = new Paint();
        mBorderFillPaint.set(mBorderLinePaint);
        mBorderFillPaint.setStyle(Paint.Style.FILL);
        mNumberTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mNumberTextPaint.setColor(mColorNumberText);
        mNumberTextPaint.setTextSize(mNumberTextSize);
        mNumberTextPaint.setTextAlign(Paint.Align.CENTER);
        mScaleTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mScaleTextPaint.setColor(mColorScaleText);
        mScaleTextPaint.setTextSize(mScaleTextSize);
        mScaleTextPaint.setTextAlign(Paint.Align.CENTER);
        mDialPaint.setColor(mColorDialStart);
        mDialPaint.setStrokeWidth(mDialLength);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mOuterRadius = mBoundRadius * 0.92f;
        mInnerRadius = mBoundRadius * 0.76f;
        float initDialRadius = (mOuterRadius + mInnerRadius) * 0.5f;
        LinearGradient linearGradient = new LinearGradient(
                0, -initDialRadius, 0, initDialRadius, mColorDialStart, mColorDialEnd, Shader.TileMode.CLAMP);
        mMatrix.reset();
        mMatrix.setRotate(45);
        linearGradient.setLocalMatrix(mMatrix);
        mDialPaint.setShader(linearGradient);
        setDialRadius(initDialRadius);
        initPath();
    }

    private void initPath() {
        initInnerPath();
        initOuterPath();
        initTrianglePath();
    }

    private void initTrianglePath() {
        Path tri = new Path();
        tri.moveTo(-dp2px(4), -mOuterRadius);
        tri.rLineTo(dp2px(8), 0);
        tri.lineTo(0, -mOuterRadius + dp2px(5));
        tri.lineTo(-dp2px(4), -mOuterRadius);
        tri.close();
        if (mTrianglePath == null) {
            mTrianglePath = new Path();
            for (int i = 0; i < 4; i++) {
                mMatrix.reset();
                mMatrix.preRotate(i * 90);
                tri.transform(mMatrix);
                mTrianglePath.addPath(tri);
            }
        }
    }

    public void setDialRadius(float radius) {
        if (mDialRadius != radius) {
            mDialRadius = radius;
            updateDialPathEffect();
        }
    }

    private void updateDialPathEffect() {
        float unitDial = (float) (2 * Math.PI * mDialRadius / 240);
        mDash[0] = mDialWidth;
        mDash[1] = unitDial - mDialWidth;
        mDialPaint.setPathEffect(new DashPathEffect(mDash, 0));
    }

    private void initInnerPath() {
        if (mInnerPath == null) {
            mInnerPath = new Path();
            mInnerPath.addOval(-mInnerRadius, -mInnerRadius, mInnerRadius, mInnerRadius, Path.Direction.CCW);
            Path temp = new Path();
            temp.moveTo(0, -mInnerRadius - dp2px(1));
            temp.rLineTo(0, -mDividerLength);
            mInnerPath.addPath(temp);
            temp.reset();
            temp.moveTo(mInnerRadius + dp2px(1), 0);
            temp.rLineTo(mDividerLength, 0);
            mInnerPath.addPath(temp);
            temp.reset();
            temp.moveTo(0, mInnerRadius + dp2px(1));
            temp.rLineTo(0, mDividerLength);
            mInnerPath.addPath(temp);
            temp.reset();
            temp.moveTo(-mInnerRadius - dp2px(1), 0);
            temp.rLineTo(-mDividerLength, 0);
            mInnerPath.addPath(temp);
        }
    }

    private void initOuterPath() {
        if (mOuterPath == null) {
            mOuterPath = new Path();
            float start = 5;
            for (int i = 0; i < 4; i++) {
                mOuterPath.addArc(-mOuterRadius, -mOuterRadius, mOuterRadius, mOuterRadius, start, 80);
                start += 90;
            }
        }
    }

    @Override
    protected void onTimeChanged() {
        setRemainingTime(Math.max(0, mTotalTime - getElapsedTime()));
        if (mRemainingTime == 0) {
            setStarted(false);
        }
    }

    private void setRemainingTime(long millis) {
        if (millis != mRemainingTime) {
            mRemainingTime = millis;
            onRemainingTimeChanged();
        }
    }

    private synchronized void onRemainingTimeChanged() {
        updateText(mRemainingTime);
        updateCheckedDial(mRemainingTime);
        invalidate();
    }

    private void updateText(long millis) {
        String time = TimeFormatter.getFormatTime(millis);
        setTimeText(time);
    }

    private void updateCheckedDial(long millis) {
        mCheckedDegree = Math.min(360, calculateDegreeWithMillis(millis));
        invalidate();
    }

    private float calculateDegreeWithMillis(long millis) {
        float degree;
        float dpm;//degree per millisecond
        if (millis <= 1000 * 60) {
            dpm = 90f / (1000 * 60);
            degree = millis * dpm;
        } else if (millis <= 1000 * 60 * 10) {
            dpm = 90f / (1000 * 60 * 9);
            degree = (millis - (1000 * 60)) * dpm + 90;
        } else if (millis <= 1000 * 60 * 30) {
            dpm = 90f / (1000 * 60 * 20);
            degree = (millis - 1000 * 60 * 10) * dpm + 180;
        } else if (millis <= 1000 * 60 * 60) {
            dpm = 90f / (1000 * 60 * 30);
            degree = (millis - 1000 * 60 * 30) * dpm + 270;
        } else {
            dpm = 360f / (1000 * 60 * 60);
            degree = (millis - 1000 * 60 * 60) * dpm + 360;
        }
        return degree;
    }

    public void setTimeText(String text) {
        mTimeText = text;
        invalidate();
    }

    public void setBase() {
        mBase = SystemClock.elapsedRealtime();
    }

    private long getElapsedTime() {
        return SystemClock.elapsedRealtime() - mBase;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.translate(mXOffset, mYOffset);
        drawBorder(canvas);
        drawDial(canvas);
        drawNumberText(canvas);
        drawScaleText(canvas);
    }

    private void drawBorder(Canvas canvas) {
        mMatrix.reset();
        mMatrix.preScale(mInnerScale, mInnerScale);
        mInnerPath.transform(mMatrix, mDstInnerPath);
        mBorderLinePaint.setAlpha(mInnerAlpha);
        canvas.drawPath(mDstInnerPath, mBorderLinePaint);
        mMatrix.reset();
        mMatrix.preScale(mOuterScale, mOuterScale);
        mOuterPath.transform(mMatrix, mDstOuterPath);
        mTrianglePath.transform(mMatrix, mDstTrianglePath);
        mBorderLinePaint.setAlpha(mOuterAlpha);
        mBorderFillPaint.setAlpha(mOuterAlpha);
        canvas.drawPath(mDstOuterPath, mBorderLinePaint);
        canvas.drawPath(mDstTrianglePath, mBorderFillPaint);
    }

    private void drawDial(Canvas canvas) {
        canvas.save();
        canvas.rotate(mOffsetDegree);
        mDialPaint.setAlpha(mDialAlpha);
        canvas.drawArc(-mDialRadius, -mDialRadius, mDialRadius, mDialRadius,
                -90, mCheckedDegree, false, mDialPaint);
        canvas.restore();
    }

    private void drawNumberText(Canvas canvas) {
        if (!TextUtils.isEmpty(mTimeText)) {
            mNumberTextPaint.setAlpha(mNumberTextAlpha);
            canvas.save();
            canvas.scale(mNumberTextScale, mNumberTextScale);
            canvas.drawText(mTimeText, 0, dp2px(8), mNumberTextPaint);
            canvas.restore();
        }
    }

    private void drawScaleText(Canvas canvas) {
        final int y = mTextPadding + mTextVerticalOffset + mScaleTextSize;
        mScaleTextPaint.setAlpha(mScaleTextAlpha);
        canvas.drawText(mHourScale, -dp2px(56), y, mScaleTextPaint);
        canvas.drawText(mMinScale, 0, y, mScaleTextPaint);
        canvas.drawText(mSecScale, dp2px(56), y, mScaleTextPaint);
    }

    @Override
    public void setTotalTime(long millis) {
        if (mTotalTime != millis) {
            mTotalTime = millis;
            setRemainingTime(millis);
            onRemainingTimeChanged();
        }
    }

    @Override
    public void start() {
        if (mTotalTime != 0) {
            setBase();
            setStarted(true);
        }
    }

    @Override
    public void pause() {
        if (mTotalTime != 0) {
            setPaused(true);
            mPauseTime = SystemClock.elapsedRealtime();
        }
    }

    @Override
    public void resume() {
        if (mTotalTime != 0) {
            long pauseElapseTime = SystemClock.elapsedRealtime() - mPauseTime;
            mBase += pauseElapseTime;
            setPaused(false);
        }
    }

    @Override
    public void reset() {
        setStarted(false);
        setTotalTime(0);
    }

    @Override
    protected void onInitShowAnimState() {
        mScaleTextAlpha = 0;
        mNumberTextAlpha = 0;
        mDialAlpha = 0;
        mOuterAlpha = 0;
        mInnerAlpha = 0;
    }

    @Override
    protected AnimatorSet getShowAnimSet() {
        AnimatorSet animSet = new AnimatorSet();
        ValueAnimator anim1_1 = ValueAnimator.ofInt(0, 255);
        anim1_1.setDuration(300);
        anim1_1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mOuterAlpha = (Integer) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim1_2 = ValueAnimator.ofFloat(0.3f, 1);
        anim1_2.setDuration(540);
        anim1_2.setInterpolator(new OvershootInterpolator(1.5f));
        anim1_2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mOuterScale = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        ValueAnimator anim2_1 = ValueAnimator.ofInt(0, 255);
        anim2_1.setStartDelay(120);
        anim2_1.setDuration(300);
        anim2_1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mInnerAlpha = (int) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim2_2 = ValueAnimator.ofFloat(0.3f, 1);
        anim2_2.setStartDelay(120);
        anim2_2.setDuration(540);
        anim2_2.setInterpolator(new OvershootInterpolator(1.5f));
        anim2_2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mInnerScale = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        ValueAnimator anim3_1 = ValueAnimator.ofInt(0, 255);
        anim3_1.setStartDelay(450);
        anim3_1.setDuration(150);
        anim3_1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mScaleTextAlpha = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        ValueAnimator anim4_1 = ValueAnimator.ofFloat(0.5f, 1);
        anim4_1.setStartDelay(450);
        anim4_1.setDuration(210);
        anim4_1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mNumberTextScale = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        ValueAnimator anim4_2 = ValueAnimator.ofInt(0, 255);
        anim4_2.setStartDelay(450);
        anim4_2.setDuration(150);
        anim4_2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mNumberTextAlpha = (int) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim5_1 = ValueAnimator.ofFloat(-45, 0);
        anim5_1.setStartDelay(300);
        anim5_1.setDuration(300);
        anim5_1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mOffsetDegree = (float) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim5_2 = ValueAnimator.ofInt(0, 255);
        anim5_2.setStartDelay(300);
        anim5_2.setDuration(300);
        anim5_2.setInterpolator(new AccelerateDecelerateInterpolator());
        anim5_2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mDialAlpha = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        animSet.playTogether(anim1_1, anim1_2, anim2_1, anim2_2, anim3_1, anim4_1, anim4_2, anim5_1, anim5_2);
        return animSet;
    }

    @Override
    protected AnimatorSet getHideAnimSet() {
        AnimatorSet animSet = new AnimatorSet();
        ValueAnimator anim1_1 = ValueAnimator.ofInt(255, 0);
        anim1_1.setStartDelay(90);
        anim1_1.setDuration(360);
        anim1_1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mOuterAlpha = (int) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim1_2 = ValueAnimator.ofFloat(1, 0);
        anim1_2.setStartDelay(90);
        anim1_2.setDuration(360);
        anim1_2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mOuterScale = (float) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim2_1 = ValueAnimator.ofInt(255, 0);
        anim2_1.setDuration(450);
        anim2_1.setInterpolator(new AccelerateDecelerateInterpolator());
        anim2_1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mInnerAlpha = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        ValueAnimator anim2_2 = ValueAnimator.ofFloat(1, 0);
        anim2_2.setDuration(450);
        anim2_2.setInterpolator(new AccelerateDecelerateInterpolator());
        anim2_2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mInnerScale = (float) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim3_1 = ValueAnimator.ofInt(255, 0);
        anim3_1.setDuration(150);
        anim3_1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mScaleTextAlpha = (int) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim4_1 = ValueAnimator.ofInt(255, 0);
        anim4_1.setDuration(150);
        anim4_1.setInterpolator(new AccelerateDecelerateInterpolator());
        anim4_1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mNumberTextAlpha = (int) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim4_2 = ValueAnimator.ofFloat(1, 0.5f);
        anim4_2.setDuration(210);
        anim4_2.setInterpolator(new AccelerateDecelerateInterpolator());
        anim4_2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mNumberTextScale = (float) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim5_1 = ValueAnimator.ofFloat(0, -45);
        anim5_1.setDuration(300);
        anim5_1.setInterpolator(new AccelerateDecelerateInterpolator());
        anim5_1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mOffsetDegree = (float) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim5_2 = ValueAnimator.ofInt(255, 0);
        anim5_2.setDuration(240);
        anim5_2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mDialAlpha = (int) animation.getAnimatedValue();
            }
        });
        animSet.playTogether(anim1_1, anim1_2, anim2_1, anim2_2, anim3_1, anim4_1, anim4_2, anim5_1, anim5_2);
        return animSet;
    }
}
