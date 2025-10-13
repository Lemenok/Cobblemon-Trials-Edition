package com.lemenok.cobblemontrialsedition.models;

import java.util.ArrayList;

public class ListOfPokemonToSpawn{
    public int waveNumber;
    public boolean capturable;
    public boolean mustBeDefeatedInBattle;
    public int spawnNumberMinimum;
    public int spawnNumberMaximum;
    public int minLevel;
    public int maxLevel;
    public ArrayList<String> natures;
    public ArrayList<Integer> defaultIVs;
    public ArrayList<Integer> defaultEvs;
    public boolean isShiny;
    public ArrayList<Object> forms;
    public String name;
}
