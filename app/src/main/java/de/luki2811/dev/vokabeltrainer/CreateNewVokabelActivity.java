package de.luki2811.dev.vokabeltrainer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

public class CreateNewVokabelActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_vokabel);


        // TEMP Test
        Intent intent = getIntent();
        try {
            JSONObject jsonObject = new JSONObject(intent.getStringExtra(NewLektion.JSON_OBJECT));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void createAndRestartActivity(View view){
        Intent intent = getIntent();
        JSONObject jsonObject;
        JSONArray vocabsArray;
        try {
            jsonObject = new JSONObject(intent.getStringExtra(NewLektion.JSON_OBJECT));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}