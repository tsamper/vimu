package org.tsamper.proyecto_final.modelo.daos;

import org.tsamper.proyecto_final.modelo.Concierto;
import org.tsamper.proyecto_final.modelo.Usuario;
import org.tsamper.proyecto_final.modelo.bbdd.ConexionBBDD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Contiene los métodos relacionados con la bbdd de la clase Guardado
 */
public class GuardadoDao {

    /**
     * Introducir un guardado en la bbdd
     * @param concierto Concierto a guardar
     * @param usuario Usuario que guarda el concierto
     * @return Entero si se ha guardado o no
     */
    public static int introducirConciertoGuardado(Concierto concierto, Usuario usuario){
        String querySelectGuardado = "SELECT * FROM guardados WHERE usuario = ? and concierto = ?";
        boolean yaCreado = false;
        String queryGuardado = "INSERT INTO guardados (usuario, concierto) "
                + "VALUES (?, ?)";
        try (PreparedStatement statement =  ConexionBBDD.getConnection().prepareStatement(querySelectGuardado)) {
            statement.setInt(1, usuario.getId());
            statement.setInt(2, concierto.getId());
            try (ResultSet resultSeto = statement.executeQuery()) {
                if (resultSeto.next()) {
                    yaCreado = true;
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al ejecutar la consulta: " + e.getMessage());
        }
        if (!yaCreado){
            try (PreparedStatement statement =  ConexionBBDD.getConnection().prepareStatement(queryGuardado)) {
                statement.setInt(1, usuario.getId());
                statement.setInt(2, concierto.getId());
                return statement.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Error al ejecutar la consulta: " + e.getMessage());
            }
        }
        return 0;
    }

    /**
     * Obtiene los guardados de un usuaario
     * @param usuario Usuario
     * @return Lista de conciertos guardados
     */
    public static List<Concierto> buscarGuardadosPorUsuario(Usuario usuario){
        String query = "SELECT * FROM conciertos INNER JOIN guardados ON conciertos.id = guardados.concierto WHERE guardados.usuario = ? AND fecha>CURRENT_DATE";
        List<Concierto> conciertos = new ArrayList<>();
        try (PreparedStatement statement = ConexionBBDD.getConnection().prepareStatement(query)) {
            statement.setInt(1, usuario.getId());
            ResultSet rsConciertos = statement.executeQuery();
            while (rsConciertos.next()) {
                Concierto concierto = new Concierto();
                concierto.setId(rsConciertos.getInt("id"));
                concierto.setNombre(rsConciertos.getString("nombre"));
                concierto.setImagen(rsConciertos.getString("imagen"));
                concierto.setRecinto(RecintoDao.obtenerRecintoPorId(rsConciertos.getInt("recinto")));
                concierto.setFecha(LocalDate.parse(rsConciertos.getString("fecha")));
                concierto.setHora(LocalTime.parse(rsConciertos.getString("hora")));
                concierto.setCantidadEntradas(rsConciertos.getInt("cantidad_entradas"));
                concierto.setCantidadEntradasVendidas(rsConciertos.getInt("cantidad_entradas_vendidas"));
                concierto.setCantidadEntradasVip(rsConciertos.getInt("cantidad_entradas_vip"));
                concierto.setCantidadEntradasVipVendidas(rsConciertos.getInt("cantidad_entradas_vip_vendidas"));
                concierto.setPrecioEntradas(rsConciertos.getDouble("precio_entradas"));
                concierto.setPrecioEntradasVip(rsConciertos.getInt("precio_entradas_vip"));
                concierto.setGrupo(GrupoDao.obtenerGrupoPorId(rsConciertos.getInt("grupo")));
                conciertos.add(concierto);
            }
            return conciertos;
        } catch (SQLException e) {
            System.out.println("Error al ejecutar la consulta: " + e.getMessage());
        }
        return null;
    }

    /**
     * Elimina un concierto guardado de un usuario
     * @param concierto Concierto
     * @param usuario Usuario
     */
    public static void eliminarGuardado(Concierto concierto, Usuario usuario){
        String query = "DELETE FROM guardados WHERE usuario = ? AND concierto = ?";
        try (PreparedStatement statement = ConexionBBDD.getConnection().prepareStatement(query)) {
            statement.setInt(1, usuario.getId());
            statement.setInt(2, concierto.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al ejecutar la consulta: " + e.getMessage());
        }
    }
}
