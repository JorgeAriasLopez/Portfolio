/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package code;

import java.io.File;
import java.util.ArrayList;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.ml.SVM;
import java.util.HashMap;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.objdetect.HOGDescriptor;

/**
 *
 * @author JorgeArias
 */

public class Classification{
    private final HOGDescriptor hog;
    
    private Mat trainingImages;
  
    private Mat mean;
    private Mat eigenVectors;
    private final Properties prop;
    private static final Logger LOG = Logger.getLogger(Controller.class.getName());
    
    private ArrayList<SVM> clasificadoresSVM;
    private ArrayList<Mat> trainingLabels;
    private ArrayList<String> etnias;

    /**
     * Constructor de la clase
     * @param prop archivo properties que contiene información relevante
     * @param loadedClassifier boolean que indica si se deben cargar los clasificadores o no 
     */
    public Classification(Properties prop, Boolean loadedClassifier){
        this.eigenVectors = new Mat();
        this.mean = new Mat();
        this.prop = prop;
        this.hog = new HOGDescriptor( new Size(Integer.parseInt(this.prop.getProperty("WIDTH_FACE")), Integer.parseInt(this.prop.getProperty("HEIGHT_FACE")))
                , new Size(16,16), new Size(8,8), new Size(8,8), 9);  
        this.clasificadoresSVM  = new ArrayList();
        this.trainingLabels = new ArrayList();
        this.etnias = new ArrayList();
        
        String value;
        for(int i = 1; (value = this.prop.getProperty("ethnicity." + i)) != null; i++) {
            this.etnias.add(value);
        }
        
        if(loadedClassifier)
            this.loadClassifier();
        LOG.log(Level.INFO, "Creada la clase Classification");
    }
        
    /**
     * Función para cargar los clasificadores.
     * Teniendo los clasificadores accesibles para toda la clase se evitan llamadas redundantes a esta función.
     */
    private void loadClassifier(){
        try{
            for(int i = 0; i<etnias.size(); i++){
                this.clasificadoresSVM.add(i, SVM.load(this.prop.getProperty("PATH_" + etnias.get(i) + "_CLASSIFIER")));
            }
        }catch(Exception e){
            LOG.log(Level.SEVERE, "Error al leer los archivos xml que contienen los clasificadores. Por favor revise la ruta en files.properties");
            System.exit(0);
        }
    }
    
    /**
     * Función para iniciar un clasificador SVM
     * @param c Parámetro C del clasificador SVM con kernel RBF
     * @param gamma Parámetro Gamma del clasificador SVM con kernel RBF
     * @return 
     */
    private SVM inicialiteSVM(double c, double gamma){
        SVM svm = SVM.create();
        svm.setType(SVM.C_SVC);
        svm.setKernel(SVM.RBF);
        svm.setC(c);
        svm.setGamma(gamma);
        return svm;
    }
    
