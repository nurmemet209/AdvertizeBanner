package com.example.nurmemet.advertizebanner;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import java.util.ArrayList;

/**
 * Created by nurmemet on 2015/12/12.
 */
public class AdverTizeBanner extends ViewGroup {
    private int ANIM_DURATION = 700;
    private Interpolator animInterpolator = new DecelerateInterpolator();
    private static final int MIN_DISTANCE_FOR_FLING = 25; // dips
    private static final int MIN_FLING_VELOCITY = 400; // dips
    private int mFlingDistance;
    private int mMinimumVelocity;
    private static final String TAG = "ViewPager";
    private static final boolean DEBUG = false;
    private int viewNum = 3;
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
    private int mDuration = 700;
    private Handler handler;
    private int mCurrentScreen;
    private int mNextScreen;
    //private int mAdapterPosition = 0;
    //布局孩子节点的空间的大小
    private Rect availableSpace = new Rect();
    private ArrayList<ViewHolder> viewList = new ArrayList<ViewHolder>();
    ValueAnimator autoScrollAnim;
    ValueAnimator adjustPostionAnim;
    private static float OVER_PAGE_FLAG = 0.9f;


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

    private void init() {

        final ViewConfiguration configuration = ViewConfiguration
                .get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        handler = new Handler();

        mFlingDistance = (int) dp2px(getContext(), MIN_DISTANCE_FOR_FLING);
        mMinimumVelocity = (int) dp2px(getContext(), MIN_FLING_VELOCITY);
    }


    public void setDuration(int duration) {
        mDuration = duration;
    }


    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;

        // Always take care of the touch gesture being complete.
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            // Release the drag.
            if (DEBUG) Log.v(TAG, "Intercept done!");
            resetTouch();
            return false;
        }

        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean b = super.dispatchTouchEvent(ev);
        //System.out.println("dispatch");
        return b;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

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
                mLastMotionX = mInitialMotionX = ev.getX();
                mLastMotionY = mInitialMotionY = ev.getY();
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                if (autoScrollAnim != null && autoScrollAnim.isRunning()) {
                    autoScrollAnim.cancel();

                }
                resetPosition(ev.getX());
                handler.removeCallbacksAndMessages(null);
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
                        requestParentDisallowInterceptTouchEvent(true);
                    }
                }
                if (mIsBeingDragged) {
                    final int activePointerIndex = MotionEventCompat.findPointerIndex(
                            ev, mActivePointerId);
                    final float x = MotionEventCompat.getX(ev, activePointerIndex);
                    performDrag(x);

                }


                break;
            case MotionEvent.ACTION_UP:
                if (true) {
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
            case MotionEvent.ACTION_CANCEL:
                System.out.println("canceled");
                if (mIsBeingDragged) {
                    int nextPage = determineTargetPage(mCurrentScreen, 0,
                            0);
                    setCurrentItemInternal(nextPage, true, 0);
                    resetTouch();

                }
                break;
//            case MotionEventCompat.ACTION_POINTER_DOWN: {
//                final int index = MotionEventCompat.getActionIndex(ev);
//                final float x = MotionEventCompat.getX(ev, index);
//                mLastMotionX = x;
//                mActivePointerId = MotionEventCompat.getPointerId(ev, index);
//                break;
//            }
//            case MotionEventCompat.ACTION_POINTER_UP:
//                onSecondaryPointerUp(ev);
//                mLastMotionX = MotionEventCompat.getX(ev,
//                        MotionEventCompat.findPointerIndex(ev, mActivePointerId));
//                break;
//        }
//        if (needsInvalidate) {
//            ViewCompat.postInvalidateOnAnimation(this);
//        }
        }
        return true;

