package r01f.services.shf;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;

import lombok.RequiredArgsConstructor;
import r01f.ejie.model.shf.SignatureRequestOutputData;
import r01f.guids.CommonOIDs.AppCode;
import r01f.types.Path;

/**
 * El servicio horizontal de firma falla más que una escopeta de feria, así 
 * que se utiliza este tipo para re-intentar la firma ante errores
 */
@RequiredArgsConstructor
public class SignRetryer {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final int _maximumTimeSeconds;
	private final int _stopAfterAttempt;
	private final SignatureService _signatureService;
	private final AppCode _requestorAppCode;
/////////////////////////////////////////////////////////////////////////////////////////
//  INTERFACE
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Signs text
	 * @param contenttoBeSigned
	 * @return
	 * @throws Exception
	 */
	public SignatureRequestOutputData signText(final String contenttoBeSigned) throws Exception {
		return _sign(new Callable<SignatureRequestOutputData>() {
							@Override
							public SignatureRequestOutputData call() throws Exception {
								return _signatureService.requiredBy(_requestorAppCode)
														.createXAdESSignatureOf(contenttoBeSigned);
							}
					 });
	}
	/**
	 * Signs a file
	 * @param filetoBeSigned
	 * @return
	 * @throws Exception
	 */
	public SignatureRequestOutputData signFile(final Path filetoBeSigned) throws Exception{
		return _sign(new Callable<SignatureRequestOutputData>() {				
										@Override
										public SignatureRequestOutputData call() throws Exception {
											File f = new File(filetoBeSigned.asAbsoluteString());
											return _signatureService.requiredBy(_requestorAppCode)
																	.createXAdESSignatureOf(f);
										}
					 });
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private SignatureRequestOutputData _sign(final Callable<SignatureRequestOutputData> signCmd) throws Exception{
		SignatureRequestOutputData result = null;
		Retryer<SignatureRequestOutputData> retryer = 
				RetryerBuilder.<SignatureRequestOutputData>newBuilder()
							  .retryIfExceptionOfType(IllegalStateException.class)
							  .retryIfRuntimeException()
							  .withWaitStrategy(WaitStrategies.fibonacciWait(1, 
									  										 _maximumTimeSeconds, 
									  										 TimeUnit.SECONDS))
							  .withStopStrategy(StopStrategies.stopAfterAttempt(_stopAfterAttempt))
							  .build();
		try {
			result = retryer.call(signCmd);
		    
		    
		} catch (RetryException e) {
		    throw new Exception(e.getCause());
		} catch (ExecutionException e) {
		   throw new Exception(e.getCause());
		}
		return result;
	}
}
