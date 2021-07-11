package com.dryadandnaiad.sethlans.enums;

public enum SecurityQuestion {
    QUESTION1("What city were you born in?"),
    QUESTION2("What is your oldest sibling’s middle name?"),
    QUESTION3("What was the first concert you attended?"),
    QUESTION4("What was the make and model of your first car?"),
    QUESTION5("In what city or town did your parents meet?"),
    QUESTION6("In what city or town was your first job?"),
    QUESTION7("What street did you live on in third grade?");

    private final String question;

    SecurityQuestion(String question) {
        this.question = question;
    }

    public String getName() {
        return question;
    }

}
