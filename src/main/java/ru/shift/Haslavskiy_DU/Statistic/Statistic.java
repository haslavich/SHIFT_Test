package ru.shift.Haslavskiy_DU.Statistic;

public class Statistic {
    protected int count;

    public void update(String line) {
        count++;
    }

    public String summarize() {
        return "Total: " + count;
    }
}
