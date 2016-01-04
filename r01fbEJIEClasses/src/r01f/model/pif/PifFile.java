package r01f.model.pif;

import java.io.IOException;
import java.io.InputStream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.exceptions.Throwables;
import r01f.io.Streams;
import r01f.patterns.Memoized;
import r01f.types.CanBeRepresentedAsString;
import r01f.types.Path;

@Accessors(prefix="_")
@RequiredArgsConstructor
public class PifFile 
  implements PifObject,
  			 CanBeRepresentedAsString {

	private static final long serialVersionUID = 1760197891958522392L;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final Path _path;
	@Getter private final InputStream _fileIS;	
	/**
	 * If a call to asString() is issued, the underlying input stream is closed
	 * so another call to asString() will fail
	 */
	private Memoized<String> _fileAsString = new Memoized<String>() {
													@Override
													protected String supply() {
														try {
															return Streams.inputStreamAsString(_fileIS);
														} catch(IOException ioEx) {
															throw Throwables.throwUnchecked(ioEx);
														}
													}
											 };
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String asString() {
		return _fileAsString.get();
	}
	public InputStream asInputStream(){
		return this._fileIS;
	}	
	public byte[] asByteArray() throws IOException {
		return Streams.inputStreamBytes(_fileIS);
	}
}
