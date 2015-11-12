package r01f.test.ejie;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.google.inject.Guice;
import com.google.inject.Injector;

import lombok.extern.slf4j.Slf4j;
import r01f.guids.CommonOIDs.AppCode;
import r01f.guids.CommonOIDs.AppComponent;
import r01f.model.pif.PifFile;
import r01f.model.pif.PifFileInfo;
import r01f.services.pif.PifService;
import r01f.services.pif.PifServiceGuiceModule;
import r01f.types.Path;
import r01f.util.types.Strings;
import r01f.xmlproperties.XMLPropertiesGuiceModule;

@Slf4j
public class TestPifService {
/////////////////////////////////////////////////////////////////////////////////////////
//  Check at: http://svc.integracion.jakina.ejiedes.net/y31dBoxWAR/appbox
/////////////////////////////////////////////////////////////////////////////////////////
	public static void main(String[] args) {
		
		try {
			Injector injector = Guice.createInjector(new XMLPropertiesGuiceModule(),
													 new PifServiceGuiceModule(AppCode.forId("r01fb"),
															 				   AppComponent.forId("test"),
															 				   "test"));
			Path testFilePath = Path.of("d:/temp_dev/r01fb/r01fbTestFile.txt");
			PifService service = injector.getInstance(PifService.class);
			FileInputStream input = new FileInputStream(new File(testFilePath.asAbsoluteString()));
			
			log.info("Upload file: {} with content << {} >>",
					 testFilePath,
					 Strings.of(new File(testFilePath.asAbsoluteString()))
							.removeNewlinesOrCarriageRetuns()
							.asString());
			
			Path pifDstPath = Path.of("/x42t/r01fb/r01fbTestFile.txt");
			PifFileInfo uploadedFileInfo = service.uploadFile(input,
												  			  pifDstPath,
												  			  true,						// preserve file name 
												  			  1L,TimeUnit.MINUTES);		// do not remove the file from pif for 1h
			PifFile downloadedFile = service.downloadFile(uploadedFileInfo.getFilePath());
			
			log.info("Downloaded PIF file from {}: << {} >>",
					 pifDstPath,
					 Strings.of(downloadedFile.asString())
					 		.removeNewlinesOrCarriageRetuns()
					 		.asString());
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
