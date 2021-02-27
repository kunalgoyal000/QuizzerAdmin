package com.example.quizzeradmin;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class GridAdapter extends BaseAdapter {

    public List<String> sets;
    private String category;
    private GridListener listener;

    public GridAdapter(List<String> sets, String category, GridListener listener) {
        this.sets = sets;
        this.category = category;
        this.listener=listener;

    }

    @Override
    public int getCount() {
        return sets.size() + 1;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View convertView, final ViewGroup viewGroup) {

        View view;

        if (convertView == null) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.set_item, viewGroup, false);
        } else {
            view = convertView;
        }
        if (i == 0) {
            ((TextView) view.findViewById(R.id.textview)).setText("+");

        } else {
            ((TextView) view.findViewById(R.id.textview)).setText(String.valueOf(i));
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (i == 0) {
                    listener.addSet();
                } else {
                Intent questionIntent=new Intent(viewGroup.getContext(),QuestionsActivity.class);
                questionIntent.putExtra("category",category);
                questionIntent.putExtra("setId",sets.get(i-1));
                viewGroup.getContext().startActivity(questionIntent);

                }
            }
        });


        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(i !=0) {
                    listener.onLongClick(sets.get(i-1),i);
                }
                return false;
            }
        });
        return view;
    }

    public interface GridListener{

        public void addSet();

        void onLongClick(String setId,int position);
    }
}
