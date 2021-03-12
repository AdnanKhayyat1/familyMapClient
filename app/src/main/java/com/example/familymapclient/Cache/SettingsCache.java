package com.example.familymapclient.Cache;

public class SettingsCache {
    public boolean showLifeLines;
    public boolean showFamilyLines;
    public boolean showSpouseLines;
    public boolean showFatherSide;
    public boolean showMotherSide;
    public boolean showMaleEvents;
    public boolean showFemaleEvents;

    private static SettingsCache sc;

    public static SettingsCache getInstance(){
        if(sc == null){
            sc = new SettingsCache();
        }
        return sc;
    }
    private SettingsCache(){
        showLifeLines = true;
        showFamilyLines = true;
        showSpouseLines = true;
        showFatherSide = true;
        showMotherSide = true;
        showMaleEvents = true;
        showFemaleEvents = true;
    }
    public void resetSettings(){
        showLifeLines = true;
        showFamilyLines = true;
        showSpouseLines = true;
        showFatherSide = true;
        showMotherSide = true;
        showMaleEvents = true;
        showFemaleEvents = true;
    }
}
