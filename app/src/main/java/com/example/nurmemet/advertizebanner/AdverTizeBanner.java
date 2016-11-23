package com.cn.advertizebanner;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.database.Observable;
import android.graphics.Rect;
import android.os.Parcelable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * Created by nurmemet on 2015/12/12.
 */
public class AdverTizeBanner extends ViewGroup {
    private int mDuration = 250;
    private int mDelay = 5000;
    private Interpolator mAnimInterpolator = new DecelerateInterpolator();
    private static final int MIN_DISTANCE_FOR_FLING = 25; // dips
    private static final int MIN_FLING_VELOCITY = 400; // dips
    private int mFlingDistance;
    private int mMinimumVelocity;
    private BannerAdapter mAdapter;
    int mTouchSlop;
    int mMaximumVelocity;
    private VelocityTracker mVelocityTracker;
    private float mLastMotionX;
    private float mLastMotionY;
    private float mInitialMotionX;
    private float mInitialMotionY;
    private boolean mIsBeingDragged;
    private int mActivePointerId = INVALID_POINTER;
    private static final int INVALID_POINTER = -1;
    //private Handler handler;
    private int mCurrentScreen;
    private int mNextScreen;
    private ArrayList<ViewHolder> viewList = new ArrayList<ViewHolder>();
    ValueAnimator autoScrollAnim;
    ValueAnimator adjustPostionAnim;
    private static float OVER_PAGE_FLAG = 0.9f;
    private boolean isAutoScroll = false;
    private OnPageChangedListener onPageChangedListener;
    private BannerDataObserver mObserver = new BannerDataObserver();

    private boolean isClick = false;
    private boolean isRunning = false;

    public AdverTizeBanner(Context context) {
        super(context);
        init();
    }

    public AdverTizeBanner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AdverTizeBanner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setOnPageChangedListener(OnPageChangedListener onPageChangedListener) {
        this.onPageChangedListener = onPageChangedListener;
    }

    public int getCurrentPosition() {
        return mCurrentScreen;
    }

    private void init() {
        final ViewConfiguration configuration = ViewConfiguration
                .get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        //handler = new Handler();
        mFlingDistance = (int) dp2px(getContext(), MIN_DISTANCE_FOR_FLING);
        mMinimumVelocity = (int) dp2px(getContext(), MIN_FLING_VELOCITY);
    }

    public void setDuration(int duration) {
        mDuration = duration;
    }

    public void setDelay(int delay) {
        this.mDelay = delay;
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean b = super.dispatchTouchEvent(ev);

        return b;
    }

    private void stop() {
        isRunning = false;
        //handler.removeCallbacksAndMessages(null);
        removeCallbacks(mRunnable);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mAdapter.getCount() < 2) {
            return false;
        }

         /*
         * This method JUST determines whether we want to intercept the motion.
         * If we return true, onMotionEvent will be called and we do the actual
         * scrolling there.
         */

        final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;

