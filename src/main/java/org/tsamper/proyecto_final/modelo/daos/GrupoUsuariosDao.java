package org.tsamper.proyecto_final.modelo.daos;

import org.tsamper.proyecto_final.modelo.GrupoUsuarios;
import org.tsamper.proyecto_final.modelo.Usuario;
import org.tsamper.proyecto_final.modelo.bbdd.ConexionBBDD;
import org.tsamper.proyecto_final.modelo.enums.Privilegios;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Contiene los métodos relacionados con la bbdd de la clase GrupoUsuarios
 */
public class GrupoUsuariosDao {

    /**
     * Introducir un grupo de usuarios en la bbdd a partir de un usuario
     * @param usuario Usuario
     */
    public static void introducirGrupoUsuarios(Usuario usuario){
        String querySelect = "SELECT id FROM grupo_usuarios WHERE tipo = ?";
        String queryGrupos = "INSERT INTO grupo_usuarios (tipo) "+ "VALUES (?)";
        try (PreparedStatement statement = ConexionBBDD.getConnection().prepareStatement(querySelect)){
            statement.setString(1, usuario.getGrupoUsuarios().getTipo().name());
            ResultSet rs = statement.executeQuery();
            if(!rs.next()) {
                try (PreparedStatement statement1 = ConexionBBDD.getConnection().prepareStatement(queryGrupos)) {
                    statement1.setString(1, usuario.getGrupoUsuarios().getTipo().name());
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
     * Obtiene un grupo de usuarios por id
     * @param id id
     * @return Grupo de usuarios
     */
    public static GrupoUsuarios obtenerGrupoPorId(int id){
        GrupoUsuarios grupo = null;
        String querySelect = "SELECT * FROM grupo_usuarios WHERE id = ?";
        try (PreparedStatement statement = ConexionBBDD.getConnection().prepareStatement(querySelect)){
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
               GrupoUsuarios grupoUsuarios = new GrupoUsuarios();
               grupoUsuarios.setTipo(Privilegios.valueOf(rs.getString("tipo")));
               return grupoUsuarios;
            }
        }catch (SQLException e){
            System.out.println("Error al ejecutar la consulta: " + e.getMessage());
        }
        return null;
    }
}
