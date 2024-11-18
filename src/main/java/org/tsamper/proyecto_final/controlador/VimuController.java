package org.tsamper.proyecto_final.controlador;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.json.JSONArray;
import org.json.JSONObject;
import org.tsamper.proyecto_final.modelo.*;
import org.tsamper.proyecto_final.modelo.bbdd.ConexionBBDD;
import org.tsamper.proyecto_final.modelo.constantes.Constantes;
import org.tsamper.proyecto_final.modelo.daos.*;
import org.tsamper.proyecto_final.modelo.enums.OpcionesOpinion;
import org.tsamper.proyecto_final.modelo.enums.Privilegios;
import org.tsamper.proyecto_final.vista.VimuApplication;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Controlador
 */
public class VimuController {
    VimuApplication vista;
    public Usuario usuarioActivo = null;
    public ObservableList<Concierto> conciertos = FXCollections.observableArrayList();

    public VimuController(VimuApplication vista) {
        this.vista = vista;
    }

    /**
     * Solo se crean las tablas y se importan los datos si el usuario no estaba creado, para que solo se haga la primera
     * vez que se arranca el programa
     */
    public void conectarBBDD(){
        ConexionBBDD.crearConexion();
        boolean yaCreado = ConexionBBDD.crearUsuario();
        ConexionBBDD.crearTablas();
        ConexionBBDD.conectar();
        if (!yaCreado){
            importar();
        }
        obtenerConciertos();
    }

    /**
     * Desconectar la bbdd
     */
    public void desconectar(){
        ConexionBBDD.desconectar();
    }

    /**
     * Guardar todos los conciertos de un archivo json en una lista
     * @param archivoConciertos Archivo Json
     * @return Lista de conciertos
     */
    public List<Concierto> guardarConciertos(File archivoConciertos){
        List<Concierto> conciertos = new ArrayList<>();
        try (FileReader reader = new FileReader(archivoConciertos)) {
            char[] buffer = new char[(int) archivoConciertos.length()];
            reader.read(buffer);
            String jsonString = new String(buffer);
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray("conciertos");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonConcierto = jsonArray.getJSONObject(i);
                Concierto concierto = new Concierto();
                concierto.setNombre(jsonConcierto.getString("nombre"));
                concierto.setImagen(jsonConcierto.getString("imagen"));
                JSONObject jsonRecinto = jsonConcierto.getJSONObject("recinto");
                Recinto recinto = new Recinto();
                recinto.setNombre(jsonRecinto.getString("nombre"));
                recinto.setDireccion(jsonRecinto.getString("direccion"));
                recinto.setCiudad(jsonRecinto.getString("ciudad"));
                recinto.setTelefono(jsonRecinto.getString("telefono"));
                recinto.setEmail(jsonRecinto.getString("correo"));
                recinto.setEnlaceMaps(jsonRecinto.getString("enlace_maps"));
                concierto.setRecinto(recinto);
                concierto.setFecha(LocalDate.parse(jsonConcierto.getString("fecha")));
                concierto.setHora(LocalTime.parse(jsonConcierto.getString("hora")));
                concierto.setCantidadEntradas(jsonConcierto.getInt("cantidad_entradas"));
                concierto.setPrecioEntradas(jsonConcierto.getInt("precio_entradas"));
                concierto.setCantidadEntradasVip(jsonConcierto.getInt("cantidad_entradas_vip"));
                concierto.setPrecioEntradasVip(jsonConcierto.getInt("precio_entradas_vip"));
                JSONObject jsonGrupo = jsonConcierto.getJSONObject("grupo");
                Grupo grupo = new Grupo();
                grupo.setNombre(jsonGrupo.getString("nombre"));
                grupo.setDescripcion(jsonGrupo.getString("descripcion"));
                grupo.setGenero(jsonGrupo.getString("genero"));
                grupo.setCiudad(jsonGrupo.getString("ciudad"));
                grupo.setPais(jsonGrupo.getString("pais"));
                grupo.setImagen(jsonGrupo.getString("imagen"));
                grupo.setPerfilSpotify(jsonGrupo.getString("perfil_spotify"));
                concierto.setGrupo(grupo);
                concierto.setPromotor(UsuarioDao.obtenerUsuarioPorNomUsuario(jsonConcierto.getString("promotor")));
                conciertos.add(concierto);
            }
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
        return conciertos;
    }

