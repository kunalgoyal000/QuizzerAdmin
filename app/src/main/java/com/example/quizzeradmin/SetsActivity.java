package com.example.quizzeradmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SetsActivity extends AppCompatActivity {

    private GridView gridView;
    private GridAdapter adapter;
    private List<CategoryModel> list;
    private Dialog loadingDialog;
    private String categoryName;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sets);

        /////////// Loading Dialog

        loadingDialog = new Dialog(SetsActivity.this);
        loadingDialog.setContentView(R.layout.loading);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_corners));

        //////////// Loading Dialog

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        categoryName=getIntent().getStringExtra("title");
        getSupportActionBar().setTitle(getIntent().getStringExtra(categoryName));

        gridView = findViewById(R.id.gridview);
        list = new ArrayList<>();
        myRef=FirebaseDatabase.getInstance().getReference();

        adapter = new GridAdapter(getIntent().getIntExtra("sets", 0), getIntent().getStringExtra("title"), new GridAdapter.GridListener() {
            @Override
            public void addSet() {

                loadingDialog.show();

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                database.getReference().child("Categories").child(getIntent().getStringExtra("key")).child("sets").setValue(getIntent().getIntExtra("sets", 0) + 1).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            adapter.sets++;
                            //list.get(getIntent().getIntExtra("position",0)).setSets(adapter.sets++);
                            adapter.notifyDataSetChanged();
                        } else {

                        }
                        loadingDialog.dismiss();
                    }
                });
            }


            @Override
            public void onLongClick(final int setNo) {

                new AlertDialog.Builder(SetsActivity.this, R.style.Theme_AppCompat_Light_Dialog)
                        .setTitle("Delete SET "+setNo)
                        .setMessage("Are you sure, you want to delete this SET?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                loadingDialog.show();
                                myRef.child("SETS").child(categoryName).child("questions").orderByChild("setNo").equalTo(setNo).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                            String id = dataSnapshot1.getKey();
                                            myRef.child("SETS").child(categoryName)
                                                    .child("questions").child(id).removeValue();

                                        }
                                        adapter.sets--;
                                        loadingDialog.dismiss();
                                        adapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Toast.makeText(SetsActivity.this, "Something went wrong!!", Toast.LENGTH_SHORT).show();
                                        loadingDialog.dismiss();
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }
        });


        gridView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
