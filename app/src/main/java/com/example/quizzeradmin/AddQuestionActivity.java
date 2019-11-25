package com.example.quizzeradmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.UUID;

public class AddQuestionActivity extends AppCompatActivity {


    private EditText question;
    private RadioGroup option;
    private LinearLayout answers;
    private Button uploadBtn;
    private String categoryName;
    private int setNo, position;
    private QuestionModel questionModel;
    private String id;
    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        /////////// Loading Dialog

        loadingDialog = new Dialog(AddQuestionActivity.this);
        loadingDialog.setContentView(R.layout.loading);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_corners));

        //////////// Loading Dialog

        question = findViewById(R.id.question);
        answers = findViewById(R.id.answers);
        option = findViewById(R.id.options);
        uploadBtn = findViewById(R.id.upload_btn);


        categoryName = getIntent().getStringExtra("categoryName");
        setNo = getIntent().getIntExtra("setNo", -1);
        position = getIntent().getIntExtra("position", -1);

        if (setNo == -1) {
            finish();
            return;
        }

        if (position != -1) {
            getSupportActionBar().setTitle("Update the question");
            uploadBtn.setText("Update");
            questionModel = QuestionsActivity.list.get(position);
            setData();
        } else {
            uploadBtn.setText("Upload");
            getSupportActionBar().setTitle("Add a new question");
        }


        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (question.getText().toString().isEmpty()) {
                    question.setError("Required");
                    return;
                }
                upload();
            }
        });
    }

    private void setData() {
        question.setText(questionModel.getQuestion());


        ((EditText) answers.getChildAt(0)).setText(questionModel.getA());
        ((EditText) answers.getChildAt(1)).setText(questionModel.getB());
        ((EditText) answers.getChildAt(2)).setText(questionModel.getC());
        ((EditText) answers.getChildAt(3)).setText(questionModel.getD());

        for (int i = 0; i < answers.getChildCount(); i++) {

            if (((EditText) answers.getChildAt(i)).getText().toString().equals(questionModel.getAnswer())) {

                RadioButton radioButton = (RadioButton) option.getChildAt(i);
                radioButton.setChecked(true);
                break;
            }

        }
    }


    private void upload() {

        int correct = -1;
        for (int i = 0; i < option.getChildCount(); i++) {

            EditText answer = (EditText) answers.getChildAt(i);
            EditText nextAnswer=(EditText) answers.getChildAt(i+1);
            if (answer.getText().toString().isEmpty()) {
                answer.setError("Required");
               return;
            }

            RadioButton radioButton = (RadioButton) option.getChildAt(i);
            if (radioButton.isChecked()) {
                correct = i;
                break;
            }
        }

        if (correct == -1) {
            Toast.makeText(this, "Please mark the correct option", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < option.getChildCount(); i++) {

            if (((EditText) answers.getChildAt(i)).getText().toString().equals(((EditText) answers.getChildAt(i + 1)).getText().toString())) {
                ((EditText) answers.getChildAt(i)).setError("Options can't be same");
                return;
            }
        }


        final HashMap<String, Object> map = new HashMap<>();

        map.put("correctANS", ((EditText) answers.getChildAt(correct)).getText().toString());
        map.put("optionA", ((EditText) answers.getChildAt(0)).getText().toString());
        map.put("optionB", ((EditText) answers.getChildAt(1)).getText().toString());
        map.put("optionC", ((EditText) answers.getChildAt(2)).getText().toString());
        map.put("optionD", ((EditText) answers.getChildAt(3)).getText().toString());
        map.put("question", question.getText().toString());
        map.put("setNo", setNo);

        if (position != -1) {
            id = questionModel.getId();
        } else {
            id = UUID.randomUUID().toString();
        }

        loadingDialog.show();
        FirebaseDatabase.getInstance().getReference()
                .child("SETS").child(categoryName).child("questions").child(id)
                .setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    questionModel = new QuestionModel(id, map.get("question").toString(), map.get("optionA").toString(), map.get("optionB").toString(), map.get("optionC").toString(), map.get("optionD").toString(), map.get("correctANS").toString(), (int) map.get("setNo"));

                    if (position != -1) {
                        QuestionsActivity.list.set(position, questionModel);

                    } else {
                        QuestionsActivity.list.add(questionModel);
                    }
                    finish();
                } else {
                    Toast.makeText(AddQuestionActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
                loadingDialog.dismiss();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
