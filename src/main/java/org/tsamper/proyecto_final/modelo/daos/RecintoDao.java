package org.tsamper.proyecto_final.modelo.daos;

import org.tsamper.proyecto_final.modelo.Recinto;
import org.tsamper.proyecto_final.modelo.bbdd.ConexionBBDD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Contiene los métodos relacionados con la bbdd de la clase Recinto
 */
public class RecintoDao {

    /**
     * Introducir un recinto en la bbdd
     * @param recinto Recinto
     */
    public static void introducirRecinto(Recinto recinto){
        String querySelect = "SELECT id FROM recintos WHERE nombre = ?";
        String queryRecintos = "INSERT INTO recintos (nombre, direccion, ciudad, telefono, correo, enlace_maps) "+ "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = ConexionBBDD.getConnection().prepareStatement(querySelect)){
            statement.setString(1, recinto.getNombre());
            ResultSet rs = statement.executeQuery();
            if(!rs.next()) {
                try (PreparedStatement statement1 = ConexionBBDD.getConnection().prepareStatement(queryRecintos)) {
                    statement1.setString(1, recinto.getNombre());
                    statement1.setString(2, recinto.getDireccion());
                    statement1.setString(3, recinto.getCiudad());
                    statement1.setString(4, recinto.getTelefono());
                    statement1.setString(5, recinto.getEmail());
                    statement1.setString(6, recinto.getEnlaceMaps());
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
     * Obtiene un recinto por su id
     * @param id Id de Recinto
     * @return Recinto
     */
    public static Recinto obtenerRecintoPorId(int id){
        String querySelect = "SELECT * FROM recintos WHERE id = ?";
        try (PreparedStatement statement = ConexionBBDD.getConnection().prepareStatement(querySelect)){
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();
            if (rs.next()){
                Recinto recinto = new Recinto();
                recinto.setId(rs.getInt("id"));
                recinto.setNombre(rs.getString("nombre"));
                recinto.setDireccion(rs.getString("direccion"));
                recinto.setCiudad(rs.getString("ciudad"));
                recinto.setTelefono(rs.getString("telefono"));
                recinto.setEmail(rs.getString("correo"));
                recinto.setEnlaceMaps(rs.getString("enlace_maps"));
                return recinto;
            }
        }catch (SQLException e) {
            System.out.println("Error al ejecutar la consulta: " + e.getMessage());
        }
        return null;
    }

    /**
     * Obtiene un recinto por su nombre
     * @param nombre Nombre del recinto
     * @return Recinto
     */
    public static Recinto obtenerRecintoPorNombre(String nombre){
        String querySelect = "SELECT * FROM recintos WHERE nombre = ?";
        try (PreparedStatement statement = ConexionBBDD.getConnection().prepareStatement(querySelect)){
            statement.setString(1, nombre);
            ResultSet rs = statement.executeQuery();
            if (rs.next()){
                Recinto recinto = new Recinto();
                recinto.setId(rs.getInt("id"));
                recinto.setNombre(rs.getString("nombre"));
                recinto.setDireccion(rs.getString("direccion"));
                recinto.setCiudad(rs.getString("ciudad"));
                recinto.setTelefono(rs.getString("telefono"));
                recinto.setEmail(rs.getString("correo"));
                recinto.setEnlaceMaps(rs.getString("enlace_maps"));
                return recinto;
            }
        }catch (SQLException e) {
            System.out.println("Error al ejecutar la consulta: " + e.getMessage());
        }
        return null;
    }
}
