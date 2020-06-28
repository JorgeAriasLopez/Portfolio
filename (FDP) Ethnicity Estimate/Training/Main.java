package code;

import org.opencv.core.Core;

/**
 *
 * @author JorgeArias
 */

public class Main {  
    public static void main(String[] args) {
        Controller controller;
        if(args.length <= 0){
            System.out.println("ERROR: No se encontró ninguna opción correcta. Introduzca -H para la ayuda.");
            showHelp();
            return;
        }
        
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);      
        switch(args[0]){
            case "-gs":             //Grid Search {-cv salto inicioC finalC inicioGamma finalGamma ficheroGuardado [retainedDataPCA]}
                if(args.length < 7){
                    System.out.println("ERROR: No se encontraron los argumentos validos para la opcion -gs.");
                    showHelp();
                    break;
                }
                controller = new Controller(true);
                if(args.length == 7){   //Sin PCA
                    try{
                        controller.gridSearch(Double.parseDouble(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), args[6]);
                    }catch(NumberFormatException except){
                        System.out.println( "ERROR: No se encontraron los numeros tras la opcion -cv. ");
                        showHelp();
                        break;
                    }
                }else{                  //Con PCA
                    try{
                        controller.gridSearch(Double.parseDouble(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), args[6], Double.parseDouble(args[7]));
                    }catch(NumberFormatException except){
                        System.out.println( "ERROR: No se encontraron los numeros tras la opcion -cv. ");
                        showHelp();
                        break;
                    }
                }
               break;
            case "-cv":             //Cross Validation {-cv c gamma}
                if(args.length < 3){
                    System.out.println("ERROR: No se encontraron los argumentos validos para la opcion -cv. Introduzca -H para la ayuda.");
                    break;
                }
                controller = new Controller(true);
                try{
                    System.out.println("Precisión media: " + controller.crossValidation(Double.parseDouble(args[1]), Double.parseDouble(args[2])));
                }catch(NumberFormatException except){
                    System.out.println("ERROR: No se encontraron numeros enteros tras la opcion -cv.");
                    showHelp();
                    break;
                }
               break;
            case "-t":              //Training {-t c gamma}
                if(args.length < 3){
                    System.out.println( "ERROR: No se encontraron los argumentos validos para la opcion -t.");
                    showHelp();
                    break;
                }
                controller = new Controller(false);
                try{
                    controller.train(Double.parseDouble(args[1]), Double.parseDouble(args[2]));
                }catch(NumberFormatException except){
                    System.out.println( "ERROR: No se encontraron números enteros tras la opcion -t.");
                    showHelp();
                    break;
                }
                break;
            case "-d":              //Detectar {-d ruta}
                if(args.length < 2){
                    System.out.println("ERROR: No se encontraron los argumentos validos para la opcion -d. ");
                    showHelp();
                    break;
                }
                controller = new Controller(true);
                controller.detect(args[1]);
                break;
            case "-S":              //Preprocesar imagenes de la base de datos y guardarlas en memoria  {-S}
                controller = new Controller(false);
                controller.saveDBImages();
                break;
            case "-H":              //Ayuda {-H}||{?}
            case "?":
                showHelp();
                break;
            default:                //Error
                System.out.println("ERROR: No se encontró ninguna opción correcta.");
                showHelp();
                break;
        }
    }
    
    /**
     * Función que muestra la ayuda
     */
    public static void showHelp(){
        System.out.println("\t\t-x-x-x-x-x-x- AYUDA -x-x-x-x-x-x-\n" +
                "\nINFORMACION:\n" + 
                "\tAplicación de entrenamiento. para obtener clasificadores SVM que predigan etnias de una persona\n" +
                "\tTambién se pueden realizar pruebas de rendimiento usando tecnicas como la validacion cruzada y la busqueda por fuerza bruta\n"+
                
                "\nANTES DE EMPEZAR:\n" + 
                "\tActualizar el fichero 'files.properties' con las rutas que se deseen usar.\n" +
                
                "\nOPCIONES:\n" +
                "\t ? \n\t\t\t Muestra este mensaje\n"+
                "\t -H \n\t\t\t Muestra este mensaje\n"+
                "\t -S \n\t\t\t Guarda el resultado del preprocesamiento en una estructura de ficheros en memoria\n"+
                "\t -cv valorC valorGamma \n\t\t\t Ejecuta la validacion cruzada con C=2^valorC y gamma=2^valorGamma\n"+
                "\t -d rutaImagen \n\t\t\t Detecta la etnia de la imagen guardada en rutaImagen\n"+
                "\t -t valorC valorGamma \n\t\t\t Ejecuta un entrenamiento de ficheros con C=2^valorC y gamma=2^valorGamma. "
                +"\n\t\t\t AVISO: Es necesario haber ejecutado primero la opción -S\n"+
                "\t -gs salto minC maxC minGamma maxGamma rutaFicheroXLSX [retainedDataPCA]" + 
                "\n\t\t\t Se ejecuta la busqueda de fuerza bruta en un espacio que se describe como:" +
                "\n\t\t\t - El valor de C va desde 2^minC a 2^maxC" + 
                "\n\t\t\t - El valor de gamma va desde 2^minGamma a 2^maxGamma" + 
                "\n\t\t\t - En cada iteracion se varia este valor en valor+=salto." + 
                "\n\t\t\t - Además el resultado se guarda en un fichero xlsx descrito en la ruta=rutaFicheroXLSX " +
                "\n\t\t\t Si se usa PCA, su portentaje de retencion de datos se indica en retainedDataPCA. Su valor es desde 0.0 a 1.0\n " + 
                
                "\nEJEMPLOS:\n" +
                "\t -cv 21 -3 \n\t\t\t Ejecuta la validacion cruzada con C=2^21 y gamma=2^-3\n"+
                "\t -d resultados/rutaFichero.xlsx \n\t\t\t Detecta la etnia de la imagen guardada en 'resultados/rutaFichero.xlsx'\n"+
                "\t -t 2 -9 \n\t\t\t Ejecuta un entrenamiento de ficheros con C=2^2 y gamma=2^-9\n"+
                "\t -gs 2.0 1 9 -12 12 resultados/rutaFichero.xlsx 0.5" +
                "\n\t\t\t Se ejecuta la busqueda de fuerza bruta en un espacio definido por:" +
                "\n\t\t\t - El valor de C va desde 2^1 a 2^9" + 
                "\n\t\t\t - El valor de gamma va desde 2^-12 a 2^12" + 
                "\n\t\t\t - En cada iteracion se varia este valor en valor+=2.0" + 
                "\n\t\t\t - Los resultados se almacenan en 'resultados/rutaFichero.xlsx'" +
                "\n\t\t\t - PCA se ejecuta con un retencion de datos del 50%" 
                
        );
    }
}
