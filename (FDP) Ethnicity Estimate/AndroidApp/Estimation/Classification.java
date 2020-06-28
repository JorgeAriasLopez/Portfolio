/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jorgearias.tfg.Estimation;

import android.content.Context;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.ml.SVM;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.objdetect.HOGDescriptor;

/**
 *
 * @author Alpha
 */
public class Classification {
    private HOGDescriptor hog;

    private ArrayList<SVM> clasificadoresSVM;
    private ArrayList<String> etnias;

    private Properties prop;

    /**
     * Constructor de la clase
     * @param prop archivo properties que contiene información relevante
     */
    public Classification(Properties prop, Context context) {
        this.prop = prop;
        this.hog = new HOGDescriptor(new Size(Integer.parseInt(this.prop.getProperty("WIDTH_FACE")), Integer.parseInt(this.prop.getProperty("HEIGHT_FACE")))
                , new Size(16, 16), new Size(8, 8), new Size(8, 8), 9);
        this.etnias = new ArrayList();
        this.clasificadoresSVM = new ArrayList();
        String value;
        for(int i = 1; (value = this.prop.getProperty("ethnicity." + i)) != null; i++) {
            this.etnias.add(value);
        }

        this.loadClassifier(context);
    }

    /**
     * Función para cargar los clasificadores.
     * Teniendo los clasificadores accesibles para toda la clase se evitan llamadas redundantes a esta función.
     */
    public void loadClassifier(Context context) {
        try{
            for(int i = 0; i<etnias.size(); i++){
                InputStream is = context.getResources().openRawResource(context.getResources().getIdentifier(etnias.get(i).toLowerCase()+"_classifier","raw", context.getPackageName()));
                File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
                File mCascadeFile = new File(cascadeDir, etnias.get(i).toLowerCase()+"classifier.xml");
                FileOutputStream os = new FileOutputStream(mCascadeFile);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                is.close();
                os.close();

                this.clasificadoresSVM.add(i, SVM.load(mCascadeFile.getAbsolutePath()));
            }
        }catch(Exception e){
            Log.d("ERROR", "Error al leer clasificadores desde xml");
            System.exit(0);
        }
    }


    /**
     * Función para obtener las características HOG de una imagen, se pasa una imagen de los ojos, la nariz y la boca
     * @param map HashMap con imagenes de los ojos, la nariz y la boca
     */
    private Mat HOGAnalysis(HashMap<String, Mat> map) {
        MatOfFloat descriptores = new MatOfFloat(0);    //Vector vacio de descriptores //5
        Size winStride = new Size(Integer.parseInt(this.prop.getProperty("WIDTH_FACE")) / 2, Integer.parseInt(this.prop.getProperty("HEIGHT_FACE")) / 2); //Ventana deslizante
        Size padding = new Size(0, 0);                   //Sin padding
        MatOfPoint locations = new MatOfPoint();        //an empty vector of locations, so perform full search
        Mat resultado = new MatOfFloat();
        this.hog.compute(map.get("eye1"), descriptores, winStride, padding, locations);   //Genera el array de las caracteristicas;
        resultado.push_back(descriptores);

        this.hog.compute(map.get("eye2"), descriptores, winStride, padding, locations);   //Genera el array de las caracteristicas;
        resultado.push_back(descriptores);

        this.hog.compute(map.get("nose"), descriptores, winStride, padding, locations);   //Genera el array de las caracteristicas;
        resultado.push_back(descriptores);

        this.hog.compute(map.get("mouth"), descriptores, winStride, padding, locations);   //Genera el array de las caracteristicas;
        resultado.push_back(descriptores);

        return resultado.t();
    }

    /**
     * Detecta la raza a partir de un mapa con las imagenes correspondientes a la nariz, ojos y boca
     * @param map HashMap con las diferentes partes de la cara preprocesadas
     * @return solucion detectada
     */
    public Integer detect(HashMap<String, Mat> map) {
        Mat image = HOGAnalysis(map);

        for(int i = 0; i<this.clasificadoresSVM.size(); i++){
            if(this.clasificadoresSVM.get(i).predict(image) == 1){
                return (i+1);   //El 0 está reservado para la solución 'Desconocido'
            }
        }
        return Controller.UNKNOWN;   //Valor desconocido
    }

}