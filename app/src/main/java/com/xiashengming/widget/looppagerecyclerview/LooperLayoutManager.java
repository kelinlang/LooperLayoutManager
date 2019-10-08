package com.xiashengming.widget.looppagerecyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.xiashengming.widget.MponLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LooperLayoutManager extends RecyclerView.LayoutManager {
    private static final String TAG = "LooperLayoutManager";
    private boolean looperEnable = true;

    private int rowNum = 3;

//    private Map<Integer,Integer> leftPosMap = new HashMap<>();
//    private Map<Integer,Integer> rightPosMap = new HashMap<>();
    private List<Integer> drawPostionCache = new ArrayList<>();

    private Map<Integer,List<Integer>> posMap = new HashMap<>();
    private Map<Integer,List<Integer>> posChildMap = new HashMap<>();

    private int totalPos;

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

        posMap.put(0,new ArrayList<Integer>());
        posMap.put(1,new ArrayList<Integer>());
        posMap.put(2,new ArrayList<Integer>());

        posChildMap.put(0,new ArrayList<Integer>());
        posChildMap.put(1,new ArrayList<Integer>());
        posChildMap.put(2,new ArrayList<Integer>());

        int tmpNum = 0;

        int autualWidth = 0;
        int autualHeight = 0;
        MponLog.d("getWidth : "+ getWidth());
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
            }else {
                autualHeight += height;
                tmpNum++;
                totalPos++;
            }
        }

        looperLeftStartPos = 0;
        looperRightEndPos = getItemCount()-1;
        MponLog.d("totalPos : "+ totalPos+   " getChildCount : "+ getChildCount());
    }


    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        //1.左右滑动的时候，填充子view
