package r01f.test.types;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.junit.Assert;
import org.junit.Test;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.marshalling.Marshaller;
import r01f.marshalling.simple.SimpleMarshallerBuilder;
import r01f.types.Path;
import r01f.types.Paths;

public class PathsTest {
	@Test
	public void asStringConversionMethodsTest() {
		Path test1 = new Path(Arrays.asList("foo","bar","baz"));
		Assert.assertEquals(test1.asAbsoluteString(),"/foo/bar/baz");
		Assert.assertEquals(test1.asRelativeString(),"foo/bar/baz");
		
		Path test2 = new Path("foo");
		Assert.assertEquals(test2.asAbsoluteString(),"/foo");
		Assert.assertEquals(test2.asRelativeString(),"foo");
		
		Path test3 = new Path(Arrays.asList("foo/bar/baz"));
		Assert.assertEquals(test3.asAbsoluteString(),"/foo/bar/baz");
		Assert.assertEquals(test3.asRelativeString(),"foo/bar/baz");
		
		Path test4 = new Path("/foo///bar//baz/");
		Assert.assertEquals(test4.asAbsoluteString(),"/foo/bar/baz");
		Assert.assertEquals(test4.asRelativeString(),"foo/bar/baz");
		
		Path test5 = new Path(test4);
		Assert.assertEquals(test5.asAbsoluteString(),"/foo/bar/baz");
		Assert.assertEquals(test5.asRelativeString(),"foo/bar/baz");		
	}
	@Test
	public void getterMethodsTest() {
		Path test = new Path("/foo/bar/baz");
		Assert.assertEquals(test.getFirstPathElement(),"foo");
		Assert.assertEquals(test.getLastPathElement(),"baz");
		Assert.assertEquals(test.getPathElementAt(1),"bar");	
		Assert.assertTrue(test.getFirstNPathElements(2).size() == 2 
				       && test.getFirstNPathElements(2).get(0).equals("foo")
				       && test.getFirstNPathElements(2).get(1).equals("bar"));
		Assert.assertTrue(test.getPathElementsFrom(1).size() == 2 
					   && test.getPathElementsFrom(1).get(0).equals("bar")
				       && test.getPathElementsFrom(1).get(1).equals("baz"));
		Assert.assertTrue(test.containsPathElement("foo") && test.containsPathElement("bar") && test.containsPathElement("baz"));
		Assert.assertTrue(test.containsAllPathElements("foo","bar","baz"));
	}
	@Test
	public void pathTypeMethodsTest() {
		Path testFile = new Path("/foo/bar/baz/qux.txt");
		Assert.assertTrue(testFile.isFilePath());
		Assert.assertFalse(testFile.isFolderPath());
		Assert.assertEquals(testFile.getFileExtension(),"txt");
		
		Path testFolder = new Path("/foo/bar/baz");
		Assert.assertFalse(testFolder.isFilePath());
		Assert.assertTrue(testFolder.isFolderPath());
	}
	@Test
	public void pathMarshallingTest() {
		Marshaller marshaller = SimpleMarshallerBuilder.createForPackages(PathsTest.class.getPackage().getName())
													   .getForSingleUse();
		MyTestModelObj testModelObj = MyTestModelObj.createTestInstance();
		String xml = marshaller.xmlFromBean(testModelObj);
		Assert.assertTrue(xml.contains("aPath='" + testModelObj.getAPath().asRelativeString() + "'"));
		Assert.assertTrue(xml.contains("<anotherPath>" + testModelObj.getAnotherPath().asRelativeString() + "</anotherPath>"));
		
		MyTestModelObj testModelObj2 = marshaller.beanFromXml(xml);
		String xml2 = marshaller.xmlFromBean(testModelObj2);
		Assert.assertEquals(xml,xml2);
	}
	@Test
	public void joinTest() {
		Path path1 = Paths.forPaths().join("foo","bar");
		Assert.assertTrue(path1.equals(Path.from("foo/bar")));
		
		Path path2 = Paths.forPaths().join(null,"bar","baz");
		Assert.assertTrue(path2.equals(Path.from("bar/baz")));
		
		Path path3 = Paths.forPaths().join((Path)null,(Path)null);
		Assert.assertTrue(path3 == null);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@XmlRootElement(name="myTestModelObj")
	@Accessors(prefix="_")
	@NoArgsConstructor @AllArgsConstructor
	public static class MyTestModelObj {
		@XmlAttribute(name="aPath")
		@Getter @Setter private Path _aPath;
		
		@XmlElement(name="anotherPath")
		@Getter @Setter private Path _anotherPath;
		
		public static MyTestModelObj createTestInstance() {
			return new MyTestModelObj(new Path("/foo/bar"),
									  new Path("/foo/bar/baz"));
		}
		}
}
