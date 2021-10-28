package de.luki2811.dev.vokabeltrainer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class EditLektionActivity extends AppCompatActivity {

    Lektion lektion;
    int counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_lektion);

        Intent comingInt = getIntent();
        String lektionName = comingInt.getStringExtra(MainActivity.LEKTION_NAME);

        Datei datei = new Datei(lektionName + ".json");
        try {
            JSONObject lektionAsJSON = new JSONObject(datei.loadFromFile(this));
            lektion = new Lektion(lektionAsJSON);

        }catch (JSONException e){
            e.printStackTrace();
        }

        counter = 1;
        final TextView textViewKnown = findViewById(R.id.textViewEditLessonKnown);
        final TextView textViewNew = findViewById(R.id.textViewEditLessonNew);
        final TextView textViewTop = findViewById(R.id.textViewEditLessonTop);

        textViewKnown.setText(getString(R.string.voc_in, lektion.getLanguageKnow().getName()));
        textViewNew.setText(getString(R.string.voc_in, lektion.getLanguageNew().getName()));
        textViewTop.setText(getString(R.string.number_voc_of_rest,counter, lektion.getCount()));

        final EditText editTextKnown = findViewById(R.id.editTextEditLessonKnown);
        final EditText editTextNew = findViewById(R.id.editTextEditLessonNew);
        final Switch switchIgnoreCase = findViewById(R.id.switchEditLektionIgnoreCase);

        editTextKnown.setText(lektion.getVokabelAtPos(counter-1).getKnownWord());
        editTextKnown.setHint(lektion.getVokabelAtPos(counter-1).getKnownWord());
        editTextNew.setText(lektion.getVokabelAtPos(counter-1).getNewWord());
        editTextNew.setHint(lektion.getVokabelAtPos(counter-1).getNewWord());

        switchIgnoreCase.setChecked(lektion.getVokabelAtPos(counter-1).isIgnoreCase());

    }

    public void saveAndNext(View view){

        // Speichern der Vokabel in der Datei
        final EditText editTextKnown = findViewById(R.id.editTextEditLessonKnown);
        final EditText editTextNew = findViewById(R.id.editTextEditLessonNew);
        final Switch switchIgnoreCase = findViewById(R.id.switchEditLektionIgnoreCase);
        final TextView textViewTop = findViewById(R.id.textViewEditLessonTop);

        if(editTextKnown.getText().toString().trim().isEmpty() || editTextNew.getText().toString().trim().isEmpty()){
            Toast.makeText(this, getText(R.string.err_missing_input),Toast.LENGTH_LONG).show();
            return;
        }

        Vokabel vocNew = new Vokabel(editTextKnown.getText().toString(), editTextNew.getText().toString(), switchIgnoreCase.isChecked());
        lektion.setVokabelAtPos(counter-1, vocNew);

        Datei datei = new Datei(lektion.getName() + ".json");
        datei.writeInFile(lektion.getLektionAsJSON().toString(),this);

        counter = counter+1;

        if(counter <= lektion.getCount()){
            // UI ändern auf die nächste Vokabel

            editTextKnown.setText(lektion.getVokabelAtPos(counter-1).getKnownWord());
            editTextKnown.setHint(lektion.getVokabelAtPos(counter-1).getKnownWord());
            editTextNew.setText(lektion.getVokabelAtPos(counter-1).getNewWord());
            editTextNew.setHint(lektion.getVokabelAtPos(counter-1).getNewWord());

            switchIgnoreCase.setChecked(lektion.getVokabelAtPos(counter-1).isIgnoreCase());

            textViewTop.setText(getString(R.string.number_voc_of_rest,counter, lektion.getCount()));
        }else{
            startActivity(new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }
    }

    public void cancelOnClick(View view){
        startActivity(new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

}