package r01f.file;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.aspects.interfaces.dirtytrack.ConvertToDirtyStateTrackable;
import r01f.patterns.Memoized;
import r01f.types.annotations.Inmutable;

/**
 * Models a file name like myFile.ext
 */
@Inmutable
@ConvertToDirtyStateTrackable
@XmlRootElement(name="file")
@RequiredArgsConstructor
@Accessors(prefix="_")
public class FileName
  implements Serializable {
	
	private static final long serialVersionUID = -7901960255575168878L;
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@XmlAttribute(name="name")
	@Getter private final String _fileName;
/////////////////////////////////////////////////////////////////////////////////////////
//  BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public static FileName of(final String fileName) {
		return new FileName(fileName);
	}
	public static FileName valueOf(final String fileName) {
		return new FileName(fileName);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the file name part
	 */
	public String getName() {
		return _fileNameAndExtension.get()[0];
	}
	/**
	 * @return the file extension part
	 */
	public String getExtension() {
		return _fileNameAndExtension.get()[1];
	}		
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private Memoized<String[]> _fileNameAndExtension = new Memoized<String[]>() {
																@Override
																protected String[] supply() {
																	return Files.fileNameAndExtension(_fileName);
																}
													   };												   
}
