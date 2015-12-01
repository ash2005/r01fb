package r01f.test.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import r01f.io.CharacterStreamSource;
import r01f.io.UnReadableLineReader;

public class IOTest {
/////////////////////////////////////////////////////////////////////////////////////////
//  CharacterStreamSource
/////////////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testCharacterStreamSource() {
		int times = 1001;
 		StringBuilder src = new StringBuilder();
 		for (int i=0; i < times; i++) {
 			src.append("[").append(i).append("]:")
 			   .append("0123456789");
 			if (i < times-1) src.append("_");
 		}
 		
 		try {
	 		String readedStr = "";
	 		CharacterStreamSource source = new CharacterStreamSource(new ByteArrayInputStream(src.toString().getBytes()),
	 																 Charset.defaultCharset());
	 		int i=0;
	 		while(source.hasData()) {
	 			int bufSize = 1024;
	 			char[] readBuf = new char[bufSize];
	 			int readed = source.read(readBuf);
	 			String readedSlice = new String(Arrays.copyOf(readBuf,readed));
	 			readedStr += readedSlice;
	 
	 			System.out.print("====> " + i + ": '" + readedSlice + "'\n");
	 			i++;
	 		}
	 		
	 		System.out.println("Original: '" + src + "'");
	 		System.out.println("  Readed: '" + readedStr + "'");
	 		Assert.assertTrue(readedStr.equals(src.toString()));
 		} catch(Throwable th) {
 			th.printStackTrace(System.out);
 		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  UnReadableLineReader
/////////////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testUnReadableLineReader() throws IOException {
		String src = "1\n2\n3\n4\n5\n6\n7\n8\n9\n10";
		UnReadableLineReader lr = new UnReadableLineReader(new StringReader(src),
														   5);
		
		StringBuilder sb = new StringBuilder();
		String l = null;
		int i=1;
		do {
			l = lr.readLine();
			if (l != null) sb.append(l).append("\n");
			if (i == 5) lr.unreadLines(4);
			i++;
		} while(l != null);
		
		String shouldHaveRead = "1\n2\n3\n4\n5\n" +
						  		"2\n3\n4\n5\n" +	// unreaded
						  		"6\n7\n8\n9\n10\n";
		Assert.assertTrue(shouldHaveRead.equals(sb.toString()));
	}
}
