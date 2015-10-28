package r01f.util.types;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import lombok.Cleanup;

import org.apache.commons.io.input.CharSequenceReader;

import r01f.resources.ResourcesLoader;

class StringPersistenceUtils {
///////////////////////////////////////////////////////////////////////////////////////////
//  CARGAR FICHEROS UTILIZANDO UN LOADER
/////////////////////////////////////////////////////////////////////////////////////////// 
    /**
     * Carga una cadena a partir de un fichero
     * @param loader una clase que implementa el interfaz ResourcesLoader (ej: ClassPathResourcesLoader o FileSystemResourcesLoader)
     * 				 y que se encarga que obtener el fichero
     * @param filePath path al fichero
     * @return La cadena cargada a partir del fichero
     * @throws IOException Si ocurre algun error al acceder al fichero
     */
	@SuppressWarnings("resource")
	public static String load(final ResourcesLoader loader,final String filePath) throws IOException {
    	@Cleanup InputStream is = loader.getInputStream(filePath,true);	// true indica que NO hay que utilizar cachés (si existen)
        String outStr = StringPersistenceUtils.load(is);
        return outStr;
    }
    /**
     * Carga una cadena a partir de un fichero y la encodea
     * @param filePath path del fichero
     * @param loader una clase que implementa el interfaz ResourcesLoader 
     * 				 (ej: ClassPathResourcesLoader o FileSystemResourcesLoader)
     * 				 y que se encarga que obtener el fichero
     * @param encoding encoding del fichero de salida
     * @return el contenido del fichero codificado
     * @throws IOException si ocurre algún error al acceder al fichero
     */
	@SuppressWarnings("resource")
	public static String load(final ResourcesLoader loader,final String filePath,final Charset encoding) throws IOException {
    	@Cleanup InputStream is = loader.getInputStream(filePath,true);	// true indica que NO hay que utilizar cachés (si existen)
    	String outStr = StringPersistenceUtils.load(is,encoding);
    	return outStr;
    }  
///////////////////////////////////////////////////////////////////////////////////////////
//  CARGAR FICHEROS A PARTIR DEL File
///////////////////////////////////////////////////////////////////////////////////////////    
    /**
     * Lectura completa del fichero(y caracteres especiales), incluso retornos de carro
     * @param f fichero a cargar
     * @return el contenido del fichero en formato de cadena
     * @throws IOException si ocurre algún error al acceder al fichero
     */
    public static String load(final File f) throws IOException {
    	String outStr = StringPersistenceUtils.load(f,null);
    	return outStr;
    }    
    /**
     * Carga una cadena a partir de un fichero y la encodea
     * @param f  del fichero
     * @param encoding encoding del fichero de salida
     * @return el contenido del fichero codificado
     * @throws IOException si ocurre algún error al acceder al fichero
     */
    public static String load(final File f,final Charset encoding) throws IOException {
    	Charset theEncoding = encoding != null ? encoding : Charset.defaultCharset();
    	@Cleanup FileInputStream fis = new FileInputStream(f);
    	@Cleanup BufferedInputStream bis = new BufferedInputStream(fis);
    	String outStr = StringPersistenceUtils.load(bis,theEncoding);
    	return outStr;
    }  
///////////////////////////////////////////////////////////////////////////////////////////
//  CARGAR FICHEROS A PARTIR DEL Stream/Reader
///////////////////////////////////////////////////////////////////////////////////////////     
    /**
     * Lee una cadena de un inputStream
     * @param is El inputStream
     * @return La cadena leida
     * @throws IOException
     */
    public static String load(final InputStream is) throws IOException {
        if (is == null) return null;
        String outStr = StringPersistenceUtils.load(is,null);
        return outStr;
    }
    /**
     * Lee una cadena de un InputStream y encodea el resultado
     * @param is InputStream del que leer
     * @param encoding el encoding
     * @return la cadena encodeada
     * @throws IOException si se produce un error al cargar
     */
	@SuppressWarnings("resource")
	public static String load(final InputStream is,final Charset encoding) throws IOException {
    	Charset theEncoding = encoding != null ? encoding : Charset.defaultCharset();  
        @Cleanup Reader r = new InputStreamReader(is,theEncoding);    	
        String outStr = StringPersistenceUtils.load(r);
        return outStr;
    }
    /**
     * Lee una cadena de un reader
     * @param r El reader
     * @return La cadena leida
     * @throws IOException
     */
    public static String load(final Reader r) throws IOException {
        StringBuilder outString = new StringBuilder();
        if (r != null) {
            char[] buf = new char[2 * 1024]; // Buffer de 2K
            int charsReaded = -1;
            do {
                charsReaded = r.read(buf);
                if (charsReaded != -1) outString.append(new String(buf, 0, charsReaded));
            } while (charsReaded != -1);
            return outString.toString();
        }
        return null;
    }    
    /**
     * Lee una cadena de un reader y la encodea
     * @param r reader
     * @param encoding para codificar la cadena
     * @return la cadena leida codificada
     * @throws IOException
     */
    public static String load(final Reader r,
    						  final Charset encoding) throws IOException {
    	Charset theEncoding = encoding != null ? encoding 
    										   : Charset.defaultCharset();    	
        String srcString = StringPersistenceUtils.load(r);
        ByteBuffer bb = theEncoding.encode(CharBuffer.wrap(srcString)); 
        return new String(bb.array());
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  GUARDAR FICHEROS
///////////////////////////////////////////////////////////////////////////////////////////    
    /**
     * Guarda una cadena en un fichero
     * @param f El fichero
     * @param theString La cadena a guardar en el fichero
     * @throws IOException Si ocurre algun error al acceder al fichero
     */
    public static void save(final CharSequence theString,final File f) throws IOException {
        BufferedReader reader = new BufferedReader(new CharSequenceReader(theString));
        BufferedWriter writer = new BufferedWriter(new FileWriter(f, false));
        String line = reader.readLine();
        while (line != null) {
            writer.write(line + "\r\n");
            line = reader.readLine();
        }
        reader.close();
        writer.flush();
        writer.close();
    }    
    /**
     * Guarda una cadena en un fichero
     * @param filePath El path del fichero
     * @param theString La cadena a guardar en el fichero
     * @throws IOException Si ocurre algun error al acceder al fichero
     */
    public static void save(final CharSequence theString,final String filePath) throws IOException {
        StringPersistenceUtils.save(theString,new File(filePath));
    }      
}
