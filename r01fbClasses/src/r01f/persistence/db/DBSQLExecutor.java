package r01f.persistence.db;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface DBSQLExecutor {
/**
     * Consulta sin parámetros sobre la base de datos.
     * @param querySql Sentencia a ejecutar.
     * @return lista de resultados de la query.
     */
    public List<Map<String,String>> query(final String querySql) throws SQLException;
    /**
     * Consulta con parámetros sobre la base de datos.
     * @param querySql Sentencia a ejecutar.
     * @param params Parámetros a incorporar en la query.
     * @return lista de resultados de la query en forma de una lista de filas en la que cada fila es un
     *         mapa indexado por el nombre de la columna.
     * @throws SQLException si ocurre algún error
     */
    public List<Map<String,String>> query(final String querySql,
    						 			  final List<String> params) throws SQLException;
    /**
     * Insert sin parametros sobre la base de datos
     * @param insertSQL Sentencia a ejecutar
     * @throws SQLException
     */
    public void insert(final String insertSQL) throws SQLException;
    /**
     * Insert con parametros sobre la base de datos
     * @param insertSQL Sentencia a ejecutar
     * @param params de parametros a incorporar en la query
     * @throws SQLException si ocurre algún error
     */
    public void insert(final String insertSQL,
    				   final List<String> params) throws SQLException;
    /**
     * Update sin parametros sobre la base de datos
     * @param updateSQL Sentencia de actualización a ejecutar
     * @throws SQLException si hay un error
     */
    public void update(final String updateSQL) throws SQLException;
    /**
     * Update con parametros sobre la base de datos
     * @param updateSQL Sentencia de actualización a ejecutar
     * @param params de parametros a incorporar en la update
     * @throws SQLException si ocurre algún error
     */
    public void update(final String updateSQL,
    				   final List<String> params) throws SQLException;
    /**
     * Ejecuta una delete sobre la base de datos
     * @param deleteSQL sentencia delete a ejecutar
     * @throws SQLException si ocurre algún error
     */
    public void delete(final String deleteSQL) throws SQLException;
    /**
     * Insert con parametros sobre la base de datos
     * @param deleteSQL Sentencia a ejecutar
     * @param params de parametros a incorporar en la query
     * @throws SQLException si ocurre algún error
     */
    public void delete(final String deleteSQL,
    				   final List<String> params) throws SQLException;
}