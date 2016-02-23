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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
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
	public boolean isTextPlain() {
		return MimeTypes.TEXT_PLAIN_GRP.contains(_mimeType);
	}
	public boolean isBinary() {
		return MimeTypes.BINARY_GRP.contains(_mimeType);
	}
	public boolean isCompressed() {
		return MimeTypes.COMPRESSED_GRP.contains(_mimeType);
	}
	public boolean isFont() {
		return MimeTypes.FONT_GRP.contains(_mimeType);
	}
	public boolean isDocument() {
		return MimeTypes.DOCUMENT_GRP.contains(_mimeType);
	}
	public boolean isWeb() {
		return MimeTypes.WEB_GRP.contains(_mimeType);
	}
	public boolean isHtml() {
		return _mimeType.equals(MimeTypes.HTML_MIME) || _mimeType.equals(MimeTypes.XHTML_MIME);
	}
	public boolean isStyleSheet() {
		return _mimeType.equals(MimeTypes.CSS_MIME) || _mimeType.equals(MimeTypes.LESS_MIME);
	}
	public boolean isJavaScript() {
		return _mimeType.equals(MimeTypes.JS_MIME);
	}
	public boolean isMultiPart() {
		return MimeTypes.MULTI_PART_GRP.contains(_mimeType);
	}
	public boolean isImage() {
		return MimeTypes.IMAGE_GRP.contains(_mimeType);
	}
	public boolean isAudio() {
		return MimeTypes.AUDIO_GRP.contains(_mimeType);
	}
	public boolean isVideo() {
		return MimeTypes.VIDEO_GRP.contains(_mimeType);
	}
	public boolean is3DModel() {
		return MimeTypes.MODEL3D_GRP.contains(_mimeType);
	}
	public boolean isMap() {
		return MimeTypes.MAP_GRP.contains(_mimeType);
	}
	public boolean isData() {
		return MimeTypes.DATA_GRP.contains(_mimeType);
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
		org.apache.tika.mime.MimeType mimeType = MimeTypes.mimeTypeFor(_mimeType.getName());
		return mimeType != null ? mimeType.getExtensions()
								: null;
	}
	public static MimeType forFileExtension(final String fileExtension) {
		String theExt = fileExtension.startsWith(".") ? fileExtension : ("." + fileExtension);	// ensure the file extension starts with a dot
		
		org.apache.tika.mime.MimeType mimeType = null;
							  mimeType = MimeTypes.TEXT_PLAIN_GRP.mimeTypeForFileExtension(theExt);
		if (mimeType == null) mimeType = MimeTypes.BINARY_GRP.mimeTypeForFileExtension(theExt);
		if (mimeType == null) mimeType = MimeTypes.COMPRESSED_GRP.mimeTypeForFileExtension(theExt);
		if (mimeType == null) mimeType = MimeTypes.FONT_GRP.mimeTypeForFileExtension(theExt);
		if (mimeType == null) mimeType = MimeTypes.DOCUMENT_GRP.mimeTypeForFileExtension(theExt);
		if (mimeType == null) mimeType = MimeTypes.WEB_GRP.mimeTypeForFileExtension(theExt);
		if (mimeType == null) mimeType = MimeTypes.IMAGE_GRP.mimeTypeForFileExtension(theExt);
		if (mimeType == null) mimeType = MimeTypes.AUDIO_GRP.mimeTypeForFileExtension(theExt);
		if (mimeType == null) mimeType = MimeTypes.VIDEO_GRP.mimeTypeForFileExtension(theExt);
		if (mimeType == null) mimeType = MimeTypes.MODEL3D_GRP.mimeTypeForFileExtension(theExt);
		if (mimeType == null) mimeType = MimeTypes.DATA_GRP.mimeTypeForFileExtension(theExt);
		if (mimeType == null) mimeType = MimeTypes.MAP_GRP.mimeTypeForFileExtension(theExt);
		
		return mimeType != null ? new MimeType(mimeType) 
								: null;
	}
	/**
	 * Returns the 
	 * @param mediaTypeName
	 * @return
	 */
	public static MimeType forName(final String mediaTypeName) {
		org.apache.tika.mime.MimeType mimeType = MimeTypes.mimeTypeFor(mediaTypeName);
		return new MimeType(mimeType);
	}
	public org.apache.tika.mime.MimeType getType() {
		return _mimeType;
	}
	public String getTypeName() {
		return _mimeType.getName();
	}
}
