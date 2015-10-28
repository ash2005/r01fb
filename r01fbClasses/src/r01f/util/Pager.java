package r01f.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Clase que modela la paginación en un conjunto de resultados
 * Esencialmente es una lista que contiene el código de los primeros elementos
 * de cada página y el número total de elementos encontrados. 
 * 
 * Para utilizar el paginador es imprescindible que:
 * <ol>
 * 		<li>Tener ANTES de componer el pager el número TOTAL de resultados y los OIDs de cada elemento de resultado</li>
 * 		<li>Los resultados se carguen ORDENADOS por algun criterio arbitrario</li>
 * 		<li>Cuando se recuperen los elementos de una página la query se haga utilizando el criterio de ordenación anterior</li>
 * </ol>
 * El uso del page es el siguiente:
 * <pre class='brush:java'>
 * 		Pager<String> pager = new Pager(10,		// Elementos por página
 * 										10);	// Páginas por bloque
 * 		long totalResultsCount = _computeTotalResultsCount(query);
 * 		for (long i=0; i<totalResultsCount; i++) {
 * 			page.add(i)
 * 		}
 * </pre>
 */
@Accessors(prefix="_")
public class Pager<T>
  implements Serializable {
	
    private static final long serialVersionUID = -4966151337012633456L;
///////////////////////////////////////////////////////////////////////////////////////////
//  MIEMBROS
///////////////////////////////////////////////////////////////////////////////////////////        
    private List<PageFirstAndLastItemsOids<T>> _pages = null;	// Lista de páginas
    @Getter @Setter private int  _pageSize  = 10;				// Número de elementos por página
    @Getter @Setter private int  _blockSize = 5;    			// Número de páginas en cada bloque (utilizado para barras de navegación)
    @Getter 		private int _totalNumberOfItems;     		// Número total de elementos encontrados en la búsqueda
    @Getter 		private int _currentPageOrderNumber;        // La página actualmente mostrada
    
    @NoArgsConstructor
    private class PageFirstAndLastItemsOids<U> {
    	U _firstItemOid;
    	U _lastItemOid;
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORS
///////////////////////////////////////////////////////////////////////////////////////////        
    /** 
     * Constructor Pager 
     */
    public Pager() throws IllegalArgumentException {
        super();
        _pageSize = 10;		// Por defecto el número de elementos por página es 10
        _blockSize = 5;		// Inicializar la página actual
        _currentPageOrderNumber = 1;		// Página actual = 1
        _pages = new ArrayList<PageFirstAndLastItemsOids<T>>();	// Inicializar la lista que contiene los oids del primer item de cada página
    }
    /**
     * Constructor del pager en base a el tamaño de la pagina
     * @param newPageSize El tamaño de la pagina
     * @throws IllegalArgumentException si el tamaño de la pagina es menor que cero
     */
    public Pager(final int newPageSize) throws IllegalArgumentException {
        this();
        if (newPageSize <= 0) throw new IllegalArgumentException("El numero de elementos en la pagina no puede ser menor que cero");
        _pageSize = newPageSize;
    }
    /**
     * Constructor en base al tamaño de la página y al tamaño del bloque de páginas de la barra de navegación
     * @param newPageSize
     * @param newBlockSize
     * @throws IllegalArgumentException si el tamaño de la pagina es menor que cero
     */
    public Pager(final int newPageSize,final int newBlockSize) throws IllegalArgumentException {
        this();
        if (newPageSize <= 0 || newBlockSize <= 0) throw new IllegalArgumentException("El numero de elementos en la pagina o el tamaño del bloque de páginas de la barra de navegación, no puede ser menor que cero");
        _pageSize = newPageSize;
        _blockSize = newBlockSize;
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  INTRODUCCION DE PAGINAS
///////////////////////////////////////////////////////////////////////////////////////////    
    /**
     * Mete un item en el registro de páginas
     * OJO!!!!! En realidad SOLO se guardan los oids del PRIMER ITEM y ULTIMO ITEM DE LA PAGINA, es decir, solo se introduce el item
     * 			si la parte decimal de (numeroTotalElementos/pageSize) es cero, lo que quiere decir que se está en 
     * 			un registro múltiplo del tamaño de página (primer registro de la página)
     * @param itemOid  El oid del registro actual resultado de la búsqueda que se mete en el paginador
     */
    public void addItem(final T itemOid) throws IllegalArgumentException {
    	if (itemOid == null) throw new IllegalArgumentException("NO se puede insertar un itemOid=null en el pager!");
        if (_pages == null) _pages = new ArrayList<PageFirstAndLastItemsOids<T>>();
        PageFirstAndLastItemsOids<T> currPageItem = null;
        if ((_totalNumberOfItems % _pageSize) == 0) {
        	currPageItem = new PageFirstAndLastItemsOids<T>();
        	currPageItem._firstItemOid = itemOid;
        	currPageItem._lastItemOid = itemOid;
        	_pages.add(currPageItem); 	// Introducir el indice del primer elemento de la nueva pagina
        } else {
        	currPageItem = _pages.get(_pages.size()-1);
        	currPageItem._lastItemOid = itemOid;
        }
        _totalNumberOfItems++;
    }    
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS DE ACCESO AL TAMAÑO DE PAGINA Y BLOQUE
///////////////////////////////////////////////////////////////////////////////////////////    
    /**
     * Sirve para crear barras de navegación en las que las páginas se dividen en bloques. 
     * Ejemplo: Si se han encontrado 23 páginas de
     *          resultados se puede mostrar una barra de navegación con marcadores a 5 páginas como la siguiente: 
     *                  Pg Ant 1 2 3 4 5 Pg Sig
     * A medida que se va paginando, la ventana de las páginas en la barra de navegación se va desplazando: 
     *                  Pg Ant 6 7 8 9 10 Pg Sig 
     * Este método devuelve un array con los números de las páginas en el bloque actual 
     * NOTA: El bloque actual depende de la página actual.
     * @return Un array con los números de las páginas en el bloque actual
     */
    public int[] getCurrentBlockPageNumbers() {
        int numeroBloques = this.getBlockCount();           // Número total de bloques
        int bloqueActual = this.getCurrentBlockNumber();    // Número del bloque actual
        // Obtener el tamaño del bloque actual
        // Todos los bloques contienen un numero de marcadores igual a _blockSize, excepto el último
        // que puede contener menos marcadores:
        // [Numero total paginas] - [Numero de paginas en los bloques anteriores al ultimo]
        int currentBlockSize = _blockSize;
        if (bloqueActual == numeroBloques) currentBlockSize = (_pages.size() - (numeroBloques - 1) * _blockSize);
        // Devolver los números de las páginas del bloque actual en un array
        int[] blockPages = new int[currentBlockSize];
        int j = 0;
        for (int i = 1; i <= blockPages.length; i++) blockPages[i - 1] = i;
        for (int i = ((bloqueActual - 1) * _blockSize + 1); ((i <= (bloqueActual * _blockSize)) && (i <= _pages.size())); i++) {
            blockPages[j++] = i; // Meter la página en el array
        }
        return blockPages;
    }
    /**
     * Devuelve el número del bloque actual
     * @return el número del bloque actual
     */
    public int getCurrentBlockNumber() {
        // El bloque actual es la parte entera de [Pagina Actual-1] / [Tamaño Bloque] + 1
        int bloqueActual = (new Double(((_currentPageOrderNumber - 1) / _blockSize) + 1)).intValue(); // Los bloques empiezan por 1
        return bloqueActual;
    }
    /**
     * Devuelve el número total de bloques de páginas
     * @return el número de bloques
     */
    public int getBlockCount() {
        // Obtener el número de bloques 
        // El número de bloques es el primer entero mayor que [Numero total de paginas] / [Tamaño bloque]
        double dblPaginas = _pages.size();
        int numeroBloques = (new Double(Math.ceil(dblPaginas / _blockSize))).intValue();
        return numeroBloques;
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS DE ACCESO A LOS VALORES DE LOS OIDs DE LOS PRIMEROS ELEMENTOS DE LA PÁGINA
///////////////////////////////////////////////////////////////////////////////////////////    
    /**
     * Devuelve oid del item del primer elemento de la página actual
     * @return Código del primer elemento de la página actual
     */
    public T getCurrentPageFirstItemOid() {
        return this.getPageFirstItemOid(_currentPageOrderNumber);
    }
    /**
     * Devuelve el oid del item del ultimo elemento de la pagina actual
     * @return Codigo del ultimo elemento de la página actual
     */
    public T getCurrentPageLastItemOid() {
        return this.getPageLastItemOid(_currentPageOrderNumber);
    }
    /**
     * Devuelve el valor del primer elemento de la página anterior o null si se está en la primera página
     * @return Object
     */
    public T getPrevPageLastItemOid() {
        return _currentPageOrderNumber > 1 ? this.getPageLastItemOid(_currentPageOrderNumber-1) : null;
    }
    /**
     * Devuelve el valor del primer elemento de la página siguiente o null si se está en la última pñagina
     * @return El código del primer elemento de la siguiente página
     */
    public T getNextPageFirstItemOid() {
    	return _currentPageOrderNumber < _pages.size() ? this.getPageFirstItemOid(_currentPageOrderNumber+1) : null;
    }
    /**
     * Devuelve el oid del primer item de una página
     * @param inPage Número de la página solicitada
     * @return Código del primer elemento de la página que se indica
     */
    public T getPageFirstItemOid(final int inPage) throws IllegalArgumentException {
        if (inPage < 1 || inPage > _pages.size()) throw new IllegalArgumentException("Página ilegal");
        PageFirstAndLastItemsOids<T> pageFirstAndLastItems = _pages.get(inPage-1);
        return pageFirstAndLastItems._firstItemOid;
    }
    /**
     * Devuelve el oid del último item de una página
     * @param inPage número de la página solicitada
     * @return código del último elemento de la página
     */
    public T getPageLastItemOid(final int inPage) throws IllegalArgumentException {
        if (inPage < 1 || inPage > _pages.size()) throw new IllegalArgumentException("Página ilegal");
        PageFirstAndLastItemsOids<T> pageFirstAndLastItems = _pages.get(inPage-1);
        return pageFirstAndLastItems._lastItemOid;	
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS DE ACCESO A LOS NÚMEROS DE ORDEN DE LOS ITEMS EN FUNCIÓN DE LA PÁGINA
///////////////////////////////////////////////////////////////////////////////////////////    
    /**
     * Devuelve el número de orden de la primera página
     * @return Siempre devuelve 1
     */
    @SuppressWarnings("static-method")
	public int getFirstPageOrderNumber() {
        return 1;
    }
    /**
     * Devuelve el número de orden de la última página
     * @return el número de pagágina
     */
    public int getLastPageOrderNumber() {
        return _pages.size();
    }
    /**
     * Devuelve el número de orden de la página anterior o -1 si se está en la primera página
     * @return 
     */
    public int getPrevPageOrderNumber() {
    	if (_currentPageOrderNumber == 1) return -1;
    	return _currentPageOrderNumber - 1;
    }
    public int getNextPageOrderNumber() {
    	if (_currentPageOrderNumber == _pages.size()) return -1;
    	return _currentPageOrderNumber + 1;
    }
    /**
     * Devuelve el número de orden del primer elemento de la página 
     * Si por ejemplo hay 12 páginas de 10 elementos cada una, la página 2
     * tendrá los elementos del 11 al 20. Este método devolverá 11
     * @return el numero de orden del primer elemento de la pagina
     */
    public int getCurrentPageFirstItemOrderNumber() {
        return this.getPageFirstItemOrderNumber(_currentPageOrderNumber);
    }
    /**
     * Devuelve el número de orden del ultimo elemento de la página 
     * Si por ejemplo hay 12 páginas de 10 elementos cada una, la página 2
     * tendrá los elementos del 11 al 20. Este método devolverá 20
     * @return El numero de orden del ultimo elemento de la pagina 
     */
    public int getCurrentPageLastItemOrderNumber() {
        return this.getPageLastItemOrderNumber(_currentPageOrderNumber);
    }
    /**
     * Devuelve el número de orden del elemento anterior al primero de la página actual
     * (el último elemento de la página anterior)
     * Si por ejemplo hay 12 páginas de 10 elemento cada una, la página 2
     * tendrá los elementos del 11 al 20. Este método devolverá 10
     * @return el número de orden del elemento anterior al primero de la página
     */
    public int getPreviousPageLastItemOrderNumber() {
    	if (_currentPageOrderNumber == 1) throw new IllegalArgumentException("Página Ilegal");
    	return this.getPageLastItemOrderNumber(_currentPageOrderNumber-1);
    }
    /**
     * Devuelve el número de orden del elemento siguiente al primero de la página actual
     * (el primer elemento de la página siguiente)
     * Si por ejemplo hay 12 páginas de 10 elemento cada una, la página 2
     * tendrá los elementos del 11 al 20. Este método devolverá 21
     * @return el número de orden del elemento siguiente al último de la página
     */    
    public int getNextPageFirstItemOrderNumber() {
    	if (_currentPageOrderNumber == _pages.size()) throw new IllegalArgumentException("Página Ilegal");
    	return this.getPageFirstItemOrderNumber(_currentPageOrderNumber + 1);
    }
    /**
     * Obtiene el número de orden del primer elemento de una página
     * @param pageOrderNum número de orden de la página
     * @return 
     */
    public int getPageFirstItemOrderNumber(final int pageOrderNum) throws IllegalArgumentException {
        if (pageOrderNum < 1 || pageOrderNum > _pages.size()) throw new IllegalArgumentException("Página ilegal");
        if (pageOrderNum == this.getFirstPageOrderNumber()) return 1;
        return (pageOrderNum * _pageSize) - _pageSize + 1;
    }
    /**
     * Devuelve el número de orden del último elemento de una página
     * @param pageOrderNum número de orden de la página
     * @return
     */
    public int getPageLastItemOrderNumber(final int pageOrderNum) throws IllegalArgumentException {
        if (pageOrderNum < 1 || pageOrderNum > _pages.size()) throw new IllegalArgumentException("Página ilegal");
        if (pageOrderNum == this.getLastPageOrderNumber()) return this.getLastPageOrderNumber();
        return pageOrderNum * _pageSize;
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS DE ACCESO A LOS NUMEROS DE ORDEN DE LOS ITEMS EN FUNCIÓN DEL BLOQUE
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Devuelve el número de orden de la última página del bloque anterior al actual
     * @return
     */
    public int getPrevBlockLastPageOrderNumber() {
        int currentBlockPageNums[] = this.getCurrentBlockPageNumbers();
        int prevBlockLastPage = currentBlockPageNums[0] == 1 ? 1 : currentBlockPageNums[0]-1;
        return prevBlockLastPage;
    }
    /**
     * Devuelve el número de orden de la primera página del bloque siguiente al actual
     * @return
     */
    public int getNextBlockFirstPageOrderNumber() {
        int currentBlockPageNums[] = this.getCurrentBlockPageNumbers();
        int nextBlockFirstPage = currentBlockPageNums[currentBlockPageNums.length-1] == _pages.size() ? currentBlockPageNums[currentBlockPageNums.length-1]
        																							  : currentBlockPageNums[currentBlockPageNums.length-1]+1;
        return nextBlockFirstPage;
    }
    /**
     * Devuelve el número de orden del último elemento del bloque anterior al bloque actual.
     * Si por ejemplo hay 25 páginas de 10 elementos cada una y se está en el segundo bloque
     * (de la página 11 a la 25), este método devolverá 10 (el número de orden del último
     * elemento de la página anterior)
     * @return el número de orden del último elememento del bloque anterior.
     */
    public int getPrevBlockLastItemOrderNumber() {
        int prevBlockLastPageOrderNumber = this.getPrevBlockLastPageOrderNumber();
        return this.getPageLastItemOrderNumber(prevBlockLastPageOrderNumber);
    }
    /**
     * Devuelve el número de orden del último elemento del bloque anterior al 
     * bloque actual.
     * Si por ejemplo hay 25 páginas de 10 elementos cada una y se está en el segundo bloque
     * (de la página 11 a la 25), este método devolverá 10 (el número de orden del último
     * elemento de la página anterior)
     * @return el número de orden del último elememento del bloque anterior.
     */
    public int getNextBlockFirstItemOrderNumber() {
        int nextBlockFirstPageOrderNumber = this.getNextBlockFirstPageOrderNumber();
        return this.getPageFirstItemOrderNumber(nextBlockFirstPageOrderNumber);
    }    
///////////////////////////////////////////////////////////////////////////////////////////
//  CUENTA DE ELEMENTOS
///////////////////////////////////////////////////////////////////////////////////////////        
    /**
     * Devuelve el número de páginas de la búsqueda
     * @return El número de páginas encontradas
     */
    public int getPageCount() {
        return _pages.size();
    }
    
///////////////////////////////////////////////////////////////////////////////////////////
//  MOVIMIENTO ENTRE PAGINAS
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Establece el puntero a la primera página
     */
    public void goToFirstPage() {
        _currentPageOrderNumber = 1;
    }
    /**
     * Establece el puntero en la última página
     */
    public void goToLastPage() {
        _currentPageOrderNumber = _pages.size();
    } 
    /**
     * Establece el puntero en la página anterior
     */
    public void goToPrevPage() {
        if (_currentPageOrderNumber == 1) return; 	// Comprobar que la nueva página no es mayor que el número total de páginas
        _currentPageOrderNumber--;
    }
    /**
     * Establece el puntero en la siguiente página
     */
    public void goToNextPage() {
        if (_currentPageOrderNumber == _pages.size()) return;	// Comprobar que la nueva página no es mayor que el número total de páginas
        _currentPageOrderNumber++;
    }
    /**
     * Establece el puntero en la página indicada
     * @param newPage Número de la nueva página a la que hay que moverse
     */
    public void goToPage(int newPage) throws IllegalArgumentException {       
        if (newPage < 0 || newPage > _pages.size()) throw new IllegalArgumentException("Página ilegal");
        _currentPageOrderNumber = newPage;
    } 
    /**
     * Mueve el puntero al ultimo elemento del bloque anterior de resultados
     */
    public void goToPrevBlockLastPage() {
        _currentPageOrderNumber = this.getPrevBlockLastPageOrderNumber();
    }
    /**
     * Mueve el puntero al primer elemento del bloque siguiente de resultados
     */
    public void goToNextBlockFirstPage() {
        _currentPageOrderNumber = this.getNextBlockFirstPageOrderNumber();
    }
}
