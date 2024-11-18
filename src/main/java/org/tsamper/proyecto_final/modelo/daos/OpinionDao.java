package org.tsamper.proyecto_final.modelo.daos;

import org.tsamper.proyecto_final.modelo.*;
import org.tsamper.proyecto_final.modelo.bbdd.ConexionBBDD;
import org.tsamper.proyecto_final.modelo.enums.OpcionesOpinion;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Contiene los métodos relacionados con la bbdd de la clase Opìnion
 */
public class OpinionDao {

    /**
     * Introducir una opinión en la bbdd
     * @param opinion Opinion
     */
    public static void introducirOpinion(Opinion opinion){
        String queryOpinion = "INSERT INTO opiniones (usuario, comentario, fecha, recomendado) "
                + "VALUES (?, ?, ?, ?)";
        String querySelect = "SELECT * FROM opiniones WHERE usuario = ? AND comentario = ?";
        String queryOpinion2 = "INSERT INTO opiniones_conciertos_grupos (grupo, concierto, opinion) "
                + "VALUES (?, ?, ?)";
        try (PreparedStatement statement =  ConexionBBDD.getConnection().prepareStatement(queryOpinion)) {
            statement.setInt(1, opinion.getUsuario().getId());
            statement.setString(2, opinion.getComentario());
            statement.setString(3, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toString());
            statement.setString(4, opinion.getRecomendado().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al ejecutar la consulta: " + e.getMessage());
        }
        try (PreparedStatement statement =  ConexionBBDD.getConnection().prepareStatement(querySelect)) {
            statement.setInt(1, opinion.getUsuario().getId());
            statement.setString(2, opinion.getComentario());
            ResultSet rs = statement.executeQuery();
            rs.next();
            try (PreparedStatement statement1 =  ConexionBBDD.getConnection().prepareStatement(queryOpinion2)) {
                statement1.setInt(1, opinion.getGrupo().getId());
                statement1.setInt(2, opinion.getConcierto().getId());
                statement1.setInt(3, rs.getInt("id"));
                statement1.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Error al ejecutar la consulta: " + e.getMessage());
        }
    }

    /**
     * Obtiene las opiniones de los conciertos de un grupo
     * @param grupo Grupo
     * @return Lista de opiniones
     */
    public static List<Opinion> buscarOpinionesPorGrupo(Grupo grupo){
        String query = "SELECT * FROM opiniones INNER JOIN opiniones_conciertos_grupos ON opiniones.id = opiniones_conciertos_grupos.opinion WHERE opiniones_conciertos_grupos.grupo = ?";
        List<Opinion> opiniones = new ArrayList<>();
        try (PreparedStatement statement = ConexionBBDD.getConnection().prepareStatement(query)) {
            statement.setInt(1, grupo.getId());
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Opinion opinion = new Opinion();
                opinion.setId(rs.getInt("id"));
                opinion.setComentario(rs.getString("comentario"));
                opinion.setUsuario(UsuarioDao.obtenerUsuarioPorId(rs.getInt("usuario")));
                opinion.setFecha(LocalDate.parse(rs.getString("fecha"), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                opinion.setGrupo(grupo);
                opinion.setConcierto(ConciertoDao.buscarConciertoPorId(rs.getInt("concierto")));
                opinion.setRecomendado(OpcionesOpinion.valueOf(rs.getString("recomendado")));
                opiniones.add(opinion);
            }
            return opiniones;
        } catch (SQLException e) {
            System.out.println("Error al ejecutar la consulta: " + e.getMessage());
        }
        return null;
    }

    /**
     * Compruba si el usuario ya ha comentado ese concierto o no
     * @param usuario Usuario
     * @param concierto Concierto
     * @return Si ya ha comentado o no
     */
    public static boolean buscarOpinionesPorUsuarioYConcierto(Usuario usuario, Concierto concierto){
        String query = "SELECT * FROM opiniones INNER JOIN opiniones_conciertos_grupos ON opiniones.id = opiniones_conciertos_grupos.opinion" +
                " WHERE opiniones_conciertos_grupos.concierto = ? AND opiniones.usuario = ?";
        List<Opinion> opiniones = new ArrayList<>();
        try (PreparedStatement statement = ConexionBBDD.getConnection().prepareStatement(query)) {
            statement.setInt(1, concierto.getId());
            statement.setInt(2, usuario.getId());
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error al ejecutar la consulta: " + e.getMessage());
        }
        return false;
    }
}
