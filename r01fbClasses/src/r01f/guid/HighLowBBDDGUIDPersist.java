package r01f.guid;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import lombok.Cleanup;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.google.inject.Provider;



/**
 * Se encarga del acceso a base de datos para la generacion de GUIDs.
 * Si no se facilitan las propiedades para acceder a la tabla de UIDs, se
 * utiliza la tabla de uids de la base de datos de R01F
 */
@NoArgsConstructor
@Slf4j
public class HighLowBBDDGUIDPersist 
  implements HighLowGUIDPersist {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
    // Descripcion de la base de datos
    private static final String GUIDSTABLE = "guidsTable";                // Tabla donde se guardan los valores HIGH
    private static final String HIGHKEYCOLUMN = "highKeyColumn";          // Columna de la tabla donde se almacenan los valores HIGH
    private static final String SEQUENCENAMECOLUMN = "seqNameColumn";     // Secuencia de la que se quiere obtener el HIGH (linea de la tabla)

/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
    private Provider<Connection> _conxProvider;
    
    public HighLowBBDDGUIDPersist(final Provider<Connection> bbddConnectionProvider) {
    	_conxProvider = bbddConnectionProvider;
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  INTERFAZ GUIDPersist
///////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public HighLowKey getHighKeyValue(final GUIDDispenserDef dispDef) {
        if (_checkDispenserDef(dispDef) != null) throw new IllegalArgumentException(_checkDispenserDef(dispDef));
        
        String guidsTable = dispDef.getProperty(GUIDSTABLE);
        String highKeyCol = dispDef.getProperty(HIGHKEYCOLUMN);
        String seqCol = dispDef.getProperty(SEQUENCENAMECOLUMN);
    	
    	HighLowKey outKey = null;
        try {
        	@Cleanup Connection conx = _conxProvider.get();
            @Cleanup PreparedStatement ps = null;
            @Cleanup ResultSet rs = null;
            // Obtener una conexion con la base de datos
            // TODO terminar cuando se "migre" la clase BBDDConnectionHelpper
//            conx = BDConnectionHelpper.getConnection(dispDef.getAppCode(),conxName);

            // Primero ejecutar una SELECT FOR UPDATE en la base de datos
            // que asegura que la linea se bloquea y nadie puede acceder a ella
            String sqlSELECT = null;
            sqlSELECT = "SELECT " + highKeyCol + " " +
                          "FROM " + guidsTable + " " +
                         "WHERE " + seqCol + "= ?";
            ps = conx.prepareStatement(sqlSELECT);
            ps.setString(1,dispDef.getSequenceName());
            rs = ps.executeQuery();

            if (rs.next()) {
                outKey = new HighLowKey(rs.getString(highKeyCol));
            }
        } catch (SQLException sqlEx) {
            log.error("Error al obtener el valor de la parte high de la clave: {}",sqlEx.getMessage(),sqlEx);
        }
        return outKey;
    }
    @Override
    public boolean updateGUID(final GUIDDispenserDef dispDef,
    						  final HighLowKey newHighKey) {
        if (_checkDispenserDef(dispDef) != null) throw new IllegalArgumentException(_checkDispenserDef(dispDef));
        if (newHighKey == null) throw new IllegalArgumentException("La nueva clave high NO es valida!");
        
        String guidsTable = dispDef.getProperty(GUIDSTABLE);
        String highKeyCol = dispDef.getProperty(HIGHKEYCOLUMN);
        String seqCol = dispDef.getProperty(SEQUENCENAMECOLUMN);
        
        boolean outResult = false;
        try {
	        @Cleanup Connection conx = _conxProvider.get();
	        @Cleanup PreparedStatement ps = null;
            // Obtener una conexion con la base de datos
            // TODO terminar cuando se "migre" la clase BBDDConnectionHelpper
//            conx = BDConnectionHelpper.getConnection(dispDef.getAppCode(),conxName);
            String sqlUPDATE = "UPDATE " + guidsTable + " " +
                                  "SET " + highKeyCol + " = ? " +
                                "WHERE " + seqCol + " = ?";

            ps = conx.prepareStatement(sqlUPDATE);
            ps.setString(1,newHighKey.toString());     // El nuevo valor de HIGH
            ps.setString(2,dispDef.getSequenceName()); // El nombre de la secuencia
            outResult = ps.executeUpdate() != 1;	// ps.executeUpdate == 1 > true 
        } catch (SQLException sqlEx) {
            log.error("Error al actualizar el valor high de la clave en la base de datos: {}",sqlEx.getMessage(),sqlEx);
        }
        return outResult;
    }

///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS PRIVADOS
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Comprueba la defincion de un dispenser y devuelve una cadena con los errores
     * @param dispDef La definicion del dispenser
     * @return los errores en la definicion o null si no hay errores
     */
    private static String _checkDispenserDef(final GUIDDispenserDef dispDef) {
        StringBuilder sb = new StringBuilder("");
        if (dispDef == null) {
            sb.append("\r\nDescriptor del dispenser NO es valido!\r\n");
        } else {
            if (dispDef.getProperties() == null) {
                sb.append("\tNo se han definido las propiedades del dispenser que permiten conocer la Base de Datos\r\n");
            } else {
                String guidsTable = dispDef.getProperty(GUIDSTABLE);
                String highKeyCol = dispDef.getProperty(HIGHKEYCOLUMN);
                //String seqCol = dispDef.properties.getProperty(SEQUENCENAMECOLUMN);
                if (guidsTable == null) sb.append("\tFalta la propiedad " + GUIDSTABLE + " en la definicion del dispenser " + dispDef.getSequenceName());
                if (highKeyCol == null) sb.append("\tFalta la propiedad " + HIGHKEYCOLUMN + " en la definicion del dispenser " + dispDef.getSequenceName());
                if (guidsTable == null) sb.append("\tFalta la propiedad " + GUIDSTABLE + " en la definicion del dispenser " + dispDef.getSequenceName());
            }
        }
        if (sb.length() == 0) return null;
        return sb.toString();
    }

}
