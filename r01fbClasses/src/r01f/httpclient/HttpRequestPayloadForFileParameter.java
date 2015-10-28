package r01f.httpclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import lombok.AccessLevel;
import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;

@Accessors(prefix="_")
public class HttpRequestPayloadForFileParameter 
     extends HttpRequestPayloadBase<HttpRequestPayloadForFileParameter> {
/////////////////////////////////////////////////////////////////////////////////////////
//  STATUS
/////////////////////////////////////////////////////////////////////////////////////////	
	@Getter @Setter(AccessLevel.PRIVATE) private String _fileName;
/////////////////////////////////////////////////////////////////////////////////////////
//   BUILDERS
/////////////////////////////////////////////////////////////////////////////////////////
	public static HttpRequestPayloadForFileParameter wrap(final InputStream is) throws IOException {
		HttpRequestPayloadForFileParameter param = new HttpRequestPayloadForFileParameter();
		param.setContent(IOUtils.toByteArray(is));
		param.setMustEncodeToTargetServerCharset(false);
		return param;
	}
	public static HttpRequestPayloadForFileParameter wrap(final byte[] bytes) {
		HttpRequestPayloadForFileParameter param = new HttpRequestPayloadForFileParameter();
		param.setContent(bytes);
		param.setMustEncodeToTargetServerCharset(false);
		return param;
	}
	@SuppressWarnings("resource")
	public static HttpRequestPayloadForFileParameter wrap(final File file) throws IOException {
		HttpRequestPayloadForFileParameter param = new HttpRequestPayloadForFileParameter();
		@Cleanup InputStream fis = new FileInputStream(file);
		byte[] fileBytes = IOUtils.toByteArray(fis);
		param.setContent(fileBytes);
		param.setMustEncodeToTargetServerCharset(false);
		return param;
	}
	@SuppressWarnings("resource")
	public static HttpRequestPayloadForFileParameter wrap(final Reader reader) throws IOException {
		HttpRequestPayloadForFileParameter param = new HttpRequestPayloadForFileParameter();
		@Cleanup ReaderInputStream ris = new ReaderInputStream(reader);
		byte[] readerBytes = IOUtils.toByteArray(ris);
		param.setContent(readerBytes);
		param.setMustEncodeToTargetServerCharset(true);
		return param;
	}
	public static HttpRequestPayloadForFileParameter wrap(final String str) {
		HttpRequestPayloadForFileParameter param = new HttpRequestPayloadForFileParameter();
		byte[] strBytes = str.getBytes();
		param.setContent(strBytes);
		param.setMustEncodeToTargetServerCharset(true);
		return param;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FLUENT-APIs
/////////////////////////////////////////////////////////////////////////////////////////
	public HttpRequestPayloadForFileParameter withFileName(final String name) {
		_fileName = name;
		return this;
	}
}
