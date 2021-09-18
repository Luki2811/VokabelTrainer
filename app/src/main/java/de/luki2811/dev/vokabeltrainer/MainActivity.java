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

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class MainActivity extends AppCompatActivity {

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
                    cardView.setCardBackgroundColor(getColor(R.color.Black));
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


                    ImageButton delete = new ImageButton(this);
                    delete.setBackgroundResource(R.drawable.rounded_red_button);
                    delete.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.outline_delete_24));
                    RelativeLayout.LayoutParams layoutparamsDelete = new RelativeLayout.LayoutParams(100,100);
                    delete.setLayoutParams(layoutparamsDelete);

                    delete.setOnClickListener(view -> {
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
                                            indexDatei.writeInFile(indexJson.toString(),this);

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


                    });

                    // Add all to a Layout
                    LinearLayout cardLayout = new LinearLayout(this);

                    cardLayout.addView(delete);
                    cardLayout.addView(textInCard);

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