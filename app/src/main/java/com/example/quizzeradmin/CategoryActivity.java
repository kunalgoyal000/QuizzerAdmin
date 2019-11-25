package com.example.quizzeradmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CategoryActivity extends AppCompatActivity {

    // Write a message to the database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    private List<CategoryModel> list;
    private CategoryAdapter adapter;


    private RecyclerView recyclerView;
    private Dialog loadingDialog, categoryDialog;
    private CircleImageView addImageView;
    private EditText categoryName;
    private Button addBtn;
    private Uri image = null;
    private String downloadUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);


        /////////// Loading Dialog

        loadingDialog = new Dialog(CategoryActivity.this);
        loadingDialog.setContentView(R.layout.loading);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_corners));

        //////////// Loading Dialog

        setCategoryDailog();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle("Categories");


        recyclerView = findViewById(R.id.rv);
        list = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);

        recyclerView.setLayoutManager(layoutManager);

        adapter = new CategoryAdapter(list, new CategoryAdapter.DeleteListener() {

            @Override
            public void onDelete(final String key, final int position) {

                new AlertDialog.Builder(CategoryActivity.this, R.style.Theme_AppCompat_Light_Dialog)
                        .setTitle("Delete Category")
                        .setMessage("Are you sure, you want to delete this category?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                loadingDialog.show();
                                myRef.child("Categories").child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            myRef.child("SETS").child(list.get(position).getName()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        list.remove(position);
                                                        adapter.notifyDataSetChanged();
                                                    } else {
                                                        Toast.makeText(CategoryActivity.this, "Failed to delete!!", Toast.LENGTH_SHORT).show();
                                                    }
                                                    loadingDialog.dismiss();

                                                }
                                            });

                                        } else {
                                            Toast.makeText(CategoryActivity.this, "Failed to delete!!", Toast.LENGTH_SHORT).show();
                                            loadingDialog.dismiss();

                                        }
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });

        recyclerView.setAdapter(adapter);

        loadingDialog.show();
        myRef.child("Categories").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    list.add(new CategoryModel(dataSnapshot1.child("name").getValue().toString()
                            , Integer.parseInt(dataSnapshot1.child("sets").getValue().toString())
                            , dataSnapshot1.child("url").getValue().toString()
                            , dataSnapshot1.getKey()));
                }
                adapter.notifyDataSetChanged();
                loadingDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CategoryActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
                finish();
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main, menu);


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.add) {
            categoryDialog.show();
            return true;
        }else if(item.getItemId() == R.id.sign_out){
            new AlertDialog.Builder(CategoryActivity.this, R.style.Theme_AppCompat_Light_Dialog)
                    .setTitle("Logout")
                    .setMessage("Are you sure, you want to logout?")
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            loadingDialog.show();
                            FirebaseAuth.getInstance().signOut();
                            Intent registerIntent = new Intent(CategoryActivity.this, MainActivity.class);
                            startActivity(registerIntent);
                            finish();
                        }
                    })
                    .setNegativeButton("NO", null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show(); }

        return super.onOptionsItemSelected(item);
    }

    private void setCategoryDailog() {

        categoryDialog = new Dialog(CategoryActivity.this);
        categoryDialog.setContentView(R.layout.add_category_dialog);
        categoryDialog.setCancelable(true);
        categoryDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        categoryDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_box));

        addImageView = categoryDialog.findViewById(R.id.placeholder);
        categoryName = categoryDialog.findViewById(R.id.categoryName);
        addBtn = categoryDialog.findViewById(R.id.add_btn);

        addImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (CategoryActivity.this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
                        galleryIntent.setType("image/*");
                        startActivityForResult(galleryIntent, 101);
                    } else {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
                    }
                } else {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK);
                    galleryIntent.setType("image/*");
                    startActivityForResult(galleryIntent, 101);
                }

            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (categoryName.getText().toString().isEmpty()) {
                    categoryName.setError("Required");
                    return;
                }

                for (CategoryModel model : list) {

                    if (categoryName.getText().toString().toLowerCase().equals(model.getName().toLowerCase())) {
                        categoryName.setError("Category name already present!!");
                        return;
                    }
                }
                uploadData();
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101) {
            if (resultCode == RESULT_OK) {

                image = data.getData();
                addImageView.setImageURI(image);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 2) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, 101);
            } else {
                Toast.makeText(CategoryActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void uploadData() {

        if (image != null) {

            loadingDialog.show();
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();

            final StorageReference imageReference = storageReference.child("categories").child(image.getLastPathSegment());


            UploadTask uploadTask = imageReference.putFile(image);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return imageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                downloadUrl = task.getResult().toString();
                                uploadCategoryName();
                            } else {
                                loadingDialog.show();
                                Toast.makeText(CategoryActivity.this, "Something went wrong!!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();

                    } else {
                        Toast.makeText(CategoryActivity.this, "Something went wrong!!", Toast.LENGTH_SHORT).show();
                        loadingDialog.show();

                    }
                }
            });
        } else {
            Toast.makeText(this, "Please select image to proceed", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadCategoryName() {

        Map<String, Object> map = new HashMap<>();

        map.put("name", categoryName.getText().toString());
        map.put("sets", 0);
        map.put("url", downloadUrl);

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        database.getReference().child("Categories").child("Category" + (list.size() + 1)).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    list.add(new CategoryModel(categoryName.getText().toString(), 0, downloadUrl, "Category" + (list.size() + 1)));
                    adapter.notifyDataSetChanged();
                    categoryDialog.dismiss();
                    categoryName.setText(null);
                    addImageView.setImageDrawable(getDrawable(R.drawable.placeholder));
                    image = null;
                } else {
                    Toast.makeText(CategoryActivity.this, "Something went wrong!!", Toast.LENGTH_SHORT).show();
                }
                loadingDialog.dismiss();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.notifyDataSetChanged();

    }
}
