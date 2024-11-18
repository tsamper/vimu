package org.tsamper.proyecto_final.modelo.daos;

import org.tsamper.proyecto_final.modelo.Grupo;
import org.tsamper.proyecto_final.modelo.bbdd.ConexionBBDD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Contiene los métodos relacionados con la bbdd de la clase Grupo
 */
public class GrupoDao {

    /**
     * Introducir un grupo a la bbdd
     * @param grupo Grupo
     */
    public static void introducirGrupo(Grupo grupo){
        String querySelect = "SELECT id FROM grupos WHERE nombre = ?";
        String queryGrupos = "INSERT INTO grupos (nombre, descripcion, genero, ciudad, pais, imagen, perfil_spotify) "+ "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = ConexionBBDD.getConnection().prepareStatement(querySelect)){
            statement.setString(1, grupo.getNombre());
            ResultSet rs = statement.executeQuery();
            if(!rs.next()) {
                try (PreparedStatement statement1 = ConexionBBDD.getConnection().prepareStatement(queryGrupos)) {
                    statement1.setString(1, grupo.getNombre());
                    statement1.setString(2, grupo.getDescripcion());
                    statement1.setString(3, grupo.getGenero());
                    statement1.setString(4, grupo.getCiudad());
                    statement1.setString(5, grupo.getPais());
                    statement1.setString(6, grupo.getImagen());
                    statement1.setString(7, grupo.getPerfilSpotify());
                    statement1.executeUpdate();
                } catch (SQLException e) {
                    System.out.println("Error al ejecutar la consulta: " + e.getMessage());
                }
            }
        }catch (SQLException e) {
            System.out.println("Error al ejecutar la consulta: " + e.getMessage());
        }
    }

    /**
     * Obtiene un grupo en función de su id
     * @param id id del grupo
     * @return Grupo
     */
    public static Grupo obtenerGrupoPorId(int id){
        String querySelect = "SELECT * FROM grupos WHERE id = ?";
        try (PreparedStatement statement = ConexionBBDD.getConnection().prepareStatement(querySelect)){
            statement.setInt(1, id);
            ResultSet rs =  statement.executeQuery();
            rs.next();
            Grupo grupo = new Grupo();
            grupo.setId(rs.getInt("id"));
            grupo.setNombre(rs.getString("nombre"));
            grupo.setDescripcion(rs.getString("descripcion"));
            grupo.setGenero(rs.getString("genero"));
            grupo.setCiudad(rs.getString("ciudad"));
            grupo.setPais(rs.getString("pais"));
            grupo.setImagen(rs.getString("imagen"));
            grupo.setPerfilSpotify(rs.getString("perfil_spotify"));
            return grupo;
        }catch (SQLException e) {
            System.out.println("Error al ejecutar la consulta: " + e.getMessage());
        }
        return null;
    }

    /**
     * Obtiene un grupo por su nombre
     * @param nombre Nombre del grupo
     * @return Grupo
     */
    public static Grupo obtenerGrupoPorNombre(String nombre){
        String querySelect = "SELECT * FROM grupos WHERE nombre = ?";
        try (PreparedStatement statement = ConexionBBDD.getConnection().prepareStatement(querySelect)){
            statement.setString(1, nombre);
            ResultSet rs =  statement.executeQuery();
            rs.next();
            Grupo grupo = new Grupo();
            grupo.setId(rs.getInt("id"));
            grupo.setNombre(rs.getString("nombre"));
            grupo.setDescripcion(rs.getString("descripcion"));
            grupo.setGenero(rs.getString("genero"));
            grupo.setCiudad(rs.getString("ciudad"));
            grupo.setPais(rs.getString("pais"));
            grupo.setImagen(rs.getString("imagen"));
            grupo.setPerfilSpotify(rs.getString("perfil_spotify"));
            return grupo;
        }catch (SQLException e) {
            System.out.println("Error al ejecutar la consulta: " + e.getMessage());
        }
        return null;
    }
}
