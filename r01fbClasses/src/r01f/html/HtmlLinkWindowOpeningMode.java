package r01f.html;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.aspects.interfaces.dirtytrack.ConvertToDirtyStateTrackable;
import r01f.debug.Debuggable;
import r01f.marshalling.annotations.XmlCDATA;
import r01f.util.types.Strings;



/**
 * Contiene la informacion de la ventana de presentacion de un link 
 * Usage:
 * <pre class='brush:java'>
 * 		// Custom opening mode
 * 		HtmlLinkWindowOpeningMode openingMode = HtmlLinkWindowOpeningMode.create()
 * 														.withName("My new window")
 * 														.withAppearance(OpeningWindowAppearance.create(CENTERED)
 * 																				.withDimensions(800,600)
 * 																				.notResizable()
 * 																				.withBars(OpeningWindowBars.create()
 * 																								.showingLocationBar()
 * 																								.showingMenuBar()
 * 																								.hidingStatusBar()
 * 																								.hidingScrollBars()));
 * 		// Using templates
 * 		HtmlLinkWindowOpeningMode openingMode = HtmlLinkWindowOpeningMode.forOpenNewWindowCentered();
 * </pre>
 */
@ConvertToDirtyStateTrackable
@XmlRootElement(name="openingData")
@Accessors(prefix="_")
@NoArgsConstructor(access=AccessLevel.PRIVATE)
public class HtmlLinkWindowOpeningMode 
  implements Serializable, 
  			 Debuggable {
	
    private static final long serialVersionUID = 5828844380339964547L;
///////////////////////////////////////////////////////////////////////////////////////////
// 	ESTADO
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Nombre de la ventana
     */ 
    @XmlElement(name="name") @XmlCDATA
    @Getter @Setter private String _name = "r01NewWindow";
    /**
     * Apariencia de la ventana que se abre (si se abre en nueva ventana)
     * Si el link se abre en la MISMA ventana este campo es NULL
     */
    @XmlElement(name="appearance")
    @Getter @Setter private OpeningWindowAppearance _appearance; 
    /**
     * ¿Lleva el enlace un icono para indicar que se abre en nueva ventana?
     */
    @XmlElement(name="showNewWindowIcon")
    @Getter @Setter private boolean _showNewWindowIcon = false;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
    public static HtmlLinkWindowOpeningMode create() {
    	HtmlLinkWindowOpeningMode outOpening = new HtmlLinkWindowOpeningMode();
    	return outOpening;
    }
    public HtmlLinkWindowOpeningMode withAppearance(final OpeningWindowAppearance apparance) {
    	_appearance = apparance;
    	return this;
    }
    public HtmlLinkWindowOpeningMode showingNewWindowIcon() {
    	_showNewWindowIcon = true;
    	return this;
    }
    public HtmlLinkWindowOpeningMode withName(final String windowName) {
    	_name = windowName;
    	return this;
    }
///////////////////////////////////////////////////////////////////////////////////////////
// TIPOS AUXILIARES
///////////////////////////////////////////////////////////////////////////////////////////    
    /**
     * Modo de apertura de la ventana
     */
    public static enum OpeningWindowMode {
    	SIMPLE,
    	CENTERED,
    	MAXIMIZED,
    	CUSTOMIZED;
    }
    @ConvertToDirtyStateTrackable
    @XmlRootElement(name="openingWindowApearance")
    @Accessors(prefix="_")
    @NoArgsConstructor
    public static class OpeningWindowAppearance 
             implements Serializable {
		private static final long serialVersionUID = 5359408350581058949L;
		
		@XmlAttribute(name="mode")		@Getter @Setter private OpeningWindowMode _openingMode = OpeningWindowMode.SIMPLE;		// Tipo de nueva ventana
		@XmlAttribute(name="width") 	@Getter @Setter private int _width = 800;			// Ancho
		@XmlAttribute(name="height")	@Getter @Setter private int _height = 600;			// Alto
		@XmlAttribute(name="x")		    @Getter @Setter private int _x = 0;					// Posicion X desde la esquina sup izq
		@XmlAttribute(name="y")		    @Getter @Setter private int _y = 0;					// Posición Y desde la esquina sup izq
		@XmlAttribute(name="resizable") @Getter @Setter private boolean _resizable = true;	// Es la ventana re-dimensionable
		@XmlElement(name="bars")	    @Getter @Setter private OpeningWindowBars _bars = new OpeningWindowBars();	// Barras de la ventana
		
		public static OpeningWindowAppearance create(final OpeningWindowMode mode,
													 final boolean resizable) {
			OpeningWindowAppearance outAppearance = new OpeningWindowAppearance();
			outAppearance.setOpeningMode(mode);
			outAppearance.setResizable(resizable);
			return outAppearance;
		}
		public OpeningWindowAppearance withDimensions(final int width,final int height) {
			_width = width;
			_height = height;
			return this;
		}
		public OpeningWindowAppearance locatedAt(final int x,final int y) {
			if (_openingMode != OpeningWindowMode.CENTERED) {
				_x = x;
				_y = y;
			}
			return this;
		}
		public OpeningWindowAppearance resizable() {
			_resizable = true;
			return this;
		}
		public OpeningWindowAppearance notResizable() {
			_resizable = false;
			return this;
		}
		public OpeningWindowAppearance withBars(final OpeningWindowBars bars) {
			_bars = bars;
			return this;
		}
    }
    @ConvertToDirtyStateTrackable
    @XmlRootElement(name="openingWindowBars")
    @Accessors(prefix="_")
    @NoArgsConstructor
    public static class OpeningWindowBars
             implements Serializable {
		private static final long serialVersionUID = -3991014897290734846L;
		
		@XmlAttribute(name="showLocationBar")	@Getter @Setter private boolean _showLocationBar = false;	// Mostrar barra para escribir la url
	    @XmlAttribute(name="showMenuBar")		@Getter @Setter private boolean _showMenuBar = false;		// Mostrar barra de menú
	    @XmlAttribute(name="showStatusBar")		@Getter @Setter private boolean _showStatusBar = false;		// Mostrar barra de estado
	    @XmlAttribute(name="showToolsBar")		@Getter @Setter private boolean _showTooslBar = false;		// Mostrar barra de herramientas
	    @XmlAttribute(name="showScrollsBar")	@Getter @Setter private boolean _showScrollBars = true;		// Mostrar barra de scroll
	    
	    public static OpeningWindowBars create() {
	    	OpeningWindowBars outBars = new OpeningWindowBars();
	    	return outBars;
	    }
	    public OpeningWindowBars showingLocationBar() {
	    	_showLocationBar = true;
	    	return this;
	    }
	    public OpeningWindowBars hidingLocationBar() {
	    	_showLocationBar = false;
	    	return this;
	    }
	    public OpeningWindowBars showingMenuBar() {
	    	_showMenuBar = true;
	    	return this;
	    }
	    public OpeningWindowBars hidingMenuBar() {
	    	_showMenuBar = false;
	    	return this;
	    }
	    public OpeningWindowBars showingStatusBar() {
	    	_showStatusBar = true;
	    	return this;
	    }
	    public OpeningWindowBars hidingStatusBar() {
	    	_showStatusBar = false;
	    	return this;
	    }
	    public OpeningWindowBars showingScrollBars() {
	    	_showScrollBars = true;
	    	return this;
	    }
	    public OpeningWindowBars hidingScrollBars() {
	    	_showScrollBars = false;
	    	return this;
	    }
    }
///////////////////////////////////////////////////////////////////////////////////////////
// TEMPLATES
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Características para abrir una ventana centrada de 800x600
     */
    public static HtmlLinkWindowOpeningMode forOpenNewWindowCentered() {
    	HtmlLinkWindowOpeningMode outFeatures = new HtmlLinkWindowOpeningMode();
    	
    	OpeningWindowAppearance appearance = new OpeningWindowAppearance();
    	appearance.setOpeningMode(OpeningWindowMode.CENTERED);
    	
    	outFeatures.setAppearance(appearance);
    	return outFeatures;
    }
    /**
     * Características para abrir una ventana maximizada
     */
    public static HtmlLinkWindowOpeningMode forOpenNewWindowMaximized() {
    	HtmlLinkWindowOpeningMode outFeatures = new HtmlLinkWindowOpeningMode();
    	
    	OpeningWindowAppearance appearance = new OpeningWindowAppearance();
    	appearance.setOpeningMode(OpeningWindowMode.MAXIMIZED);
    	
    	outFeatures.setAppearance(appearance);
    	return outFeatures;
    }
    /**
     * Características para abrir una ventana personalizada
     */
    public static HtmlLinkWindowOpeningMode forOpenNewWindowCustomized() {
    	HtmlLinkWindowOpeningMode outFeatures = new HtmlLinkWindowOpeningMode();
    	
    	OpeningWindowAppearance appearance = new OpeningWindowAppearance();
    	appearance.setOpeningMode(OpeningWindowMode.CUSTOMIZED);
    	appearance.getBars().setShowStatusBar(true);
    	appearance.getBars().setShowTooslBar(true);
    	outFeatures.setAppearance(appearance);
    	
    	return outFeatures;
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  DEBUG
/////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public String debugInfo() {
    	String appearanceDbg = null;
    	String barsDbg = null;
    	if (_appearance != null) {
	    	appearanceDbg = Strings.create()
	    		  				   .addCustomized("\n\tOpeningWindow Mode: {}",this.getAppearance().getOpeningMode())
	    				  		   .addCustomized("\n\t\t         Resizable:",this.getAppearance().isResizable())
	    				  		   .addCustomized("\n\t\t        Dimensions: {}x{}",this.getAppearance().getWidth(),
	    				  					  									    this.getAppearance().getHeight())
	    				  		   .addCustomized("\n\t\t          Position: {},{}",this.getAppearance().getX(),
	    				  					  									    this.getAppearance().getY())
	    				  		   .asString();
	    	if (this.getAppearance().getBars() != null) {
	    		barsDbg = Strings.create()
	    				  		 .add("\n\t              Bars:")
	    				  		 .addCustomized("\n\t\tShow Location bar: {}",this.getAppearance().getBars().isShowLocationBar())
	    				  		 .addCustomized("\n\t\t    Show Menu bar: {}",this.getAppearance().getBars().isShowMenuBar())
	    				  		 .addCustomized("\n\t\t  Show Status bar: {}",this.getAppearance().getBars().isShowStatusBar())
	    				  		 .addCustomized("\n\t\t Show Scroll bars: {}",this.getAppearance().getBars().isShowScrollBars())
	    				  		 .addCustomized("\n\t\t  Show Tools bars: {}",this.getAppearance().getBars().isShowTooslBar())
	    				  		 .asString();
	    	}
    	}
    	return Strings.of("Presentation:")
    				  .addCustomized("\n\t               Name: {}",_name)
    				  .addCustomized("\n\tShow newWindow icon: {}",_showNewWindowIcon)
    				  .add(appearanceDbg)
    				  .add(barsDbg)
    				  .asString();
    }
}
