package com.example.multiplequiz;


import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.multiplequiz.Common.Common;
import com.example.multiplequiz.Interface.IQuestion;
import com.example.multiplequiz.Model.CurrentQuestion;
import com.example.multiplequiz.Model.Question;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class QuestionFragment extends Fragment implements IQuestion {

    TextView txt_question_text;
    CheckBox CKBA, CKBB, CKBC, CKBD;
    FrameLayout layout_image;
    ProgressBar progressBar;
    LayoutInflater inflater;

    Question question;
    int questionIndex = -1;

    public QuestionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View itemView = inflater.inflate(R.layout.fragment_question, container, false);

        //view
        CKBA = itemView.findViewById(R.id.ckbA_new);
        CKBB = itemView.findViewById(R.id.ckbB_new);
        CKBC = itemView.findViewById(R.id.ckbC_new);
        CKBD = itemView.findViewById(R.id.ckbD_new);
        txt_question_text = itemView.findViewById(R.id.txt_question_text_new);

        layout_image = itemView.findViewById(R.id.layout_image_new);
        progressBar = itemView.findViewById(R.id.progress_bar_new);

        //Get Question
        questionIndex = getArguments().getInt("index", -1);
        question = Common.questionList.get(questionIndex);

        if (question != null) {

            if (question.isImageQuestion())
            {
                ImageView img_question = itemView.findViewById(R.id.img_question_new);
                Picasso.get().load(question.getQuestionImage()).into(img_question, new Callback() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else
                layout_image.setVisibility(View.GONE);

            txt_question_text.setText(question.getQuestionText());

            CKBA.setText(question.getAnswerA());
            CKBA.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked)
                        Common.selected_values.add(CKBA.getText().toString());
                    else
                        Common.selected_values.remove(CKBA.getText().toString());
                }
            });

            CKBB.setText(question.getAnswerB());
            CKBB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked)
                        Common.selected_values.add(CKBB.getText().toString());
                    else
                        Common.selected_values.remove(CKBB.getText().toString());
                }
            });

            CKBC.setText(question.getAnswerC());
            CKBC.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked)
                        Common.selected_values.add(CKBC.getText().toString());
                    else
                        Common.selected_values.remove(CKBC.getText().toString());
                }
            });

            CKBD.setText(question.getAnswerD());
            CKBD.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked)
                        Common.selected_values.add(CKBD.getText().toString());
                    else
                        Common.selected_values.remove(CKBD.getText().toString());
                }
            });
        }

        return itemView;
    }

    @Override
    public CurrentQuestion getSelectedAnswer() {
        //this function will return state of answer
        //right, wrong, normal
        CurrentQuestion currentQuestion = new CurrentQuestion(questionIndex, Common.ANSWER_TYPE.NO_ANSWER); //default no answer
        StringBuilder result = new StringBuilder();
        if (Common.selected_values.size() > 1)
        {
            //if multichoice
            //splite answer to array
            //EX: arr[0] = A. New Yourk
            //EX: arr[1] = B. Paris

            Object[] arrayAnswer = Common.selected_values.toArray();
            for (int i = 0; i < arrayAnswer.length;i++)
            {
                if (i<arrayAnswer.length-1)
                    //Take first letter of answer: ex: arr[0] = A. Newyourk. We will take letter A
                    result.append(new StringBuilder((String)arrayAnswer[i]).substring(0, 1)).append(",");
                else
                    result.append(new StringBuilder((String)arrayAnswer[i]).substring(0, 1)); //Too
            }
        }
        else if (Common.selected_values.size() == 1)
        {
            //if only one choice
            Object[] arrayAnswer = Common.selected_values.toArray();
            result.append((String)arrayAnswer[0]).substring(0,1);
        }

        if (question != null)
        {
            //compare correctanswer with user answer
            if (!TextUtils.isEmpty(result))
            {
                if (result.toString().equals(question.getCorrectAnswer()))
                    currentQuestion.setType(Common.ANSWER_TYPE.RIGHT_ANSWER);
                else
                    currentQuestion.setType(Common.ANSWER_TYPE.WRONG_ANSWER);
            }
            else
                currentQuestion.setType(Common.ANSWER_TYPE.NO_ANSWER);
        }
        else {
            Toast.makeText(getContext(), "Cannot get question", Toast.LENGTH_SHORT).show();
            currentQuestion.setType(Common.ANSWER_TYPE.NO_ANSWER);
        }
        //Alwasy clear selected value when compared done
        Common.selected_values.clear();
        return currentQuestion;
    }

    @Override
    public void showCorrecctAnswer() {

        //bold correct answer
        //pattern: A,B
        String[] correctAnswer = question.getCorrectAnswer().split(",");
        for (String answer:correctAnswer)
        {
            if (answer.equals("A"))
            {
                CKBA.setTypeface(null, Typeface.BOLD);
                CKBA.setTextColor(Color.RED);
            }
            else if (answer.equals("B"))
            {
                CKBB.setTypeface(null, Typeface.BOLD);
                CKBB.setTextColor(Color.RED);
            }
            else if (answer.equals("C"))
            {
                CKBC.setTypeface(null, Typeface.BOLD);
                CKBC.setTextColor(Color.RED);
            }
            else if (answer.equals("D"))
            {
                CKBD.setTypeface(null, Typeface.BOLD);
                CKBD.setTextColor(Color.RED);
            }
        }
    }

    @Override
    public void disabledAnswer() {

        CKBA.setEnabled(false);
        CKBB.setEnabled(false);
        CKBC.setEnabled(false);
        CKBD.setEnabled(false);
    }

    @Override
    public void resetQuestion() {

        //enable checkbox
        CKBA.setEnabled(true);
        CKBB.setEnabled(true);
        CKBC.setEnabled(true);
        CKBD.setEnabled(true);

        //remove all selected
        CKBA.setChecked(false);
        CKBB.setChecked(false);
        CKBC.setChecked(false);
        CKBD.setChecked(false);

        //Remove all bold on text
        CKBA.setTypeface(null, Typeface.NORMAL);
        CKBA.setTextColor(Color.BLACK);
        CKBB.setTypeface(null, Typeface.NORMAL);
        CKBB.setTextColor(Color.BLACK);
        CKBC.setTypeface(null, Typeface.NORMAL);
        CKBC.setTextColor(Color.BLACK);
        CKBD.setTypeface(null, Typeface.NORMAL);
        CKBD.setTextColor(Color.BLACK);

        Common.selected_values.clear();

    }
}
