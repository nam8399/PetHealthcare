package com.example.pethealth;

public class feed_data {
    double DWeight;
    double Kcal;
    double Num;
    String Status;

    public feed_data(double DWeight, double kcal, double num, String status) {
        this.DWeight = DWeight;
        Kcal = kcal;
        Num = num;
        Status = status;
    }

    public double getDWeight() {
        return DWeight;
    }

    public void setDWeight(double DWeight) {
        this.DWeight = DWeight;
    }

    public double getKcal() {
        return Kcal;
    }

    public void setKcal(double kcal) {
        Kcal = kcal;
    }

    public double getNum() {
        return Num;
    }

    public void setNum(double num) {
        Num = num;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }
}
