package r01f.guids;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import r01f.guids.CommonOIDs.AppCode;
import r01f.util.types.Strings;



/**
 * AppCode and component in a single object
 * Normally the id is internally stored as a {@link String} like appCode.appComponent
 * The appCode and appComponent parts can be accessed individually:
 * <pre class='brush:java'>
 * 		AppAndComponent appAndComp = AppAndComponent.composedBy("myApp","myComp");
 * 		AppCode = appAndComp.getAppCode();
 * 		AppComponent = appAndComp.getComponent();
 * </pre>
 */
@XmlRootElement(name="appAndComponent")
@EqualsAndHashCode(callSuper=true)
@NoArgsConstructor
public class AppAndComponent 
     extends OIDBaseMutable<String> {
	
	private static final long serialVersionUID = -1130290632493385784L;

///////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
///////////////////////////////////////////////////////////////////////////////////////////
	public AppAndComponent(final String oid) {
		super(oid);
	}
	public AppAndComponent(final AppCode appCode,final AppComponent appComponent) {
		this(appCode.asString(),appComponent.asString());
	}
	public AppAndComponent(final String appCode,final String appComponent) {
		this(appCode + "." + appComponent);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  BUILDER
/////////////////////////////////////////////////////////////////////////////////////////	
	public static AppAndComponent forId(final String id) {
		return new AppAndComponent(id);
	}
	public static AppAndComponent composedBy(final AppCode appCode,final AppComponent appComponent) {
		return AppAndComponent.composedBy(appCode.asString(),appComponent.asString());
	}
	public static AppAndComponent composedBy(final String appCode,final String appComponent) {
		return new AppAndComponent(appCode,appComponent);
	} 
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public AppCode getAppCode() {
		String[] parts = Strings.of(this.getId())
								.splitter(".")
								.toArray();
		return AppCode.forId(parts[0]);
	}
	public AppComponent getAppComponent() {
		String[] parts = Strings.of(this.getId())
								.splitter(".")
								.toArray();
		return AppComponent.forId(parts[1]);
	}
}
