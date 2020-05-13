package com.example.findme.character;

import java.util.ArrayList;
import java.util.Collections;

public class CharacterProvider {
    private ArrayList<String> statuses = new ArrayList<>();
    private ArrayList<String> characters = new ArrayList<>();
    public CharacterProvider(){
        statuses.add("happy");
        statuses.add("bored");
        statuses.add("angry");
        statuses.add("overenthusiastic");
        statuses.add("really dumb");
        statuses.add("really smart");
        statuses.add("agitated");
        statuses.add("in love");
        statuses.add("quiet");
        statuses.add("confused");
        statuses.add("famous");
        statuses.add("irritated");
        statuses.add("enchanted");
        statuses.add("burned out");
        statuses.add("annoyed");

        characters.add("vlogger");
        characters.add("neighbour");
        characters.add("dog");
        characters.add("pizza delivery");
        characters.add("circus performer");
        characters.add("spy");
        characters.add("forester");
        characters.add("historical hero");
        characters.add("artist");
        characters.add("top athlete");
        characters.add("teacher");
        characters.add("fairy-tale figure");
        characters.add("Santa Claus");
        characters.add("Easter Bunny");
        characters.add("yoga teacher");
        characters.add("archaeologist");
        characters.add("astronaut");
        characters.add("agent");
        characters.add("cat");
        characters.add(" postman");
        characters.add("hero");
        characters.add("mythical figure");
        characters.add("serial killer");
        characters.add("knight");
        characters.add("wizard");
        characters.add("celebrity");
        characters.add("king/queen");
    }
    public String getStatus() {
        Collections.shuffle(statuses);
        return statuses.get(0);
    }
    public ArrayList<String> getCharacters(){
        return characters;
    }

}
