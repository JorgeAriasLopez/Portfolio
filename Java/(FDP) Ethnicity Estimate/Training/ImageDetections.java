package code;

/**
 *
 * @author JorgeArias
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

public class ImageDetections{
    
    private CascadeClassifier faceDetector;
    private CascadeClassifier eyesDetector;
    private CascadeClassifier noseDetector;
    private CascadeClassifier mouthDetector;
    private final Properties prop;
    private static final Logger LOG = Logger.getLogger(ImageDetections.class.getName());

    /**
     * Constructor.
     * Crea los clasificadores y establece los properties
     * @param prop archivo Properties creado en el controlador
     */
    public ImageDetections(Properties prop) {
        this.faceDetector = null;
        this.eyesDetector = null;
        this.noseDetector = null;
        this.mouthDetector = null;
        this.prop = prop;
        this.loadClassifier();
        LOG.log(Level.INFO, "Creada la clase ImageDetections");
    }
    
    /**
     * Función para cargar los clasificadores.
     * Teniendo los clasificadores accesibles para toda la clase se evitan llamadas redundantes a esta función.
     */
    private void loadClassifier(){
        this.faceDetector = new CascadeClassifier(this.prop.getProperty("PATH_FACE_CLASSIFIER"));
        this.eyesDetector = new CascadeClassifier(this.prop.getProperty("PATH_EYE_CLASSIFIER"));
        this.noseDetector = new CascadeClassifier(this.prop.getProperty("PATH_NOSE_CLASSIFIER"));
        this.mouthDetector = new CascadeClassifier(this.prop.getProperty("PATH_MOUTH_CLASSIFIER"));
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
                
             Imgcodecs.imwrite("C:\\Users\\Alpha\\Desktop\\Presentacion\\image.png", image);
             Imgproc.cvtColor( image, image, Imgproc.COLOR_BGR2GRAY,1);
             Imgcodecs.imwrite("C:\\Users\\Alpha\\Desktop\\Presentacion\\imageg.png", image);
            //DIVIDIR LA CARA EN TOP, BOT Y CENTER
            Mat top = new Mat(image, new Rect(cara.x, cara.y, cara.width, (int)(cara.height*0.6)));
            Imgcodecs.imwrite("C:\\Users\\Alpha\\Desktop\\Presentacion\\top.png", top);
            //DETECTAR OJOS, SI TIENE MENOS DE DOS, SE BUSCA OTRA CARA
            eyesDetector.detectMultiScale(top, eyesDetections, 1.03, 3, 0, new Size(cara.height*0.15, cara.width*0.15), new Size(cara.height*0.3, cara.width*0.3));
            if(eyesDetections.toArray().length < 2){
                LOG.log(Level.WARNING, "Se han encontrado menos de dos ojos");
                continue;
            }
Mat bot = new Mat(image, new Rect(cara.x, cara.y + (int)(cara.height*0.65), cara.width, (int)(cara.height*0.35)));
            Mat mid = new Mat(image, new Rect(cara.x, cara.y + (int)(cara.height*0.3), cara.width, (int)(cara.height*0.5)));
            
            //DIBUJADO
            Rect eye1 = eyesDetections.toArray()[0];                            
            Rect eye2 = eyesDetections.toArray()[1];
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
            Imgcodecs.imwrite("C:\\Users\\Alpha\\Desktop\\Presentacion\\face.png", face);
            //ROTATE	
            //Se rota la imagen
            Imgproc.warpAffine(bot, bot, Imgproc.getRotationMatrix2D(center, angle, 1), image.size());
            Imgproc.warpAffine(mid, mid, Imgproc.getRotationMatrix2D(center, angle, 1), image.size());
            Imgproc.warpAffine(top, top, Imgproc.getRotationMatrix2D(center, angle, 1), image.size());
            
            
             Imgcodecs.imwrite("C:\\Users\\Alpha\\Desktop\\Presentacion\\bot.png", bot);
            
             Imgcodecs.imwrite("C:\\Users\\Alpha\\Desktop\\Presentacion\\mid.png", mid);
            Imgcodecs.imwrite("C:\\Users\\Alpha\\Desktop\\Presentacion\\rot.png", face);
            
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
//            Mateyes1 = this.normalizarColor(Mateyes1);
            result.put("eye1", Mateyes1);
            
            Imgcodecs.imwrite("C:\\Users\\Alpha\\Desktop\\Presentacion\\eye1.png", Mateyes1);
            Mat Mateyes2 = new Mat(top, eye2); 
            Imgproc.resize(Mateyes2 ,Mateyes2 ,new Size(width, height));  
//            Mateyes2 = this.normalizarColor(Mateyes2);
            result.put("eye2", Mateyes2);
            Imgcodecs.imwrite("C:\\Users\\Alpha\\Desktop\\Presentacion\\eye2.png", Mateyes2);
            try{
                Mat mouth = new Mat(bot, mouthDetections.toArray()[0]);		//MOUTH
                Imgproc.resize(mouth ,mouth ,new Size(width, height));    
//                mouth = this.normalizarColor(mouth);
                result.put("mouth", mouth);
                Imgcodecs.imwrite("C:\\Users\\Alpha\\Desktop\\Presentacion\\mouth.png", mouth);
            }catch(java.lang.ArrayIndexOutOfBoundsException a){
                LOG.log(Level.WARNING, "Mouth not found", a);
                continue; //Se prueba con otra cara detectada
            }

            try{
                Mat nose = new Mat(mid, noseDetections.toArray()[0]);		//NOSE
                Imgproc.resize(nose ,nose ,new Size(width, height)); 
//                nose = this.normalizarColor(nose);
                result.put("nose", nose);
                Imgcodecs.imwrite("C:\\Users\\Alpha\\Desktop\\Presentacion\\nose.png", nose);
            }catch(java.lang.ArrayIndexOutOfBoundsException a){
                LOG.log(Level.WARNING, "Nose not found", a);
                continue; //Se prueba con otra cara detectada
            }
            
            Imgproc.equalizeHist(face, face);
            Imgcodecs.imwrite("C:\\Users\\Alpha\\Desktop\\Presentacion\\eq.png", face);
            
            break;                  //SI SE QUISIERA ANALIZAR MAS CARAS EN LA IMAGEN, ELIMINAR ESTA INSTRUCCIÓN
        }
        return result;
    }
    
    /** 
     * Función para procesar el color de las imagenes.
     * @param m Imagen origen
     * @return imagen con color normalizado (gris y ecualizado)
     */
    private Mat normalizarColor(Mat m){
        Imgproc.cvtColor( m, m, Imgproc.COLOR_BGR2GRAY,1); //1 channel
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
