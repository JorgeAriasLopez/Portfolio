package jorgearias.tfg;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

/**
 *
 * @author JorgeArias
 */


public class TutorialActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(getResources().getText(R.string.app_name));

        TextView t2 = findViewById(R.id.editText2);
        t2.setText(getResources().getText(R.string.text_tutorial));
        TextView t1 = findViewById(R.id.editText);
        t1.setText(getResources().getText(R.string.btn_tutorial));
        t1.setTypeface(null, Typeface.BOLD);
        t1.setTextSize(20);
    }

    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, res.getDisplayMetrics());
        recreate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater mMenuInflater = getMenuInflater();
        mMenuInflater.inflate(R.menu.my_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about_us:
                startActivity(new Intent(TutorialActivity.this, AboutActivity.class));
                return true;
            case R.id.action_language:
                showLanguageDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    protected void showLanguageDialog(){
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle(getResources().getString(R.string.select_language));
        String[] pictureDialogItems = {
                getResources().getString(R.string.label_galician),
                getResources().getString(R.string.label_spanish),
                getResources().getString(R.string.label_english),
                getResources().getString(R.string.label_french)};
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                setLocale("gl");
                                break;
                            case 1:
                                setLocale("es");
                                break;
                            case 2:
                                setLocale("en");
                                break;
                            case 3:
                                setLocale("fr");
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }
}
