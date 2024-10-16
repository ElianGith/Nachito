package com.example.nachito;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class Process {
    private String name;
    private int arrivalTime;
    private int duration;
    private DoubleProperty progress;

    public Process(String name, int arrivalTime, int duration) {
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.duration = duration;
        this.progress = new SimpleDoubleProperty(0); // Inicializar con 0% de progreso
    }

    public String getName() {
        return name;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getDuration() {
        return duration;
    }

    public DoubleProperty progressProperty() {
        return progress;
    }

    public double getProgress() {
        return progress.get();
    }

    public void setProgress(double progress) {
        this.progress.set(progress);
    }
}
