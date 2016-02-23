package r01f.mime;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypesFactory;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import r01f.patterns.Memoized;
import r01f.resources.ResourcesLoader;
import r01f.resources.ResourcesLoaderBuilder;
import r01f.util.types.collections.CollectionUtils;

public class MimeTypes {
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
	public static MimeType TEXT_PLAIN = MimeType.forName("text/plain");																			
	public static MimeType APPLICATION_XML = MimeType.forName("application/xml");
	public static MimeType APPLICATION_JSON = MimeType.forName("application/json");
	public static MimeType OCTECT_STREAM = MimeType.forName("application/octet-stream");
	public static MimeType XHTML = MimeType.forName("application/xhtml+xml");
	public static MimeType HTML = MimeType.forName("text/html");
	public static MimeType JAVASCRIPT = MimeType.forName("application/javascript");
	public static MimeType STYLESHEET = MimeType.forName("text/css");
	
	static org.apache.tika.mime.MimeType CSS_MIME = MimeTypes.mimeTypeFor("text/css");
	static org.apache.tika.mime.MimeType LESS_MIME = MimeTypes.mimeTypeFor("text/x-less");
	static org.apache.tika.mime.MimeType JS_MIME = MimeTypes.mimeTypeFor("application/javascript");
	static org.apache.tika.mime.MimeType HTML_MIME = MimeTypes.mimeTypeFor("text/html");
	static org.apache.tika.mime.MimeType XHTML_MIME = MimeTypes.mimeTypeFor("application/xhtml+xml");
	
	static org.apache.tika.mime.MimeType mimeTypeFor(final String mediaTypeName) {
		org.apache.tika.mime.MimeType outMimeType = null;
		try {
			outMimeType = MIME_TYPES.get().forName(mediaTypeName);
		} catch (MimeTypeException mimeEx) {
			mimeEx.printStackTrace(System.out);
		} 
		return outMimeType;
	}
	
/////////////////////////////////////////////////////////////////////////////////////////
//  MimeType Groups
/////////////////////////////////////////////////////////////////////////////////////////
	public static class MimeTypeGroupDef {
		private final Collection<org.apache.tika.mime.MimeType> _mimeTypes;
		private final Map<org.apache.tika.mime.MimeType,Collection<String>> _extensions;
		
