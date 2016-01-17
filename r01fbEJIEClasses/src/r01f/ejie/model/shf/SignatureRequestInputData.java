package r01f.ejie.model.shf;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;



/**
 * Data to be signed
 */
@NoArgsConstructor
@Accessors(prefix="_")
public class SignatureRequestInputData 
  implements SignatureServiceObject{

	private static final long serialVersionUID = -6157048459531863608L;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private InputStream  _dataToBeSigned; 
	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	 public void setInput(final String text){
		 _dataToBeSigned  = new ByteArrayInputStream(text.getBytes());
	 }	 
	 public void setInput(final InputStream input){
		 _dataToBeSigned = input;
	 }

}
