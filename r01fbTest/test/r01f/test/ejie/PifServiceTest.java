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
import r01f.util.types.StringPersistenceUtils;
import r01f.util.types.Strings;
import r01f.xmlproperties.XMLPropertiesGuiceModule;

@Slf4j
public class PifServiceTest {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
	private static final Path TEST_FILE_PATH = Path.from("d:/temp_dev/r01fb/r01fbTestFile.txt");
/////////////////////////////////////////////////////////////////////////////////////////
//  Check at: http://svc.integracion.jakina.ejiedes.net/y31dBoxWAR/appbox
/////////////////////////////////////////////////////////////////////////////////////////
	public static void main(String[] args) {
		
		try {
			// Get a PIF service instance
			Injector injector = Guice.createInjector(new XMLPropertiesGuiceModule(),
													 new PifServiceGuiceModule(AppCode.forId("r01fb"),
															 				   AppComponent.forId("test"),
															 				   "test"));
			
			PifService service = injector.getInstance(PifService.class);
			
			// Create a test file
			FileInputStream input = new FileInputStream(new File(TEST_FILE_PATH.asAbsoluteString()));
			_createTestFile();
			log.info("Upload file: {} with content << {} >>",
					 TEST_FILE_PATH,
					 Strings.of(new File(TEST_FILE_PATH.asAbsoluteString()))
							.removeNewlinesOrCarriageRetuns()
							.asString());
			
			// Do upload
			Path pifDstPath = Path.from("/x42t/r01fb/r01fbTestFile.txt");
			PifFileInfo uploadedFileInfo = service.uploadFile(input,
												  			  pifDstPath,
												  			  true,						// preserve file name 
												  			  1L,TimeUnit.MINUTES);		// do not remove the file from pif for 1h
			log.info(">>UPLOADED!");
			
			// Dowload
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
	private static String _createTestFile() throws IOException {
		String testFileContent = "Hello PIF World!";
		
		StringPersistenceUtils.save(testFileContent,
									TEST_FILE_PATH);
		
		return testFileContent;
	}

}
