package r01f.ejie.model.shf;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;

import r01f.xml.XMLUtils;
import x43f.ejie.com.X43FNSHF.EjgvDocument;

@RequiredArgsConstructor
@Slf4j
public class SignatureRequestOutputData 
  implements SignatureServiceObject {

	private static final long serialVersionUID = 6262317400678418019L;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private final EjgvDocument _ejgvSignature;

/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public String asString(){
		return new String(Base64.decodeBase64(_ejgvSignature.getBody().getSign()));
	}	
	public String asBase64String(){
		return _ejgvSignature.getBody().getSign();
	}	
	public Document asXMLDocument(){
		Document xml = null;
		try{
			xml = XMLUtils.parse(this.asString());
		}catch(Exception e){
			 log.error("[SignatureService] > Error while decoding signature {}",e.getMessage(),e);
		 }
		return xml;
	}
	
	
		
	
}