		public MimeTypeGroupDef(final String... mimeTypes) {
			_mimeTypes = Collections2.transform(CollectionUtils.of(mimeTypes).asCollection(),
												new Function<String,org.apache.tika.mime.MimeType>() {
														@Override
														public org.apache.tika.mime.MimeType apply(final String mimeTypeName) {
															try {
																return MIME_TYPES.get().forName(mimeTypeName);
															} catch (MimeTypeException mimeEx) {
																mimeEx.printStackTrace();
															}
															return null;
														}
												});
			_extensions = new HashMap<org.apache.tika.mime.MimeType,Collection<String>>();
			for (org.apache.tika.mime.MimeType mimeType : _mimeTypes) {
				Collection<String> extensions = mimeType.getExtensions();
				if (CollectionUtils.hasData(extensions)) _extensions.put(mimeType,extensions);
			}
		}
		public boolean contains(final org.apache.tika.mime.MimeType mimeType) {
			return _mimeTypes.contains(mimeType);
		}
		public org.apache.tika.mime.MimeType mimeTypeForFileExtension(final String ext) {
			org.apache.tika.mime.MimeType outMime = null;
			if (CollectionUtils.hasData(_extensions)) {
				for (Map.Entry<org.apache.tika.mime.MimeType,Collection<String>> me : _extensions.entrySet()) {
					if (me.getValue().contains(ext)) {
						outMime = me.getKey();
						break;
					}
				}
			}
			return outMime;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
//--Text/plain
	static final MimeTypeGroupDef TEXT_PLAIN_GRP = new MimeTypeGroupDef("text/plain");
//--Binary
	static final MimeTypeGroupDef BINARY_GRP = new MimeTypeGroupDef("application/octet-stream",
																	"application/x-deb");		// debian install package
	
	static final MimeTypeGroupDef COMPRESSED_GRP = new MimeTypeGroupDef("application/zip",
													  			        "application/gzip");
													  			      //"x-rar-compressed",
																      //"x-tar"
	static final MimeTypeGroupDef FONT_GRP = new MimeTypeGroupDef("application/x-font-ttf",
													  			  "application/font-woff");
//--Document
	static final MimeTypeGroupDef DOCUMENT_GRP = new MimeTypeGroupDef("application/pdf",
																		 
																      "application/vnd.ms-excel","application/msexcel",
												  				      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",			// excel 2007+
												  				      "application/vnd.oasis.opendocument.spreadsheet",
												  				      
												  				      "application/vnd.ms-powerpoint","application/mspowerpoint",
												  				      "application/vnd.openxmlformats-officedocument.presentationml.presentation",	// powerpoint 2007+
												  				      "application/vnd.oasis.opendocument.presentation",
												  				      
												  				      "application/vnd.ms-word","application/msword","application/msword2","application/msword5",
												  				      "application/vnd.openxmlformats-officedocument.wordprocessingml.document",		// word 2007+
												  				      "application/vnd.oasis.opendocument.text",
												  				      
												  				      "application/vnd.ms-visio","application/vnd.visio",
												  				      "application/vnd.oasis.opendocument.graphics",
												  				      
												  				      "application/vnd.ms-outlook",
												  				      
												  				      "application/postscript");
//--HTML
	static final MimeTypeGroupDef WEB_GRP = new MimeTypeGroupDef("text/html","application/xhtml+xml",
													 				    "text/css",
													 				    "application/javascript",
													 				    "application/x-www-form-urlencoded");
//--MultiPart
	static final MimeTypeGroupDef MULTI_PART_GRP = new MimeTypeGroupDef("multipart/form-data",
																		"multipart/mixed",
																		"multipart/alternative",
																		"multipart/related",
																		"multipart/signed",
																		"multipart/encrypted");
//--Image	
	static final MimeTypeGroupDef IMAGE_GRP = new MimeTypeGroupDef("image/gif",
													   		       "image/jpeg",
													   		       "image/pjpeg",
													   		       "image/bmp",
													   		       "image/png",
													   		       "image/svg+xml",
													   		       "image/tiff",
													   		       "image/webp");
//--Audio
	static final MimeTypeGroupDef AUDIO_GRP = new MimeTypeGroupDef("audio/basic",
													   		       "audio/L24",
													   		       "audio/mp3",
													   		       "audio/mp4",
													   		       "audio/mpeg",
													   		       "audio/ogg",
													   		       "audio/vorbis",
													   		       "audio/vnd.rn-realaudio",
													   		       "audio/vnd.wave",
													   		       "audio/webm",
													   		       "audio/x-aac");
//--Video
	static final MimeTypeGroupDef VIDEO_GRP = new MimeTypeGroupDef("video/mpeg",
													   		       "video/mp4",
													   		       "video/ogg",
													   		       "video/quicktime",
													   		       "video/webm",
													   		       "video/x-matroska",
													   		       "video/x-ms-wmv",
													   		       "video/x-flv");
//--Flash
//	public static final MimeTypeGroupDef FLASH = new MimeTypeGroupDef("x-shockwave-flash");
	
//--Model3D
	static final MimeTypeGroupDef MODEL3D_GRP = new MimeTypeGroupDef("model/example",
														 			 "model/iges",
														 			 "model/mesh",
														 			 "model/vrml",
														 			 "model/x3d+binary",
														 			 "model/x3d+vrml",
														 			 "model/x3d+xml");
//--OpenData
	static final MimeTypeGroupDef DATA_GRP = new MimeTypeGroupDef("text/xml","application/xml",
															      "application/json",
															      "text/csv",
															      "text/vcard",
															      "application/rdf+xml",
															      "application/rss+xml","application/atom+xml",
															      "application/soap+xml");
	
	static final MimeTypeGroupDef MAP_GRP = new MimeTypeGroupDef("application/vnd.google-earth.kml+xml",
																 "application/vnd.google-earth.kmz");
}
