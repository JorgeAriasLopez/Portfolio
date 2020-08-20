/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jorgearias.tfg.Estimation;

/**
 *
 * @author JorgeArias
 */

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

public class ImageDetections{

    private CascadeClassifier faceDetector = null;
    private CascadeClassifier eyesDetector = null;
    private CascadeClassifier noseDetector = null;
    private CascadeClassifier mouthDetector = null;
    private Properties prop;

    /**
     * Constructor.
     * Crea los clasificadores y establece los properties
     * @param prop archivo Properties creado en el controlador
     * @param context contexto de la aplicación
     */
    public ImageDetections(Properties prop, Context context) {
        this.prop = prop;
        this.loadClassifier(context);
    }

    /**
     * Función para cargar los clasificadores.
     * Teniendo los clasificadores accesibles para toda la clase se evitan llamadas redundantes a esta función.
     */
    private void loadClassifier(Context context){
        try {
            InputStream is = context.getResources().openRawResource(R.raw.haarcascade_frontalface_default);
            File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_defaults.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            this.faceDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());

            is = context.getResources().openRawResource(R.raw.haarcascade_eye);
            mCascadeFile = new File(cascadeDir, "haarcascade_eye.xml");
            os = new FileOutputStream(mCascadeFile);

            buffer = new byte[4096];
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            this.eyesDetector =  new CascadeClassifier(mCascadeFile.getAbsolutePath());

            is = context.getResources().openRawResource(R.raw.haarcascade_mcs_mouth);
            mCascadeFile = new File(cascadeDir, "haarcascade_mcs_mouth.xml");
            os = new FileOutputStream(mCascadeFile);

            buffer = new byte[4096];
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            this.mouthDetector =  new CascadeClassifier(mCascadeFile.getAbsolutePath());

            is = context.getResources().openRawResource(R.raw.haarcascade_mcs_nose);
            mCascadeFile = new File(cascadeDir, "haarcascade_mcs_nose.xml");
            os = new FileOutputStream(mCascadeFile);

            buffer = new byte[4096];
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            this.noseDetector =  new CascadeClassifier(mCascadeFile.getAbsolutePath());

        }catch(Exception e){
            Log.d("ERROR", "Error al leer clasificadores desde xml");
        }
    }

    /**
     * Preprocesamiento de las imagenes.
     * @param image Matriz que contiene la imagen a procesar
     * @return devuelve un hashmap con varias imagenes de diferentes regiones de la imagen
     * pueden ser: face, mouth, nose, eye1 y eye2
     */
    public HashMap<String, Mat> processImage(Mat image){
        HashMap<String, Mat> result = new HashMap<>();
        Integer width = Integer.parseInt(this.prop.getProperty("WIDTH_FACE"));
        Integer height = Integer.parseInt(this.prop.getProperty("HEIGHT_FACE"));

        MatOfRect faceDetections = new MatOfRect();
        MatOfRect eyesDetections = new MatOfRect();
        MatOfRect mouthDetections = new MatOfRect();
        MatOfRect noseDetections = new MatOfRect();

        faceDetector.detectMultiScale(image, faceDetections, 1.01 , 3, 0, new Size(image.height()*0.2, image.width()*0.2), new Size(image.height(), image.width()));
        for(int i = 0; i < faceDetections.toArray().length; i++){
            result = new HashMap<>();
            Rect cara = faceDetections.toArray()[i];     //CROP DE LA CARA

            //DIVIDIR LA CARA EN TOP, BOT Y CENTER
            Mat top = new Mat(image, new Rect(cara.x, cara.y, cara.width, (int)(cara.height*0.6)));

            //DETECTAR OJOS, SI TIENE MENOS DE DOS, SE BUSCA OTRA CARA
            eyesDetector.detectMultiScale(top, eyesDetections, 1.03, 3, 0, new Size(cara.height*0.15, cara.width*0.15), new Size(cara.height*0.3, cara.width*0.3));
            if(eyesDetections.toArray().length < 2){
                Log.d("WARNING", "Se han encontrado menos de dos ojos");
                continue;
            }

            Mat bot = new Mat(image, new Rect(cara.x, cara.y + (int)(cara.height*0.65), cara.width, (int)(cara.height*0.35)));
            Mat mid = new Mat(image, new Rect(cara.x, cara.y + (int)(cara.height*0.3), cara.width, (int)(cara.height*0.5)));

            //DIBUJADO
            Rect eye1 = eyesDetections.toArray()[0];
            Rect eye2 = eyesDetections.toArray()[1];
            //eye1 izquierdo y eye2 derecho
            if(eye1.x > eye2.x){
                eye1 = eyesDetections.toArray()[1];
                eye2 = eyesDetections.toArray()[0];
            }

            Point peye1 =  new Point(eye1.x + eye1.width/2, eye1.y + eye1.height/2);
            Point peye2 = new Point(eye2.x + eye2.width/2, eye2.y + eye2.height/2);

            //angle: angulo que forman los dos ojos
            Double angle = Math.atan((peye1.y - peye2.y )/ (peye1.x - peye2.x)) * 57.295779513;	 //Change radiant to degreed
            //center: Punto central entre los dos ojos
            Point center = new Point((peye1.x - peye2.x)/2 + peye2.x, (peye1.y - peye2.y)/2 + peye2.y);

            Mat face = new Mat(image.clone(), cara);
            result.put("face", face.clone());

            //ROTATE
            //Se rota la imagen
            Imgproc.warpAffine(bot, bot, Imgproc.getRotationMatrix2D(center, angle, 1), image.size());
            Imgproc.warpAffine(mid, mid, Imgproc.getRotationMatrix2D(center, angle, 1), image.size());
            Imgproc.warpAffine(top, top, Imgproc.getRotationMatrix2D(center, angle, 1), image.size());

            //Relocalizar la cara en la imagen rotada
            cara = relocalizar(cara, center, angle);
            eye1 = relocalizar(eye1, center, angle);
            eye2 = relocalizar(eye2, center, angle);

            //Se detectan la nariz y la boca
            mouthDetector.detectMultiScale(bot, mouthDetections, 1.03, 3, 0, new Size(cara.height*0.2, cara.width*0.2), new Size(cara.height*0.5, cara.width*0.5));
            noseDetector.detectMultiScale(mid, noseDetections, 1.03 , 3, 0, new Size(cara.height*0.2, cara.width*0.2), new Size(cara.height*0.5, cara.width*0.5));

            //Se obtiene la imagen de los ojos
            Mat Mateyes1 = new Mat(top, eye1);
            Imgproc.resize(Mateyes1 ,Mateyes1 ,new Size(width, height));
            Mateyes1 = this.normalizarColor(Mateyes1);
            result.put("eye1", Mateyes1);

            Mat Mateyes2 = new Mat(top, eye2);
            Imgproc.resize(Mateyes2 ,Mateyes2 ,new Size(width, height));
            Mateyes2 = this.normalizarColor(Mateyes2);
            result.put("eye2", Mateyes2);

            try{
                Mat mouth = new Mat(bot, mouthDetections.toArray()[0]);		//MOUTH
                Imgproc.resize(mouth ,mouth ,new Size(width, height));
                mouth = this.normalizarColor(mouth);
                result.put("mouth", mouth);
            }catch(java.lang.ArrayIndexOutOfBoundsException a){
                Log.d("WARNING", "Mouth not found", a);
                continue; //Se prueba con otra cara detectada
            }

            try{
                Mat nose = new Mat(mid, noseDetections.toArray()[0]);		//NOSE
                Imgproc.resize(nose ,nose ,new Size(width, height));
                nose = this.normalizarColor(nose);
                result.put("nose", nose);
            }catch(java.lang.ArrayIndexOutOfBoundsException a){
                Log.d("WARNING","Nose not found", a);
                continue; //Se prueba con otra cara detectada
            }

            break;                  //SI SE QUISIERA ANALIZAR MAS CARAS, ELIMINAR ESTA INSTRUCCIÓN
        }
        return result;
    }

    /**
     * Función para procesar el color de las imagenes.
     * @param m Imagen origen
     * @return imagen con color normalizado (gris y ecualizado)
     */
    private Mat normalizarColor(Mat m){
        Imgproc.cvtColor( m, m, Imgproc.COLOR_BGR2GRAY, 1); //1 channel
        Imgproc.equalizeHist(m, m);
        return m;
    }

    /**
     * Función para calcular la nueva posición de una recta tras ser girada la imagen.
     * @param cara  recta a procesar
     * @param center punto desde donde se realizó el giro
     * @param angle angulo de giro
     * @return
     */
    private Rect relocalizar(Rect cara, Point center, Double angle){
        Point a = new Point (cara.x, cara.y + cara.height);
        Point b = new Point (cara.x+ cara.width, cara.y + cara.height);
        Point c = new Point (cara.x, cara.y);
        Point d = new Point (cara.x + cara.width, cara.y);

        angle = angle*-0.01745329252;

        ArrayList<Point> p = new ArrayList<>();

        p.add(new Point(((a.x - center.x) * Math.cos(angle)) - ((a.y - center.y) * Math.sin(angle)) + center.x, ((a.x - center.x) * Math.sin(angle)) + ((a.y - center.y) * Math.cos(angle)) + center.y));
        p.add(new Point(((b.x - center.x) * Math.cos(angle)) - ((b.y - center.y) * Math.sin(angle)) + center.x, ((b.x - center.x) * Math.sin(angle)) + ((b.y - center.y) * Math.cos(angle)) + center.y));
        p.add(new Point(((c.x - center.x) * Math.cos(angle)) - ((c.y - center.y) * Math.sin(angle)) + center.x, ((c.x - center.x) * Math.sin(angle)) + ((c.y - center.y) * Math.cos(angle)) + center.y));
        p.add(new Point(((d.x - center.x) * Math.cos(angle)) - ((d.y - center.y) * Math.sin(angle)) + center.x, ((d.x - center.x) * Math.sin(angle)) + ((d.y - center.y) * Math.cos(angle)) + center.y));


        Double maxx = p.get(0).x, maxy = p.get(0).y, minx = p.get(0).x, miny = p.get(0).y;

        for(int t=1;t<4;t++){
            if(p.get(t).x > maxx){
                maxx = p.get(t).x;
            }
            else if(p.get(t).x < minx){
                minx = p.get(t).x;
            }
            if(p.get(t).y > maxy){
                maxy = p.get(t).y;
            }
            else if(p.get(t).y < miny){
                miny = p.get(t).y;
            }
        }
        
        if(minx < 0){
            minx = 0.0;
        }if (miny < 0){
            miny = 0.0;
        }
        
        return new Rect(new Point(maxx, maxy), new Point(minx, miny));
    }
}
