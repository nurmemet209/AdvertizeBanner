package com.example.nurmemet.advertizebanner;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

import java.util.ArrayList;

/**
 * Created by nurmemet on 2015/12/12.
 */
public class AdverTizeBanner extends ViewGroup {


    private BannerAdapter mAdapter;
    private int mCurrentLayoutPosition = -1;
    private Scroller mScroller;
    int mTouchSlop;
    int mMaximumVelocity;
    private int mDuration = 2000;
    private Handler handler;
    private int mCurrentScreen;
    private int mNextScreen;
    private int mLastScrollDirection;
    //布局孩子节点的空间的大小
    private Rect availableSpace = new Rect();
    private boolean isAutoScroll;

    private ArrayList<View> viewList = new ArrayList<View>();
    private OnSelectedChanged onSelectedChanged;

    public void SetOnSelectedChanged(OnSelectedChanged onSelectedChanged) {
        this.onSelectedChanged = onSelectedChanged;
    }

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
        mScroller = new Scroller(getContext());
        final ViewConfiguration configuration = ViewConfiguration
                .get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();


    }

    public void setDuration(int duration) {
        mDuration = duration;
    }

    public void statrtAutoPlay() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                snapToScreen();
                Message message = handler.obtainMessage(0);
                sendMessageDelayed(message, mDuration);
            }
        };

        Message message = handler.obtainMessage(0);
        handler.sendMessageDelayed(message, mDuration);
        isAutoScroll=true;
    }

    private int calculateCurrentScreen(){
        int x=getScrollX();
        int screen=x/getWidth();
        return screen;
    }

    public void snapToScreen() {
        if (!mScroller.isFinished())
            return;
        if (mAdapter.getCount() > 1) {
            if (mCurrentScreen + 1 < mAdapter.getCount()) {
                mCurrentScreen=calculateCurrentScreen();
                mNextScreen = mCurrentScreen+1;
                mLastScrollDirection = mNextScreen - mCurrentScreen;
                final int newX = mNextScreen * getWidth();
                final int delta = newX - getScrollX();
                mScroller.startScroll(getScrollX(), 0, delta, 0, Math.abs(delta) * 2);
                invalidate();
            } else {
                mCurrentScreen = mAdapter.getCount()-1;
                mNextScreen = 0;
                mLastScrollDirection = mNextScreen - mCurrentScreen;
                final int delta = -mCurrentScreen * getWidth();
                mScroller.startScroll(getScrollX(), 0, delta, 0, Math.abs(delta) * 2);
                invalidate();

            }

        }

    }

    @Override
    public void computeScroll() {
        if(isAutoScroll){
            if (mScroller.computeScrollOffset()) {
                int desX = mScroller.getCurrX();
                int desY = getScrollY();
                scrollTo(desX, desY);
                invalidate();
            } else if (mNextScreen == -1) {
                mCurrentScreen = mAdapter.getCount() - 1;
            } else {
                mCurrentScreen = getScrollX() / getWidth();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mCurrentScreen + 1 == mNextScreen || mCurrentScreen == mAdapter.getCount() - 1) {
                            if (onSelectedChanged != null) {
                                onSelectedChanged.OnSelectedChanged(mCurrentScreen, mNextScreen);
                            }
                        }
                    }
                });
            }
        }

    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {


        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean b=super.dispatchTouchEvent(ev);
        System.out.println("dispatch");
        return b;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        int maskAction=event.getActionMasked();


        switch (maskAction){
            case MotionEvent.ACTION_DOWN:
                //Toast.makeText(this.getContext(),"第一次按下",Toast.LENGTH_SHORT).show();
                System.out.println("第一次按下");
                break;
            case MotionEvent.ACTION_UP:
                //Toast.makeText(this.getContext(),"手指松开",Toast.LENGTH_SHORT).show();
                System.out.println("手指松开");
                break;
            case MotionEvent.ACTION_MOVE:
                //Toast.makeText(this.getContext(),"手指一动",Toast.LENGTH_SHORT).show();
                System.out.println("手指一动");
                break;
            case MotionEvent.ACTION_CANCEL:
                //Toast.makeText(this.getContext(),"取消",Toast.LENGTH_SHORT).show();
                System.out.println("取消");
                break;
            case MotionEvent.ACTION_OUTSIDE:
                //Toast.makeText(this.getContext(),"ui之外的触摸事件",Toast.LENGTH_SHORT).show();
                System.out.println("ui之外的触摸事件");
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
               // Toast.makeText(this.getContext(),"单手指按下",Toast.LENGTH_SHORT).show();
                System.out.println("单手指按下");
                break;
            case MotionEvent.ACTION_POINTER_UP:
                //Toast.makeText(this.getContext(),"单手指松开",Toast.LENGTH_SHORT).show();
                System.out.println("单手指松开");
                break;
            case MotionEvent.ACTION_HOVER_MOVE:
                //Toast.makeText(this.getContext(),"滑过",Toast.LENGTH_SHORT).show();
                System.out.println("滑过");
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                //Toast.makeText(this.getContext(),"滑过离开",Toast.LENGTH_SHORT).show();
                System.out.println("滑过离开");
                break;

            case MotionEvent.ACTION_HOVER_ENTER:
                //Toast.makeText(this.getContext(),"滑过进入",Toast.LENGTH_SHORT).show();
                System.out.println("滑过进入");
                break;
            case MotionEvent.ACTION_POINTER_INDEX_SHIFT:
               // Toast.makeText(this.getContext(),"手指移动",Toast.LENGTH_SHORT).show();
                System.out.println("手指移动");
                break;

        }
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        dispatchLayout();
    }

    private void dispatchLayout() {
        fill();
    }

    private void fill() {
        if (mAdapter == null) {
            throw new IllegalStateException("adapter can not be null");
        }
        //此操作不会导致requestLayout
        detachAllViewsFromParent();
        //更新布局空间
        updateAvailableSpace();
        mCurrentLayoutPosition = -1;
        int count = mAdapter.getCount();
        for (int i = 0; i < count; i++) {
            mCurrentLayoutPosition = i;
            View temp = getViewFromPool(i);
            View view = mAdapter.getView(mCurrentLayoutPosition, temp);
            if (temp == null) {
                viewList.add(i, view);
            }
            addView(view);
            measureChildWithMargins(view, 0, 0);
            layoutChild(view, mCurrentLayoutPosition);
        }
        removeScrappedViews();


    }

    private void updateAvailableSpace() {
        availableSpace.left = getLeft() + getPaddingLeft();
        availableSpace.top = getTop() + getPaddingTop();
        availableSpace.right = getRight() - getPaddingRight();
        availableSpace.bottom = getBottom() - getPaddingBottom();
    }

    private void removeScrappedViews() {
        for (int i = mAdapter.getCount(); i < viewList.size(); i++) {
            removeDetachedView(viewList.get(i), false);
        }
    }


    private View getViewFromPool(int position) {
        if (position < viewList.size()) {
            return viewList.get(position);
        }
        return null;
    }

    private void layoutChild(View view, int position) {
        Rect rf = new Rect(availableSpace);
        int width = rf.width();
        rf.left = rf.left + position * (rf.right - rf.left);
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

        abstract public View getView(int position, View mainView);

        public void notifyDataSetChannged() {
        }

    }

    public static interface OnSelectedChanged {
        public void OnSelectedChanged(int oldPosition, int newPosition);
    }


}
