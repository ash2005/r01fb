package r01f.resources;

import java.util.Map;
import java.util.ResourceBundle;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.debug.Debuggable;
import r01f.types.TimeLapse;
import r01f.util.types.Strings;
import r01f.util.types.Strings.StringExtended;
import r01f.xmlproperties.XMLProperties;

/**
 * Resource reloading definition
 * Usually comes from a {@link XMLProperties} file:<br>
 * <pre class="brush:xml">
 *		<resourcesLoader type='CLASSPATH'>
 *			<!-- PERIODIC, BBDD, CONTENT_SERVER_FILE_LAST_MODIF_TIMESTAMP, FILE_LAST_MODIF_TIMESTAMP, VOID -->
 *			<reloadControl impl='PERIODIC' enabled='true' checkInterval='2s'>
 *				<props>
 *					<period>2s</period>
 *				</props>
 *			</reloadControl>
 *		</resourcesLoader>
 * </pre>
 * If the {@link ResourcesReloadControlDef} must be created manually, use:
 * <ul>
 * 		<li>ResourcesReloadControlBBDDTimeStampBasedDef</li>
 * 		<li>ResourcesReloadControlContentServerFileLastModifTimeStampBasedDef</li>
 * 		<li>ResourcesReloadControlPeriodicDef</li>
 * 		<li>ResourcesReloadControlFileLastModifTimeStampBasedDef</li>
 * </ul>
 * 
 * or a builder...
 * 
 * If a {@link ResourcesReloadControlDef} must be created a builder method should be used:
 * <pre class='brush:java'>
 *		ResourcesReloadControlDef periodicReloadDef = ResourcesReloadControlDef.periodicReloading("5s")
 *																			   .enabled();
 *		ResourcesReloadControlDef fileSystemLastModifReloadDef = ResourcesReloadControlDef.fileSystemLastModifTimeStampReloading(ResourcesLoaderType.CLASSPATH,
 *																																 "/datos/r01f/myFile.chk")
 *																			   			  .enabled()
 * </pre>
 */
@XmlRootElement(name="reloadControl")
@Accessors(prefix="_")
@NoArgsConstructor
public class ResourcesReloadControlDef 
  implements Debuggable {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public enum ResourcesReloadPolicy {
		VOID,
		NO_RELOAD,
		BBDD,
		PERIODIC,
		FILE_LAST_MODIF_TIMESTAMP;
	}
///////////////////////////////////////////////////////////////////////////////
// 	VALOR POR DEFECTO (sin recarga)
///////////////////////////////////////////////////////////////////////////////
	public static ResourcesReloadControlDef DEFAULT = new ResourcesReloadControlDef(ResourcesReloadPolicy.NO_RELOAD) {
															@Override
															public boolean isEnabled() {
																return false;
															}
												   	  };
	public static ResourcesReloadControlDef NO_RELOAD = DEFAULT;
///////////////////////////////////////////////////////////////////////////////
// 	FIELDS
///////////////////////////////////////////////////////////////////////////////
	@XmlAttribute(name="impl")
	@Getter @Setter private ResourcesReloadPolicy _impl;
	
	@XmlAttribute(name="checkInterval")
	@Getter @Setter private String _checkInterval = "3000s";	// Time between two reload need checks
	
	@XmlAttribute(name="enabled")
	@Getter @Setter private boolean _enabled = true;
	
	@XmlElementWrapper(name="props")
	@Getter @Setter private Map<String,String> _controlProps;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public ResourcesReloadControlDef(final ResourcesReloadPolicy policy) {
		_impl = policy;
	}
///////////////////////////////////////////////////////////////////////////////
// 	METHODS
///////////////////////////////////////////////////////////////////////////////
	public long getCheckIntervalMilis() {
		return Strings.isNOTNullOrEmpty(_checkInterval) ? TimeLapse.createFor(_checkInterval).asMilis()
														: ResourceBundle.Control.TTL_NO_EXPIRATION_CONTROL;
	}
	/**
	 * Creates a {@link ResourcesReloadControl} using this definition
	 * @return
	 */
	public ResourcesReloadControl createResourcesReloadControl() {
		ResourcesReloadControl outCtrl = null;
		if (_impl == null) throw new IllegalStateException("NO ResourcesReloadPolicy was set");
		switch(_impl) {
		case BBDD:
			outCtrl = new ResourcesReloadControlBBDDFlagBased(this);
			break;
		case FILE_LAST_MODIF_TIMESTAMP:
			outCtrl = new ResourcesReloadControlFileLastModifTimeStampBased(this);
			break;
		case NO_RELOAD:
			// nothing
			break;
		case PERIODIC:
			outCtrl = new ResourcesReloadControlPeriodic(this);
			break;
		case VOID:
			outCtrl = new ResourcesReloadControlVoid(this);
			break;
		default:
			break;
		}
		return outCtrl;
	}
	@Override
	public CharSequence debugInfo() {
		StringExtended sw = Strings.create(100);
		sw.addCustomized("\r\n\t\t\t      -enabled: {}",Boolean.toString(_enabled))
		  .addCustomized("\r\n\t\t\t-         impl: {}",_impl.name())
		  .addCustomized("\r\n\t\t\t-checkInterval: {}",_checkInterval)
		  .addCustomized("\r\n\t\t\t-        props: ({})",(_controlProps == null ? "null":Integer.toString(_controlProps.size())));
		if (_controlProps != null) {
			for (Map.Entry<String,String> prop : _controlProps.entrySet()) {
				sw.addCustomized("\r\n\t\t\t\t-{}:{}",prop.getKey(),prop.getValue());
			}
		}
		return sw;
	}
}
