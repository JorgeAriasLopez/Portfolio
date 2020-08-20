/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package code;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author JorgeArias
 */


public class Controller{
    protected static final Integer UNKNOWN = 0;
    
    private final Classification svmc;
    private final ImageDetections id;
   
    private Properties prop;
    private static final Logger LOG = Logger.getLogger(Controller.class.getName());
    
    private ArrayList<String> etnias;
    
    /**
     * Constructor de la aplicación.
     * Tiene la función de obtener los properties y crear el resto de clases de controlador
     * @param loadedClassifier boolean que indica si se deben leer los clasificadores de etnias o no
     */
    public Controller(Boolean loadedClassifier) {
        this.etnias = new ArrayList();
        try {
            this.prop = new Properties();
            String path = getClass().getClassLoader().getResource("").getPath();
            InputStream stream = new FileInputStream(path + "resources/files.properties");
            this.prop.load(stream);
            LOG.log(Level.INFO, "Properties de archivos cargado");
            String value;
            for(int i = 1; (value = this.prop.getProperty("ethnicity." + i)) != null; i++) {
                this.etnias.add(value);
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }

        this.svmc = new Classification(this.prop, loadedClassifier);
        this.id = new ImageDetections(this.prop);
    }
    
    /**
     * Función para realizar entrenamiento de los clasificadores.
     * @param c
     * @param gamma 
     */
    public void train(Double c, Double gamma){
        this.svmc.trainImagenesCargadas(Math.pow(2, c), Math.pow(2, gamma));
    }
    
    /**
     * Guarda las imágenes procesadas de la base de datos en una red de archivos.
     * El origen de la función es agilizar el proceso de pruebas. (Se evita preprocesar las imágenes al comprobar parametros de la clasificación)
     */
    public void saveDBImages(){
        HashMap<String, Mat> res;
        Integer j=1;
        String fileName;
        /* Crear la estructura de ficheros si no existe */
        this.createFiles(this.prop.getProperty("PATH_ALL_LOADED"));
        for(String etnia : this.etnias){
            this.createFiles(this.prop.getProperty("PATH_"+etnia+"_LOADED"));
            /**********************/
            LOG.log(Level.INFO, "-- Guardando "+etnia);
            for (File file : new File(this.prop.getProperty("PATH_"+etnia+"_BD")).listFiles()){
                res = id.processImage(Imgcodecs.imread(file.getAbsolutePath()));
                j++;
                fileName = file.getName();
                if(res.containsKey("mouth") && res.containsKey("nose")){
                    Imgcodecs.imwrite(this.prop.getProperty("PATH_"+etnia+"_LOADED") + "/mouth/" + j + "-mouth.png", res.get("mouth"));
                    Imgcodecs.imwrite(this.prop.getProperty("PATH_"+etnia+"_LOADED") + "/nose/" + j + "-nose.png", res.get("nose"));
                    Imgcodecs.imwrite(this.prop.getProperty("PATH_"+etnia+"_LOADED") + "/eye1/" + j + "-eye1.png", res.get("eye1"));
                    Imgcodecs.imwrite(this.prop.getProperty("PATH_"+etnia+"_LOADED") + "/eye2/" + j + "-eye2.png", res.get("eye2"));

                    Imgcodecs.imwrite(this.prop.getProperty("PATH_ALL_LOADED") + "/mouth/" + fileName + "-mouth.png", res.get("mouth"));
                    Imgcodecs.imwrite(this.prop.getProperty("PATH_ALL_LOADED") + "/nose/" + fileName + "-nose.png", res.get("nose"));
                    Imgcodecs.imwrite(this.prop.getProperty("PATH_ALL_LOADED") + "/eye1/" + fileName + "-"+etnia.toLowerCase()+"-eye1.png", res.get("eye1"));
                    Imgcodecs.imwrite(this.prop.getProperty("PATH_ALL_LOADED") + "/eye2/" + fileName + "-eye2.png", res.get("eye2"));  
                } 
            }
        }

        LOG.log(Level.INFO, "Finalizado");  
    }
    
    /**
     * Función que detecta la etnia de una persona
     * @param path Ruta a la imagen a analizar.
     * @return un HashMap donde se duelve el resultado y una matriz con la imagen de la cara.
     */
    public HashMap<String, Mat>  detect(String path){
        long time_start, time_end;
        time_start = System.currentTimeMillis();
        
        HashMap<String, Mat> res = id.processImage(Imgcodecs.imread(path));
        if(res.containsKey("face") && res.containsKey("mouth") && res.containsKey("nose")) {
            Integer resultado = svmc.detect(res);
            HashMap devolver = new HashMap();
            if(resultado == Controller.UNKNOWN){
                LOG.log(Level.INFO, "El resultado es DESCONOCIDO");
                devolver.put("unknown", res.get("face"));
            }else{
                for(int i = 0; i < this.etnias.size(); i++){
                    if(resultado.equals(i+1)){
                        LOG.log(Level.INFO, "El resultado es " + this.etnias.get(i));
                        devolver.put(this.etnias.get(i).toLowerCase(), res.get("face"));
                        break;
                    }
                }
            }
            time_end = System.currentTimeMillis();
            LOG.log(Level.INFO, "La predición tomó {0} milisegundos", (time_end-time_start));
            return devolver;
        }else{
            LOG.log(Level.SEVERE, "No se encontró una cara valida en la imagen. Por favor pruebe otra");
            return null;
        }  
    }
    
    /**
     * Uso de la técnica crossValidation enla base de datos previamente guardad tras el preprocesamiento.
     * La técncia de crossValidation se basa dividir la BD en varios segmentos unos dedicados para el entrenamiento y otros para el testeo
     * @param c variable c de la clasificación SVM usando kernel RBF 
     * @param gamma variable gamma de la clasificación SVM usando kernel RBF 
     * @return devuelve la precisión obtenida
     */
    public Double crossValidation(double c, double gamma){
        String path = prop.getProperty("PATH_ALL_LOADED");
        if(new File(path+"/eye1").length() == 0){
            LOG.log(Level.SEVERE, "No existe la estructura de ficheros de las imagenes preprocesadas. Por favor, ejecuta antes la opción -S o modifica la ruta en el archivo files.properties.");
            System.exit(0);   
        }
 
        Integer tamConjunto = new File(path+"/eye1").listFiles().length/4;

        int contador, empieza=0, fin=0, aciertos = 0, total=0;
        double precision = 0;
        String nombre;
        //En cada iteración se coge un cuarto de los archivos como datos de prueba
        for(int k=1; k<5; k++){
            LOG.log(Level.INFO, "ITERACION {0}", k);
            switch(k){
                case 1:
                    empieza = 0;
                    fin = tamConjunto;
                    break;
                case 2:
                    empieza=tamConjunto;
                    fin = tamConjunto*2;
                    break;
                case 3:
                    empieza = tamConjunto*2;
                    fin = tamConjunto*3;
                    break;
                case 4:
                    empieza = tamConjunto*3;
                    fin = tamConjunto*4;
                    break;
            }
            contador=0;
            //TRAINING
            svmc.initCrossValidations(c, gamma);
            for (File file : new File(path + "/eye1").listFiles()){
                contador++;
                if(empieza >= contador || contador > fin){
                    svmc.trainCrossValidation(file);
                }
            }
            svmc.finishTrainCrossValidation();
            contador=0;
            //TEST
            HashMap<String, Mat> processImage;
            for (File file : new File(path + "/eye1").listFiles()){
                contador++;
                if(empieza < contador && contador <= fin){
                    nombre = file.getName().split("-")[0];
                    processImage = new HashMap();
                    processImage.put("eye1", Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.IMREAD_GRAYSCALE));
                    processImage.put("eye2", Imgcodecs.imread(path + "/eye2/" + nombre + "-eye2.png", Imgcodecs.IMREAD_GRAYSCALE));
                    processImage.put("nose", Imgcodecs.imread(path + "/nose/" + nombre + "-nose.png", Imgcodecs.IMREAD_GRAYSCALE));
                    processImage.put("mouth", Imgcodecs.imread(path + "/mouth/" + nombre + "-mouth.png", Imgcodecs.IMREAD_GRAYSCALE));
                    total++;
                    if(svmc.detectCrossValidation(file, processImage))
                        aciertos++;
                    
                }else if (contador > fin){
                    break;
                }
            }
            precision = precision + (((float)aciertos/(float)total)*100);
            LOG.log(Level.INFO, "-Precisión: {0}% ", String.format("%.2f", ((float)aciertos/(float)total)*100));
            LOG.log(Level.INFO,"---------------------");
        }
        LOG.log(Level.INFO,"-Precisión Media: {0}%", String.format("%.2f", precision/4));
        LOG.log(Level.INFO,"---------------------");
        return (precision/4);
    }
    