    /**
     * Guardar todos los usuarios de un archivo json en una lista
     * @param archivoUsuarios Archivo Json
     * @return Lista de usuarios
     */
    public List<Usuario> guardarUsuarios(File archivoUsuarios){
        List<Usuario> usuarios = new ArrayList<>();
        try (FileReader reader = new FileReader(archivoUsuarios)) {
            char[] buffer = new char[(int) archivoUsuarios.length()];
            reader.read(buffer);
            String jsonString = new String(buffer);
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray("usuarios");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonConcierto = jsonArray.getJSONObject(i);
                Usuario usuario = new Usuario();
                usuario.setNombre(jsonConcierto.getString("nombre"));
                usuario.setApellidos(jsonConcierto.getString("apellidos"));
                usuario.setEmail(jsonConcierto.getString("email"));
                usuario.setNomUsuario(jsonConcierto.getString("nomusuario"));
                usuario.setContrasenya(jsonConcierto.getString("contrasenya"));
                JSONObject jsonUsuarios = jsonConcierto.getJSONObject("privilegios");
                GrupoUsuarios grupoUsuarios = new GrupoUsuarios();
                grupoUsuarios.setTipo(Privilegios.valueOf(jsonUsuarios.getString("tipo").toUpperCase()));
                usuario.setGrupoUsuarios(grupoUsuarios);
                usuarios.add(usuario);
            }
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
        return usuarios;
    }

    /**
     * Guardar todas las canciones de un archivo json en una lista
     * @param archivoCanciones Archivo Json
     * @return Lista de canciones
     */
    public List<Cancion> guardarCanciones(File archivoCanciones){
        List<Cancion> canciones = new ArrayList<>();
        try (FileReader reader = new FileReader(archivoCanciones)) {
            char[] buffer = new char[(int) archivoCanciones.length()];
            reader.read(buffer);
            String jsonString = new String(buffer);
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray("canciones");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonConcierto = jsonArray.getJSONObject(i);
                Cancion cancion = new Cancion();
                cancion.setTitulo(jsonConcierto.getString("titulo"));
                cancion.setGrupo(GrupoDao.obtenerGrupoPorNombre(jsonConcierto.getString("grupo")));
                cancion.setEnlaceYoutube(jsonConcierto.getString("enlace_youtube"));
                canciones.add(cancion);
            }
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
        return canciones;
    }

    /**
     * Guardar todos las entradas de conciertos de un archivo json en una lista
     * @param archivoEntradas Archivo Json
     * @return Lista de entradas
     */
    public List<EntradaConcierto> guardarEntradas(File archivoEntradas){
        List<EntradaConcierto> entradas = new ArrayList<>();
        try (FileReader reader = new FileReader(archivoEntradas)) {
            char[] buffer = new char[(int) archivoEntradas.length()];
            reader.read(buffer);
            String jsonString = new String(buffer);

            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray("entradas");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonEntrada = jsonArray.getJSONObject(i);
                EntradaConcierto entrada = new EntradaConcierto();
                entrada.setPrecio(jsonEntrada.getDouble("precio"));
                entrada.setUsuario(UsuarioDao.obtenerUsuarioPorNomUsuario(jsonEntrada.getString("usuario")));
                entrada.setTipo(jsonEntrada.getString("tipo"));
                entrada.setFechaCompra(LocalDateTime.parse(jsonEntrada.getString("fecha_compra")));
                entrada.setConcierto(ConciertoDao.buscarConciertosPorNombre(jsonEntrada.getString("concierto")));
                entradas.add(entrada);
            }
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
        return entradas;
    }

    /**
     * Guardar todas las opiniones de un archivo json en una lista
     * @param archivoOpiniones Archivo Json
     * @return Lista de opiniones
     */
    public List<Opinion> guardarOpiniones(File archivoOpiniones){
        List<Opinion> opiniones = new ArrayList<>();
        try (FileReader reader = new FileReader(archivoOpiniones)) {
            char[] buffer = new char[(int) archivoOpiniones.length()];
            reader.read(buffer);
            String jsonString = new String(buffer);

            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray("opiniones");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonOpinion = jsonArray.getJSONObject(i);
                Opinion opinion = new Opinion();
                opinion.setUsuario(UsuarioDao.obtenerUsuarioPorNomUsuario(jsonOpinion.getString("usuario")));
                opinion.setGrupo(GrupoDao.obtenerGrupoPorNombre(jsonOpinion.getString("grupo")));
                opinion.setConcierto(ConciertoDao.buscarConciertosPorNombre(jsonOpinion.getString("concierto")));
                opinion.setComentario(jsonOpinion.getString("comentario"));
                opinion.setFecha(LocalDate.parse(jsonOpinion.getString("fecha")));
                opinion.setRecomendado(OpcionesOpinion.valueOf(jsonOpinion.getString("recomendado")));
                opiniones.add(opinion);
            }
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
        return opiniones;
    }


