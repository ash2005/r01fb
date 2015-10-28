package r01f.httpclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import lombok.Cleanup;
import lombok.experimental.Accessors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;

@Accessors(prefix="_")
public class HttpRequestPayloadForBinaryParameter 
     extends HttpRequestPayloadBase<HttpRequestPayloadForBinaryParameter> {
/////////////////////////////////////////////////////////////////////////////////////////
//  STATUS
/////////////////////////////////////////////////////////////////////////////////////////	
	
/////////////////////////////////////////////////////////////////////////////////////////
//   BUILDERS
/////////////////////////////////////////////////////////////////////////////////////////
	public static HttpRequestPayloadForBinaryParameter wrap(final InputStream is) throws IOException {
		HttpRequestPayloadForBinaryParameter param = new HttpRequestPayloadForBinaryParameter();
		param.setContent(IOUtils.toByteArray(is));
		param.setMustEncodeToTargetServerCharset(false);
		return param;
	}
	public static HttpRequestPayloadForBinaryParameter wrap(final byte[] bytes) {
		HttpRequestPayloadForBinaryParameter param = new HttpRequestPayloadForBinaryParameter();
		param.setContent(bytes);
		param.setMustEncodeToTargetServerCharset(false);
		return param;
	}
	@SuppressWarnings("resource")
	public static HttpRequestPayloadForBinaryParameter wrap(final File file) throws IOException {
		HttpRequestPayloadForBinaryParameter param = new HttpRequestPayloadForBinaryParameter();
		@Cleanup InputStream fis = new FileInputStream(file);
		byte[] fileBytes = IOUtils.toByteArray(fis);
		param.setContent(fileBytes);
		param.setMustEncodeToTargetServerCharset(false);
		return param;
	}
	@SuppressWarnings("resource")
	public static HttpRequestPayloadForBinaryParameter wrap(final Reader reader) throws IOException {
		HttpRequestPayloadForBinaryParameter param = new HttpRequestPayloadForBinaryParameter();
		@Cleanup ReaderInputStream ris = new ReaderInputStream(reader);
		byte[] readerBytes = IOUtils.toByteArray(ris);
		param.setContent(readerBytes);
		param.setMustEncodeToTargetServerCharset(true);
		return param;
	}
	public static HttpRequestPayloadForBinaryParameter wrap(final String str) {
		HttpRequestPayloadForBinaryParameter param = new HttpRequestPayloadForBinaryParameter();
		byte[] strBytes = str.getBytes();
		param.setContent(strBytes);
		param.setMustEncodeToTargetServerCharset(true);
		return param;
	}
}