    /**
     * Función que realiza gridSearch con PCA.
     * @param salto
     * @param inicioGamma
     * @param finalGamma
     * @param inicioC
     * @param finalC
     * @param URLGuardado
     * @param retained Porcentaje de datos que retiene PCA
     */
    public void gridSearch(Double salto, Integer inicioGamma, Integer finalGamma, Integer inicioC, Integer finalC, String URLGuardado, Double retained){
        this.svmc.init_PCA(retained);
        this.gridSearch(salto, inicioGamma, finalGamma, inicioC, finalC, URLGuardado);
    }
    
    /**
     * Función que ejecuta la búsqueda por fuerza bruta para encontrar el mejor valor de c y gamma para la clasificación
     */ 
    public void gridSearch(Double salto, Integer inicioGamma, Integer finalGamma, Integer inicioC, Integer finalC, String URLGuardado){
        double gamma, c;
        int contador, i;
        LOG.log(Level.INFO, "Inicio de busqueda por fuerza bruta");
        try {
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("GridSearch");
            Row row = sheet.createRow(0);
            
            //Se establecen los encabezados con los valores de C
            for(c=inicioC, i=1; c<=finalC; c+=salto, i++){
                row.createCell(i).setCellValue(c);
            }
            for(gamma=inicioGamma, contador=1; gamma<=finalGamma; gamma+=salto, contador++ ){
                row = sheet.createRow(contador);
                //Se empieza añadido el valor de gamma
                row.createCell(0).setCellValue(gamma);
                for(c=inicioC, i=1; c<=finalC; c+=salto, i++){
                    LOG.log(Level.INFO,"-------------------------------");
                    //Se guarda el resultado del crossValidation
                    row.createCell(i).setCellValue(crossValidation(Math.pow(2, c), Math.pow(2,gamma)));
                }
            }
            //Se guardan los resultados obtenidos
            try (FileOutputStream outputStream = new FileOutputStream(URLGuardado)) {
                workbook.write(outputStream);
            }
            workbook.close();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } 
    }
    
