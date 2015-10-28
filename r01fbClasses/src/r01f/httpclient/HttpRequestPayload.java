package r01f.httpclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;

import lombok.Cleanup;
import lombok.experimental.Accessors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;

@Accessors(prefix="_")
public class HttpRequestPayload 
     extends HttpRequestPayloadBase<HttpRequestPayload> {
/////////////////////////////////////////////////////////////////////////////////////////
//  STATUS
/////////////////////////////////////////////////////////////////////////////////////////	
	
/////////////////////////////////////////////////////////////////////////////////////////
//   BUILDERS
/////////////////////////////////////////////////////////////////////////////////////////
	public static HttpRequestPayload wrap(final InputStream is) throws IOException {
		HttpRequestPayload param = new HttpRequestPayload();
		param.setContent(IOUtils.toByteArray(is));
		param.setMustEncodeToTargetServerCharset(false);
		return param;
	}
	public static HttpRequestPayload wrap(final byte[] bytes) {
		HttpRequestPayload param = new HttpRequestPayload();
		param.setContent(bytes);
		param.setMustEncodeToTargetServerCharset(false);
		return param;
	}
	@SuppressWarnings("resource")
	public static HttpRequestPayload wrap(final File file) throws IOException {
		HttpRequestPayload param = new HttpRequestPayload();
		@Cleanup InputStream fis = new FileInputStream(file);
		byte[] fileBytes = IOUtils.toByteArray(fis);
		param.setContent(fileBytes);
		param.setMustEncodeToTargetServerCharset(false);
		return param;
	}
	@SuppressWarnings("resource")
	public static HttpRequestPayload wrap(final Reader reader) throws IOException {
		HttpRequestPayload param = new HttpRequestPayload();
		@Cleanup ReaderInputStream ris = new ReaderInputStream(reader);
		byte[] readerBytes = IOUtils.toByteArray(ris);
		param.setContent(readerBytes);
		param.setMustEncodeToTargetServerCharset(true);
		return param;
	}
	public static HttpRequestPayload wrap(final String str) {
		return HttpRequestPayload.wrap(str,
									   Charset.defaultCharset());
	}
	public static HttpRequestPayload wrap(final String str,
										  final Charset charset) {
		HttpRequestPayload param = new HttpRequestPayload();
		byte[] strBytes = str.getBytes(charset);
		param.setContent(strBytes);
		param.setMustEncodeToTargetServerCharset(true);
		return param;
	}
}
