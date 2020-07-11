package com.example.multiplequiz;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.multiplequiz.Common.Common;
import com.example.multiplequiz.Common.SpaceDecoration;
import com.example.multiplequiz.DBHelper.DBHelper;
import com.example.multiplequiz.Model.CurrentQuestion;
import com.example.multiplequiz.adapter.AnswerSheetAdapter;
import com.example.multiplequiz.adapter.AnswerSheetHelperAdapter;
import com.example.multiplequiz.adapter.QuestionFragmentAdapter;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

public class QuestionActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int CODE_GET_RESULT = 9999;
    int time_play = Common.TOTAL_TIME;
    boolean isAnswerModeView = false;

    TextView txt_right_answer, txt_timer, txt_wrong_answer;

    RecyclerView answer_sheet_view;
    AnswerSheetAdapter answerSheetAdapter;
    AnswerSheetHelperAdapter answerSheetHelperAdapter;

    ViewPager viewPager;
    TabLayout tabLayout;
    DrawerLayout drawer;

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(gotoQuestionNum);
        if (Common.countDownTimer != null)
            Common.countDownTimer.cancel();
        if(Common.fragmentList != null)
            Common.fragmentList.clear();
        if (Common.answerSheetList != null)
            Common.answerSheetList.clear();
        super.onDestroy();
    }

    BroadcastReceiver gotoQuestionNum = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().toString().equals(Common.KEY_GO_TO_QUESTION))
            {
                int question = intent.getIntExtra(Common.KEY_GO_TO_QUESTION, -1);
                if (question != -1)
                    viewPager.setCurrentItem(question);
                drawer.closeDrawer(Gravity.LEFT);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(Common.selectedCategory.getName());
        setSupportActionBar(toolbar);

        //Register Broadcast
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(gotoQuestionNum, new IntentFilter(Common.KEY_GO_TO_QUESTION));

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        RecyclerView recycler_helper_answer_sheet = navigationView.getHeaderView(0)
                .findViewById(R.id.answer_sheet);
        recycler_helper_answer_sheet.setHasFixedSize(true);
        recycler_helper_answer_sheet.setLayoutManager(new GridLayoutManager(QuestionActivity.this, 3));
        recycler_helper_answer_sheet.addItemDecoration(new SpaceDecoration(2));

        Button btn_done = navigationView.getHeaderView(0)
                .findViewById(R.id.btn_done);
        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAnswerModeView)
                {
                    new MaterialStyledDialog.Builder(QuestionActivity.this)
                            .setTitle("Finish ?")
                            .setIcon(R.drawable.ic_mood_black_24dp)
                            .setDescription("Do you really want to finish ?")
                            .setNegativeText("No")
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            })
                            .setPositiveText("Yes")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                    finishGame();
                                    drawer.closeDrawer(Gravity.LEFT);
                                }
                            }).show();
                }
                else
                    finishGame();
            }
        });

        //First, We need take question from DB

        takeQuestion();
        if (Common.questionList.size() > 0) {

            //Show TextView right answer and Text View Timer
            txt_right_answer = findViewById(R.id.txt_question_right);
            txt_timer = findViewById(R.id.txt_timer);

            txt_timer.setVisibility(View.VISIBLE);
            txt_right_answer.setVisibility(View.VISIBLE);

            txt_right_answer.setText(new StringBuilder(String.format("%d/%d", Common.right_answer_count, Common.questionList.size())));

            countTimer();


            answer_sheet_view = findViewById(R.id.grid_answer);
            answer_sheet_view.setHasFixedSize(true);
            if (Common.questionList.size() > 5) // If qestion List have size > 5, We will seperate 2 rows
                answer_sheet_view.setLayoutManager(new GridLayoutManager(this, Common.questionList.size() / 2));
            answerSheetAdapter = new AnswerSheetAdapter(this, Common.answerSheetList);
            answer_sheet_view.setAdapter(answerSheetAdapter);


            viewPager = findViewById(R.id.viewPager);
            tabLayout = findViewById(R.id.sliding_tab);

            genFragmentList();

            QuestionFragmentAdapter questionFragmentAdapter = new QuestionFragmentAdapter(getSupportFragmentManager(),
                    this,
                    Common.fragmentList);
            viewPager.setAdapter(questionFragmentAdapter);

            tabLayout.setupWithViewPager(viewPager);

            //event
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                int SCROLLING_RIGHT = 0;
                int SCROLLING_LEFT = 1;
                int SCROLLING_UNDETERMIND = 2;

                int currentScrollDirection = SCROLLING_UNDETERMIND;

                private void setScrollingDirection(float positionOffset)
                {
                    if (1-positionOffset >= 0.5)
                        this.currentScrollDirection = SCROLLING_RIGHT;
                    else if (1-positionOffset <= 0.5)
                        this.currentScrollDirection = SCROLLING_LEFT;
                }

                private boolean isetScrollingDirectionUndetermind()
                {
                    return currentScrollDirection == SCROLLING_UNDETERMIND;
                }

                private boolean isScrollingRight()
                {
                    return currentScrollDirection == SCROLLING_RIGHT;
                }

                private boolean isScrollingLeft()
                {
                    return currentScrollDirection == SCROLLING_LEFT;
                }

                @Override
                public void onPageScrolled(int i, float positionOffset, int positionOffsetPixels) {

                    if (isetScrollingDirectionUndetermind())
                        setScrollingDirection(positionOffset);
                }

                @Override
                public void onPageSelected(int i) {

                    QuestionFragment questionFragment;
                    int position = 0;
                    if (i > 0)
                    {
                        if (isScrollingRight())
                        {
                            //if user scroll to right, get previous fragment to calculate result
                            questionFragment = Common.fragmentList.get(i-1);
                            position = i - 1;
                        }
                        else if (isScrollingLeft())
                        {
                            //if user scroll to left, get next fragment to calculate result
                            questionFragment = Common.fragmentList.get(i+1);
                            position = i + 1;
                        }
                        else
                            questionFragment = Common.fragmentList.get(position);
                    }
                    else {
                        questionFragment = Common.fragmentList.get(0);
                        position = 0;
                    }

                    //If you want to show correct answer, just call function here
                    CurrentQuestion question_state = questionFragment.getSelectedAnswer();
                    Common.answerSheetList.set(position, question_state); //set qustion answer for answr sheet
                    answerSheetAdapter.notifyDataSetChanged();//change color in answer sheet
                    answerSheetHelperAdapter.notifyDataSetChanged();

                    countCorrectAnswer();

                    txt_right_answer.setText(new StringBuilder(String.format("%d", Common.right_answer_count))
                    .append("/")
                    .append(String.format("%d", Common.questionList.size())).toString());
                    txt_wrong_answer.setText(String.valueOf(Common.wrong_answer_count));

                    if (question_state.getType() == Common.ANSWER_TYPE.NO_ANSWER)
                    {
                        questionFragment.showCorrecctAnswer();
                        questionFragment.disabledAnswer();
                    }
                }

                @Override
                public void onPageScrollStateChanged(int i) {
                    if (i == ViewPager.SCROLL_STATE_IDLE)
                        this.currentScrollDirection = SCROLLING_UNDETERMIND;
                }
            });

            //txt_right_answer.setText(Common.right_answer_count/Common.questionList.size());
            answerSheetHelperAdapter = new AnswerSheetHelperAdapter(this, Common.answerSheetList);
            recycler_helper_answer_sheet.setAdapter(answerSheetHelperAdapter);
        }
    }

    private void finishGame() {
        int position = viewPager.getCurrentItem();
        QuestionFragment questionFragment = Common.fragmentList.get(position);
        //If you want to show correct answer, just call function here
        CurrentQuestion question_state = questionFragment.getSelectedAnswer();
        Common.answerSheetList.set(position, question_state); //set qustion answer for answr sheet
        answerSheetAdapter.notifyDataSetChanged();//change color in answer sheet
        answerSheetHelperAdapter.notifyDataSetChanged();

        countCorrectAnswer();

        txt_right_answer.setText(new StringBuilder(String.format("%d", Common.right_answer_count))
                .append("/")
                .append(String.format("%d", Common.questionList.size())).toString());
        txt_wrong_answer.setText(String.valueOf(Common.wrong_answer_count));


        if (question_state.getType() == Common.ANSWER_TYPE.NO_ANSWER)
        {
            questionFragment.showCorrecctAnswer();
            questionFragment.disabledAnswer();
        }

        //We will navigate to new result activity here
        Intent intent = new Intent(QuestionActivity.this, ResultActivity.class);
        Common.timer = Common.TOTAL_TIME - time_play;
        Common.no_answer_count = Common.questionList.size() - (Common.wrong_answer_count+Common.right_answer_count);
        Common.data_question = new StringBuilder(new Gson().toJson(Common.answerSheetList));

        startActivityForResult(intent, CODE_GET_RESULT);
    }

    private void countCorrectAnswer() {
        //Reset variable
        Common.right_answer_count = Common.wrong_answer_count = 0;
        for (CurrentQuestion item:Common.answerSheetList)
            if (item.getType() == Common.ANSWER_TYPE.RIGHT_ANSWER)
                Common.right_answer_count++;
            else if(item.getType() == Common.ANSWER_TYPE.WRONG_ANSWER)
                Common.wrong_answer_count++;
    }

    private void genFragmentList() {
        for (int i = 0; i < Common.questionList.size(); i++)
        {
            Bundle bundle = new Bundle();
            bundle.putInt("index", i);
            QuestionFragment fragment = new QuestionFragment();
            fragment.setArguments(bundle);

            Common.fragmentList.add(fragment);
        }
    }

    private void countTimer() {
        if (Common.countDownTimer == null)
        {
            Common.countDownTimer = new CountDownTimer(Common.TOTAL_TIME, 1000) {
                @Override
                public void onTick(long l) {

                    txt_timer.setText(String.format("%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(l),
                            TimeUnit.MILLISECONDS.toSeconds(l) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(l))));
                    time_play -= 1000;
                }

                @Override
                public void onFinish() {
                    //finish game
                }
            }.start();
        }
        else
        {
            Common.countDownTimer.cancel();
            Common.countDownTimer = new CountDownTimer(Common.TOTAL_TIME, 1000) {
                @Override
                public void onTick(long l) {

                    txt_timer.setText(String.format("%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(l),
                            TimeUnit.MILLISECONDS.toSeconds(l) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(l))));
                    time_play -= 1000;
                }

                @Override
                public void onFinish() {
                    //finish game
                }
            }.start();
        }
    }

    private void takeQuestion() {

        Common.questionList = DBHelper.getInstance(this).getquestionByCategory(Common.selectedCategory.getId());
        if (Common.questionList.size() == 0)
        {
            // if no question
            MaterialStyledDialog dialog = new MaterialStyledDialog.Builder(QuestionActivity.this)
                    .setTitle("Opps !")
                    .setDescription("We don't have any question in this " + Common.selectedCategory.getName()+ " Category")
                    .setPositiveText("OK")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                            finish();
                        }
                    }).build();
            dialog.show();
        }
        else
        {
            if (Common.answerSheetList.size() > 0)  Common.answerSheetList.clear();
            //if (Common.fragmentList.size() > 0) Common.fragmentList.clear();

            //Generate answerSheet item from question
            //30 question = 30 answer sheet item
            // 1 question = 1 answer sheet item

            for (int i = 0; i < Common.questionList.size(); i++)
            {
                //Becuase we need to take index of Question in list, so we will use for i
                Common.answerSheetList.add(new CurrentQuestion(i, Common.ANSWER_TYPE.NO_ANSWER)); //Default all answer is no answer
            }
        }
    }

    @Override
    public void onBackPressed() {

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.menu_wrong_answer);
        ConstraintLayout constraintLayout = (ConstraintLayout)item.getActionView();
        txt_wrong_answer = constraintLayout.findViewById(R.id.txt_wrong_answer);
        txt_wrong_answer.setText(String.valueOf(0));

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.question, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_finish_game) {
            if (!isAnswerModeView)
            {
                new MaterialStyledDialog.Builder(QuestionActivity.this)
                        .setTitle("Finish ?")
                        .setIcon(R.drawable.ic_mood_black_24dp)
                        .setDescription("Do you really want to finish ?")
                        .setNegativeText("No")
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveText("Yes")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                                finishGame();
                                drawer.closeDrawer(Gravity.LEFT);
                            }
                        }).show();
            }
            else
                finishGame();
            return true;
        }

       // return super.onOptionsItemSelected(item);
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_tools) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CODE_GET_RESULT)
        {
            if (resultCode == Activity.RESULT_OK) {
                String action = data.getStringExtra("action");
                if (action == null || TextUtils.isEmpty(action))
                {
                    int questionNum = data.getIntExtra(Common.KEY_BACK_FROM_RESULT, -1);
                    viewPager.setCurrentItem(questionNum);

                    isAnswerModeView = true;
                    Common.countDownTimer.cancel();

                    txt_wrong_answer.setVisibility(View.GONE);
                    txt_right_answer.setVisibility(View.GONE);
                    txt_timer.setVisibility(View.GONE);
                }
                else
                {
                    if (action.equals("viewquizanswer"))
                    {
                        viewPager.setCurrentItem(0);

                        isAnswerModeView = true;
                        Common.countDownTimer.cancel();

                        txt_wrong_answer.setVisibility(View.GONE);
                        txt_right_answer.setVisibility(View.GONE);
                        txt_timer.setVisibility(View.GONE);

                        for (int i = 0; i < Common.fragmentList.size(); i++)
                        {
                            Common.fragmentList.get(i).showCorrecctAnswer();
                            Common.fragmentList.get(i).disabledAnswer();
                        }
                    }
                    else if (action.equals("quizagain")) {

                        Toast.makeText(QuestionActivity.this, "Hi There", Toast.LENGTH_LONG).show();
                        viewPager.setCurrentItem(0);
                        isAnswerModeView = false;
                        countTimer();

                        txt_wrong_answer.setVisibility(View.VISIBLE);
                        txt_right_answer.setVisibility(View.VISIBLE);
                        txt_timer.setVisibility(View.VISIBLE);

                        for (CurrentQuestion item:Common.answerSheetList)
                        {
                            item.setType(Common.ANSWER_TYPE.NO_ANSWER); // Reset all question
                        }

                        for (int i = 0; i < Common.fragmentList.size(); i++)
                        {
                            Common.fragmentList.get(i).resetQuestion();
                        }

                        answerSheetAdapter.notifyDataSetChanged();
                        answerSheetHelperAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    }
}