    /**
     * Funcion para crear la estructura de archivos si no existe
     * @param rute 
     */
    private void createFiles(String rute){
        Path path = Paths.get(rute);
        if(!Files.exists(path)) {
            try {
              Files.createDirectories(path);
            } catch (IOException e) {
              LOG.log(Level.SEVERE, "No se pudo crear el sistema de directorios", e);
              return;
            }
        }
        path=Paths.get((rute+"/eye1"));
        if(!Files.exists(path)) {
            try {
              Files.createDirectories(path);
            } catch (IOException e) {
              LOG.log(Level.SEVERE, "No se pudo crear el sistema de directorios", e);
              return;
            }
        }
        path=Paths.get(rute+"/eye2");
        if(!Files.exists(path)) {
            try {
              Files.createDirectories(path);
            } catch (IOException e) {
              LOG.log(Level.SEVERE, "No se pudo crear el sistema de directorios", e);
              return;
            }
        }
        path=Paths.get(rute+"/mouth");
        if(!Files.exists(path)) {
            try {
              Files.createDirectories(path);
            } catch (IOException e) {
              LOG.log(Level.SEVERE, "No se pudo crear el sistema de directorios", e);
              return;
            }
        }
        path=Paths.get(rute+"/nose");
        if(!Files.exists(path)) {
            try {
              Files.createDirectories(path);
            } catch (IOException e) {
              LOG.log(Level.SEVERE, "No se pudo crear el sistema de directorios", e);
              return;
            }
        }
    }
}