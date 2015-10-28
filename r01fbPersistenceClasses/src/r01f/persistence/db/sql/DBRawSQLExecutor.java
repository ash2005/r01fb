package r01f.persistence.db.sql;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import r01f.persistence.db.DBSQLExecutor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * Clase Base para realizar consultas sobre la Base de Datos.<br>
 * Puede ser extendida por EJBs, Beans, .... <br>
 * En el fichero de propiedades de la aplicación ES PRECISO especificar las propiedades
 * de la conexión a Base de Datos (Ver {@link DBManager}).<br>
 *
 * <p>Ejemplo de conexión vía JDBC a la Base de datos:
 * <pre class="brush:xml">
 * <props>
 * 		<class>oracle.jdbc.OracleDriver</class> <!-- Driver de acceso a la base de datos -->
 *		<uri>jdbc:oracle:thin:@ejhp67:1524:ede2</uri> <!-- Cadena de coneon a la BBDD  -->
 *		<user>r01</user> <!-- Usuario de acceso a la base de datos -->
 *		<password>r01</password> <!-- Clave de acceso a la base de datos -->
 * </props>
 * </pre>
 *
 * <p>Ejemplo de conexión con Datasource:
 * <pre class="brush:xml">
 * <props>
 *		<class>DataSource</class>
 *		<uri>r01n.r01nDataSource</uri>
 * </props>
 * </pre>
 */
@Slf4j
@NoArgsConstructor
@Accessors(prefix="_")
public class DBRawSQLExecutor implements DBSQLExecutor {
///////////////////////////////////////////////////////////////////////////////////////////
//  MIEMBROS
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Manejador de conexión sobre Base de Datos.
     */
	@Getter @Setter
    private DBManager _dbManager;
///////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORES
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Constructor
     * @param conxProps propiedades con la información necesaria para establecer la conexión con la Base de Datos.
     */
    public DBRawSQLExecutor(final Properties conxProps) {
        this.init(conxProps);
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  BUILDERS
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Crea un query executor para las propiedades de conexión que se pasan como parámetro
     * @param conxProps las propiedades de conexión
     * @return el ejecutor
     */
    public static DBSQLExecutor forConnectionCreatedWith(final Properties conxProps) {
    	DBSQLExecutor outExec = new DBRawSQLExecutor(conxProps);
    	return outExec;
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  INICIALIZACION
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Inicialización de los componentes.
     * @param conxProps propiedades con la información necesaria para establecer la conexión con la Base de Datos.
     */
    protected void init(final Properties conxProps) {
        _dbManager = new DBManager(conxProps);
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  QUERY
///////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public List<Map<String,String>> query(final String querySql) throws SQLException {
        return query(querySql,null);
    }
    @Override
    public List<Map<String,String>> query(final String querySql,
    						 			  final List<String> params) throws SQLException {
    	List<Map<String,String>> rdo = null;
        if (querySql != null) {
            // Ejecutar la query
            rdo = _dbManager.executeQuery(querySql,params);
            if (rdo == null) {
                log.debug("La consulta " + querySql + " NO ha devuelto elementos.");
            } else {
            	log.debug("La consulta " + querySql + " ha devuelto " + rdo.size() + " elementos");
            }
        } else {
        	log.warn("Se ha intentado ejecutar una consulta NULA contra la base de datos");
        }
        // Devolver el resutado de la consulta
        return rdo;
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  INSERT
/////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void insert(final String insertSQL) throws SQLException {
        insert(insertSQL,null);
    }
    @Override
    public void insert(final String insertSQL,
    				   final List<String> params) throws SQLException {
        if (insertSQL != null) {
            // Ejecutar la sentencia contra Base de Datos
            _dbManager.executeStatement(insertSQL, params);
        } else {
            log.warn("Se ha intentado ejecutar una insert NULA contra la base de datos");
        }
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  UPDATE
/////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void update(final String updateSQL) throws SQLException {
        this.update(updateSQL,null);
    }
    @Override
    public void update(final String updateSQL,
    				   final List<String> params) throws SQLException {
        if (updateSQL != null) {
            // Ejecutar la sentencia contra Base de Datos
            _dbManager.executeStatement(updateSQL, params);
        } else {
            log.warn("Se ha intentado ejecutar una update NULA contra la base de datos");
        }
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  DELETE
/////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void delete(final String deleteSQL) throws SQLException {
        this.delete(deleteSQL,null);
    }
    @Override
    public void delete(final String deleteSQL,
    		           final List<String> params) throws SQLException {
        if (deleteSQL != null) {
            // Ejecutar la sentencia contra Base de Datos
            _dbManager.executeStatement(deleteSQL,params);
        } else {
            log.warn("Se ha intentado ejecutar una delete NULA contra la base de datos");
        }
    }

}
