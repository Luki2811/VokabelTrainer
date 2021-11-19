package de.luki2811.dev.vokabeltrainer;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.time.LocalDate;

public class Streak {

    private int length;
    private int xpReached;
    private int xpGoal;
    LocalDate lastTimeReachedGoal;
    LocalDate lastTimeChecked;
    private boolean reachedToday;
    Context context;

    public Streak(Context con){
        context = con;
        File streakFile = new File(context.getFilesDir(),Datei.NAME_FILE_STREAK);
        Datei streakDatei = new Datei(Datei.NAME_FILE_STREAK);
        if(streakFile.exists()){
            try {
                JSONObject streakData = new JSONObject(streakDatei.loadFromFile(context));
                length = streakData.getInt("lengthInDays");
                xpGoal = streakData.getInt("goalInXP");
                xpReached = streakData.getInt("reachedInXPToday");
                lastTimeChecked = LocalDate.parse(streakData.getString("lastTimeChecked"));
                lastTimeReachedGoal = LocalDate.parse(streakData.getString("lastDayReachedGoal"));
                reachedToday = streakData.getBoolean("reachedToday");

                // Check if streak is valid

                LocalDate today = LocalDate.now();
                if(lastTimeReachedGoal.isEqual(today.minusDays(1)) && ! lastTimeChecked.isEqual(today)){
                    xpReached = 0;
                    reachedToday = false;
                    lastTimeChecked = today;
                }else if(!lastTimeChecked.isEqual(today)){
                    length = 0;
                    reachedToday = false;
                    xpReached = 0;
                    lastTimeChecked = today;
                }

                // Check if need to increase streak

                if(!isReachedToday()){
                    if(xpReached>=xpGoal){
                        length = length + 1;
                        lastTimeReachedGoal = today;
                        reachedToday = true;
                    }
                }

                streakData.put("lengthInDays", length);
                streakData.put("reachedToday", reachedToday);
                streakData.put("reachedInXPToday", xpReached);
                streakData.put("lastTimeChecked", lastTimeChecked);
                streakData.put("lastDayReachedGoal", lastTimeReachedGoal);

                streakDatei.writeInFile(streakData.toString(), context);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }else{
            try {
                JSONObject streakData = new JSONObject();
                streakData.put("lengthInDays",0);
                streakData.put("goalInXP",50);
                streakData.put("reachedInXPToday", 0);
                LocalDate localDate = LocalDate.now();
                streakData.put("lastDayReachedGoal",LocalDate.of(2005, 11, 28));
                streakData.put("reachedToday", false);
                streakData.put("lastTimeChecked", localDate.toString());

                length = streakData.getInt("lengthInDays");
                xpGoal = streakData.getInt("goalInXP");
                xpReached = streakData.getInt("reachedInXPToday");
                reachedToday = streakData.getBoolean("reachedToday");
                lastTimeChecked = LocalDate.parse(streakData.getString("lastTimeChecked"));
                lastTimeReachedGoal = LocalDate.parse(streakData.getString("lastDayReachedGoal"));

                streakDatei.writeInFile(streakData.toString(), context);
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    public void addXP(int value){
        Datei streakDatei = new Datei(Datei.NAME_FILE_STREAK);
        try {
            JSONObject streakData = new JSONObject(streakDatei.loadFromFile(context));
            streakData.put("reachedInXPToday", value + xpReached);
            xpReached = xpReached + value;
            streakDatei.writeInFile(streakData.toString(), context);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean isReachedToday() {
        return reachedToday;
    }

    public int getLength() {
        return length;
    }

    public int getXpGoal() {
        return xpGoal;
    }

    public int getXpReached() {
        return xpReached;
    }
}
