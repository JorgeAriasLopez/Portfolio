package jorgearias.tfg;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import jorgearias.tfg.com.tfg.Estimation.Controller;

/**
 *
 * @author JorgeArias
 */

public class EstimationActivity extends AppCompatActivity {
    private Button tryAgainButton;
    private ImageView fotografia;
    private TextView result, textoAuxiliar;
    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estimation);

        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(getResources().getText(R.string.app_name));


        tryAgainButton = findViewById(R.id.tryAgain);
        tryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPictureDialog();
            }
        });

        fotografia = findViewById(R.id.image);
        result = findViewById(R.id.resultado);
        textoAuxiliar = findViewById(R.id.textView2);

        Intent intent = getIntent();
        if (null != intent) { //Null Checking
            Controller c = new Controller(getApplicationContext());
            HashMap<String, Mat> resultado = c.detect(intent.getStringExtra("pictureURI"));
            if (resultado == null) {
                Log.d("__WARNING", "No se puede hacer nada, prueba con otra imagen");
                textoAuxiliar.setText(getResources().getText(R.string.text_tryOtherImage));
            } else {
                Mat m;
                String resName = (String) resultado.keySet().toArray()[0];
                result.setVisibility(View.VISIBLE);
                result.setText(getResources().getText(getResources().getIdentifier("label_" + resName.toLowerCase() + "", "string", getPackageName())));
                m = resultado.get(resName);

                //Corrector de color debido a un bug con matToBitMap.
                Imgproc.cvtColor(m, m, Imgproc.COLOR_RGB2BGRA);
                Bitmap bm = Bitmap.createBitmap(m.cols(), m.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(m, bm);

                ImageView iv = findViewById(R.id.fotografia);
                iv.setImageBitmap(bm);
            }
            //Se borran los datos de los Files creados para contener los clasificadores (y así obtener su ruta)
            File cache = getCacheDir();
            File appDir = new File(cache.getParent());
            if (appDir.exists()) {
                String[] children = appDir.list();
                for (String s : children) {
                    if (!s.equals("lib")) {
                        deleteDir(new File(appDir, s));
                        Log.i("TAG", "Memoria liberada");
                    }
                }
            }
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
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
                startActivity(new Intent(EstimationActivity.this, AboutActivity.class));
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

    /*+*****+*************************/



    protected void showPictureDialog(){
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle(getResources().getString(R.string.select_action));
        String[] pictureDialogItems = {
                getResources().getString(R.string.btn_fromGallery),
                getResources().getString(R.string.btn_fromCamera)};
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                startFromGallery(true);
                                break;
                            case 1:
                                startFromCamera(true);
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MainActivity.GALLERY: {
                if (ContextCompat.checkSelfPermission(EstimationActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    startFromGallery(false);
                }
                return;
            } case MainActivity.CAMERA: {
                if (ContextCompat.checkSelfPermission(EstimationActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(EstimationActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ){
                    startFromCamera(false);
                }
                return;
            }
        }
    }

    //Con 'b' false no se hace la comprobación de si hay permisos
    public void startFromGallery(Boolean b){
        if (b && ContextCompat.checkSelfPermission(EstimationActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(EstimationActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MainActivity.GALLERY);
        } else {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, MainActivity.GALLERY);
        }
    }

    //Con 'b' false no se hace la comprobación de si hay permisos
    public void startFromCamera(Boolean b){
        if(b && ContextCompat.checkSelfPermission(EstimationActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(EstimationActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(EstimationActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, MainActivity.CAMERA);
        }else if (b && ContextCompat.checkSelfPermission(EstimationActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(EstimationActivity.this, new String[]{Manifest.permission.CAMERA}, MainActivity.CAMERA);
        } else if(b && ContextCompat.checkSelfPermission(EstimationActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(EstimationActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MainActivity.CAMERA);
        }else{
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile;
                try {
                    photoFile = createImageFile();
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(EstimationActivity.this,
                                BuildConfig.APPLICATION_ID + ".provider",
                                createImageFile());
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, MainActivity.CAMERA);
                    }
                }catch (IOException ex) {
                    return;
                }
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "Camera");
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MainActivity.GALLERY && resultCode == RESULT_OK && null != data) {
            Uri contentURI = data.getData();
            String[] FILE = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(contentURI, FILE, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(FILE[0]);
            String ImageDecode = cursor.getString(columnIndex);
            cursor.close();

            Intent intent = new Intent(this, EstimationActivity.class);
            intent.putExtra("pictureURI", ImageDecode);
            startActivity(intent);

        } else if (requestCode == MainActivity.CAMERA && resultCode == RESULT_OK) {

            Uri imageUri = Uri.parse(mCurrentPhotoPath);
            File file = new File(imageUri.getPath());

            Intent intent = new Intent(this, EstimationActivity.class);
            intent.putExtra("pictureURI", file.getAbsolutePath());
            startActivity(intent);
        }
    }

}