        // Always take care of the touch gesture being complete.
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            // Release the drag.
            resetTouch();
            return false;
        }

        // Nothing more to do here if we have decided whether or not we
        // are dragging.
        if (action != MotionEvent.ACTION_DOWN) {
            if (mIsBeingDragged) {
                return true;
            }
        }


        switch (action) {
            case MotionEvent.ACTION_DOWN: {

                mLastMotionX = mInitialMotionX = ev.getX();
                mLastMotionY = mInitialMotionY = ev.getY();
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                if (autoScrollAnim != null && autoScrollAnim.isRunning()) {
                    autoScrollAnim.cancel();
                }
                // requestDisallowInterceptTouchEvent(true);
                resetPosition(ev.getX());
                //rotate();


            }
            break;
            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                if (pointerIndex == -1) {
                    resetTouch();
                    break;
                }
                final float x = MotionEventCompat.getX(ev, pointerIndex);
                final float xDiff = Math.abs(x - mLastMotionX);
                final float y = MotionEventCompat.getY(ev, pointerIndex);
                final float yDiff = Math.abs(y - mLastMotionY);
                if (xDiff > mTouchSlop && xDiff > yDiff) {
                    mIsBeingDragged = true;
                    mLastMotionX = x - mInitialMotionX > 0 ? mInitialMotionX + mTouchSlop :
                            mInitialMotionX - mTouchSlop;
                    mLastMotionY = y;
                    // Disallow Parent Intercept, just in case
                    requestDisallowInterceptTouchEvent(true);
                } else {
                    rotate();
                }

                if (mIsBeingDragged) {
                    final int activePointerIndex = MotionEventCompat.findPointerIndex(
                            ev, mActivePointerId);
                    final float x_ = MotionEventCompat.getX(ev, activePointerIndex);
                    performDrag(x_);

                }
            }

            break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mIsBeingDragged) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int initialVelocity = (int) VelocityTrackerCompat.getXVelocity(
                            velocityTracker, mActivePointerId);
                    final int activePointerIndex =
                            MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                    final float x = MotionEventCompat.getX(ev, activePointerIndex);
                    final int totalDelta = (int) (x - mInitialMotionX);
                    int nextPage = determineTargetPage(mCurrentScreen, initialVelocity,
                            totalDelta);
                    setCurrentItemInternal(nextPage, true, initialVelocity);
                    resetTouch();
                } else {

                }
                isClick = false;
                break;
        }
        return mIsBeingDragged;
    }


    @Override
    public boolean canScrollHorizontally(int direction) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        if (!mIsBeingDragged) {
            return false;
        }
        if (mAdapter.getCount() < 2) {
            return false;
        }
        int action = MotionEventCompat.getActionMasked(ev);
        if (action == MotionEvent.ACTION_DOWN && ev.getEdgeFlags() != 0) {
            // Don't handle edge touches immediately -- they may actually belong to one of our
            // descendants.
            return false;
        }
        if (mAdapter == null || mAdapter.getCount() == 0) {
            // Nothing to present or scroll; nothing to touch.
            return false;
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        if (adjustPostionAnim != null && adjustPostionAnim.isRunning()) {
            return false;
        }
        mVelocityTracker.addMovement(ev);
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mIsBeingDragged = false;
                mLastMotionX = mInitialMotionX = ev.getX();
                mLastMotionY = mInitialMotionY = ev.getY();
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                if (autoScrollAnim != null && autoScrollAnim.isRunning()) {
                    autoScrollAnim.cancel();
                }
                resetPosition(ev.getX());
                stop();
                break;
            }
            case MotionEvent.ACTION_MOVE:
                if (!mIsBeingDragged) {
                    final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                    if (pointerIndex == -1) {
                        resetTouch();
                        break;
                    }
                    final float x = MotionEventCompat.getX(ev, pointerIndex);
                    final float xDiff = Math.abs(x - mLastMotionX);
                    final float y = MotionEventCompat.getY(ev, pointerIndex);
                    final float yDiff = Math.abs(y - mLastMotionY);
                    if (xDiff > mTouchSlop && xDiff > yDiff) {
                        mIsBeingDragged = true;
                        mLastMotionX = x - mInitialMotionX > 0 ? mInitialMotionX + mTouchSlop :
                                mInitialMotionX - mTouchSlop;
                        mLastMotionY = y;
                        // Disallow Parent Intercept, just in case
                        requestDisallowInterceptTouchEvent(true);
                    }
                }
                if (mIsBeingDragged) {
                    final int activePointerIndex = MotionEventCompat.findPointerIndex(
                            ev, mActivePointerId);
                    final float x = MotionEventCompat.getX(ev, activePointerIndex);
                    performDrag(x);

                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mIsBeingDragged) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int initialVelocity = (int) VelocityTrackerCompat.getXVelocity(
                            velocityTracker, mActivePointerId);
                    final int activePointerIndex =
                            MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                    final float x = MotionEventCompat.getX(ev, activePointerIndex);
                    final int totalDelta = (int) (x - mInitialMotionX);
                    int nextPage = determineTargetPage(mCurrentScreen, initialVelocity,
                            totalDelta);
                    setCurrentItemInternal(nextPage, true, initialVelocity);
                    resetTouch();
                }
                break;
        }
        return true;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        return super.onSaveInstanceState();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
    }

    private void resetPosition(float x) {
        float scrollX = getScrollX();
        //如果当前屏幕是最后一个屏幕，为了防止手动拖动时拖出屏幕外把当前屏幕移到中间
        if (scrollX + getMeasuredWidth() + x > 3 * getMeasuredWidth()) {
            //最后两个视图移到最前面，第一个移到最后面
            ViewHolder view0 = viewList.remove(0);
            view0.adapterPosition = getNextAdapterPosition();
            viewList.add(view0);
            scrollBy(-getWidth(), 0);
            requestLayout();
            mAdapter.getView(viewList.get(2).adapterPosition, viewList.get(2).view, AdverTizeBanner.this);
            mCurrentScreen = 1;
            //如果当前屏幕是第一个屏幕，为了防止手动拖动时拖出屏幕外把当前屏幕移到中间，即mCurrentScreen 1
        } else if (scrollX - (getMeasuredWidth() - x) < 0) {
            mCurrentScreen = 1;
            //第三个视图加载第二个位置的内容，因为第二个视图即将换到第一个位置并且当且屏幕是第二个视图
            //把第一个视图移到第二个位置
            ViewHolder view2 = viewList.remove(2);
            view2.adapterPosition = getPreviousAdapterPosition();
            viewList.add(0, view2);
            scrollBy(getWidth(), 0);
            requestLayout();
            //第一个视图加载数据
            mAdapter.getView(viewList.get(0).adapterPosition, viewList.get(0).view, AdverTizeBanner.this);
        }
    }

    /**
     * @param nextPage
     * @param b
     * @param initialVelocity 用来计算剩余的时间以便松开时的动画效果更自然
     */
    private void setCurrentItemInternal(int nextPage, boolean b, final int initialVelocity) {
        mNextScreen = nextPage;
        final int x = nextPage * getWidth();
        double p = getScrollX() / getWidth();
        double duration = (1 - (p - (int) p)) * mDuration;
        adjustPostionAnim = ValueAnimator.ofInt(getScrollX(), x);
        adjustPostionAnim.setDuration((int) duration);
        adjustPostionAnim.setInterpolator(mAnimInterpolator);
        adjustPostionAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                scrollTo(value, getScrollY());
            }
        });
        adjustPostionAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mCurrentScreen != mNextScreen) {

                    if (mNextScreen < mCurrentScreen) {
                        mCurrentScreen = 1;
                        ViewHolder view2 = viewList.remove(2);
                        view2.adapterPosition = getPreviousAdapterPosition();
                        viewList.add(0, view2);
                        mAdapter.getView(viewList.get(0).adapterPosition, viewList.get(0).view, AdverTizeBanner.this);
                    } else if (mNextScreen > mCurrentScreen) {
                        mCurrentScreen = 1;
                        ViewHolder view0 = viewList.remove(0);
                        view0.adapterPosition = getNextAdapterPosition();
                        viewList.add(view0);
                        mAdapter.getView(viewList.get(2).adapterPosition, viewList.get(2).view, AdverTizeBanner.this);
                    }
                    int width = getWidth();
                    scrollTo(width, getScrollY());
                    requestLayout();
                }
                isCanceled = false;
                onPageChanged(viewList.get(1).adapterPosition);
                if (isAutoScroll) {
                    rotate();
                }


            }


            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        adjustPostionAnim.start();


    }

    private int determineTargetPage(int currentPage, int velocity, int deltaX) {
        int targetPage = currentPage;
        if (Math.abs(deltaX) > mFlingDistance && Math.abs(velocity) > mMinimumVelocity) {
            if (deltaX > 0) {
                targetPage = currentPage - 1;
            } else {
                if (getScrollX() > getWidth() * OVER_PAGE_FLAG) {
                    targetPage = currentPage + 1;
                }
            }
        } else {
            targetPage = currentPage;
        }
        return targetPage;
    }

    private boolean performDrag(float x) {
        final float deltaX = mLastMotionX - x;
        mLastMotionX = x;
        float oldScrollX = getScrollX();
        float scrollX = oldScrollX + deltaX;
        mLastMotionX += scrollX - (int) scrollX;
        scrollTo((int) scrollX, getScrollY());
        return true;
    }


    private boolean resetTouch() {
        mActivePointerId = INVALID_POINTER;
        mIsBeingDragged = false;
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mAdapter == null) {
            return;
        }
        for (int i = 0; i < 3; i++) {
            View temp = getViewFromPool(i);
            if (temp == null) {
                View view = mAdapter.getView(i >= mAdapter.getCount() ? 0 : i, temp, AdverTizeBanner.this);

                viewList.add(i, new ViewHolder(view, i, i >= mAdapter.getCount() ? 0 : i));
                addView(view);
                measureChildWithMargins(view);
                layoutChild(view, i, l, t, r, b);
            } else {
                layoutChild(viewList.get(i).view, i, l, t, r, b);
            }

        }
    }

    private View getViewFromPool(int position) {
        if (position < viewList.size()) {
            return viewList.get(position).view;
        }
        return null;
    }

    private void layoutChild(View view, int position, int l, int t, int r, int b) {
        Rect rf = new Rect(l, t, r, b);
        int width = rf.width();
        rf.left = rf.left + position * width;
        rf.right = rf.left + width;
        view.layout(rf.left, rf.top, rf.right, rf.bottom);
    }

    public void measureChildWithMargins(View child) {
        final int widthSpec = getChildMeasureSpec(getMeasuredWidth(),
                getPaddingLeft() + getPaddingRight());
        final int heightSpec = getChildMeasureSpec(getMeasuredHeight(),
                getPaddingTop() + getPaddingBottom());
        child.measure(widthSpec, heightSpec);
    }

    public static int getChildMeasureSpec(int parentSize, int padding) {
        int resultSize = Math.max(0, parentSize - padding);
        int resultMode = MeasureSpec.EXACTLY;
        return MeasureSpec.makeMeasureSpec(resultSize, resultMode);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    public void setAdapter(BannerAdapter adapter) {
        if (adapter != null) {
            mAdapter = adapter;
            mAdapter.registerAdapterDataObserver(mObserver);
        } else {
            throw new IllegalStateException("adapter can not be null");
        }
        requestLayout();
    }

    @Override
    public void requestLayout() {
        super.requestLayout();
    }


    private boolean isCanceled = false;


    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {

            int fromX = mCurrentScreen * getWidth();
            int toX = (mCurrentScreen + 1) * getWidth();
            autoScrollAnim = ValueAnimator.ofInt(fromX, toX);
            autoScrollAnim.setDuration(mDuration);
            autoScrollAnim.setInterpolator(mAnimInterpolator);
            autoScrollAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (Integer) animation.getAnimatedValue();
                    scrollTo(value, getScrollY());
                }
            });

            autoScrollAnim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (viewList.isEmpty()){
                        return;
                    }
                    mNextScreen = mCurrentScreen + 1;
                    if (mNextScreen > 2) {
                        mNextScreen = 0;
                    }
                    mAdapter.getView(viewList.get(mNextScreen).adapterPosition, viewList.get(mNextScreen).view, AdverTizeBanner.this);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (viewList.isEmpty()){
                        if (isAutoScroll) {
                            rotate();
                        }
                        return;
                    }
                    if (isCanceled) {
                        return;
                    }
                    mCurrentScreen = mNextScreen;
                    if (mCurrentScreen == 2) {
                        mCurrentScreen = 1;
                        ViewHolder v0 = viewList.remove(0);
                        v0.adapterPosition = getNextAdapterPosition();
                        viewList.add(v0);
                        scrollTo(getWidth(), getScrollY());
                        requestLayout();
                        mAdapter.getView(viewList.get(2).adapterPosition, viewList.get(2).view, AdverTizeBanner.this);
                    } else {

                    }
                    onPageChanged(viewList.get(1).adapterPosition);
                    if (isAutoScroll) {
                        rotate();
                    }

                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    isCanceled = true;
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            autoScrollAnim.start();
        }
    };

    private void rotate() {

        //handler.removeCallbacksAndMessages(null);
        removeCallbacks(mRunnable);
        postDelayed(mRunnable,mDelay);
        //handler.postDelayed(mRunnable, mDelay);
    }

    public void start() {
        isAutoScroll = true;
        if (isRunning) {
            return;
        }
        if (mAdapter.getCount() < 2) {
            return;
        }
        isRunning = true;
        rotate();

    }


    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    public float dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }

    private int getNextAdapterPosition() {
        int pos = viewList.get(viewList.size() - 1).adapterPosition;
        if (pos + 1 >= mAdapter.getCount()) {
            return 0;
        } else {
            return pos + 1;
        }

    }

    private int getPreviousAdapterPosition() {
        int pos = viewList.get(0).adapterPosition;
        if (pos - 1 < 0) {
            return mAdapter.getCount() - 1;
        } else {
            return pos - 1;
        }
    }


    class ViewHolder {
        public int adapterPosition = -1;
        public int viewPosition = -1;
        public View view;

        public ViewHolder(View view, int viewPosition, int adapterPosition) {
            this.adapterPosition = adapterPosition;
            this.view = view;
            this.viewPosition = viewPosition;
        }
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new LinearLayout.LayoutParams(p);
    }

    public interface OnPageChangedListener {
        void OnPageChange(int position);
    }

    private void onPageChanged(int postion) {
        if (onPageChangedListener != null) {
            onPageChangedListener.OnPageChange(postion);
        }
    }
    /**
     * 具体目标
     */
    static class AdapterDataObservable extends Observable<AdapterDataObserver> {
        public void notifyChanged() {
            synchronized (mObservers) {
                for (int i = mObservers.size() - 1; i >= 0; i--) {
                    mObservers.get(i).onChanged();
                }
            }
        }
    }

    /**
     * 观察者
     */
    public static abstract class AdapterDataObserver {
        public void onChanged() {
            // Do nothing
        }

    }

    /**
     * 具体观察者
     */
    private class BannerDataObserver extends AdapterDataObserver {
        public void onChanged() {
            //handler.removeCallbacksAndMessages(null);
            removeCallbacks(mRunnable);
            mCurrentScreen = 0;
            mNextScreen = 0;
            viewList.clear();
            scrollTo(0, getScrollY());
            forceLayout();
            if (isAutoScroll) {
                isRunning = false;
                rotate();
            }

            if (onPageChangedListener != null) {
                onPageChangedListener.OnPageChange(0);
            }

        }
    }


    public static abstract class BannerAdapter {

        private final AdapterDataObservable mObservable = new AdapterDataObservable();

        public BannerAdapter() {
        }

        abstract public int getCount();

        abstract public View getView(int position, View mainView, ViewGroup parent);


        public void registerAdapterDataObserver(AdapterDataObserver observer) {
            mObservable.registerObserver(observer);
        }

        public void unregisterAdapterDataObserver(AdapterDataObserver observer) {
            mObservable.unregisterObserver(observer);
        }

        public void notifyDataSetChanged() {
            mObservable.notifyChanged();
        }

    }

}
