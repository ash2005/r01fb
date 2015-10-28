package r01f.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

import lombok.RequiredArgsConstructor;

/**
 * Utilidades sobre Streams
 */
public class Streams {
///////////////////////////////////////////////////////////////////////////////
// 
///////////////////////////////////////////////////////////////////////////////	
	@RequiredArgsConstructor
	public static class InputStreamWrapper {
		private final InputStream _is;
		
		
		/**
		 * Gets an {@link InputStream} as a byte array
		 * @return the byte array from the {@link InputStream}
		 * @throws IOException if an I/O error occurs
		 */
		public byte[] getBytes() throws IOException {
			byte[] outBytes = IOUtils.toByteArray(_is);
			return outBytes;
		}
	    /**
	     * Pasa un InputStream a una cadena codificada en el encoding por defecto
	     * @param is el inputStream a convertir
	     * @return la cadena
	     * @throws IOException si se produce un error de IO
	     */
		public String asString() throws IOException {
			return this.asString(Charset.defaultCharset());
		}
	    /**
	     * Pasa un InputStream a una cadena
	     * @param is el inputStream a convertir
	     * @param charset el charset en el que se codifica el String
	     * @return la cadena
	     * @throws IOException si se produce un error de IO
	     */
		public String asString(final Charset charSet) throws IOException {
			Writer writer = null;
	        if (_is != null) {
	            writer = new StringWriter();
	            char[] buffer = new char[1024];
	            try {
	                Reader reader = new BufferedReader(new InputStreamReader(_is,charSet));
	                int n;
	                while ((n = reader.read(buffer)) != -1) {
	                    writer.write(buffer, 0, n);
	                }
	            } finally {
	            	try {
	            		_is.close();
	            	} catch (IOException ioEx) {
	            		/* ignore */
	            	}
	            }
	        }         
	        return writer != null ? writer.toString()
	        					  : null;
		}
	}
///////////////////////////////////////////////////////////////////////////////
// 	FACTORIAS
///////////////////////////////////////////////////////////////////////////////
	public static InputStreamWrapper of(final InputStream is) {
		return new InputStreamWrapper(is);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METODOS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * (from org.apache.tomcat.util.http.fileupload.util.Streams)
	 * Copies the contents from the given {@link InputStream} to the given {@link OutputStream}
	 * Shortcut of 
	 * <pre class='brush:java'>
	 * 		copy(pInputStream, pOutputStream, new byte[8192]);
	 * </pre>
	 * @param pInputStream The input stream, which is being read. It is guaranteed, that java.io.InputStream.close() is called on the stream
	 * @param pOutputStream The output stream, to which data should be written. May be null, in which case the input streams contents are simply discarded
	 * @param pClose True guarantees, that java.io.OutputStream.close() is called on the stream. False indicates, that only java.io.OutputStream.flush() should be called finally
	 * @return Number of bytes, which have been copied.
	 */
	public static long copy(final InputStream pInputStream,
             				final OutputStream pOutputStream,
             				final boolean pClose) throws IOException {
		return Streams.copy(pInputStream,
						 	pOutputStream,
						 	pClose,
						 	new byte[8192]);
		 
	}
	/**
	 * (from org.apache.tomcat.util.http.fileupload.util.Streams)
	 * Copies the contents from the given {@link InputStream} to the given {@link OutputStream}
	 * @param pInputStream The input stream, which is being read. It is guaranteed, that java.io.InputStream.close() is called on the stream
	 * @param pOutputStream The output stream, to which data should be written. May be null, in which case the input streams contents are simply discarded
	 * @param pClose True guarantees, that java.io.OutputStream.close() is called on the stream. False indicates, that only java.io.OutputStream.flush() should be called finally
	 * @return Number of bytes, which have been copied.
	 */
	public static long copy(final InputStream pIn,
							final OutputStream pOut,
							final boolean pClose,
							final byte[] pBuffer) throws IOException {
		OutputStream out = pOut;
        InputStream in = pIn;
        try {
            long total = 0;
            for (;;) {
                int res = in.read(pBuffer);
                if (res == -1) {
                    break;
                }
                if (res > 0) {
                    total += res;
                    if (out != null) {
                        out.write(pBuffer, 0, res);
                    }
                }
            }
            if (out != null) {
                if (pClose) {
                    out.close();
                } else {
                    out.flush();
                }
                out = null;
            }
            in.close();
            in = null;
            return total;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioEx) {
                    /* Ignore me */
                }
            }
            if (pClose  &&  out != null) {
                try {
                    out.close();
                } catch (IOException ioEx) {
                    /* Ignore me */
                }
            }
        }
	}
}
