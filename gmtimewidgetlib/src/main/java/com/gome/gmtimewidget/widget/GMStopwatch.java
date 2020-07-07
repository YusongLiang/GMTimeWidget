package com.gome.gmtimewidget.widget;

import android.animation.Animator;
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
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.gome.gmtimewidget.R;
import com.gome.gmtimewidget.util.IStopwatch;
import com.gome.gmtimewidget.util.ViewConfig;

/**
 * @author Felix.Liang
 */
public class GMStopwatch extends AbsTimeView implements IStopwatch {

    private static final String BORDER_PATH_DATA1 = "M333.2-224.6C376.6-160.4,402-83,402,0.3" +
            "c0,222-180,402-402,402s-402-180-402-402c0-212.6,165.1-386.7,374.1-401c0-0.5-0.1-25.3-0.1-74.5" +
            "h-14.4c-15.9,0-28.9-12.9-28.9-28.9c0-15.9,12.9-28.9,28.9-28.9h86.6c15.9,0,28.9,12.9,28.9,28.9" +
            "c0,15.9-12.9,28.9-28.9,28.9H29.7v72.2c67.5,7.3,130.4,29,184.6,63.2";
    private static final String BORDER_PATH_DATA2 = "M330.2-383.2l-54.5,58.1c-10,10.7-9.4,27.4,1.3,37.4" +
            "s27.6,9.4,37.6-1.3l54.5-58.1c10-10.7,9.4-27.4-1.3-37.4S340.2-393.9,330.2-383.2z";
    private final int mVerticalOffset = dp2px(16);
    private int mColorDialEnd;
    private int mColorDialStart;
    private int mColorAxle;
    private int mColorUnCheckedDial;
    private int mColorShadow;
    private int mColorHand;
    private int mColorInnerDial;
    private int mColorBorder;
    private int mAxleRadius;
    private int mHandWidth;
    private int mInnerDialWidth;
    private int mOuterDialWidth;
    private int mInnerDialLength;
    private int mOuterDialLength;
    private int mBorderLineWidth;
    private Paint mAxlePaint;
    private Paint mBorderLinePaint;
    private Paint mHandPaint;
    private Paint mInnerDialPaint;
    private float mOuterDialRadius;
    private float mInnerDialRadius;
    private float[] mDash = new float[2];
    private float mTimeDegree;
    private Path mInitBorderPath;
    private Path mInitBorderSmallPath;
    private Path mDstBorderPath = new Path();
    private Path mBorderPath = new Path();
    private Path mBorderSmallPath = new Path();
    private float mBorderRadius;
    private PathMeasure mPathMeasure = new PathMeasure();
    private Matrix mMatrix = new Matrix();
    private long mBase;
    private int[] mDialColors = new int[3];
    private float[] mDialColorPositions = {0.5f, 0.75f, 1f};
    private SweepGradient mSweepGradient;
    private long mElapsedTime;
    private long mPauseTime;
    private float mHandOffsetDegree;
    private int mHandAlpha = 255;
    private float mHandScale = 1;
    private int mDialAlpha = 255;
    private int mInnerDialAlpha = 255;
    private int mAxleAlpha = 255;
    private int mSmallBorderAlpha = 255;
    private float mInitOuterDialRadius;
    private float mInitInnerDialRadius;
    private float mBorderLength;
    private RectF mBounds;
    private float mBorderSmallScale = 1;
    private ValueAnimator mRecordAnim;
    private StopwatchListener mStopwatchListener;
    private long mRecordTime;

    public GMStopwatch(Context context) {
        super(context);
    }

