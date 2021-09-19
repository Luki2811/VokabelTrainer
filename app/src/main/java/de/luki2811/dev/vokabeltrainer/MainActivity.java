package de.luki2811.dev.vokabeltrainer;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    final static String LEKTION_NAME = "de.luki2811.dev.vokabeltrainer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File indexFile = new File(getApplicationContext().getFilesDir(),"indexLections.json");
        if(indexFile.exists()){
            Datei indexDatei = new Datei("indexLections.json");
            try {
                JSONObject indexJson = new JSONObject(indexDatei.loadFromFile(this));
                JSONArray indexArrayJson = indexJson.getJSONArray("index");

                LinearLayout layout = findViewById(R.id.cardsLayoutHome);
                System.out.println("Array Length: " + indexArrayJson.length());

                for(int i = 0; i <= indexArrayJson.length() - 1; i++){
                    RelativeLayout.LayoutParams layoutparams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT
                    );
                    layoutparams.bottomMargin = 25;
                    layoutparams.rightMargin = 25;
                    layoutparams.leftMargin = 25;
                    layoutparams.topMargin = 25;


                    CardView cardView = new CardView(this);
                    cardView.setLayoutParams(layoutparams);
                    cardView.setRadius(25);
                    cardView.setContentPadding(10,10,10,10);
                    cardView.setCardBackgroundColor(getColor(R.color.Aquamarine));
                    cardView.setCardElevation(3);
                    cardView.setMaxCardElevation(5);

                    layout.addView(cardView);

                    TextView textInCard = new TextView(this);
                    textInCard.setId(i + 2000);
                    RelativeLayout.LayoutParams layoutparamsText = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT
                    );
                    textInCard.setPadding(10,10,10,10);
                    textInCard.setLayoutParams(layoutparamsText);
                    try {
                        textInCard.setText(indexArrayJson.getJSONObject(i).getString("name"));
                    } catch (JSONException e){
                        e.printStackTrace();
                    }
                    textInCard.setTextColor(Color.WHITE);
                    textInCard.setGravity(Gravity.TOP);

                    // H+W for icons
                    RelativeLayout.LayoutParams layoutparamsIcons = new RelativeLayout.LayoutParams(100,100);

                    // Delete button
                    ImageButton delete = new ImageButton(this);
                    delete.setBackgroundResource(R.drawable.rounded_red_button);
                    delete.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.outline_delete_24));
                    delete.setLayoutParams(layoutparamsIcons);

                    delete.setOnClickListener(view -> new AlertDialog.Builder(this)
                            .setTitle("")
                            .setMessage("Möchtest du wirklich die Lektion löschen")
                            .setIcon(R.drawable.outline_delete_24)
                            .setPositiveButton(R.string.delete, (dialogInterface, i1) -> {
                                File file = new File(getApplicationContext().getFilesDir(), textInCard.getText() + ".json");
                                if(file.exists()){
                                    boolean deleted = file.delete();
                                    if(deleted){
                                        try {
                                            for(int i2 = 0; i2 <= indexArrayJson.length() - 1; i2++){
                                                if(indexArrayJson.getJSONObject(i2).getString("name").contentEquals(textInCard.getText())){
                                                    System.out.println(indexArrayJson.toString());
                                                    indexArrayJson.remove(i2);
                                                    indexJson.put("index", indexArrayJson);
                                                    indexDatei.writeInFile(indexJson.toString(),getApplicationContext());
                                                    cardView.setVisibility(View.INVISIBLE);
                                                    cardView.setLayoutParams(new LinearLayout.LayoutParams(0,0));
                                                    Toast.makeText(getApplicationContext(), getString(R.string.deleted_succesfull), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        } catch (JSONException e){
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            })
                            .setNegativeButton(R.string.cancel, (dialogInterface, i12) -> Toast.makeText(getApplicationContext(), getString(R.string.cancel), Toast.LENGTH_SHORT).show())
                            .show());
                    // Edit Button
                    ImageButton cardEdit = new ImageButton(this);
                    cardEdit.setBackgroundResource(R.drawable.rounded_blue_button);
                    cardEdit.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_outline_edit_24));
                    cardEdit.setLayoutParams(layoutparamsIcons);
                    cardEdit.setOnClickListener(view -> {
                        Intent intent = new Intent(this, EditLektionActivity.class);
                        intent.putExtra(LEKTION_NAME, textInCard.getText());
                        startActivity(intent);
                    });
                    // Train Button
                    MaterialButton cardLearnButton = new MaterialButton(this, null, R.attr.borderlessButtonStyle);
                    cardLearnButton.setText(R.string.practice);
                    cardLearnButton.setBackgroundDrawable(getDrawable(R.drawable.outline_button));
                    cardLearnButton.setCornerRadius(100);
                    cardLearnButton.setOnClickListener(view -> {

                    });

                    // Add all to a Layout
                    // TEMP without structure
                    LinearLayout cardLayout = new LinearLayout(this);

                    cardLayout.addView(delete);
                    cardLayout.addView(cardEdit);
                    cardLayout.addView(textInCard);
                    cardLayout.addView(cardLearnButton);

                    cardView.addView(cardLayout);

                }

            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    public void createNewLektion(View view){
        Intent intent = new Intent(MainActivity.this, NewLektion.class);
        startActivity(intent);
    }
}