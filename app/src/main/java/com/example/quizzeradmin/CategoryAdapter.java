package com.example.quizzeradmin;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.Viewholder> {

    private List<CategoryModel> categoryModelList;
    private DeleteListener deleteListener;

    public CategoryAdapter(List<CategoryModel> categoryModelList,DeleteListener deleteListener) {
        this.categoryModelList = categoryModelList;
        this.deleteListener=deleteListener;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item, parent, false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {


        holder.setData(categoryModelList.get(position).getUrl(), categoryModelList.get(position).getName(), categoryModelList.get(position).getSets(), categoryModelList.get(position).getKey(),position);
    }

    @Override
    public int getItemCount() {
        return categoryModelList.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {

        private CircleImageView imageview;
        private TextView textView;
        private ImageButton deleteBtn;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            imageview = itemView.findViewById(R.id.image_view);
            textView = itemView.findViewById(R.id.title);
            deleteBtn = itemView.findViewById(R.id.delete_btn);

        }

        private void setData(final String url, final String title, final int sets, final String key,final int position) {

            Glide.with(itemView.getContext()).load(url).into(imageview);
            this.textView.setText(title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent setsIntent = new Intent(itemView.getContext(), SetsActivity.class);
                    setsIntent.putExtra("title", title);
                    setsIntent.putExtra("sets", sets);
                    setsIntent.putExtra("key", key);
                    setsIntent.putExtra("position",position);
                    itemView.getContext().startActivity(setsIntent);
                }
            });

            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    deleteListener.onDelete(key,position);
                }
            });


        }
    }

    public interface DeleteListener{

        public void onDelete(String key,int position);

    }


}
