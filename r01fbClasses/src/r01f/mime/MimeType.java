package r01f.mime;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.tika.Tika;
import org.apache.tika.metadata.HttpHeaders;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaMetadataKeys;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypesFactory;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.patterns.Memoized;
import r01f.resources.ResourcesLoader;
import r01f.resources.ResourcesLoaderBuilder;
import r01f.util.types.Strings;

/**
 * Encapsulates a {@link MimeType}
 * Usage:
 * <pre class='brush:java'>
 * 		MimeType mime = MimeType.forName("application/vnd.google-earth.kml+xml");
 * 		Collection<String> extensions = mime.getExtensions();
 * </pre>
 * 
 * @see http://filext.com/
 */
@XmlRootElement(name="mimeType")
@Accessors(prefix="_")
@RequiredArgsConstructor
public class MimeType {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@XmlAttribute
	@Getter private final org.apache.tika.mime.MimeType _mimeType;
	
	@Override
	public String toString() {
		return _mimeType.toString();
	}
	public static MimeType fromString(final String mimeType) {
		return MimeType.forName(mimeType);
	}
	public static MimeType valueOf(final String mimeType) {
		return MimeType.forName(mimeType);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public boolean isBinary() {
		return MimeTypes.BINARY.contains(_mimeType);
	}
	public boolean isCompressed() {
		return MimeTypes.COMPRESSED.contains(_mimeType);
	}
	public boolean isFont() {
		return MimeTypes.FONT.contains(_mimeType);
	}
	public boolean isDocument() {
		return MimeTypes.DOCUMENT.contains(_mimeType);
	}
	public boolean isWeb() {
		return MimeTypes.DOCUMENT.contains(_mimeType);
	}
	public boolean isHtml() {
		return _mimeType.equals(HTML_MIME) || _mimeType.equals(XHTML_MIME);
	}
	public boolean isStyleSheet() {
		return _mimeType.equals(CSS_MIME) || _mimeType.equals(LESS_MIME);
	}
	public boolean isJavaScript() {
		return _mimeType.equals(JS_MIME);
	}
	public boolean isMultiPart() {
		return MimeTypes.MULTI_PART.contains(_mimeType);
	}
	public boolean isImage() {
		return MimeTypes.IMAGE.contains(_mimeType);
	}
	public boolean isAudio() {
		return MimeTypes.AUDIO.contains(_mimeType);
	}
	public boolean isVideo() {
		return MimeTypes.VIDEO.contains(_mimeType);
	}
	public boolean is3DModel() {
		return MimeTypes.MODEL3D.contains(_mimeType);
	}
	public boolean isMap() {
		return MimeTypes.MAP.contains(_mimeType);
	}
	public boolean isData() {
		return MimeTypes.DATA.contains(_mimeType);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Detects the {@link MimeType} from the bytes stream
	 * @see http://tika.apache.org/1.4/detection.html
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static MimeType from(final InputStream is) throws IOException {
		return MimeType.from(is,
					 		 null,null); 
	}
	/**
	 * Detects the {@link MimeType} from the bytes stream
	 * @see http://tika.apache.org/1.4/detection.html
	 * @param is
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static MimeType from(final InputStream is,
								final String fileName) throws IOException {
		return MimeType.from(is,
							 fileName,null);
	}
	/**
	 * Detects the {@link MimeType} from the bytes stream
	 * @see http://tika.apache.org/1.4/detection.html
	 * @param is
	 * @param fileName
	 * @param contentType
	 * @return
	 * @throws IOException
	 */
	public static MimeType from(final InputStream is,
								final String fileName,final String contentType) throws IOException {
		Metadata md = new Metadata();
		if (Strings.isNOTNullOrEmpty(fileName)) md.add(TikaMetadataKeys.RESOURCE_NAME_KEY ,fileName);
		if (Strings.isNOTNullOrEmpty(contentType)) md.add(HttpHeaders.CONTENT_TYPE,contentType);
		
		Tika tika = new Tika();
		String mimeTypeStr = tika.detect(is,fileName);
		
		return MimeType.forName(mimeTypeStr);
	}
	
	/** 
	 * @return the possible file extensions for the {@link MimeType}
	 */
	public Collection<String> getFileExtensions() {
		org.apache.tika.mime.MimeType mimeType = _mimeTypeFor(_mimeType.getName());
		return mimeType != null ? mimeType.getExtensions()
								: null;
	}
	public static MimeType forFileExtension(final String fileExtension) {
		String theExt = fileExtension.startsWith(".") ? fileExtension : ("." + fileExtension);	// ensure the file extension starts with a dot
		
		org.apache.tika.mime.MimeType mimeType = null;
							  mimeType = MimeTypes.BINARY.mimeTypeForFileExtension(theExt);
		if (mimeType == null) mimeType = MimeTypes.COMPRESSED.mimeTypeForFileExtension(theExt);
		if (mimeType == null) mimeType = MimeTypes.FONT.mimeTypeForFileExtension(theExt);
		if (mimeType == null) mimeType = MimeTypes.DOCUMENT.mimeTypeForFileExtension(theExt);
		if (mimeType == null) mimeType = MimeTypes.WEB.mimeTypeForFileExtension(theExt);
		if (mimeType == null) mimeType = MimeTypes.IMAGE.mimeTypeForFileExtension(theExt);
		if (mimeType == null) mimeType = MimeTypes.AUDIO.mimeTypeForFileExtension(theExt);
		if (mimeType == null) mimeType = MimeTypes.VIDEO.mimeTypeForFileExtension(theExt);
		if (mimeType == null) mimeType = MimeTypes.MODEL3D.mimeTypeForFileExtension(theExt);
		if (mimeType == null) mimeType = MimeTypes.DATA.mimeTypeForFileExtension(theExt);
		if (mimeType == null) mimeType = MimeTypes.MAP.mimeTypeForFileExtension(theExt);
		
		return mimeType != null ? new MimeType(mimeType) 
								: null;
	}
	/**
	 * Returns the 
	 * @param mediaTypeName
	 * @return
	 */
	public static MimeType forName(final String mediaTypeName) {
		org.apache.tika.mime.MimeType mimeType = _mimeTypeFor(mediaTypeName);
		return new MimeType(mimeType);
	}
	private static org.apache.tika.mime.MimeType _mimeTypeFor(final String mediaTypeName) {
		org.apache.tika.mime.MimeType outMimeType = null;
		try {
			outMimeType = MIME_TYPES.get().forName(mediaTypeName);
		} catch (MimeTypeException mimeEx) {
			mimeEx.printStackTrace(System.out);
		} 
		return outMimeType;
	}
	public org.apache.tika.mime.MimeType getType() {
		return _mimeType;
	}
	public String getTypeName() {
		return _mimeType.getName();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	static Memoized<org.apache.tika.mime.MimeTypes> MIME_TYPES = new Memoized<org.apache.tika.mime.MimeTypes>() {
																					@Override
																					protected org.apache.tika.mime.MimeTypes supply() {
																							ResourcesLoader resLoader = ResourcesLoaderBuilder.createDefaultResourcesLoader();
																							try {
																								return MimeTypesFactory.create(resLoader.getInputStream("org/apache/tika/mime/tika-mimetypes.xml"),		// tika's core (located at tika-core.jar)
																															   resLoader.getInputStream("org/apache/tika/mime/custom-mimetypes.xml"));	// tika's extension (located at R01F)
																							} catch (Throwable th) {
																								th.printStackTrace();
																								throw new InternalError(th.getMessage());
																							} 
																					}
																			};
	public static MimeType APPLICATION_XML = MimeType.forName("application/xml");
	public static MimeType APPLICATION_JSON = MimeType.forName("application/json");
	public static MimeType OCTECT_STREAM = MimeType.forName("application/octet-stream");
	public static MimeType XHTML = MimeType.forName("application/xhtml+xml");
	public static MimeType HTML = MimeType.forName("text/html");
	public static MimeType JAVASCRIPT = MimeType.forName("application/javascript");
	public static MimeType STYLESHEET = MimeType.forName("text/css");
	
	private static org.apache.tika.mime.MimeType CSS_MIME = _mimeTypeFor("text/css");
	private static org.apache.tika.mime.MimeType LESS_MIME = _mimeTypeFor("text/x-less");
	private static org.apache.tika.mime.MimeType JS_MIME = _mimeTypeFor("application/javascript");
	private static org.apache.tika.mime.MimeType HTML_MIME = _mimeTypeFor("text/html");
	private static org.apache.tika.mime.MimeType XHTML_MIME = _mimeTypeFor("application/xhtml+xml");
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
//	static class MimeTypeMarshaller 
//	  implements XmlReadCustomTransformer<MimeType>,
//	  			 XmlWriteCustomTransformer {
//		@Override
//		public MimeType beanFromXml(final CharSequence xml) {
//			if (Strings.isNullOrEmpty(xml)) {
//				return MimeType.forName(xml.toString());
//			}
//			return null;
//		}
//		@Override
//		public String xmlFromBean(final Object bean) {
//			if (bean == null) return null;
//			if (bean instanceof MimeType) {
//				return ((MimeType)bean).getTypeName();
//			}
//			throw new IllegalArgumentException(Throwables.message("{} is not a {}",bean.getClass(),MimeType.class));
//		}
//		
//	}
//	public static void main(String[] args) {
//		Marshaller m = SimpleMarshaller.createForPackages("r01f")
//									   .getForSingleUse();
//		String xml = m.xmlFromBean(MimeType.forName("text/css"));
//		System.out.println(xml);
//	}
}