    public GMStopwatch(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.GMStopwatch);
        mColorAxle = array.getColor(R.styleable.GMStopwatch_stopwatchAxleColor, 0xFF3492E6);
        mColorUnCheckedDial = array.getColor(R.styleable.GMStopwatch_stopwatchUnCheckedDialColor, 0xFF8C8C8C);
        mColorShadow = array.getColor(R.styleable.GMStopwatch_stopwatchShadowColor, 0x30000000);
        mColorHand = array.getColor(R.styleable.GMStopwatch_stopwatchHandColor, 0xFF3492E6);
        mColorBorder = array.getColor(R.styleable.GMStopwatch_stopwatchBorderColor, 0xFF8C8C8C);
        mColorDialStart = array.getColor(R.styleable.GMStopwatch_stopwatchDialStartColor, 0xFF0754FF);
        mColorDialEnd = array.getColor(R.styleable.GMStopwatch_stopwatchDialEndColor, 0xFF455DFF);
        mColorInnerDial = array.getColor(R.styleable.GMStopwatch_stopwatchInnerDialColor, 0xFF8C8C8C);
        mAxleRadius = array.getDimensionPixelSize(R.styleable.GMStopwatch_stopwatchAxleRadius, dp2px(3));
        mHandWidth = array.getDimensionPixelSize(R.styleable.GMStopwatch_stopwatchHandWidth, dp2px(1));
        mOuterDialWidth = array.getDimensionPixelSize(R.styleable.GMStopwatch_stopwatchOuterDialWidth, dp2px(1));
        mInnerDialWidth = array.getDimensionPixelSize(R.styleable.GMStopwatch_stopwatchInnerDialWidth, dp2px(1));
        mOuterDialLength = array.getDimensionPixelSize(R.styleable.GMStopwatch_stopwatchOuterDialLength, dp2px(7));
        mInnerDialLength = array.getDimensionPixelSize(R.styleable.GMStopwatch_stopwatchInnerDialLength, dp2px(10.5f));
        mBorderLineWidth = array.getDimensionPixelSize(R.styleable.GMStopwatch_stopwatchBorderLineWidth, dp2px(1));
        array.recycle();
        updateDialColors();
        initPaint();
        initBorderPath();
    }

    private void updateDialColors() {
        mDialColors[0] = mColorUnCheckedDial;
        mDialColors[1] = mColorDialStart;
        mDialColors[2] = mColorDialEnd;
    }

    private void initPaint() {
        mBorderLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mBorderLinePaint.setStyle(Paint.Style.STROKE);
        mBorderLinePaint.setColor(mColorBorder);
        mBorderLinePaint.setStrokeWidth(mBorderLineWidth);
        mAxlePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mAxlePaint.setColor(mColorAxle);
        mAxlePaint.setStyle(Paint.Style.FILL);
        mAxlePaint.setShadowLayer(mShadowRadius, 0, mShadowOffset, mColorShadow);
        mHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mHandPaint.setColor(mColorHand);
        mHandPaint.setStyle(Paint.Style.STROKE);
        mHandPaint.setStrokeCap(Paint.Cap.ROUND);
        mHandPaint.setStrokeWidth(mHandWidth);
        mInnerDialPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mInnerDialPaint.setColor(mColorInnerDial);
        mInnerDialPaint.setStyle(Paint.Style.STROKE);
        mInnerDialPaint.setStrokeWidth(mInnerDialLength);
        mSweepGradient = new SweepGradient(0, 0, mDialColors, mDialColorPositions);
        mDialPaint.setStrokeWidth(mOuterDialLength);
        mDialPaint.setShader(mSweepGradient);
    }

    @Override
    protected void onTimeChanged() {
        long time = getStopwatchTime();
        float degree = time * (360f / (60 * 1000));
        degree %= 360;
        setTimeDegree(degree);
        if (mStopwatchListener != null) mStopwatchListener.onUpdate(getStopwatchTime());
    }

    private void setBase() {
        setBase(true);
    }

    private void setBase(boolean resetElapse) {
        mBase = SystemClock.elapsedRealtime() - mElapsedTime;
        if (resetElapse) mElapsedTime = 0;
    }

    private long getElapsedTime() {
        return SystemClock.elapsedRealtime() - mBase;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float initOuterBorder = mBoundRadius * 0.76f;
        mInitOuterDialRadius = initOuterBorder - mOuterDialLength * 0.5f;
        setOuterDialRadius(mInitOuterDialRadius);
        mInitInnerDialRadius = initOuterBorder - mOuterDialLength - mInnerDialLength * 0.5f;
        setInnerDialRadius(mInitInnerDialRadius);
        LinearGradient linearGradient = new LinearGradient(
                0, -initOuterBorder, 0, initOuterBorder, mColorDialStart, mColorDialEnd, Shader.TileMode.CLAMP);
        mMatrix.reset();
        mMatrix.setRotate(45);
        linearGradient.setLocalMatrix(mMatrix);
        setBorderRadius(0.8f * mBoundRadius);
    }

    private void initBorderPath() {
        mInitBorderPath = parseSVG(BORDER_PATH_DATA1);
        mInitBorderSmallPath = parseSVG(BORDER_PATH_DATA2);
    }

    private void setInnerDialRadius(float radius) {
        if (mInnerDialRadius != radius) {
            mInnerDialRadius = radius;
            updateInnerDialPathEffect();
        }
    }

    private void updateInnerDialPathEffect() {
        float unitDial = (float) (2 * Math.PI * mInnerDialRadius / 12);
        mDash[0] = mInnerDialWidth;
        mDash[1] = unitDial - mInnerDialWidth;
        mInnerDialPaint.setPathEffect(new DashPathEffect(mDash, 0));
    }

    private void setOuterDialRadius(float radius) {
        if (mOuterDialRadius != radius) {
            mOuterDialRadius = radius;
            updateOuterDialPathEffect();
        }
    }

    private void updateOuterDialPathEffect() {
        float unitDial = (float) (2 * Math.PI * mOuterDialRadius / 240);
        mDash[0] = mOuterDialWidth;
        mDash[1] = unitDial - mOuterDialWidth;
        DashPathEffect effect = new DashPathEffect(mDash, 0);
        mDialPaint.setPathEffect(effect);
    }

    private void setBorderRadius(float radius) {
        if (mBorderRadius != radius) {
            mBorderRadius = radius;
            mMatrix.reset();
            float scale = mBorderRadius / 402f;
            mMatrix.preScale(scale, scale);
            mInitBorderPath.transform(mMatrix, mBorderPath);
            mInitBorderSmallPath.transform(mMatrix, mBorderSmallPath);
            mPathMeasure.setPath(mBorderPath, false);
            mBorderLength = mPathMeasure.getLength();
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.translate(getWidth() / 2, getHeight() / 2 + mVerticalOffset);
        drawBorder(canvas);
        drawDials(canvas);
        drawHand(canvas);
        drawAxle(canvas);
    }

    private void drawBorder(Canvas canvas) {
        if (mDstBorderPath != null) {
            canvas.drawPath(mDstBorderPath, mBorderLinePaint);
        }
        if (mBorderSmallPath != null) {
            canvas.save();
            if (ViewConfig.STOPWATCH_IS_TOUCHABLE)
                canvas.scale(mBorderSmallScale, mBorderSmallScale);
            mBorderLinePaint.setAlpha(mSmallBorderAlpha);
            canvas.drawPath(mBorderSmallPath, mBorderLinePaint);
            mBorderLinePaint.setAlpha(255);
            canvas.restore();
        }
    }

    private void drawDials(Canvas canvas) {
        mMatrix.reset();
        mMatrix.preRotate(mTimeDegree - 90);
        mSweepGradient.setLocalMatrix(mMatrix);
        mDialPaint.setShader(mSweepGradient);
        mDialPaint.setAlpha(mDialAlpha);
        mInnerDialPaint.setAlpha(mInnerDialAlpha);
        canvas.drawCircle(0, 0, mOuterDialRadius, mDialPaint);
        canvas.drawCircle(0, 0, mInnerDialRadius, mInnerDialPaint);
    }

    private void drawHand(Canvas canvas) {
        float bound = mOuterDialRadius + mOuterDialLength * 0.5f;
        canvas.save();
        canvas.scale(mHandScale, mHandScale);
        float degree = mTimeDegree - 90 + mHandOffsetDegree;
        setPaintShadow(degree, mHandPaint, mColorShadow);
        canvas.rotate(degree);
        mHandPaint.setAlpha(mHandAlpha);
        canvas.drawLine(-bound * 0.2f, 0, bound, 0, mHandPaint);
        canvas.restore();
    }

    private void drawAxle(Canvas canvas) {
        mAxlePaint.setAlpha(mAxleAlpha);
        canvas.drawCircle(0, 0, mAxleRadius, mAxlePaint);
    }

    @Override
    public void setElapsedTime(long millis) {
        if (mElapsedTime != millis) {
            mElapsedTime = millis;
            float degree = millis * (360f / (60 * 1000));
            degree %= 360;
            setTimeDegree(degree);
            if (mStopwatchListener != null) mStopwatchListener.onUpdate(millis);
        }
    }

    @Override
    public void start() {
        if (!isStarted()) {
            setBase();
            setStarted(true);
        }
    }

    @Override
    public void pause() {
        if (isRunning()) {
            setPaused(true);
            mPauseTime = SystemClock.elapsedRealtime();
        }
    }

    @Override
    public void resume() {
        if (isPaused()) {
            long pauseElapseTime = SystemClock.elapsedRealtime() - mPauseTime;
            mBase += pauseElapseTime;
            setPaused(false);
        }
    }

    @Override
    public void reset() {
        setStarted(false);
        setTimeDegree(0);
        mBase = 0;
        if (mStopwatchListener != null) mStopwatchListener.onUpdate(getStopwatchTime());
    }

    private void setTimeDegree(float degree) {
        if (mTimeDegree != degree) {
            mTimeDegree = degree;
            invalidate();
        }
    }

    @Override
    protected void onInitShowAnimState() {
        mAxleAlpha = 0;
        mDialAlpha = 0;
        mHandAlpha = 0;
        mInnerDialAlpha = 0;
        mSmallBorderAlpha = 0;
    }

    @Override
    protected AnimatorSet getShowAnimSet() {
        AnimatorSet animSet = new AnimatorSet();
        ValueAnimator anim1_1 = ValueAnimator.ofFloat(-30, 0);
        anim1_1.setDuration(300);
        anim1_1.setInterpolator(new AccelerateDecelerateInterpolator());
        anim1_1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mHandOffsetDegree = (float) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim1_2 = ValueAnimator.ofInt(0, 255);
        anim1_2.setDuration(300);
        anim1_2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mHandAlpha = (int) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim1_3 = ValueAnimator.ofFloat(0.2f, 1);
        anim1_3.setDuration(600);
        anim1_3.setInterpolator(new OvershootInterpolator(1.5f));
        anim1_3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mHandScale = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        ValueAnimator anim2_1 = ValueAnimator.ofInt(0, 255);
        anim2_1.setDuration(300);
        anim2_1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mDialAlpha = (int) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim2_2 = ValueAnimator.ofFloat(0.2f, 1);
        anim2_2.setDuration(600);
        anim2_2.setInterpolator(new OvershootInterpolator(0.7f));
        anim2_2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setOuterDialRadius(((float) animation.getAnimatedValue()) * mInitOuterDialRadius);
            }
        });
        ValueAnimator anim3_1 = ValueAnimator.ofInt(0, 255);
        anim3_1.setDuration(150);
        anim3_1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAxleAlpha = (int) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim4_1 = ValueAnimator.ofFloat(1, 0);
        anim4_1.setDuration(600);
        anim4_1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mDstBorderPath.reset();
                mPathMeasure.getSegment(((float) animation.getAnimatedValue()) * mBorderLength, mBorderLength, mDstBorderPath, true);
            }
        });
        ValueAnimator anim4_2 = ValueAnimator.ofInt(0, 255);
        anim4_2.setStartDelay(360);
        anim4_2.setDuration(240);
        anim4_2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mSmallBorderAlpha = (int) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim5_1 = ValueAnimator.ofInt(0, 255);
        anim5_1.setStartDelay(150);
        anim5_1.setDuration(300);
        anim5_1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mInnerDialAlpha = (int) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim5_2 = ValueAnimator.ofFloat(0.2f, 1);
        anim5_2.setStartDelay(150);
        anim5_2.setDuration(600);
        anim5_2.setInterpolator(new OvershootInterpolator(1.5f));
        anim5_2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setInnerDialRadius(((float) animation.getAnimatedValue()) * mInitInnerDialRadius);
                invalidate();
            }
        });
        animSet.playTogether(anim1_1, anim1_2, anim1_3,
                anim2_1, anim2_2, anim3_1, anim4_1,
                anim4_2, anim5_1, anim5_2);
        return animSet;
    }

    @Override
    protected AnimatorSet getHideAnimSet() {
        AnimatorSet animSet = new AnimatorSet();
        ValueAnimator anim1_1 = ValueAnimator.ofFloat(0, -30);
        anim1_1.setStartDelay(60);
        anim1_1.setDuration(300);
        anim1_1.setInterpolator(new AccelerateDecelerateInterpolator());
        anim1_1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mHandOffsetDegree = (float) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim1_2 = ValueAnimator.ofInt(255, 0);
        anim1_2.setStartDelay(60);
        anim1_2.setDuration(300);
        anim1_2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mHandAlpha = (int) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim1_3 = ValueAnimator.ofFloat(1, 0.64f);
        anim1_3.setStartDelay(90);
        anim1_3.setDuration(240);
        anim1_3.setInterpolator(new AnticipateInterpolator());
        anim1_3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mHandScale = (float) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim2_1 = ValueAnimator.ofInt(255, 0);
        anim2_1.setStartDelay(210);
        anim2_1.setDuration(240);
        anim2_1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mDialAlpha = (int) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim2_2 = ValueAnimator.ofFloat(1, 0.2f);
        anim2_2.setStartDelay(120);
        anim2_2.setDuration(330);
        anim2_2.setInterpolator(new AnticipateInterpolator());
        anim2_2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setOuterDialRadius(((float) animation.getAnimatedValue()) * mInitOuterDialRadius);
            }
        });
        ValueAnimator anim3_1 = ValueAnimator.ofInt(255, 0);
        anim3_1.setStartDelay(300);
        anim3_1.setDuration(150);
        anim3_1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAxleAlpha = (int) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim4_1 = ValueAnimator.ofFloat(0, 1);
        anim4_1.setDuration(450);
        anim4_1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mDstBorderPath.reset();
                mPathMeasure.getSegment(((float) animation.getAnimatedValue()) * mBorderLength, mBorderLength, mDstBorderPath, true);
                invalidate();
            }
        });
        ValueAnimator anim4_2 = ValueAnimator.ofInt(255, 0);
        anim4_2.setDuration(150);
        anim4_2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mSmallBorderAlpha = (int) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim5_1 = ValueAnimator.ofInt(255, 0);
        anim5_1.setStartDelay(60);
        anim5_1.setDuration(240);
        anim5_1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mInnerDialAlpha = (int) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim5_2 = ValueAnimator.ofFloat(1, 0.2f);
        anim5_2.setStartDelay(60);
        anim5_2.setDuration(240);
        anim5_2.setInterpolator(new AnticipateInterpolator());
        anim5_2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setInnerDialRadius(((float) animation.getAnimatedValue()) * mInitInnerDialRadius);
            }
        });
        animSet.playTogether(anim1_1, anim1_2, anim1_3,
                anim2_1, anim2_2, anim3_1, anim4_1,
                anim4_2, anim5_1, anim5_2);
        return animSet;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (ViewConfig.STOPWATCH_IS_TOUCHABLE) {
                    if (mBounds == null) {
                        mBounds = new RectF();
                        mBorderSmallPath.computeBounds(mBounds, false);
                    }
                    final float x = event.getX() - getWidth() / 2;
                    final float y = event.getY() - getHeight() / 2 - mVerticalOffset;
                    if (mBounds.contains(x, y)) {
                        startRecordAnimation();
                        return true;
                    }
                }
                return super.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    private void startRecordAnimation() {
        if (mRecordAnim == null) {
            mRecordAnim = ValueAnimator.ofFloat(1, 0.93f, 1);
            mRecordAnim.setDuration(100);
            mRecordAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mBorderSmallScale = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            mRecordAnim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mRecordTime = getStopwatchTime();
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mStopwatchListener != null) mStopwatchListener.onRecord(mRecordTime);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
        }
        mRecordAnim.start();
    }

    @Override
    public void record() {
        if (ViewConfig.STOPWATCH_IS_TOUCHABLE) startRecordAnimation();
        else if (mStopwatchListener != null) mStopwatchListener.onRecord(getStopwatchTime());
    }

    @Override
    public long getStopwatchTime() {
        if (isPaused()) {
            return mPauseTime - mBase;
        }
        return !isStarted() ? 0 : getElapsedTime();
    }

    @Override
    public void setStopwatchWatcher(StopwatchListener listener) {
        mStopwatchListener = listener;
    }
}
