package r01f.httpclient;

import static r01f.httpclient.HttpRequestFormParameterForMultiPartBinaryData.HttpRequestFormBinaryParameterTransferEncoding.BASE64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.codec.binary.Base64;

import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

import com.google.common.collect.Lists;

/**
 * Creates a param to be send to the server using a POSTed form which could be:
 * <ol>
 * 		<li>a param in a form-url-encoded post</li>
 * 		<li>a form param in a multi-part post</li>
 * </ol>
 * The creation of a param is like:
 * <pre class='brush:java'>
 * 		HttpRequestFormParameter param = HttpRequestFormParameterForMultiPartBinaryData.of(HttpRequestPayloadForFileParameter.wrap(new File("d:/myFile.txt"))
 * 																											 	  		   	 .withFileName("myFile.txt"),
 * 																		   				   HttpRequestPayloadForFileParameter.wrap(new File("d:/myOtherFile.gif")
 * 																											 	  		   	 .withFileName("myImage.gif")
 * 																											 	  		  	 .mimeType(...))
 * 																	   					.withName("myFiles");
 * </pre>
 */
@Accessors(prefix="_")
@NoArgsConstructor @AllArgsConstructor
public class HttpRequestFormParameterForMultiPartBinaryData 
  implements HttpRequestFormParameter {
/////////////////////////////////////////////////////////////////////////////////////////
//  STATE
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter @Setter(AccessLevel.PRIVATE) private String _name;
	@Getter @Setter(AccessLevel.PRIVATE) private List<HttpRequestPayloadForFileParameter> _fileParts;
			
/////////////////////////////////////////////////////////////////////////////////////////
//  ENCODING
// 	The encoding used to transfer the content
//  (see http://www.w3.org/Protocols/rfc1341/5_Content-Transfer-Encoding.html)
/////////////////////////////////////////////////////////////////////////////////////////
	public static enum HttpRequestFormBinaryParameterTransferEncoding {
		BASE64,		// The content is sent encoded in Base64
		BINARY;		// The content is sent in it's binary form 
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  BUILDERS
/////////////////////////////////////////////////////////////////////////////////////////
	public static HttpRequestFormParameterForMultiPartBinaryData of(final HttpRequestPayloadForFileParameter... parts) {
		HttpRequestFormParameterForMultiPartBinaryData param = new HttpRequestFormParameterForMultiPartBinaryData();
		param.setFileParts(Lists.newArrayList(parts));
		return param;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FLUENT-API
/////////////////////////////////////////////////////////////////////////////////////////
	public HttpRequestFormParameterForMultiPartBinaryData withName(final String name) {
		_name = name;
		return this;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public byte[] _serializeFormParam(final Charset targetServerCharset,
									  final boolean multiPart) throws IOException {
		if (!multiPart) throw new IOException("A file parameter must be POSTed in a multi-part form");
		
		Charset theTargetServerCharset = targetServerCharset == null ? Charset.defaultCharset()
																	 : targetServerCharset;
		
		byte[] outBytes = null;
		// If only one file is sent the serialized format is:
		// 		Content-Disposition: form-data; name="files"; filename="myImage.gif"
		// 		Content-Type: image/gif
		// 		Content-Transfer-Encoding: BASE64 (or BINARY)
		//
		// 		... contents of myImage.gif ...
		//
		// If multiple files are sent the serialized format is:
		//		Content-Disposition: form-data; name="files"
		//		Content-Type: multipart/mixed; boundary=BbC04y
		//
		//		--MXBOUNDARY
		// 		Content-Disposition: file; filename="file1.txt"
		// 		Content-Type: text/plain
		// 		Content-Transfer-Encoding: BASE64 (or BINARY)
		//
		// 		... contents of file1.txt ...
		//		--MXBOUNDARY
		// 		Content-Disposition: file; filename="myImage.gif"
		// 		Content-Type: image/gif
		// 		Content-Transfer-Encoding: BASE64 (or BINARY)
		//
		// 		... contents of myImage.gif ...
		//		--MXBOUNDARY--
		if (_fileParts.size() == 1) {
			// FilePart header
			HttpRequestPayloadForFileParameter filePart = CollectionUtils.of(_fileParts)
																       .pickOneAndOnlyElement();
			byte[] headerBytes = Strings.of("Content-Disposition: form-data; name=\"{}\"; filename=\"{}\"\r\n" +
											"Content-Type: {}\r\n" +
											"Content-Transfer-Encoding: {}\r\n\r\n")
									    .customizeWith(_name,
									    			   filePart.getFileName(),
									    			   filePart.getMimeType().getTypeName(),
									    			   filePart.getTransferEncoding().toString().toLowerCase())
									    .getBytes(theTargetServerCharset);
			// File Part contents
			byte[] contentBytes = null;
			contentBytes = filePart.mustEncodeToTargetServerCharset() ? Strings.of(filePart.getContent())
																	 	       .getBytes(theTargetServerCharset)		// encode the source String to the target server encoding
																      : filePart.getContent();
			contentBytes = filePart.getTransferEncoding() == BASE64 ? Base64.encodeBase64(contentBytes)
													   			    : contentBytes;	 
									
			ByteArrayOutputStream bos = new ByteArrayOutputStream(headerBytes.length + contentBytes.length);
			bos.write(headerBytes);
			bos.write(contentBytes);
			bos.write("\r\n".getBytes());
			bos.flush();
			bos.close();
			outBytes = bos.toByteArray();
			
		} else {
			// Multiple file parts header
			byte[] headerBytes = Strings.of("Content-Disposition: form-data; name=\"{}\"\r\n" +
											"Content-Type: multipart/mixed; boundary={}\r\n\r\n")
										.customizeWith(_name,
													   "**R01MXBOUNDR01**")
										.getBytes(theTargetServerCharset);
			
			List<byte[]> partsBytes = new ArrayList<byte[]>(_fileParts.size());
			int partsBytesLength = 0;
			for(HttpRequestPayloadForFileParameter filePart : _fileParts) {
				// Current file part header
				byte[] partHeaderBytes = Strings.of("--**R01MXBOUNDR01**\r\n" +
													"Content-Disposition: file; filename=\"{}\"\r\n" +
													"Content-Type: {}\r\n" + 
													"Content-Transfer-Encoding: {}\r\n\r\n")
											    .customizeWith(filePart.getFileName(),
											    			   filePart.getMimeType().getTypeName(),
											    			   filePart.getTransferEncoding().toString().toLowerCase())
											    .getBytes(theTargetServerCharset);
				// Current file part contents
				byte[] contentBytes = null;
				contentBytes = filePart.mustEncodeToTargetServerCharset() ? Strings.of(filePart.getContent())
																		 	       .getBytes(theTargetServerCharset)		// encode the source String to the target server encoding
																	      : filePart.getContent();
				contentBytes = filePart.getTransferEncoding() == BASE64 ? Base64.encodeBase64(contentBytes)
														   			    : contentBytes;
				ByteArrayOutputStream partBos = new ByteArrayOutputStream(partHeaderBytes.length + contentBytes.length);
				partBos.write(partHeaderBytes);
				partBos.write(contentBytes);
				partBos.write("\r\n".getBytes());
				partBos.flush();
				partBos.close();
				partsBytes.add(partBos.toByteArray());
				partsBytesLength = partsBytesLength + partBos.size();
			}
			
			// all together: multiple file parts header + each part (header / contents) + end
			ByteArrayOutputStream bos = new ByteArrayOutputStream(headerBytes.length + partsBytesLength);
			
			bos.write(headerBytes);						// header
			for(byte[] partBytes : partsBytes) {		// parts
				bos.write(partBytes);
			}
			bos.write("--**R01MXBOUNDR01**--\r\n".getBytes());	// boundary end
			
			bos.flush();
			bos.close();
			outBytes = bos.toByteArray();
		}
		return outBytes != null ? outBytes
								: null;
	}
}
