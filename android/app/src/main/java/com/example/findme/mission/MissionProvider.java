package com.example.findme.mission;

import android.os.Handler;
import android.widget.TextView;

import java.util.ArrayList;

public class MissionProvider {
    private TextView tv;
    ArrayList<String> missions;
    public MissionProvider(TextView tv){
        this.tv = tv;
        missions = new ArrayList<>();
        missions.add("Your character wants to dance, start dancing and if there is someone around try to dance together.");
        missions.add("You can improve the knowledge of your character by investigating something, an item, animal or person in your surroundings.");
        missions.add("Your character seems to smell something suspicious, what could it be?");
        missions.add("Your character doesn´t feel completely safe and wants to go someplace higher and take a look around if he sees something suspicious.");
        missions.add("Draw something in/on the ground using objects to leave the drawing behind.");
        startMissions();
    }
    public void startMissions(){
        final Handler handler = new Handler();
        final int delay = 10000; //milliseconds

        handler.postDelayed(new Runnable(){
            int i = 0;
            public void run(){
                tv.setText(missions.get(i));
                if (i == missions.size()-1){
                    i = 0;
                } else {
                    i++;
                }
                handler.postDelayed(this, delay);
            }
        }, delay);
    }
}
