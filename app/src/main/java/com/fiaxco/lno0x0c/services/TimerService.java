package com.fiaxco.lno0x0c.services;

public class TimerService {

    private long startTime = 0;
    private long stopTime = 0;
    public boolean running = false;

    public void start() {
        this.startTime = System.currentTimeMillis();
        this.running = true;
    }

    public void stop() {
        this.stopTime = System.currentTimeMillis();
        this.running = false;
    }

    // elapsed time in milliseconds
    public long getElapsedTime() {
        if (running) {
            return System.currentTimeMillis() - startTime;
        }
        return stopTime - startTime;
    }

    // elapsed time in seconds
    public long getElapsedTimeSecs() {
        if (running) {
            return ((System.currentTimeMillis() - startTime) / 1000);
        }
        return ((stopTime - startTime) / 1000);
    }

    public String getElapsedTimeMinutes() {
        long unitMin = 60L;
        if (running) {
            long totalSeconds = getElapsedTimeSecs();
            long minutes = totalSeconds / unitMin;
            long seconds = totalSeconds - unitMin * minutes;

            String minutesS;
            String secondsS;

            if (minutes < 10) {
                minutesS = "0" + minutes;
            } else {
                minutesS = "" + minutes;
            }
            if (seconds < 10) {
                secondsS = "0" + seconds;
            } else {
                secondsS = "" + seconds;
            }
            return "00:" + minutesS + ":" + secondsS;
        } else {
            return "00:00:00";
        }

    }

}