    /**
     * Llama a todos los métodos de guardar elementos desde archivos json y guarda todos los resultados en la BBDD
     */
    public void importar(){
        try {
            File archivoUsuarios = new File(Constantes.DIR_JSON_USUARIOS);
            List<Usuario> usuarios = guardarUsuarios(archivoUsuarios);
            for (Usuario usuario : usuarios) {
                GrupoUsuariosDao.introducirGrupoUsuarios(usuario);
                UsuarioDao.introducirUsuario(usuario);
            }
            File archivoConciertos = new File(Constantes.DIR_JSON_CONCIERTOS);
            List<Concierto> conciertos = guardarConciertos(archivoConciertos);
            for (Concierto concierto : conciertos) {
                RecintoDao.introducirRecinto(concierto.getRecinto());
                GrupoDao.introducirGrupo(concierto.getGrupo());
                ConciertoDao.introducirConcierto(concierto, concierto.getPromotor());
            }
            File archivoCanciones = new File(Constantes.DIR_JSON_CANCIONES);
            List<Cancion> canciones = guardarCanciones(archivoCanciones);
            for (Cancion cancion : canciones) {
                CancionDao.introducirCancion(cancion);
            }
            File archivoEntradas = new File(Constantes.DIR_JSON_ENTRADAS);
            List<EntradaConcierto> entradas = guardarEntradas(archivoEntradas);
            for (EntradaConcierto entrada : entradas) {
                EntradaDao.introducirEntradaConcierto(entrada);
            }
            File archivoOpiniones = new File(Constantes.DIR_JSON_OPINIONES);
            List<Opinion> opiniones = guardarOpiniones(archivoOpiniones);
            for (Opinion opinion : opiniones) {
                OpinionDao.introducirOpinion(opinion);
            }
        }catch(NullPointerException e){
            System.out.println("Salida sin archivo");
        }
    }

    /**
     * Se guarda en el ObservableList los conciertos cuya fecha es superior a la fecha actual y los ordena por fecha
     */
    public void obtenerConciertos(){
        conciertos.clear();
        conciertos.addAll(ConciertoDao.buscarConciertosPorFecha());
        conciertos = conciertos.stream().sorted(Comparator.comparing(Concierto::getFecha))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }


    /**
     * Hace una llamada as la base de datos con el nombre y la contraseña del usuario para comprobar si existen los datos,
     * si existen se inicia sesión y se guardan los datos del usuario en una variable. Si se llega al pulsar sobre el botón
     * de comprar entrada se vuelve a esa pantalla, si no se va a la pantalla de mi perfil.
     * @param usuario Nombre de usuario
     * @param contrasenya Contraseña
     * @param mensajeLogin Label para mostrar si hay fallo en el inicio de sesión
     * @param concierto Concierto si se viene desde la pantalla del concierto
     */
    public void iniciarSesion(String usuario, String contrasenya, Label mensajeLogin, Concierto concierto){
        usuarioActivo = UsuarioDao.comprobarUsuario(usuario, contrasenya);
        if (usuarioActivo != null && concierto != null){
            vista.mostrarInfoConcierto(concierto);
        }else if(usuarioActivo != null) {
            vista.mostrarMiPerfil(concierto);
        }else{
            mensajeLogin.setVisible(true);
            mensajeLogin.setText("Usuario o contraseña errónea");
            mensajeLogin.getStyleClass().add("text-danger");
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(event -> mensajeLogin.setVisible(false));
            pause.play();
        }
    }