//        MponLog.d("scrollHorizontallyBy dx : "+ dx);
        int travl = 0;
        try {

//            recyclerHideViewNew(dx, recycler, state);
            travl = fillNew(dx, recycler, state);
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
//            recyclerHideView(dx, recycler, state);
        }catch (Exception e){
            MponLog.e("",e);
        }
        return travl;
    }

    private int looperLeftStartPos =0;
    private int looperRightEndPos =0;

    private int fillNew(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {

        if (dx > 0) {
            //标注1.向左滚动
            int lastPos = getPosition(getChildAt(posChildMap.get(2).get(posChildMap.get(2).size()-1)));//右下角是最后一个决定最后一个位置
            MponLog.d("lastPos : "+lastPos);
            for (int j = 0;j < rowNum;j++){
                View lastView = getChildAt(posChildMap.get(j).get(posChildMap.get(j).size()-1));
                if (lastView == null) {
                    MponLog.d("fillNew 1");
                    return 0;
                }
                //标注2.可见的最后一个itemView完全滑进来了，需要补充新的
                if (lastView.getRight() < getWidth()) {
                    View scrap = null;
                    //标注3.判断可见的最后一个itemView的索引，
                    // 如果是最后一个，则将下一个itemView设置为第一个，否则设置为当前索引的下一个
                    int getViewPosition;
                    if (lastPos == getItemCount() - 1) {
                        if (looperEnable) {
                            MponLog.d("looperLeftStartPos : "+looperLeftStartPos);
                            getViewPosition = looperLeftStartPos++;
                            MponLog.d("getViewPosition : "+getViewPosition);
                            scrap = recycler.getViewForPosition(getViewPosition);
                            posChildMap.get(j).add(getViewPosition);
                        } else {
                            dx = 0;
                        }
                    } else {
                        getViewPosition = lastPos + 1;
                        lastPos++;

                        scrap = recycler.getViewForPosition(getViewPosition);
                        posChildMap.get(j).add(getViewPosition);
                        MponLog.d("getViewPosition 1 : "+getViewPosition);
                    }

                    //标注4.将新的itemViewadd进来并对其测量和布局
                    if (scrap != null){
                        MponLog.d("add 1 : ");
                        addView(scrap);
                        measureChildWithMargins(scrap, 0, 0);
                        int width = getDecoratedMeasuredWidth(scrap);
                        int height = getDecoratedMeasuredHeight(scrap);

                        layoutDecorated(scrap,lastView.getRight(), 0+j*height, lastView.getRight() + width, height+j*height);
                    }
                }
            }
            return dx;
        } else {
            //向右滚动
            int firstPos = getPosition(getChildAt(posChildMap.get(0).get(posChildMap.get(0).size()-1)));//左上角是第一个决定第一个位置
            for (int j = 0;j < rowNum;j++){
                View firstView = getChildAt(posChildMap.get(j).get(0));
                if (firstView == null) {
                    MponLog.d("fillNew 2");
                    return 0;
                }


                if (firstView.getLeft() >= 0) {
                    View scrap = null;
                    int getViewPosition;
                    if (firstPos == 0) {
                        if (looperEnable) {
                            getViewPosition = looperRightEndPos--;
                            scrap = recycler.getViewForPosition(getViewPosition);
                            posChildMap.get(j).add(0,getViewPosition);
                        } else {
                            dx = 0;
                        }
                    } else {
                        getViewPosition = firstPos-1;
                        scrap = recycler.getViewForPosition(getViewPosition);
                        posChildMap.get(j).add(0,getViewPosition);
                    }
                    if (scrap != null) {
                        addView(scrap, 0);
                        measureChildWithMargins(scrap,0,0);
                        int width = getDecoratedMeasuredWidth(scrap);
                        int height = getDecoratedMeasuredHeight(scrap);
                        layoutDecorated(scrap, firstView.getLeft() - width, 0+j*height, firstView.getLeft(), height+j*height);
                    }
                }
            }
            return dx;
            }
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
            //标注2.可见的最后一个itemView完全滑进来了，需要补充新的
            if (lastView.getRight() < getWidth()) {
                View scrap = null;
                //标注3.判断可见的最后一个itemView的索引，
                // 如果是最后一个，则将下一个itemView设置为第一个，否则设置为当前索引的下一个
                if (lastPos == getItemCount() - 1) {
                    if (looperEnable) {
                        scrap = recycler.getViewForPosition(0);
                    } else {
                        dx = 0;
                    }
                } else {
                    scrap = recycler.getViewForPosition(lastPos + 1);
                }
                if (scrap == null) {
                    return dx;
                }
                //标注4.将新的itemViewadd进来并对其测量和布局
                addView(scrap);
                measureChildWithMargins(scrap, 0, 0);
                int width = getDecoratedMeasuredWidth(scrap);
                int height = getDecoratedMeasuredHeight(scrap);
                layoutDecorated(scrap,lastView.getRight(), 0,
                        lastView.getRight() + width, height);
                return dx;
            }
        } else {
            //向右滚动
            View firstView = getChildAt(0);
            if (firstView == null) {
                return 0;
            }
            int firstPos = getPosition(firstView);

            if (firstView.getLeft() >= 0) {
                View scrap = null;
                if (firstPos == 0) {
                    if (looperEnable) {
                        scrap = recycler.getViewForPosition(getItemCount() - 1);
                    } else {
                        dx = 0;
                    }
                } else {
                    scrap = recycler.getViewForPosition(firstPos - 1);
                }
                if (scrap == null) {
                    return 0;
                }
                addView(scrap, 0);
                measureChildWithMargins(scrap,0,0);
                int width = getDecoratedMeasuredWidth(scrap);
                int height = getDecoratedMeasuredHeight(scrap);
                layoutDecorated(scrap, firstView.getLeft() - width, 0,
                        firstView.getLeft(), height);
            }
        }
        return dx;
    }


    /**
     * 回收界面不可见的view
     */
    private void recyclerHideViewNew(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (dx>0){//向左
            MponLog.d("循环: 移除 前  childCount=" + getChildCount());
            for (int j = 0;j < rowNum;j++){
                View view = getChildAt(posChildMap.get(j).get(0));
                if (view != null && view.getRight() < 0){
                    removeAndRecycleView(view, recycler);
                    posChildMap.get(j).remove(0);
                }
            }
            MponLog.d("循环: 移除 后  childCount=" + getChildCount());
        }else {//向右
            MponLog.d("循环: 移除 前  childCount=" + getChildCount());
            for (int j = 0;j < rowNum;j++){
                View view = getChildAt(posChildMap.get(j).get(posChildMap.get(j).size()-1));
                if (view != null && view.getLeft() > getWidth()){
                    removeAndRecycleView(view, recycler);
                    posChildMap.get(j).remove(posChildMap.get(j).size()-1);
                }
            }
            MponLog.d("循环: 移除 后  childCount=" + getChildCount());
        }
    }




    /**
     * 回收界面不可见的view
     */
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
                    removeAndRecycleView(view, recycler);
                    posChildMap.get(0).remove(new Integer(position));
                    posChildMap.get(1).remove(new Integer(position));
                    posChildMap.get(2).remove(new Integer(position));
                    MponLog.d("循环: 移除 一个view  childCount=" + getChildCount()+ ", position : "+position);
                }
            } else {
                //向右滚动，移除一个右边不在内容里的view
                if (view.getLeft() > getWidth()) {
                    removeAndRecycleView(view, recycler);
                    posChildMap.get(0).remove(new Integer(position));
                    posChildMap.get(1).remove(new Integer(position));
                    posChildMap.get(2).remove(new Integer(position));
                    MponLog.d("循环: 移除 一个view  childCount=" + getChildCount()+ ", position : "+position);
                }
            }
        }

    }


  /*  private void recyclerHideView(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view == null) {
                continue;
            }
            int position = getPosition(view);

            if (dx > 0) {
                //向左滚动，移除一个左边不在内容里的view
                if (view.getRight() < 0) {
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

    }*/
}
