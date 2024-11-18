package org.tsamper.proyecto_final.modelo.daos;

import org.tsamper.proyecto_final.modelo.EntradaConcierto;
import org.tsamper.proyecto_final.modelo.Usuario;
import org.tsamper.proyecto_final.modelo.bbdd.ConexionBBDD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Contiene los métodos relacionados con la bbdd de la clase EntradaConcierto
 */
public class EntradaDao {

    /**
     * Introducir una entrada a la bbdd
     * @param entrada Entrada
     */
    public static void introducirEntradaConcierto(EntradaConcierto entrada){
        String queryEntradas = "INSERT INTO entradas (precio, usuario, tipo, fecha, concierto) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement =  ConexionBBDD.getConnection().prepareStatement(queryEntradas)) {
            statement.setDouble(1, entrada.getPrecio());
            statement.setInt(2, entrada.getUsuario().getId());
            statement.setString(3, entrada.getTipo());
            statement.setString(4, LocalDateTime.now().toString());
            statement.setInt(5, entrada.getConcierto().getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al ejecutar la consulta: " + e.getMessage());
        }
    }

    /**
     * Obtiene las entradas que tiene un usuario
     * @param usuario Usuario
     * @return Lista de entradas
     */
    public static List<EntradaConcierto> buscarEntradasPorUsuario(Usuario usuario){
        String queryConciertos = "SELECT * FROM entradas INNER JOIN conciertos ON entradas.concierto=conciertos.id WHERE entradas.usuario = ? AND conciertos.fecha > CURRENT_DATE";
        List<EntradaConcierto> entradas = new ArrayList<>();
        try (PreparedStatement statement = ConexionBBDD.getConnection().prepareStatement(queryConciertos)) {
            statement.setInt(1, usuario.getId());
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                EntradaConcierto entrada = new EntradaConcierto();
                entrada.setId(rs.getInt("id"));
                entrada.setPrecio(rs.getDouble("precio"));
                entrada.setUsuario(usuario);
                entrada.setTipo(rs.getString("tipo"));
                entrada.setFechaCompra(LocalDateTime.parse(rs.getString("fecha"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                entrada.setConcierto(ConciertoDao.buscarConciertoPorId(rs.getInt("concierto")));
                entradas.add(entrada);
            }
            return entradas;
        } catch (SQLException e) {
            System.out.println("Error al ejecutar la consulta: " + e.getMessage());
        }
        return null;
    }
}