//        switch (maskAction) {
//            case MotionEvent.ACTION_DOWN:
//                //Toast.makeText(this.getContext(),"第一次按下",Toast.LENGTH_SHORT).show();
//                System.out.println("第一次按下");
//                break;
//            case MotionEvent.ACTION_UP:
//                //Toast.makeText(this.getContext(),"手指松开",Toast.LENGTH_SHORT).show();
//                System.out.println("手指松开");
//                break;
//            case MotionEvent.ACTION_MOVE:
//                //Toast.makeText(this.getContext(),"手指一动",Toast.LENGTH_SHORT).show();
//                System.out.println("手指一动");
//                break;
//            case MotionEvent.ACTION_CANCEL:
//                //Toast.makeText(this.getContext(),"取消",Toast.LENGTH_SHORT).show();
//                System.out.println("取消");
//                break;
//            case MotionEvent.ACTION_OUTSIDE:
//                //Toast.makeText(this.getContext(),"ui之外的触摸事件",Toast.LENGTH_SHORT).show();
//                System.out.println("ui之外的触摸事件");
//                break;
//            case MotionEvent.ACTION_POINTER_DOWN:
//                // Toast.makeText(this.getContext(),"单手指按下",Toast.LENGTH_SHORT).show();
//                System.out.println("单手指按下");
//                break;
//            case MotionEvent.ACTION_POINTER_UP:
//                //Toast.makeText(this.getContext(),"单手指松开",Toast.LENGTH_SHORT).show();
//                System.out.println("单手指松开");
//                break;
//            case MotionEvent.ACTION_HOVER_MOVE:
//                //Toast.makeText(this.getContext(),"滑过",Toast.LENGTH_SHORT).show();
//                System.out.println("滑过");
//                break;
//            case MotionEvent.ACTION_HOVER_EXIT:
//                //Toast.makeText(this.getContext(),"滑过离开",Toast.LENGTH_SHORT).show();
//                System.out.println("滑过离开");
//                break;
//
//            case MotionEvent.ACTION_HOVER_ENTER:
//                //Toast.makeText(this.getContext(),"滑过进入",Toast.LENGTH_SHORT).show();
//                System.out.println("滑过进入");
//                break;
//            case MotionEvent.ACTION_POINTER_INDEX_SHIFT:
//                // Toast.makeText(this.getContext(),"手指移动",Toast.LENGTH_SHORT).show();
//                System.out.println("手指移动");
//                break;
//
////        }
//        return true;
    }

    private void resetPosition(float x) {
        float scrollX = getScrollX();
        //如果当前屏幕是最后一个屏幕，为了防止手动拖动时拖出屏幕外把当前屏幕移到中间
        if (scrollX + getMeasuredWidth() + x > viewNum * getMeasuredWidth()) {
            //最后两个视图移到最前面，第一个移到最后面
            ViewHolder view0 = viewList.remove(0);
            view0.adapterPosition = getNextAdapterPosition();
            viewList.add(view0);
            scrollBy(-getWidth(), 0);
            requestLayout();
            mAdapter.getView(viewList.get(2).adapterPosition, viewList.get(2).view, 2);
            mCurrentScreen = 1;
            //setData(true);
            //
            //
            //
            // mAdapter.getView(viewList.get(1).adapterPosition, viewList.get(1).view, 1);
            //如果当前屏幕是第一个屏幕，为了防止手动拖动时拖出屏幕外把当前屏幕移到中间，即mCurrentScreen 1
        } else if (scrollX - (getMeasuredWidth() - x) < 0) {
            mCurrentScreen = 1;
            //第三个视图加载第二个位置的内容，因为第二个视图即将换到第一个位置并且当且屏幕是第二个视图
            //mAdapter.getView(mAdapterPosition, viewList.get(mCurrentScreen + 1).view, -1);
            //把第一个视图移到第二个位置
            ViewHolder view2 = viewList.remove(2);
            view2.adapterPosition = getPreviousAdapterPosition();
            viewList.add(0, view2);
            scrollBy(getWidth(), 0);
            requestLayout();
            //第一个视图加载数据
            mAdapter.getView(viewList.get(0).adapterPosition, viewList.get(0).view, 0);
            //setData(false);
        } else {

        }


        System.out.println("current_page   vvvvvvvvvvvvvvvvvv"+mCurrentScreen);
    }

    /**
     * @param isNext 加载后一个视图的数据还是前一个视图的数据
     */
