package org.tsamper.proyecto_final.modelo.daos;

import org.tsamper.proyecto_final.modelo.Usuario;
import org.tsamper.proyecto_final.modelo.bbdd.ConexionBBDD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Contiene los métodos relacionados con la bbdd de la clase Usuario
 */
public class UsuarioDao {

    /**
     * Introducir un usuario en la bbdd
     * @param usuario Usuario
     * @return Entero en función de si se ha introducido o no
     */
    public static int introducirUsuario(Usuario usuario){
        String querySelect = "SELECT id FROM usuarios WHERE nomusuario = ? OR email = ?";
        String queryUsuarios = "INSERT INTO usuarios (nombre, apellidos, email, nomusuario, contrasenya, privilegios) "+ "VALUES (?, ?, ?, ?, ?, ?)";
        String queryGrupos = "SELECT id FROM grupo_usuarios WHERE tipo = ?";
        try (PreparedStatement statement = ConexionBBDD.getConnection().prepareStatement(querySelect)){
            statement.setString(1, usuario.getNomUsuario());
            statement.setString(2, usuario.getEmail());
            ResultSet rs = statement.executeQuery();
            if(!rs.next()) {
                try (PreparedStatement statement1 = ConexionBBDD.getConnection().prepareStatement(queryUsuarios)) {
                    int idGrupo = -1;
                    try (PreparedStatement statement2 =  ConexionBBDD.getConnection().prepareStatement(queryGrupos)){
                        statement2.setString(1, usuario.getGrupoUsuarios().getTipo().name());
                        try (ResultSet resultSeto = statement2.executeQuery()) {
                            while (resultSeto.next()) {
                                idGrupo = resultSeto.getInt("id");
                            }
                        }
                    }
                    statement1.setString(1, usuario.getNombre());
                    statement1.setString(2, usuario.getApellidos());
                    statement1.setString(3, usuario.getEmail());
                    statement1.setString(4, usuario.getNomUsuario());
                    statement1.setString(5, usuario.getContrasenya());
                    statement1.setInt(6, idGrupo);
                    return statement1.executeUpdate();
                } catch (SQLException e) {
                    System.out.println("Error al ejecutar la consulta: " + e.getMessage());
                }
            }
        }catch (SQLException e) {
            System.out.println("Error al ejecutar la consulta: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Comprueba si existe el usuario con ese nombre y contraseña y lo devuelve si existe
     * @param nomUsuario Nombre de usuario
     * @param contrasenya Contraseña
     * @return Usuario
     */
    public static Usuario comprobarUsuario(String nomUsuario, String contrasenya){
        String querySelect = "SELECT * FROM usuarios WHERE nomusuario = ? AND contrasenya = ?";
        try (PreparedStatement statement = ConexionBBDD.getConnection().prepareStatement(querySelect)) {
            statement.setString(1, nomUsuario);
            statement.setString(2, contrasenya);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
               Usuario usuario = new Usuario();
               usuario.setId(rs.getInt("id"));
               usuario.setNombre(rs.getString("nombre"));
               usuario.setApellidos(rs.getString("apellidos"));
               usuario.setEmail(rs.getString("email"));
               usuario.setNomUsuario(rs.getString("nomusuario"));
               usuario.setContrasenya(rs.getString("contrasenya"));
               usuario.setGrupoUsuarios(GrupoUsuariosDao.obtenerGrupoPorId(rs.getInt("privilegios")));
               return usuario;
            }
        }catch (SQLException e) {
            System.out.println("Error al ejecutar la consulta: " + e.getMessage());
        }
        return null;
    }

    /**
     * Obtiene un usuario por sus id
     * @param id Id de usuario
     * @return Usuario
     */
    public static Usuario obtenerUsuarioPorId(int id){
        String querySelect = "SELECT * FROM usuarios WHERE id = ?";
        try (PreparedStatement statement = ConexionBBDD.getConnection().prepareStatement(querySelect)){
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();
            if (rs.next()){
                Usuario usuario = new Usuario();
                usuario.setId(rs.getInt("id"));
                usuario.setNombre(rs.getString("nombre"));
                usuario.setApellidos(rs.getString("apellidos"));
                usuario.setEmail(rs.getString("email"));
                usuario.setNomUsuario(rs.getString("nomusuario"));
                return usuario;
            }
        }catch (SQLException e) {
            System.out.println("Error al ejecutar la consulta: " + e.getMessage());
        }
        return null;
    }

    /**
     * Obtiene un usuario por su nombre
     * @param nombre Nombre de usuario
     * @return Usuario
     */
    public static Usuario obtenerUsuarioPorNomUsuario(String nombre){
        String querySelect = "SELECT * FROM usuarios WHERE nomusuario = ?";
        try (PreparedStatement statement = ConexionBBDD.getConnection().prepareStatement(querySelect)){
            statement.setString(1, nombre);
            ResultSet rs = statement.executeQuery();
            if (rs.next()){
                Usuario usuario = new Usuario();
                usuario.setId(rs.getInt("id"));
                usuario.setNombre(rs.getString("nombre"));
                usuario.setApellidos(rs.getString("apellidos"));
                usuario.setEmail(rs.getString("email"));
                usuario.setNomUsuario(rs.getString("nomusuario"));
                return usuario;
            }
        }catch (SQLException e) {
            System.out.println("Error al ejecutar la consulta: " + e.getMessage());
        }
        return null;
    }


}
