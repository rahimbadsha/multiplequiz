package com.example.multiplequiz.Interface;

import com.example.multiplequiz.Model.CurrentQuestion;

public interface IQuestion {
    CurrentQuestion getSelectedAnswer(); //get selected answer form user select
    void showCorrecctAnswer(); //bold correct answer text
    void disabledAnswer(); //disabled all check box
    void resetQuestion(); //reset all function on question
}