//    private void setData(boolean isNext) {
//        if (isNext) {
//            mAdapter.getView(mAdapterPosition + 1 < mAdapter.getCount() ? mAdapterPosition + 1 : 0, viewList.get(mCurrentScreen + 1).view, -1);
//        } else {
//            mAdapter.getView(mAdapterPosition - 1 > 0 ? mAdapterPosition - 1 : mAdapter.getCount() - 1, viewList.get(mCurrentScreen - 1).view, -1);
//        }
//    }


    /**
     * @param nextPage
     * @param b
     * @param initialVelocity 用来计算剩余的时间以便松开时的动画效果更自然
     */
    private void setCurrentItemInternal(int nextPage, boolean b, final int initialVelocity) {
        mNextScreen = nextPage;
        final int x = nextPage * getWidth();
        double p = getScrollX() / getWidth();
        double duration = (1 - (p - (int) p)) * ANIM_DURATION;
        adjustPostionAnim = ValueAnimator.ofInt(getScrollX(), x);
        adjustPostionAnim.setDuration((int) duration);
        adjustPostionAnim.setInterpolator(animInterpolator);
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
                //int position = (Integer) viewList.get(1).getTag();

//                if (initialVelocity < 0) {
//                    if (mNextScreen != mCurrentScreen) {
//                        mAdapterPosition++;
//                        if (mAdapterPosition >= mAdapter.getCount()) {
//                            mAdapterPosition = 0;
//                        }
//                    }
//                    if (position + 1 < mAdapter.getCount()) {
//                        mAdapterPosition = position + 1;
//                    } else {
//                        mAdapterPosition = 0;
//                    }
//                } else {
//                    if (mCurrentScreen != mNextScreen) {
//                        mAdapterPosition--;
//                        if (mAdapterPosition < 0) {
//                            mAdapterPosition = mAdapter.getCount() - 1;
//                        }
//                    }
//                    if (position == 0) {
//                        mAdapterPosition = mAdapter.getCount() - 1;
//                    } else {
//                        mAdapterPosition = position - 1;
//                    }
             //   }


            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //int cur = (int) (getScrollX() / getWidth() + 0.5F);
                if (mCurrentScreen != mNextScreen) {

                    if (mNextScreen < mCurrentScreen) {
                        mCurrentScreen = 1;
                        ViewHolder view2 = viewList.remove(2);
                        view2.adapterPosition = getPreviousAdapterPosition();
                        viewList.add(0, view2);
                        mAdapter.getView(viewList.get(0).adapterPosition, viewList.get(0).view, 0);
                    } else if (mNextScreen > mCurrentScreen) {
                        mCurrentScreen = 1;
                        ViewHolder view0 = viewList.remove(0);
                        view0.adapterPosition = getNextAdapterPosition();
                        viewList.add(view0);
                        mAdapter.getView(viewList.get(2).adapterPosition, viewList.get(2).view, 2);
                    }
                    int width=getWidth();
                    scrollTo(width, getScrollY());
                    requestLayout();

                    //


//                    if (mCurrentScreen == 2 || mCurrentScreen == 0) {
//                        mCurrentScreen = 1;
//                        mAdapter.getView(viewList.get(mCurrentScreen).adapterPosition, viewList.get(mCurrentScreen).view, mCurrentScreen);
//                        mAdapter.getView(mAdapterPosition == 0 ? mAdapter.getCount() - 1 : mAdapterPosition - 1, viewList.get(mCurrentScreen - 1).view, mCurrentScreen - 1);
//                        mAdapter.getView(mAdapterPosition == mAdapter.getCount() - 1 ? 0 : mAdapterPosition + 1, viewList.get(mCurrentScreen + 1).view, mCurrentScreen + 1);
//
//
//                    }
                }
                isCanceled = false;
                start();
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
        System.out.println("next_page   "+targetPage);
        System.out.println("current_page   "+mCurrentScreen);
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

    private void requestParentDisallowInterceptTouchEvent(boolean disallowIntercept) {
        final ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mAdapter == null) {
            throw new IllegalStateException("adapter can not be null");
        }
        //此操作不会导致requestLayout
        //detachAllViewsFromParent();
        //更新布局空间
        updateAvailableSpace();
        //mCurrentLayoutPosition = -1;
        int count = mAdapter.getCount();
        for (int i = 0; i < viewNum; i++) {
            View temp = getViewFromPool(i);
            if (temp == null) {
                View view = mAdapter.getView(i, temp, i);

                viewList.add(i, new ViewHolder(view, i, i));
                addView(view);
                measureChildWithMargins(view, 0, 0);
                layoutChild(view, i,l,t,r,b);
            } else {
                layoutChild(viewList.get(i).view, i,l,t,r,b);
            }

        }
    }



    private void updateAvailableSpace() {
        availableSpace.left = 0 + getPaddingLeft();
        availableSpace.top = 0 + getPaddingTop();
        availableSpace.right = availableSpace.left+getMeasuredWidth() - getPaddingRight();
        availableSpace.bottom = availableSpace.top+getMeasuredHeight()- getPaddingBottom();
    }


    private View getViewFromPool(int position) {
        if (position < viewList.size()) {
            return viewList.get(position).view;
        }
        return null;
    }

    private void layoutChild(View view, int position,int l, int t, int r, int b) {
        Rect rf = new Rect(l,t,r,b);
        int width = rf.width();
        rf.left = rf.left + position *width;
        rf.right = rf.left + width;
        view.layout(rf.left, rf.top, rf.right, rf.bottom);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    public void measureChildWithMargins(View child, int widthUsed, int heightUsed) {
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        final int widthSpec = getChildMeasureSpec(getWidth(),
                getPaddingLeft() + getPaddingRight());
        final int heightSpec = getChildMeasureSpec(getHeight(),
                getPaddingTop() + getPaddingBottom());
        child.measure(widthSpec, heightSpec);
    }

    public static int getChildMeasureSpec(int parentSize, int padding) {
        int resultSize = Math.max(0, parentSize - padding);
        int resultMode = MeasureSpec.EXACTLY;
        return MeasureSpec.makeMeasureSpec(resultSize, resultMode);
    }


    public void setAdapter(BannerAdapter adapter) {
        setAdapterInternal(adapter);
        requestLayout();
    }

    private void setAdapterInternal(BannerAdapter adapter) {
        if (adapter != null) {
            mAdapter = adapter;
        } else {
            throw new IllegalStateException("adapter can not be null");
        }
    }

    @Override
    public void requestLayout() {
        super.requestLayout();
    }


    public static abstract class BannerAdapter {
        public BannerAdapter() {
        }

        abstract public int getCount();

        abstract public View getView(int position, View mainView, int childIndex);

        public void notifyDataSetChannged() {
        }

    }

    private boolean isCanceled = false;

    public void start() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                int fromX = mCurrentScreen * getWidth();
                int toX = (mCurrentScreen + 1) * getWidth();

                autoScrollAnim = ValueAnimator.ofInt(fromX, toX);
                autoScrollAnim.setDuration(ANIM_DURATION);
                autoScrollAnim.setInterpolator(animInterpolator);
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
//                        mAdapterPosition++;
//                        if (mAdapterPosition == mAdapter.getCount()) {
//                            mAdapterPosition = 0;
//                        }

                        mNextScreen = mCurrentScreen + 1;
                        if (mNextScreen>2){
                            mNextScreen=0;
                        }
                        mAdapter.getView(viewList.get(mNextScreen).adapterPosition, viewList.get(mNextScreen).view, mNextScreen);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (isCanceled) {
                            return;
                        }
                        mCurrentScreen = mNextScreen;
                        if (mCurrentScreen == 2) {
                            mCurrentScreen = 1;
//                            mAdapter.getView(mAdapterPosition, viewList.get(mCurrentScreen), mCurrentScreen);
//                            mAdapter.getView(mAdapterPosition == 0 ? mAdapter.getCount() - 1 : mAdapterPosition - 1, viewList.get(mCurrentScreen - 1), mCurrentScreen - 1);
                            // switchView(1, 2);
                            ViewHolder v0 = viewList.remove(0);
                            v0.adapterPosition = getNextAdapterPosition();
                            viewList.add(v0);
                            scrollTo(getWidth(), getScrollY());
                            requestLayout();
                            mAdapter.getView(viewList.get(2).adapterPosition, viewList.get(2).view, 2);
                        } else {

                        }
                        start();
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
        }, 3000);


    }

    /**
     * p1>p2
     *
     * @param p1
     * @param p2
     */
    private void switchView(int p1, int p2) {
//        viewList.clear();
//        viewList.add(getChildAt(0));
//        viewList.add(getChildAt(2));
//        viewList.add(getChildAt(1));
        if (viewList != null && Math.max(p1, p2) < viewList.size()) {


            ViewHolder v2 = viewList.remove(p2);
            ViewHolder v1 = viewList.remove(p1);
            viewList.add(p1, v2);
            viewList.add(p2, v1);
        }
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
            int pos=viewList.get(viewList.size()-1).adapterPosition;
        if (pos + 1 >= mAdapter.getCount()) {
            return 0;
        } else {
            return pos + 1;
        }

    }

    private int getPreviousAdapterPosition() {
        int pos=viewList.get(0).adapterPosition;
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
}