    /**
     * Registrar usuario, en este método se comprueba todos los datos antes de hacer la llamada a la bbdd
     * @param usernameField Nombre de usuario
     * @param contrasenyaField Contraseña
     * @param confirmarContrasenyaField Repetir contraseña
     * @param nombreField Nombre
     * @param apellidoField Apellidos
     * @param emailField Correo
     * @param privilegiosComboBox Tipo de usuario
     * @param mensajeRegistro Mensaje para mostrar los errores
     */
    public void registrarUsuario(TextField usernameField, TextField contrasenyaField, TextField confirmarContrasenyaField, TextField nombreField, TextField apellidoField, TextField emailField, ComboBox<Privilegios> privilegiosComboBox, Label mensajeRegistro) {
        String username = usernameField.getText();
        String contrasenya = contrasenyaField.getText();
        String confirmarContrasenya = confirmarContrasenyaField.getText();
        String nombre = nombreField.getText();
        String apellido = apellidoField.getText();
        String email = emailField.getText();
        String privilegio = "";
        if (privilegiosComboBox.getValue() != null){
            privilegio = privilegiosComboBox.getValue().name();
        }
        if (username.isEmpty() || contrasenya.isEmpty() || confirmarContrasenya.isEmpty() || email.isEmpty() ||
                privilegio.isEmpty()) {
            mensajeRegistro.setText("Por favor, complete todos los campos obligatorios.");
            mensajeRegistro.setVisible(true);
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(event -> mensajeRegistro.setVisible(false));
            pause.play();
            return;
        }

        if (!contrasenya.equals(confirmarContrasenya)) {
            mensajeRegistro.setText("Las contraseñas no coinciden.");
            mensajeRegistro.setVisible(true);
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(event -> mensajeRegistro.setVisible(false));
            pause.play();
            return;
        }
        Pattern patronCorreo = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = patronCorreo.matcher(email);
        if (!matcher.matches()) {
            mensajeRegistro.setText("El email no es correcto.");
            mensajeRegistro.setVisible(true);
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(event -> mensajeRegistro.setVisible(false));
            pause.play();
            return;
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setApellidos(apellido);
        usuario.setEmail(email);
        usuario.setNomUsuario(username);
        usuario.setContrasenya(contrasenya);
        usuario.setGrupoUsuarios(new GrupoUsuarios(Privilegios.valueOf(privilegio)));

        int resultado = UsuarioDao.introducirUsuario(usuario);
        System.out.println(resultado);
        if (resultado == 0) {
            mensajeRegistro.setText("Nombre de usuario o email ya registrados, elija otro");
            mensajeRegistro.setVisible(true);
            PauseTransition pause = new PauseTransition(Duration.seconds(1));
            pause.setOnFinished(event -> mensajeRegistro.setVisible(false));
            pause.play();
        }else{
            mensajeRegistro.setText("Usuario registrado con éxito");
            mensajeRegistro.setVisible(true);
            PauseTransition pause = new PauseTransition(Duration.seconds(1));
            pause.setOnFinished(event -> {
                mensajeRegistro.setVisible(false);
                vista.mostrarMiPerfil(null);
            });
            pause.play();
        }

    }

    /**
     * Registrar nuevo concierto, comprobar que todos los campos son correctos, introducir también un recinto o un grupo si
     * no estaban creados ya en la bbdd y guardar finalmente el concierto en la bbdd
     * @param grupoField Nombre del grupo
     * @param recintoField Nombre del recinto
     * @param fechaField Fecha
     * @param horaField Hora
     * @param cantidadEntradasField Cantidad de entradas normales
     * @param precioEntradasField Precio entradas normales
     * @param cantidadEntradasVipField Cantidad de entradas vip
     * @param precioEntradasVipField Precio entradas vip
     * @param imagen Cartel
     * @param mensajeRegistro Mensaje para mostar los fallos
     */
    public void registrarConcierto(TextField grupoField, TextField recintoField, DatePicker fechaField, TextField horaField,
                                   TextField cantidadEntradasField, TextField precioEntradasField, TextField cantidadEntradasVipField,
                                   TextField precioEntradasVipField, String imagen, Label mensajeRegistro){
        if (grupoField.getText().isEmpty() || recintoField.getText().isEmpty() || horaField.getText().isEmpty()
            || cantidadEntradasField.getText().isEmpty() || precioEntradasField.getText().isEmpty()
            || cantidadEntradasVipField.getText().isEmpty() || precioEntradasVipField.getText().isEmpty()
            || imagen.isEmpty()){
            mensajeRegistro.setText("Se deben rellenar todos los campos");
            mensajeRegistro.setVisible(true);
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(event -> mensajeRegistro.setVisible(false));
            pause.play();
            return;
        }
        Pattern patronEnteros = Pattern.compile("^[0-9]*$");
        Matcher matcherEntradasNormales = patronEnteros.matcher(cantidadEntradasField.getText());
        Matcher matcherEntradasVip = patronEnteros.matcher(cantidadEntradasVipField.getText());
        if (!matcherEntradasNormales.matches() || !matcherEntradasVip.matches()) {
            mensajeRegistro.setText("La cantidad de entradas debe ser un número entero");
            mensajeRegistro.setVisible(true);
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(event -> mensajeRegistro.setVisible(false));
            pause.play();
            return;
        }
        Pattern patronPrecios = Pattern.compile("^[0-9]+\\.?[0-9]{0,2}$");
        Matcher matcherPrecioNormales = patronPrecios.matcher(precioEntradasField.getText());
        Matcher matcherPrecioVip = patronPrecios.matcher(precioEntradasVipField.getText());
        if (!matcherPrecioNormales.matches() || !matcherPrecioVip.matches()) {
            mensajeRegistro.setText("El precio debe ser una número (30.50, 30)");
            mensajeRegistro.setVisible(true);
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(event -> mensajeRegistro.setVisible(false));
            pause.play();
            return;
        }
        Pattern patronHora = Pattern.compile("^(?:[01]\\d|2[0-3]):[0-5]\\d$");
        Matcher matcherHora = patronHora.matcher(horaField.getText());
        if (!matcherHora.matches()) {
            mensajeRegistro.setText("Formato de hora erróneo (20:35)");
            mensajeRegistro.setVisible(true);
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(event -> mensajeRegistro.setVisible(false));
            pause.play();
            return;
        }
        if (fechaField.getValue().isBefore(LocalDate.now())){
            mensajeRegistro.setText("Fecha errónea. Selecciona una fecha superior al día de hoy");
            mensajeRegistro.setVisible(true);
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(event -> mensajeRegistro.setVisible(false));
            pause.play();
            return;
        }
        Concierto concierto = new Concierto();
        Recinto recinto = RecintoDao.obtenerRecintoPorNombre(recintoField.getText());
        Grupo grupo = GrupoDao.obtenerGrupoPorNombre(grupoField.getText());
        if(recinto == null && grupo == null){
            vista.mostrarVentanaNuevoGrupoYRecinto(recintoField.getText(), grupoField.getText());
        }else if(recinto == null){
            vista.mostrarVentanaNuevoRecinto(recintoField.getText());
        }else if(grupo == null){
            vista.mostrarVentanaNuevoGrupo(grupoField.getText());
        }
        if (recinto != null && grupo != null){
            concierto.setNombre(grupo.getNombre() + " en " + recinto.getCiudad());
            concierto.setRecinto(recinto);
            concierto.setGrupo(grupo);
            concierto.setFecha(fechaField.getValue());
            concierto.setHora(LocalTime.parse(horaField.getText()));
            concierto.setCantidadEntradas(Integer.parseInt(cantidadEntradasField.getText()));
            concierto.setPrecioEntradas(Double.parseDouble(precioEntradasField.getText()));
            concierto.setCantidadEntradasVip(Integer.parseInt(cantidadEntradasVipField.getText()));
            concierto.setPrecioEntradasVip(Double.parseDouble(precioEntradasVipField.getText()));
            concierto.setImagen(imagen);
            ConciertoDao.introducirConcierto(concierto, usuarioActivo);
            mensajeRegistro.setText("Concierto añadido con éxito");
            mensajeRegistro.setVisible(true);
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(event -> {
                mensajeRegistro.setVisible(false);
                vista.mostrarInicio();
            });
            pause.play();

        }
    }

    /**
     * Guarda un nuevo recinto en la base de datos si todos los campos son correctos
     * @param nombreField Nombre Recinto
     * @param direccionField Dirección
     * @param ciudadField Ciudad
     * @param telefonoField Teléfono
     * @param emailField Correo
     * @param enlaceMapsField Enlace a Google Maps
     * @param mensajeRegistro Mensaje con los errores del registro
     * @return un booleano de si el registro es correcto o no
     */
    public boolean registrarRecinto(TextField nombreField, TextField direccionField, TextField ciudadField, TextField telefonoField,
                                 TextField emailField, TextField enlaceMapsField, Label mensajeRegistro){
        if (nombreField.getText().isEmpty() || direccionField.getText().isEmpty() || ciudadField.getText().isEmpty()
        || telefonoField.getText().isEmpty() || emailField.getText().isEmpty() || enlaceMapsField.getText().isEmpty()){
            mensajeRegistro.setText("Todos los campos debes estar completos.");
            mensajeRegistro.setVisible(true);
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(event -> mensajeRegistro.setVisible(false));
            pause.play();
            return false;
        }
        Recinto recinto = new Recinto();
        recinto.setNombre(nombreField.getText());
        recinto.setDireccion(direccionField.getText());
        recinto.setCiudad(ciudadField.getText());
        recinto.setTelefono(telefonoField.getText());
        recinto.setEmail(emailField.getText());
        recinto.setEnlaceMaps(enlaceMapsField.getText());
        Pattern patronCorreo = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        Pattern patronTelefono = Pattern.compile("^[0-9]{9}$");
        Matcher matcher = patronCorreo.matcher(recinto.getEmail());
        if (!matcher.matches()) {
            mensajeRegistro.setText("El email no es correcto.");
            mensajeRegistro.setVisible(true);
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(event -> mensajeRegistro.setVisible(false));
            pause.play();
            return false;
        }
        matcher = patronTelefono.matcher(recinto.getTelefono());
        if (!matcher.matches()) {
            mensajeRegistro.setText("El telefono no es correcto.");
            mensajeRegistro.setVisible(true);
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(event -> mensajeRegistro.setVisible(false));
            pause.play();
            return false;
        }
        RecintoDao.introducirRecinto(recinto);
        return true;
    }

    /**
     * Guarda un nuevo grupo en la bbdd con los datos pasados por el usuario
     * @param nombreField Nombre del grupo
     * @param descripcionField Descripción
     * @param generoField Género
     * @param ciudadField Ciudad
     * @param paisField País
     * @param imagen Imagen del grupo
     * @param perfilSpotifyField Enlace al perfil de Spotify
     */
    public void registrarGrupo(TextField nombreField, TextField descripcionField, TextField generoField, TextField ciudadField, TextField paisField,
                               String imagen, TextField perfilSpotifyField){
        Grupo grupo = new Grupo();
        grupo.setNombre(nombreField.getText());
        grupo.setDescripcion(descripcionField.getText());
        grupo.setGenero(generoField.getText());
        grupo.setCiudad(ciudadField.getText());
        grupo.setPais(paisField.getText());
        grupo.setImagen(imagen);
        grupo.setPerfilSpotify(perfilSpotifyField.getText());
        GrupoDao.introducirGrupo(grupo);
    }

    /**
     * Cierra sesión del usuario activo y borra los datos guardados suyos
     */
    public void cerrarSesion(){
        usuarioActivo = null;
        vista.mostrarInicio();
    }

    /**
     * Guarda en la base de datos la cantidad de entradas normales compradas por el usuario
     * @param concierto Concierto de las entradas
     * @param cantidadSeleccionada Cantidad de entradas
     */
    public void comprarEntradasNormales(Concierto concierto, int cantidadSeleccionada){
        for (int i = 0; i < cantidadSeleccionada; i++) {
            EntradaConcierto entrada = new EntradaConcierto();
            entrada.setConcierto(concierto);
            entrada.setPrecio(concierto.getPrecioEntradas());
            entrada.setUsuario(usuarioActivo);
            entrada.setTipo("Normal");
            entrada.setFechaCompra(LocalDateTime.now());
            EntradaDao.introducirEntradaConcierto(entrada);
        }
        actualizarEntradasNormales(concierto, cantidadSeleccionada);
    }

    /**
     * Guarda en la base de datos la cantidad de entradas vip compradas por el usuario
     * @param concierto Concierto de las entradas
     * @param cantidadSeleccionada Cantidad de entradas
     */
    public void comprarEntradasVip(Concierto concierto, int cantidadSeleccionada){
        for (int i = 0; i < cantidadSeleccionada; i++) {
            EntradaConcierto entrada = new EntradaConcierto();
            entrada.setConcierto(concierto);
            entrada.setPrecio(concierto.getPrecioEntradasVip());
            entrada.setUsuario(usuarioActivo);
            entrada.setTipo("Vip");
            entrada.setFechaCompra(LocalDateTime.now());
            EntradaDao.introducirEntradaConcierto(entrada);
        }
        actualizarEntradasVip(concierto, cantidadSeleccionada);
    }

    /**
     * Actualiza la cantidad de entradas normales vendidas en la BBDD
     * @param concierto Concierto de las entradas
     * @param cantidadSeleccionada Cantidad de entradas
     */
    public void actualizarEntradasNormales(Concierto concierto, int cantidadSeleccionada){
        concierto.setCantidadEntradasVendidas(concierto.getCantidadEntradasVendidas() + cantidadSeleccionada);
        ConciertoDao.actualizarCantidadEntradasConciertos(concierto);
    }

    /**
     * Actualiza la cantidad de entradas vip vendidas en la BBDD
     * @param concierto Concierto de las entradas
     * @param cantidadSeleccionada Cantidad de entradas
     */
    public void actualizarEntradasVip(Concierto concierto, int cantidadSeleccionada){
        concierto.setCantidadEntradasVipVendidas(concierto.getCantidadEntradasVipVendidas() + cantidadSeleccionada);
        ConciertoDao.actualizarCantidadEntradasVipConciertos(concierto);
    }


    /**
     * Guarda un conceirto en mis guardados de un usuario
     * @param concierto Concierto para guardar
     * @return Un entero, sis es 0 no se ha guardado si es 1 si
     */
    public int guardarConciertoGuardado(Concierto concierto){
        return GuardadoDao.introducirConciertoGuardado(concierto, usuarioActivo);
    }

    /**
     * Permite guardar la imagen del grupo
     * @param selectedFile El archivo de la imagen
     * @param imagenSeleccionada El label donde se mostrará la imagen seleccionada
     * @return dirección de la imagen
     */
    public String guardarImagenGrupo(File selectedFile, Label imagenSeleccionada){
        if (selectedFile != null) {
            try {
                File destDir = new File("src/main/resources/img/grupos");
                if (!destDir.exists()) {
                    destDir.mkdirs();
                }
                File destFile = new File(destDir, selectedFile.getName());
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                imagenSeleccionada.setText("Imagen seleccionada: " + destFile.getName());
                return "/img/grupos/" + destFile.getName();
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
        return null;
    }

    /**
     * Guarda las entradas en un archivo JSON
     * @param entradas Lista de entradas
     */
    public void exportarEntradas(List<EntradaConcierto> entradas){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar entradas");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Archivos JSON (*.json)", "*.json");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir") + "\\src\\main\\resources\\entradas"));
        File file = fileChooser.showSaveDialog(null);
        if(file != null){
            try (FileWriter fileWriter = new FileWriter(file)) {
                JSONArray jsonArray = new JSONArray();
                for (EntradaConcierto ent : entradas) {
                    JSONObject jsonEntrada = new JSONObject();
                    jsonEntrada.put("id", ent.getId());
                    jsonEntrada.put("precio", ent.getPrecio());
                    jsonEntrada.put("usuario", ent.getUsuario().getNombre() + " " + ent.getUsuario().getApellidos());
                    jsonEntrada.put("tipo", ent.getTipo());
                    jsonEntrada.put("fecha_compra", ent.getFechaCompra());
                    jsonEntrada.put("concierto", ent.getConcierto().getNombre());
                    jsonEntrada.put("recinto", ent.getConcierto().getRecinto().getNombre());
                    jsonEntrada.put("fecha_concierto", ent.getConcierto().getFecha());
                    jsonArray.put(jsonEntrada);
                }
                fileWriter.write(jsonArray.toString(4));
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * Elimina un concierto de la bbdd seleccionado con el id
     * @param id ID del concierto
     */
    public void eliminarConcierto(int id){
        ConciertoDao.eliminarConciertoPorId(id);
    }

    /**
     * Selecciona todas las entradas que tiene un usuario y las agrupa por conciertos
     * @return Map con todoslos grupos de entradas
     */
    public Map<String, List<EntradaConcierto>> obtenerEntradasConcierto(){
        List<EntradaConcierto> entradas = EntradaDao.buscarEntradasPorUsuario(usuarioActivo);
        Map<String, List<EntradaConcierto>> entradasPorConcierto = new HashMap<>();
        for (EntradaConcierto entrada : entradas) {
            entradasPorConcierto
                    .computeIfAbsent(entrada.getConcierto().getNombre(), k -> new ArrayList<>())
                    .add(entrada);
        }
        return entradasPorConcierto;
    }

    /**
     * Obtiene todos los cocneirtos creados por un promotor
     * @return Lista de conciertos
     */
    public List<Concierto> obtenerConciertosPorPromotor(){
        return ConciertoDao.buscarConciertoPorPromotor(usuarioActivo.getId());
    }

    /**
     * Obtiene todas las canciones de un grupo
     * @param grupo Grupo
     * @return Lista de canciones
     */
    public List<Cancion> obtenerCancionesPorGrupo(Grupo grupo){
        return CancionDao.buscarCancionPorGrupo(grupo.getId());
    }

    /**
     * Obtiene todos los conciertos guardados por el usuario activo
     * @return Lista de conciertos
     */
    public List<Concierto> obtenerConciertosGuardadosPorUsuario(){
        return GuardadoDao.buscarGuardadosPorUsuario(usuarioActivo);
    }

    /**
     * Busca conciertos cuyo nombre o ciudad contangan la cadena pasada por parámetro
     * @param filtro El filtro elegido por el usuario
     * @param campo La cadena sobre la que buscar
     */
    public void buscarConciertos(String filtro, String campo){
        conciertos.clear();
        if (filtro.equals("Artista")){
            conciertos.addAll(ConciertoDao.buscarConciertosPorGrupoYFecha(campo));
        }else if (filtro.equals("Ciudad")){
            conciertos.addAll(ConciertoDao.buscarConciertosPorCiudadYFecha(campo));
        }
        vista.mostrarInicio(1);
    }

    /**
     * Obtiene todas las entradas de conciertos anteriores de un usuario y las agrupoa por conciertos
     * @return Map con los conciertos agrupados
     */
    public Map<String, List<Concierto>> obtenerConciertosAnteriores(){
        List<Concierto> conciertos = ConciertoDao.buscarConciertosPorUsuarioYFechaAnterior(usuarioActivo);
        Map<String, List<Concierto>> entradasPorConcierto = new HashMap<>();
        for (Concierto concierto : conciertos) {
            entradasPorConcierto
                    .computeIfAbsent(concierto.getNombre(), k -> new ArrayList<>())
                    .add(concierto);
        }
        return entradasPorConcierto;
    }

    /**
     * Guarda una canción en la bbdd
     * @param grupo Grupo
     * @param titulo Título de la canción
     * @param enlace Enlace a youtube de la canción
     */
    public void registrarCancion(Grupo grupo, String titulo, String enlace){
        Cancion cancion = new Cancion();
        cancion.setTitulo(titulo);
        cancion.setGrupo(grupo);
        cancion.setEnlaceYoutube(enlace);
        CancionDao.introducirCancion(cancion);
    }

    /**
     * Elimina de la bbdd un concierto guardado del usuario
     * @param concierto Concierto
     */
    public void eliminarGuardado(Concierto concierto){
        GuardadoDao.eliminarGuardado(concierto, usuarioActivo);
    }

    /**
     * Guarda en la bbdd una opinion de un usuario sobre un concierto
     * @param concierto Concierto
     * @param comentario Comentario
     * @param recomendado Si recomienda el conceirto o no
     */
    public void registrarComentario(Concierto concierto, String comentario, OpcionesOpinion recomendado){
        Opinion opinion = new Opinion();
        opinion.setComentario(comentario);
        opinion.setUsuario(usuarioActivo);
        opinion.setGrupo(concierto.getGrupo());
        opinion.setConcierto(concierto);
        opinion.setRecomendado(recomendado);
        OpinionDao.introducirOpinion(opinion);
    }

    /**
     * Obtiene todas las opiniones que se han guardado sobre los conciertos de un grupo específico
     * @param grupo Grupo
     * @return Lista de opiniones
     */
    public List<Opinion> obtenerOpinionesPorGrupo(Grupo grupo){
        return OpinionDao.buscarOpinionesPorGrupo(grupo);
    }

    /**
     * Comprueba si el usuario ya ha comentado en ese concierto
     * @param concierto Concierto
     * @return Si ya ha comentado o no
     */
    public boolean comprobarComentarioPorUsuarioYConcierto(Concierto concierto){
        return OpinionDao.buscarOpinionesPorUsuarioYConcierto(usuarioActivo, concierto);
    }


}