package r01f.mime;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.tika.mime.MimeTypeException;

import r01f.util.types.collections.CollectionUtils;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

class MimeTypes {
/////////////////////////////////////////////////////////////////////////////////////////
//  
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
															return MimeType.MIME_TYPES.get().forName(mimeTypeName);
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
//--Binary
	public static final MimeTypeGroupDef BINARY = new MimeTypeGroupDef("application/octet-stream",
																	   "application/x-deb");		// debian install package
	
	public static final MimeTypeGroupDef COMPRESSED = new MimeTypeGroupDef("application/zip",
															  			   "application/gzip");
															  			   //"x-rar-compressed",
																		   //"x-tar"
	public static final MimeTypeGroupDef FONT = new MimeTypeGroupDef("application/x-font-ttf",
													  				 "application/font-woff");
//--Document
	public static final MimeTypeGroupDef DOCUMENT = new MimeTypeGroupDef("application/pdf",
																		 
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
	public static final MimeTypeGroupDef WEB = new MimeTypeGroupDef("text/html","application/xhtml+xml",
													 				"text/css",
													 				"application/javascript",
													 				"application/x-www-form-urlencoded");
//--MultiPart
	public static final MimeTypeGroupDef MULTI_PART = new MimeTypeGroupDef("multipart/form-data",
																		   "multipart/mixed",
																		   "multipart/alternative",
																		   "multipart/related",
																		   "multipart/signed",
																		   "multipart/encrypted");
//--Image	
	public static final MimeTypeGroupDef IMAGE = new MimeTypeGroupDef("image/gif",
													   				  "image/jpeg",
													   				  "image/pjpeg",
													   				  "image/bmp",
													   				  "image/png",
													   				  "image/svg+xml",
													   				  "image/tiff",
													   				  "image/webp");
//--Audio
	public static final MimeTypeGroupDef AUDIO = new MimeTypeGroupDef("audio/basic",
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
	public static final MimeTypeGroupDef VIDEO = new MimeTypeGroupDef("video/mpeg",
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
	public static final MimeTypeGroupDef MODEL3D = new MimeTypeGroupDef("model/example",
														 				"model/iges",
														 				"model/mesh",
														 				"model/vrml",
														 				"model/x3d+binary",
														 				"model/x3d+vrml",
														 				"model/x3d+xml");
//--OpenData
	public static final MimeTypeGroupDef DATA = new MimeTypeGroupDef("text/xml","application/xml",
																	 "application/json",
																	 "text/csv",
																	 "text/vcard",
																	 "application/rdf+xml",
																	 "application/rss+xml","application/atom+xml",
																	 "application/soap+xml");
	
	public static final MimeTypeGroupDef MAP = new MimeTypeGroupDef("application/vnd.google-earth.kml+xml",
																	"application/vnd.google-earth.kmz");
}
