package com.xiashengming.widget.looppagerecyclerview;

//import android.support.annotation.NonNull;
//import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.xiashengming.widget.R;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter {
    ArrayList<String> data = new ArrayList<String>();

    public MyAdapter() {
        initData();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        ((MyViewHolder) holder).btn.setText(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        Button btn;
        public MyViewHolder(View itemView) {
            super(itemView);
            btn = itemView.findViewById(R.id.btn);
        }
    }

    private void initData(){
        String[] str = new String[]{"0","1","2","3","4","5","6","7"
                ,"8","9","10","11"/*,"12","13","14","15"
                ,"16","17","18","19","20","21","22","23"*/};
        for (int i = 0; i < str.length; i++) {
            data.add(i, str[i]);

        }
    }
}