    /**
     * Función que inicializa PCA.
     * @param retained Porcentaje de datos que retiene PCA (valor entre 0.0 y 1.0) 
     */
    public void init_PCA(Double retained){
        String path = prop.getProperty("PATH_ALL_LOADED");
        String nombre;
        this.eigenVectors = new Mat();
        this.mean = new Mat();
        
        LOG.log(Level.INFO, "Inicilizando el uso de la tecnica PCA con un porcentaje de retencion del {0}%", retained);
        //TEST
        Mat PCAMat = new Mat();
        HashMap<String, Mat> processImage = null;

        for (File file : new File(path + "/eye1").listFiles()){
            nombre = file.getName().split("-")[0];
            processImage = new HashMap();
            processImage.put("eye1", Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.IMREAD_GRAYSCALE));
            processImage.put("eye2", Imgcodecs.imread(path + "/eye2/" + nombre + "-eye2.png", Imgcodecs.IMREAD_GRAYSCALE));
            processImage.put("nose", Imgcodecs.imread(path + "/nose/" + nombre + "-nose.png", Imgcodecs.IMREAD_GRAYSCALE));
            processImage.put("mouth", Imgcodecs.imread(path + "/mouth/" + nombre + "-mouth.png", Imgcodecs.IMREAD_GRAYSCALE)); //1296x1
            PCAMat.push_back(this.HOGAnalysis(processImage));
        }  
        if(PCAMat.empty()){
            LOG.log(Level.SEVERE, "No existe la estructura de ficheros de las imagenes preprocesadas. Por favor, ejecuta antes la opción -S o modifica la ruta en el archivo files.properties.");
            System.exit(0);   
        }
        Core.PCACompute(PCAMat, this.mean, this.eigenVectors, retained);
        LOG.log(Level.INFO, "PCA inicializado");
    }
    
      
    /**
     * Función para obtener las características HOG de una imagen, se pasa una imagen de los ojos, la nariz y la boca
     * @param map HashMap con imagenes de los ojos, la nariz y la boca
     */
    private Mat HOGAnalysis(HashMap<String, Mat> map){
        MatOfFloat descriptores = new MatOfFloat(0);    //Vector vacio de descriptores
        Size winStride = new Size(Integer.parseInt(this.prop.getProperty("WIDTH_FACE"))/2, Integer.parseInt(this.prop.getProperty("HEIGHT_FACE"))/2); //Ventana deslizante
        Size padding = new Size(0,0);                   //Sin padding
        MatOfPoint locations = new MatOfPoint();        //Busqueda en todo el espacio
        Mat resultado = new MatOfFloat();
        
        this.hog.compute(map.get("eye1"), descriptores , winStride, padding, locations);   //Genera el array de las caracteristicas;
        resultado.push_back(descriptores);

        this.hog.compute(map.get("eye2"), descriptores , winStride, padding, locations);   //Genera el array de las caracteristicas;
        resultado.push_back(descriptores);
        
        this.hog.compute(map.get("nose"), descriptores , winStride, padding, locations);   //Genera el array de las caracteristicas;
        resultado.push_back(descriptores);
    
        this.hog.compute(map.get("mouth"), descriptores , winStride, padding, locations);   //Genera el array de las caracteristicas;
        resultado.push_back(descriptores);

        if(!mean.empty()){  //Si se realiza la optimización de PCA entrará en el if.
            Mat res = new Mat();
            Core.PCAProject(resultado.t(), this.mean, this.eigenVectors, res);
            return res;
        }else{
            return resultado.t();
        }
    }
   
    /**
     * Detecta la raza a partir de un mapa con las imagenes correspondientes a la nariz, ojos y boca
     * @param map HashMap con las diferentes partes de la cara preprocesadas
     * @return solucion detectada
     */
    public Integer detect(HashMap<String, Mat> map){
        Mat image = HOGAnalysis(map);        
        for(int i = 0; i<this.clasificadoresSVM.size(); i++){
            if(this.clasificadoresSVM.get(i).predict(image) == 1){
                return (i+1);   //El 0 está reservado para la solución 'Desconocido'
            }
        }        
        return Controller.UNKNOWN;   //Valor desconocido
    }  
    

    /********************** CARGAR IMAGENES YA PROCESADAS ********************/
    
    /**
     * Función que entrena imágenes previamente procesas y almacenadas en una jerarquía de archivos indicada en properties.
     * Estas funciones sirven para ahorrar tiempo en el entrenamiento y en la optimizacion de algoritmo
     * @param c Parámetro C de la clasificación SVM con kernel RBF
     * @param gamma Parámetro Gamma de la clasificación SVM con kernel RBF 
     */
    public void trainImagenesCargadas(double c, double gamma){   

        this.trainingImages = new Mat();
        
        this.clasificadoresSVM.forEach((clasificador) -> {
            clasificador = this.inicialiteSVM(c, gamma);
        });
        for(int i = 0; i < this.etnias.size(); i++){
            this.trainingLabels.add(i, new Mat());
        }

        HashMap<String, Mat> processImage;
        String nombre;
        
        try{
            String etnia;
            for(int ne = 0; ne < this.etnias.size(); ne++){
                etnia = this.etnias.get(ne);
                LOG.log(Level.INFO, "Obteniendo datos de " + etnia);
                for (File file : new File(this.prop.getProperty("PATH_"+etnia+"_LOADED") + "/eye1").listFiles()){
                    processImage = new HashMap();
                    nombre = file.getName().split("-")[0];
                    processImage.put("eye1", Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.IMREAD_GRAYSCALE));
                    processImage.put("eye2", Imgcodecs.imread(this.prop.getProperty("PATH_"+etnia+"_LOADED") + "/eye2/" + nombre + "-eye2.png", Imgcodecs.IMREAD_GRAYSCALE));
                    processImage.put("nose", Imgcodecs.imread(this.prop.getProperty("PATH_"+etnia+"_LOADED") + "/nose/" + nombre + "-nose.png", Imgcodecs.IMREAD_GRAYSCALE));
                    processImage.put("mouth", Imgcodecs.imread(this.prop.getProperty("PATH_"+etnia+"_LOADED") + "/mouth/" + nombre + "-mouth.png", Imgcodecs.IMREAD_GRAYSCALE));

                    this.trainingImages.push_back(this.HOGAnalysis(processImage)); 
                    
                    for(int i = 0; i < this.trainingLabels.size(); i++){
                        if(i == ne){
                            this.trainingLabels.get(i).push_back(Mat.ones(new Size(1, 1), CvType.CV_32SC1));
                        }else{
                            this.trainingLabels.get(i).push_back(Mat.zeros(new Size(1, 1), CvType.CV_32SC1));
                        }
                        
                    }
                }
            }
        }catch(NullPointerException e){
            LOG.log(Level.SEVERE, "No existe la estructura de ficheros de las imagenes preprocesadas. Por favor, ejecuta antes la opción -S o modifica la ruta en el archivo files.properties.");
            System.exit(0);
        }
        
        Optional<Mat> findAny = this.trainingLabels.stream().filter((p)-> p.cols()==0).findAny();
        if(findAny.isPresent()){
            LOG.log(Level.SEVERE, "No existe las imagenes preprocesadas. Por favor, ejecuta antes la opción -S o modifica la ruta en el archivo files.properties.");
            System.exit(0);
        }
        
        LOG.log(Level.INFO, "Entrenando clasificadores");
        for(int i = 0; i<this.clasificadoresSVM.size(); i++){
            this.clasificadoresSVM.get(i).train(this.trainingImages, 0, this.trainingLabels.get(i));
            this.clasificadoresSVM.get(i).save(this.prop.getProperty("PATH_"+ this.etnias.get(i) + "_CLASSIFIER"));
        }
    }
    
    /********************** VALIDACIÓN CRUZADA ********************/
    
    /**
     * Función para iniciar el proceso de validación cruzada.
     * Se crean clasificadores y demás estructuras que se usaran
     * @param c Parámetro C de la clasificación SVM con kernel RBF
     * @param gamma Parámetro Gamma de la clasificación SVM con kernel RBF 
     */
    public void initCrossValidations(double c, double gamma){
        this.clasificadoresSVM.forEach((clasificador) -> {
            clasificador = this.inicialiteSVM(c, gamma);
        });
        for(int i = 0; i < this.etnias.size(); i++){
            this.trainingLabels.add(i, new Mat());
        }
        this.trainingImages = new Mat();

        LOG.log(Level.INFO, "Inicializado el proceso de la validacion cruzada con los valores C={0} y gamma={1}", new Object[]{c,gamma});
    }
    
    /**
     * Función principal de la validación cruzada. 
     * En la cual se crean los datos para luego ser entrenados.
     * @param file Archivo que contiene el fichero de "eye1" de la imagen de BD previamente procesada
     */
    public void trainCrossValidation(File file){
        HashMap<String, Mat> processImage = new HashMap();
        String nombre = file.getName().split("-")[0];
        String path = this.prop.getProperty("PATH_ALL_LOADED");
        processImage.put("eye1", Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.IMREAD_GRAYSCALE ));
        processImage.put("eye2", Imgcodecs.imread(path + "/eye2/" + nombre + "-eye2.png", Imgcodecs.IMREAD_GRAYSCALE ));
        processImage.put("nose", Imgcodecs.imread(path + "/nose/" + nombre + "-nose.png", Imgcodecs.IMREAD_GRAYSCALE));
        processImage.put("mouth", Imgcodecs.imread(path + "/mouth/" + nombre + "-mouth.png", Imgcodecs.IMREAD_GRAYSCALE));
        
        for(int ne = 0; ne<this.etnias.size(); ne++){
            if(file.getName().contains(this.etnias.get(ne).toLowerCase())){
                this.trainingImages.push_back(this.HOGAnalysis(processImage)); 
                for(int i = 0; i < this.trainingLabels.size(); i++){
                    if(i == ne){
                        this.trainingLabels.get(i).push_back(Mat.ones(new Size(1, 1), CvType.CV_32SC1));
                    }else{
                        this.trainingLabels.get(i).push_back(Mat.zeros(new Size(1, 1), CvType.CV_32SC1));
                    }

                }
            }
        }
        
    }
    
    /**
     * Función que entrena los clasificadores con los resultados finales.
     */
    public void finishTrainCrossValidation(){
        for(int i = 0; i< this.clasificadoresSVM.size(); i++){
            this.clasificadoresSVM.get(i).train(this.trainingImages, 0, this.trainingLabels.get(i));
        }
    }
    
    /**
     * Función para detectar la etnia en la validación cruzada.
     * Además también verifica si se ha acertado la estimación
     * @param file Archivo que contiene el fichero de "eye1" de la imagen de BD previamente procesada. El cual contiene en su ruta la etnia asignada por la BD al sujeto.
     * @param res HashMap con las diferentes partes de la cara de la imagen a estimar ya preprocesadas
     * @return 
     */
    public boolean detectCrossValidation(File file, HashMap<String,Mat> res){
        int detectado = this.detect(res);
        for(int i = 0; i < this.etnias.size(); i++){
            if(file.getName().contains(this.etnias.get(i).toLowerCase()) && (detectado == (i+1))){
                return true;
            }
        }
        return false;
    }
}