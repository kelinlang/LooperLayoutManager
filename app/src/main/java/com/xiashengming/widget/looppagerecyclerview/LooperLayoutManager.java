package com.xiashengming.widget.looppagerecyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.xiashengming.widget.MponLog;


public class LooperLayoutManager extends RecyclerView.LayoutManager {
    private static final String TAG = "LooperLayoutManager";
    private boolean looperEnable = true;

    private int rowNum = 3;



    private int curPos;
    private int curLeftPos = 0;

    public LooperLayoutManager() {}

    public void setLooperEnable(boolean looperEnable) {
        this.looperEnable = looperEnable;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public boolean canScrollVertically() {
        return false;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        MponLog.d("onLayoutChildren  ItemCount : " + getItemCount());
        if (getItemCount() <= 0) {
            return;
        }
        //preLayout主要支持动画，直接跳过
        if (state.isPreLayout()) {
            return;
        }
        //将视图分离放入scrap缓存中，以准备重新对view进行排版
        detachAndScrapAttachedViews(recycler);


        int tmpNum = 0;

        int autualWidth = 0;
        int autualHeight = 0;
        MponLog.d("getWidth : "+ getWidth());
        for (;autualWidth < getWidth();){
            for (int i = 0; i < getItemCount(); i++) {
                MponLog.d("I : "+i +" ,autualWidth : "+ autualWidth+" ,autualHeight : "+ autualHeight);
                View itemView = recycler.getViewForPosition(i);
                addView(itemView);
                measureChildWithMargins(itemView, 0, 0);


                int width = getDecoratedMeasuredWidth(itemView);
                int height = getDecoratedMeasuredHeight(itemView);

                layoutDecorated(itemView, autualWidth, 0+autualHeight, autualWidth + width, height+autualHeight);

                if (tmpNum == rowNum-1){
                    autualWidth += width;
                    autualHeight = 0;
                    tmpNum = 0;

                    if (autualWidth > getWidth()){
                        MponLog.d("break");
                        break;
                    }
//                    curPos++;
                }else {
                    autualHeight += height;
                    tmpNum++;
//                    curPos++;
                }
            }
        }


        MponLog.d("curPos : "+ curPos +   " getChildCount : "+ getChildCount());
    }


    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        //1.左右滑动的时候，填充子view
//        MponLog.d("scrollHorizontallyBy dx : "+ dx);
        int travl = 0;
        try {

            MponLog.d(" getChildCount : "+ getChildCount());
//            recyclerHideViewNew(dx, recycler, state);
            travl = fill(dx, recycler, state);
        }catch (Exception e){
            MponLog.e("",e);
        }

        if (travl == 0) {
            return 0;
        }

        //2.滚动
        offsetChildrenHorizontal(travl * -1);

        //3.回收已经离开界面的
        try {

//            recyclerHideViewNew(dx, recycler, state);
            recyclerHideView(dx, recycler, state);
        }catch (Exception e){
            MponLog.e("",e);
        }
        return travl;
    }



    /**
     * 左右滑动的时候，填充
     */
    private int fill(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (dx > 0) {
            //标注1.向左滚动
            View lastView = getChildAt(getChildCount() - 1);
            if (lastView == null) {
                return 0;
            }
            int lastPos = getPosition(lastView);
//            MponLog.d("lastPos : "+ lastPos);
            curPos = lastPos;
            //标注2.可见的最后一个itemView完全滑进来了，需要补充新的
            if (lastView.getRight() < getWidth()) {
                for (int i = 0; i< rowNum;i++){
                    View scrap = null;
                    //标注3.判断可见的最后一个itemView的索引，
                    // 如果是最后一个，则将下一个itemView设置为第一个，否则设置为当前索引的下一个
                    if (curPos == getItemCount() - 1) {
                        curPos = 0;
                        if (looperEnable) {
                            scrap = recycler.getViewForPosition(curPos);
                        } else {
                            dx = 0;
                        }
                    } else {
                        curPos++;
//                        MponLog.d("curPos : "+ curPos);
                        scrap = recycler.getViewForPosition(curPos);
                    }
                    if (scrap == null) {
                        break;
                    }
                    //标注4.将新的itemViewadd进来并对其测量和布局
                    addView(scrap);
                    measureChildWithMargins(scrap, 0, 0);
                    int width = getDecoratedMeasuredWidth(scrap);
                    int height = getDecoratedMeasuredHeight(scrap);
                    layoutDecorated(scrap,lastView.getRight(), 0 + i*height, lastView.getRight() + width, height + i *height);
                }
                return dx;
            }
        } else {
            //向右滚动
            View firstView = getChildAt(0);
            if (firstView == null) {
                return 0;
            }
            int firstPos = getPosition(firstView);
            curLeftPos = firstPos;
//            MponLog.d("firstPos : "+ firstPos);
            if (firstView.getLeft() >= 0) {
                for (int i = 0; i< rowNum;i++){
                    View scrap = null;
                    if (curLeftPos == 0) {
                        curLeftPos = getItemCount() - 1;
                        if (looperEnable) {
                            scrap = recycler.getViewForPosition(curLeftPos);
                        } else {
                            dx = 0;
                        }
                    } else {
                        curLeftPos--;
                        scrap = recycler.getViewForPosition(curLeftPos);
                    }
                    if (scrap == null) {
                        return 0;
                    }
                    addView(scrap, 0);
                    measureChildWithMargins(scrap,0,0);
                    int width = getDecoratedMeasuredWidth(scrap);
                    int height = getDecoratedMeasuredHeight(scrap);
                    layoutDecorated(scrap, firstView.getLeft() - width, 0+ (rowNum-1 -i)*height, firstView.getLeft(), height+ (rowNum-1 -i)*height);
                }
            }
        }
        return dx;
    }







    private void recyclerHideView(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view == null) {
                continue;
            }
            int position = getPosition(view);

            if (dx > 0) {
                //向左滚动，移除一个左边不在内容里的view
                if (view.getRight() < 0) {
                    MponLog.d("循环: 移除前 一个view  childCount=" + getChildCount()+ ", position : "+position);
                    removeAndRecycleView(view, recycler);
                    MponLog.d("循环: 移除 一个view  childCount=" + getChildCount()+ ", position : "+position);
                }
            } else {
                //向右滚动，移除一个右边不在内容里的view
                if (view.getLeft() > getWidth()) {
                    removeAndRecycleView(view, recycler);
                    MponLog.d("循环: 移除 一个view  childCount=" + getChildCount()+ ", position : "+position);
                }
            }
        }

    }
}
