package r01f.services.shf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.w3c.dom.Document;

import r01f.ejie.model.shf.SignatureRequestOutputData;
import r01f.ejie.model.shf.SignatureVerifyOutputData;

public interface SignatureServiceForApp {

	public SignatureRequestOutputData createXAdESSignatureOf(final String dataToBeSigned);
	public SignatureRequestOutputData createXAdESSignatureOf(final InputStream dataToBeSigned) throws IOException;
	public SignatureRequestOutputData createXAdESSignatureOf(final byte[] dataToBeSigned);
	public SignatureRequestOutputData createXAdESSignatureOf(final File fileToBeSigned) throws IOException;
	public SignatureRequestOutputData createXAdESSignatureOf(final URL urlToBeSigned) throws IOException;
	public SignatureVerifyOutputData verifyXAdESSignature(final InputStream signedData, final InputStream signature) throws IOException;
	public SignatureVerifyOutputData verifyXAdESSignature(final InputStream signedData, final Document signature) throws IOException;
	public SignatureVerifyOutputData verifyXAdESSignature(final String signedData,final Document signature);
	public SignatureVerifyOutputData verifyXAdESSignature(final String signedData,final String signature);
	public SignatureVerifyOutputData verifyXAdESSignature(final byte[] signedData,final byte[] signature);
	
}
