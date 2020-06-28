package jorgearias.tfg.Estimation;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

/**
 *
 * @author Alpha
 */

public class Controller{

    protected static final Integer UNKNOWN = 0;
    
    private Classification svmc;
    private ImageDetections id;

    private Properties prop;

    private ArrayList<String> etnias;

    /**
     * Constructor de la aplicación.
     * Tiene la función de obtener los properties y crear el resto de clases de controlador
     */
    public Controller(Context context) {
        try{
            this.prop = new Properties();
            InputStream stream = context.getAssets().open("files.properties");
            this.prop.load(stream);
            Log.d("INFO", "Based created");
        } catch (IOException ex) {
            Log.d("SEVERE", null, ex);
        }

        this.etnias = new ArrayList();
        String value;
        for(int i = 1; (value = this.prop.getProperty("ethnicity." + i)) != null; i++) {
            this.etnias.add(value);
        }

        this.svmc = new Classification(prop, context);
        this.id = new ImageDetections(prop, context);
    }

    /**
     * Función que detecta la etnia de una persona
     * @param path Ruta a la imagen a analizar.
     * @return un HashMap donde se duelve el resultado y una matriz con la imagen de la cara.
     */
    public HashMap<String, Mat>  detect(String path){
        HashMap<String, Mat> res = id.processImage(Imgcodecs.imread(path));
        if(res.containsKey("face") && res.containsKey("mouth") && res.containsKey("nose")) {
            Integer resultado = svmc.detect(res);
            HashMap devolver = new HashMap();

            if(resultado == Controller.UNKNOWN){
                Log.d("INFO", "El resultado es DESCONOCIDO");
                devolver.put("unknown", res.get("face"));
            }else{
                for(int i = 0; i < this.etnias.size(); i++){
                    if(resultado.equals(i+1)){
                        Log.d("INFO", "El resultado es" + this.etnias.get(i));
                        devolver.put(this.etnias.get(i).toLowerCase(), res.get("face"));
                        break;
                    }
                }
            }
            return devolver;
        }else{
            return null;
        }

    }

}