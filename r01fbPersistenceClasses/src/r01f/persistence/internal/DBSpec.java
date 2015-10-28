package r01f.persistence.internal;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.xml.bind.annotation.XmlAttribute;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.config.TargetDatabase;

import r01f.exceptions.Throwables;

/**
 * Encapsulates the database specification needed when defining the
 * schema generation at persistence.xml
 * For MySql:
 * <pre class='brush:xml'>
 *			<property name="javax.persistence.database-product-name" value="mysql"/>
 *			<property name="javax.persistence.database-major-version" value="5"/>
 *			<property name="javax.persistence.database-minor-version" value="6"/>
 * </pre>
 * For Oracle:
 * <pre class='brush:xml'>
 *			<property name="javax.persistence.database-product-name" value="Oracle"/>
 *			<property name="javax.persistence.database-major-version" value="11"/>
 *			<property name="javax.persistence.database-minor-version" value="2"/>
 * </pre>
 */
@Accessors(prefix="_")
@RequiredArgsConstructor
public class DBSpec {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@XmlAttribute(name="vendor")
	@Getter private final DBVendor _vendor;
	
	@XmlAttribute(name="majorVersion")
	@Getter private final int _majorVersion;
	
	@XmlAttribute(name="minorVersion")
	@Getter private final int _minorVersion;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private static final Pattern SPEC_PATTERN = Pattern.compile("(" + TargetDatabase.MySQL + "|" + TargetDatabase.PostgreSQL + "|" + TargetDatabase.Oracle + ")\\s([0-9]+)\\.([0-9]+)");
	/**
	 * Loads the {@link DBSpec} form a {@link String} like 
	 * (mysql|Oracle) {majorVersion}.{minorVersion}
	 * @param code
	 * @return
	 */
	public static DBSpec valueOf(final String code) {
		DBSpec outSpec = null;
		
		Matcher m = SPEC_PATTERN.matcher(code);
		if (m.find()) {
			DBVendor vendor = DBVendor.fromCode(m.group(1));
			int majorVersion = Integer.parseInt(m.group(2));
			int minorVersion = Integer.parseInt(m.group(3));
			outSpec = new DBSpec(vendor,majorVersion,minorVersion);
		} else {
			throw new IllegalArgumentException(Throwables.message("The database spect DOES NOT have a valid format: {}. It MUST match {}",
																  code,SPEC_PATTERN.toString()));
		}
		return outSpec;
	}
	/**
	 * Loads the {@link DBSpec} from the {@link EntityManager} properties
	 * @param em
	 * @return
	 */
	public static DBSpec usedAt(final EntityManager em) {
		Map<String,Object> emProps = em.getProperties();
		DBVendor dbVendor = DBVendor.fromCode((String)emProps.get(PersistenceUnitProperties.SCHEMA_DATABASE_PRODUCT_NAME));
		int majorVersion = (Integer)emProps.get(PersistenceUnitProperties.SCHEMA_DATABASE_MAJOR_VERSION);
		int minorVersion = (Integer)emProps.get(PersistenceUnitProperties.SCHEMA_DATABASE_MINOR_VERSION);
		DBSpec outSpec = new DBSpec(dbVendor,
									majorVersion,minorVersion);
		return outSpec;
	}
}
