package org.tsamper.proyecto_final.vista;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.kordamp.bootstrapfx.BootstrapFX;
import org.kordamp.bootstrapfx.scene.layout.Panel;
import org.tsamper.proyecto_final.controlador.VimuController;
import org.tsamper.proyecto_final.modelo.*;
import org.tsamper.proyecto_final.modelo.constantes.Constantes;
import org.tsamper.proyecto_final.modelo.enums.OpcionesOpinion;
import org.tsamper.proyecto_final.modelo.enums.Privilegios;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class VimuApplication extends Application {

    private Stage stage;
    private Scene mainScene;
    VimuController controller = new VimuController(this);


    @Override
    public void start(Stage stage){
        this.stage = stage;
        controller.conectarBBDD();
        mostrarArraqueApp();
        Image icon = new Image("file:src/main/resources/img/iconos/icon.png");
        stage.getIcons().add(icon);
        stage.setTitle("Vimu - Los mejores conciertos a tu alcance");
        stage.setResizable(false);
        stage.sizeToScene();
        stage.show();
        stage.setOnCloseRequest(new EventHandler<>() {
            @Override
            public void handle(WindowEvent event) {
                controller.desconectar();
            }
        });
    }

    /**
     * Muestra la pantalla de arraque de la aplicación
     */
    public void mostrarArraqueApp(){
        Image imagen = new Image("file:src/main/resources/img/iconos/logo1.png");
        ImageView imageView = new ImageView(imagen);
        imageView.setFitHeight(Constantes.TAMANYO_VENTANA_VERTICAL + 5);
        imageView.setFitWidth(Constantes.TAMANYO_VENTANA_HORIZONTAL);
        imageView.setPreserveRatio(false);
        VBox box = new VBox(imageView);
        Scene scene = new Scene(box, Constantes.TAMANYO_VENTANA_HORIZONTAL, Constantes.TAMANYO_VENTANA_VERTICAL);
        stage.setScene(scene);
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(event -> mostrarInicio());
        pause.play();
    }

    /**
     * Muestra la pantalla de inicio de la aplicación
     */
    public void mostrarInicio(){
        HBox barraBotones = new HBox(10);
        barraBotones.setStyle("-fx-background-color: #c8eeff");
        barraBotones.getStyleClass().add("btn-toolbar");

        Button button1 = new Button("Inicio");
        button1.getStyleClass().addAll("btn", "btn-primary");
        button1.setOnAction(e -> mostrarInicio());
        Button button3;
        if (controller.usuarioActivo != null){
            button3 = new Button("Mi perfil");
        }else{
            button3 = new Button("Iniciar sesión");
        }
        button3.getStyleClass().addAll("btn", "btn-primary");
        button3.setOnAction(e -> mostrarMiPerfil(null));
        barraBotones.getChildren().addAll(button1, button3);
        if(controller.usuarioActivo != null && (controller.usuarioActivo.getGrupoUsuarios().getTipo().equals(Privilegios.EVENTO) ||
                controller.usuarioActivo.getGrupoUsuarios().getTipo().equals(Privilegios.ADMIN))){
            Button button2 = new Button("Añadir evento");
            button2.getStyleClass().addAll("btn", "btn-primary");
            button2.setOnAction(e -> mostrarAgregarConcierto());
            barraBotones.getChildren().add(button2);
        }
        barraBotones.setAlignment(Pos.CENTER);
        barraBotones.setPadding(new Insets(10, 0, 10, 0));

        HBox barraBusqueda = new HBox(10);
        barraBusqueda.setAlignment(Pos.CENTER);
        barraBusqueda.setPadding(new Insets(10, 0, 10, 0));

        ChoiceBox<String> tipoBusqueda = new ChoiceBox<>();
        tipoBusqueda.getItems().addAll("Artista", "Ciudad");
        tipoBusqueda.setValue("Artista");
        tipoBusqueda.getStyleClass().add("combo-box");


        TextField busquedaField = new TextField();
        busquedaField.setPromptText("Buscar conciertos...");
        busquedaField.getStyleClass().addAll("form-control");
        busquedaField.setPrefWidth(400);
        busquedaField.getStyleClass().add("search-field");

        Button busquedaButton = new Button("\uD83D\uDD0D");
        busquedaButton.getStyleClass().addAll("btn", "btn-primary");
        busquedaButton.setOnAction(e -> controller.buscarConciertos(tipoBusqueda.getValue(), busquedaField.getText()));

        Button cancelarButton = new Button("❌");
        cancelarButton.getStyleClass().addAll("btn", "btn-secondary");
        cancelarButton.setOnAction(e -> mostrarInicio());

        barraBusqueda.getChildren().addAll(tipoBusqueda, busquedaField, busquedaButton, cancelarButton);

        GridPane gridPane = new GridPane();
        gridPane.getStyleClass().add("container");
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setPrefWidth(1185);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("container");
        scrollPane.setContent(gridPane);

        controller.obtenerConciertos();
        int cantidad = controller.conciertos.size()/4+1;
        for (int row = 0; row < cantidad; row++) {
            for (int col = 0; col < 4; col++) {
                int index = row * 4 + col;
                if (index < controller.conciertos.size()) {
                    Concierto concierto = controller.conciertos.get(index);
                    Panel panel = new Panel();
                    VBox hBox = new VBox();
                    panel.getStyleClass().add("container");
                    panel.setBody(hBox);
                    hBox.getStyleClass().addAll("panel", "panel-default");
                    hBox.setMinSize(200, 300);
                    hBox.setAlignment(Pos.CENTER);
                    Label conciertoNombre = new Label(concierto.getNombre());
                    conciertoNombre.getStyleClass().addAll("text-primary", "h5");
                    ImageView imageView = new ImageView(new Image("file:src/main/resources/" + concierto.getImagen()));
                    imageView.setFitWidth(200);
                    imageView.setFitHeight(300);
                    imageView.setPreserveRatio(false);

                    hBox.getChildren().addAll(conciertoNombre, imageView);
                    GridPane.setMargin(panel, new Insets(10));
                    hBox.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> mostrarInfoConcierto(concierto));
                    gridPane.add(panel, col, row);
                }
            }
        }

        VBox root = new VBox(barraBotones, barraBusqueda, scrollPane);
        root.getStyleClass().add("container");

        mainScene = new Scene(root, Constantes.TAMANYO_VENTANA_HORIZONTAL, Constantes.TAMANYO_VENTANA_VERTICAL);
        mainScene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        mainScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        stage.setScene(mainScene);
    }

    /**
     * Muestra la pantalla de busqueda de conciertos
     * @param num
     */
    public void mostrarInicio(int num){
        HBox barraBotones = new HBox(10);
        barraBotones.setStyle("-fx-background-color: #c8eeff");
        barraBotones.getStyleClass().add("btn-toolbar");

        Button button1 = new Button("Inicio");
        button1.getStyleClass().addAll("btn", "btn-primary");
        button1.setOnAction(e -> mostrarInicio());
        Button button3;
        if (controller.usuarioActivo != null){
            button3 = new Button("Mi perfil");
        }else{
            button3 = new Button("Iniciar sesión");
        }
        button3.getStyleClass().addAll("btn", "btn-primary");
        button3.setOnAction(e -> mostrarMiPerfil(null));
        barraBotones.getChildren().addAll(button1, button3);
        if(controller.usuarioActivo != null && (controller.usuarioActivo.getGrupoUsuarios().getTipo().equals(Privilegios.EVENTO) ||
                controller.usuarioActivo.getGrupoUsuarios().getTipo().equals(Privilegios.ADMIN))){
            Button button2 = new Button("Añadir evento");
            button2.getStyleClass().addAll("btn", "btn-primary");
            button2.setOnAction(e -> mostrarAgregarConcierto());
            barraBotones.getChildren().add(button2);
        }
        barraBotones.setAlignment(Pos.CENTER);
        barraBotones.setPadding(new Insets(10, 0, 10, 0));

        HBox barraBusqueda = new HBox(10);
        barraBusqueda.setAlignment(Pos.CENTER);
        barraBusqueda.setPadding(new Insets(10, 0, 10, 0));

        ChoiceBox<String> tipoBusqueda = new ChoiceBox<>();
        tipoBusqueda.getItems().addAll("Artista", "Ciudad");
        tipoBusqueda.setValue("Artista");
        tipoBusqueda.getStyleClass().add("combo-box");


        TextField busquedaField = new TextField();
        busquedaField.setPromptText("Buscar conciertos...");
        busquedaField.getStyleClass().addAll("form-control");
        busquedaField.setPrefWidth(400);
        busquedaField.getStyleClass().add("search-field");

        Button busquedaButton = new Button("\uD83D\uDD0D");
        busquedaButton.getStyleClass().addAll("btn", "btn-primary");
        busquedaButton.setOnAction(e -> controller.buscarConciertos(tipoBusqueda.getValue(), busquedaField.getText()));

        Button cancelarButton = new Button("❌");
        cancelarButton.getStyleClass().addAll("btn", "btn-secondary");
        cancelarButton.setOnAction(e -> mostrarInicio());

        barraBusqueda.getChildren().addAll(tipoBusqueda, busquedaField, busquedaButton, cancelarButton);

        GridPane gridPane = new GridPane();
        gridPane.getStyleClass().add("container");
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setPrefWidth(1185);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("container");
        scrollPane.setContent(gridPane);

        int cantidad = controller.conciertos.size()/4+1;
        for (int row = 0; row < cantidad; row++) {
            for (int col = 0; col < 4; col++) {
                int index = row * 4 + col;
                if (index < controller.conciertos.size()) {
                    Concierto concierto = controller.conciertos.get(index);
                    Panel panel = new Panel();
                    VBox hBox = new VBox();
                    panel.getStyleClass().add("container");
                    panel.setBody(hBox);
                    hBox.getStyleClass().addAll("panel", "panel-default");
                    hBox.setMinSize(200, 300);
                    hBox.setAlignment(Pos.CENTER);
                    Label conciertoNombre = new Label(concierto.getNombre());
                    conciertoNombre.getStyleClass().addAll("text-primary", "h5");
                    ImageView imageView = new ImageView(new Image("file:src/main/resources/" + concierto.getImagen()));
                    imageView.setFitWidth(200);
                    imageView.setFitHeight(300);
                    imageView.setPreserveRatio(false);

                    hBox.getChildren().addAll(conciertoNombre, imageView);
                    GridPane.setMargin(panel, new Insets(10));
                    hBox.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> mostrarInfoConcierto(concierto));
                    gridPane.add(panel, col, row);
                }
            }
        }

        VBox root = new VBox(barraBotones, barraBusqueda, scrollPane);
        root.getStyleClass().add("container");

        mainScene = new Scene(root, Constantes.TAMANYO_VENTANA_HORIZONTAL, Constantes.TAMANYO_VENTANA_VERTICAL);
        mainScene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        mainScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        stage.setScene(mainScene);
    }

    /**
     * Muestra la pantalla de info y compra de entradas de cada concierto
     * @param concierto Concierto
     */
    public void mostrarInfoConcierto(Concierto concierto) {
        HBox barraBotones = new HBox(10);
        barraBotones.setStyle("-fx-background-color: #c8eeff");
        barraBotones.getStyleClass().add("btn-toolbar");

        Button button1 = new Button("Inicio");
        button1.getStyleClass().addAll("btn", "btn-primary");
        Button button3;
        if (controller.usuarioActivo != null){
            button3 = new Button("Mi perfil");
        }else{
            button3 = new Button("Iniciar sesión");
        }
        button3.getStyleClass().addAll("btn", "btn-primary");

        button1.setOnAction(e -> stage.setScene(mainScene));
        button3.setOnAction(e -> mostrarMiPerfil(null));

        barraBotones.getChildren().addAll(button1, button3);
        if(controller.usuarioActivo != null && (controller.usuarioActivo.getGrupoUsuarios().getTipo().equals(Privilegios.EVENTO) ||
                controller.usuarioActivo.getGrupoUsuarios().getTipo().equals(Privilegios.ADMIN))){
            Button button2 = new Button("Añadir evento");
            button2.getStyleClass().addAll("btn", "btn-primary");
            button2.setOnAction(e -> mostrarAgregarConcierto());
            barraBotones.getChildren().add(button2);
        }
        barraBotones.setAlignment(Pos.CENTER);
        barraBotones.setPadding(new Insets(10, 0, 10, 0));

        VBox detailBox = new VBox(20);
        detailBox.getStyleClass().add("container");

        Image image = new Image("file:src/main/resources/" + concierto.getImagen());
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(300);
        imageView.setFitHeight(400);
        imageView.setPreserveRatio(true);

        VBox dataBox = new VBox(10);
        dataBox.getStyleClass().addAll("panel", "panel-default");
        dataBox.setPadding(new Insets(10));
        Label grupoLabel;
        grupoLabel = crearLabelConEstilo("Grupo: " + concierto.getGrupo().getNombre(), "h4");
        grupoLabel.setStyle("-fx-text-fill: blue; -fx-underline: true;");
        grupoLabel.setOnMouseClicked(event -> mostrarDatosGrupo(concierto.getGrupo()));
        Label recintoLabel;
        recintoLabel = crearLabelConEstilo("Recinto: " + concierto.getRecinto().getNombre(), "h4");
        recintoLabel.setStyle("-fx-text-fill: blue; -fx-underline: true;");
        recintoLabel.setOnMouseClicked(event -> mostrarDatosRecinto(concierto.getRecinto()));
        Label guardadoLabel = new Label();
        Button guardarButton = new Button("Guardar");
        guardarButton.getStyleClass().addAll("btn", "btn-primary");
        guardarButton.setOnAction(e -> {
            int resultado = controller.guardarConciertoGuardado(concierto);
            if (resultado == 1){
                guardadoLabel.setText("Concierto guardado");
                guardadoLabel.getStyleClass().add("text-primary");
            }else{
                guardadoLabel.setText("Concierto ya guardado");
                guardadoLabel.getStyleClass().add("text-warning");
            }
            guardadoLabel.setVisible(true);
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(event -> guardadoLabel.setVisible(false));
            pause.play();
        });
        Label eliminadoLabel = new Label();
        Button eliminarButton = new Button("Eliminar");
        eliminarButton.getStyleClass().addAll("btn", "btn-danger");
        eliminarButton.setOnAction(e -> {
            controller.eliminarConcierto(concierto.getId());
            eliminadoLabel.setText("Concierto eliminado");
            eliminadoLabel.getStyleClass().add("text-danger");

            eliminadoLabel.setVisible(true);
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(event -> {
                eliminadoLabel.setVisible(false);
                mostrarInicio();
            });
            pause.play();
        });
        dataBox.getChildren().addAll(
                crearLabelConEstilo(concierto.getNombre(), "h3"),
                crearLabelConEstilo("Fecha: " + concierto.getFecha(), "h4"),
                crearLabelConEstilo("Hora: " + concierto.getHora(), "h4"),
                crearLabelConEstilo("Ciudad: " + concierto.getRecinto().getCiudad(), "h4"),
                recintoLabel,
                grupoLabel
        );
        if(controller.usuarioActivo != null && controller.usuarioActivo.getGrupoUsuarios().getTipo().equals(Privilegios.USER)){
            dataBox.getChildren().addAll(guardarButton, guardadoLabel);
        }
        if(controller.usuarioActivo != null && controller.usuarioActivo.getGrupoUsuarios().getTipo().equals(Privilegios.ADMIN)){
            dataBox.getChildren().addAll(eliminarButton, eliminadoLabel);
        }

        HBox contentBox = new HBox(20);
        contentBox.getChildren().addAll(imageView, dataBox);
        contentBox.setAlignment(Pos.CENTER);
        VBox entradasBox = new VBox(10);
        entradasBox.getStyleClass().addAll("panel", "panel-default");
        entradasBox.setPadding(new Insets(10));
        entradasBox.getChildren().addAll(
                crearCampoEntradas("Entradas Normales", concierto),
                crearCampoEntradas("Entradas VIP", concierto)
        );
        Label entradas = new Label("Entradas");
        entradas.getStyleClass().addAll("text-primary");
        Panel entradasPanel = new Panel();
        entradasBox.setPrefSize(200, 330);
        entradasPanel.getStyleClass().addAll("panel", "panel-info");
        entradasPanel.setHeading(entradas);
        entradasPanel.setBody(entradasBox);
        entradasBox.setAlignment(Pos.CENTER);
        detailBox.getChildren().addAll(barraBotones, contentBox, entradasPanel);
        Scene detailScene = new Scene(detailBox, Constantes.TAMANYO_VENTANA_HORIZONTAL, Constantes.TAMANYO_VENTANA_VERTICAL);
        detailScene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        stage.setScene(detailScene);
    }

    /**
     * Muestra la pantalla con los datos del grupo
     * @param grupo Grupo
     */
    private void mostrarDatosGrupo(Grupo grupo) {
        HBox buttonBar = new HBox(10);
        buttonBar.setStyle("-fx-background-color: #c8eeff");
        buttonBar.getStyleClass().add("btn-toolbar");

        Button button1 = new Button("Inicio");
        button1.getStyleClass().addAll("btn", "btn-primary");
        button1.setOnAction(e -> mostrarInicio());
        Button button3;
        if (controller.usuarioActivo != null){
            button3 = new Button("Mi perfil");
        }else{
            button3 = new Button("Iniciar sesión");
        }
        button3.getStyleClass().addAll("btn", "btn-primary");
        button3.setOnAction(e -> mostrarMiPerfil(null));
        buttonBar.getChildren().addAll(button1, button3);
        if(controller.usuarioActivo != null && (controller.usuarioActivo.getGrupoUsuarios().getTipo().equals(Privilegios.EVENTO) ||
                controller.usuarioActivo.getGrupoUsuarios().getTipo().equals(Privilegios.ADMIN))){
            Button button2 = new Button("Añadir evento");
            button2.getStyleClass().addAll("btn", "btn-primary");
            button2.setOnAction(e -> mostrarAgregarConcierto());
            buttonBar.getChildren().add(button2);
        }
        buttonBar.setAlignment(Pos.CENTER);
        buttonBar.setPadding(new Insets(10, 0, 10, 0));
        VBox detailBox = new VBox(20);
        HBox datosImagen = new HBox();
        Label datosLabel = new Label("Datos");
        Label nombreLabel = new Label("Nombre: " + grupo.getNombre());
        Label descripcionLabel = new Label("Descripción: " + grupo.getDescripcion());
        descripcionLabel.setWrapText(true);
        Label generoLabel = new Label("Género: " + grupo.getGenero());
        Label ciudadLabel = new Label("Ciudad: " + grupo.getCiudad());
        Label paisLabel = new Label("Pais: " + grupo.getPais());
        Label cancionesLabel = new Label("Sus éxitos");
        Hyperlink linkSpotify = new Hyperlink("Perfil Spotify");
        Image image = new Image("file:src/main/resources/" + grupo.getImagen());
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(300);
        imageView.setFitHeight(400);
        imageView.setPreserveRatio(true);
        if(!grupo.getPerfilSpotify().isEmpty()) {
            linkSpotify.setOnAction(e -> {
                try {
                    Desktop.getDesktop().browse(new URI(grupo.getPerfilSpotify()));
                } catch (IOException | URISyntaxException ex) {
                    System.out.println(ex.getMessage());
                }
            });
        }
        List<Cancion> canciones = controller.obtenerCancionesPorGrupo(grupo);
        ObservableList<WebView> webViews = FXCollections.observableArrayList();
        for (Cancion cancion : canciones) {
            WebView webView = new WebView();
            webView.setPrefSize(400, 300);
            webView.getEngine().load(cancion.getEnlaceYoutube());
            webViews.add(webView);
        }

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20));
        gridPane.setHgap(20);
        gridPane.setVgap(20);
        gridPane.setAlignment(Pos.CENTER);

        int columns = 3;
        int row = 0;
        int col = 0;

        for (WebView webView : webViews) {
            gridPane.add(webView, col, row);
            col++;
            if (col == columns) {
                col = 0;
                row++;
            }
        }

        datosLabel.getStyleClass().addAll("h4", "text-primary");
        cancionesLabel.getStyleClass().addAll("h4", "text-primary");
        detailBox.getChildren().addAll(datosLabel, nombreLabel, descripcionLabel, generoLabel, ciudadLabel, paisLabel, linkSpotify);
        VBox textoYImagen = new VBox(20);
        textoYImagen.getChildren().addAll(detailBox);
        textoYImagen.setAlignment(Pos.TOP_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        datosImagen.getChildren().addAll(textoYImagen, spacer, imageView);
        datosImagen.setAlignment(Pos.TOP_LEFT);

        VBox pagina = new VBox(10);
        VBox card = new VBox(20);
        card.setPadding(new Insets(20, 20, 20, 20));
        card.getStyleClass().addAll("card", "p-3");
        card.getChildren().addAll(datosImagen);
        Button anyadirCancionButton = new Button("Añadir canción");
        anyadirCancionButton.getStyleClass().addAll("btn", "btn-primary");
        anyadirCancionButton.setOnAction(e -> mostrarAnyadirCancion(grupo));
        VBox videosBox = new VBox(20);
        if (controller.usuarioActivo != null && controller.usuarioActivo.getGrupoUsuarios().getTipo().equals(Privilegios.ADMIN)){
            videosBox.getChildren().addAll(cancionesLabel, gridPane, anyadirCancionButton);
        }else{
            videosBox.getChildren().addAll(cancionesLabel, gridPane);
        }
        videosBox.setPadding(new Insets(0, 20, 20, 20));

        VBox comentariosBox = new VBox(20);
        Label comentariosLabel = new Label("Comentarios");
        comentariosBox.setPadding(new Insets(0, 20, 20, 20));
        comentariosBox.getChildren().addAll(comentariosLabel);
        comentariosLabel.getStyleClass().addAll("h4", "text-primary");
        List<Opinion> opiniones = controller.obtenerOpinionesPorGrupo(grupo);
        if (opiniones != null){
            for (Opinion opinion : opiniones) {
                VBox conciertoCard = new VBox(10);
                conciertoCard.getStyleClass().addAll("card", "p-3");
                conciertoCard.setPadding(new Insets(20, 20, 20, 20));

                HBox conciertoHeader = new HBox(10);
                Image image2 = new Image("file:src/main/resources/" + opinion.getConcierto().getImagen());
                ImageView imageView2 = new ImageView(image2);
                imageView2.setFitWidth(100);
                imageView2.setPreserveRatio(true);

                Label conciertoLabel = new Label(opinion.getConcierto().getNombre());
                conciertoLabel.getStyleClass().addAll("h4", "text-primary");

                Label comentario = new Label(opinion.getComentario());
                VBox datosConcierto = new VBox(10);
                Label recomendado = new Label(opinion.getRecomendado().name());
                if (opinion.getRecomendado().equals(OpcionesOpinion.RECOMENDADO)){
                    recomendado.getStyleClass().addAll("h4", "text-success");
                }else{
                    recomendado.getStyleClass().addAll("h4", "text-danger");
                    recomendado.setText("NO RECOMENDADO");
                }
                Label fecha = new Label(opinion.getConcierto().getFecha().toString());
                Label usuario = new Label("Usuario: " +opinion.getUsuario().getNomUsuario());

                datosConcierto.getChildren().addAll(conciertoLabel, comentario, recomendado, fecha, usuario);
                conciertoHeader.getChildren().addAll(imageView2, datosConcierto);
                conciertoCard.getChildren().addAll(conciertoHeader);
                conciertoCard.setPrefWidth(600);
                comentariosBox.getChildren().addAll(conciertoCard);
            }
        }

        StackPane root = new StackPane();
        pagina.getChildren().addAll(buttonBar, card, videosBox, comentariosBox);
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("container");
        scrollPane.setFitToWidth(true);
        scrollPane.setContent(pagina);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        root.getChildren().add(scrollPane);
        root.setAlignment(Pos.CENTER);

        Scene registerScene = new Scene(root, Constantes.TAMANYO_VENTANA_HORIZONTAL, Constantes.TAMANYO_VENTANA_VERTICAL);
        registerScene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        stage.setScene(registerScene);
    }

    /**
     * Muestra los datos del recinto
     * @param recinto Recinto
     */
    private void mostrarDatosRecinto(Recinto recinto) {
        HBox buttonBar = new HBox(10);
        buttonBar.setStyle("-fx-background-color: #c8eeff");
        buttonBar.getStyleClass().add("btn-toolbar");

        Button button1 = new Button("Inicio");
        button1.getStyleClass().addAll("btn", "btn-primary");
        button1.setOnAction(e -> mostrarInicio());
        Button button3;
        if (controller.usuarioActivo != null){
            button3 = new Button("Mi perfil");
        }else{
            button3 = new Button("Iniciar sesión");
        }
        button3.getStyleClass().addAll("btn", "btn-primary");
        button3.setOnAction(e -> mostrarMiPerfil(null));
        buttonBar.getChildren().addAll(button1, button3);
        if(controller.usuarioActivo != null && (controller.usuarioActivo.getGrupoUsuarios().getTipo().equals(Privilegios.EVENTO) ||
                controller.usuarioActivo.getGrupoUsuarios().getTipo().equals(Privilegios.ADMIN))){
            Button button2 = new Button("Añadir evento");
            button2.getStyleClass().addAll("btn", "btn-primary");
            button2.setOnAction(e -> mostrarAgregarConcierto());
            buttonBar.getChildren().add(button2);
        }
        buttonBar.setAlignment(Pos.CENTER);
        buttonBar.setPadding(new Insets(10, 0, 10, 0));
        VBox detailBox = new VBox(20);

        Label datosLabel = new Label("Datos");
        Label nombreLabel = new Label("Nombre: " + recinto.getNombre());
        Label direccionLabel = new Label("Dirección: " + recinto.getDireccion());
        Label ciudadLabel = new Label("Ciudad: " + recinto.getCiudad());
        Label telefonoLabel = new Label("Teléfono: " + recinto.getTelefono());
        Label emailLabel = new Label("Email: " + recinto.getEmail());
        Hyperlink enlaceMaps = new Hyperlink("Ver en Google Maps");
        if(recinto.getEnlaceMaps() != null) {
            enlaceMaps.setOnAction(e -> {
                try {
                    System.out.println("asd");
                    Desktop.getDesktop().browse(new URI(recinto.getEnlaceMaps()));
                } catch (IOException | URISyntaxException ex) {
                    System.out.println(ex.getMessage());
                }
            });
        }

        datosLabel.getStyleClass().addAll("h4", "text-primary");
        detailBox.getChildren().addAll(datosLabel, nombreLabel, direccionLabel, ciudadLabel, telefonoLabel, emailLabel, enlaceMaps);

        VBox pagina = new VBox(10);
        VBox card = new VBox(20);
        card.setPadding(new Insets(20, 20, 20, 20));
        card.getStyleClass().addAll("card", "p-3");


        detailBox.setPadding(new Insets(20, 20, 20, 20));
        StackPane root = new StackPane();
        pagina.getChildren().addAll(buttonBar, detailBox);
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("container");
        scrollPane.setFitToWidth(true);
        scrollPane.setContent(pagina);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        root.getChildren().add(scrollPane);
        root.setAlignment(Pos.CENTER);

        Scene registerScene = new Scene(root, Constantes.TAMANYO_VENTANA_HORIZONTAL, Constantes.TAMANYO_VENTANA_VERTICAL);
        registerScene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        stage.setScene(registerScene);
    }

    /**
     * Crea un label y le añade un estilo pasado por parámetro
     * @param text Texto del label
     * @param estilo Estilo
     * @return Label con estilo
     */
    private Label crearLabelConEstilo(String text, String estilo) {
        Label label = new Label(text);
        label.getStyleClass().add(estilo);
        return label;
    }

    /**
     * Crea el apartado de compra de entradas
     * @param tipoEntrada Tipo de entrada
     * @param concierto Concierto
     * @return HBox con los elementos creados
     */
    private HBox crearCampoEntradas(String tipoEntrada, Concierto concierto) {
        HBox entryRow = new HBox(20);
        entryRow.setAlignment(Pos.CENTER_LEFT);
        entryRow.getStyleClass().add("panel-body");
        Label tipoLabel = crearLabelConEstilo(tipoEntrada + ": ", "h5");
        Label cantidadLabel;
        Label precioLabel;
        if (tipoEntrada.equals("Entradas Normales")){
            cantidadLabel = crearLabelConEstilo("Disponibles: " + (concierto.getCantidadEntradas()-concierto.getCantidadEntradasVendidas()), "h5");
            precioLabel = crearLabelConEstilo("Precio: " + concierto.getPrecioEntradas() + "€", "h5");
        }else{
            cantidadLabel = crearLabelConEstilo("Disponibles: " + (concierto.getCantidadEntradasVip()-concierto.getCantidadEntradasVipVendidas()), "h5");
            precioLabel = crearLabelConEstilo("Precio: " + concierto.getPrecioEntradasVip() + "€", "h5");
        }
        ComboBox<Integer> cantidadComboBox = new ComboBox<>();
        for (int i = 1; i <= 10; i++) {
            cantidadComboBox.getItems().add(i);
        }
        cantidadComboBox.setValue(1);

        Button comprarButton = new Button("Comprar");
        if(controller.usuarioActivo !=null && (controller.usuarioActivo.getGrupoUsuarios().getTipo().name().equals("EVENTO")
                || controller.usuarioActivo.getGrupoUsuarios().getTipo().name().equals("ADMIN"))){
            comprarButton.setDisable(true);
        }
        comprarButton.getStyleClass().addAll("btn", "btn-success");
        if (controller.usuarioActivo == null){
            comprarButton.setOnAction(e -> mostrarMiPerfil(concierto));
        }else{
            comprarButton.setOnAction(e -> {
                int cantidadSeleccionada = cantidadComboBox.getValue();
                double precioTotal;
                if (tipoEntrada.equals("Entradas Normales")) {
                    int nuevasEntradasVendidas = concierto.getCantidadEntradasVendidas() + cantidadSeleccionada;
                    if (nuevasEntradasVendidas <= concierto.getCantidadEntradas()) {
                        precioTotal = cantidadSeleccionada * concierto.getPrecioEntradas();
                        mostrarVentanaConfirmacion(concierto, tipoEntrada, cantidadSeleccionada, precioTotal);
                    } else {
                        mostrarError("No hay suficientes entradas normales disponibles.");
                    }
                } else {
                    int nuevasEntradasVipVendidas = concierto.getCantidadEntradasVipVendidas() + cantidadSeleccionada;
                    if (nuevasEntradasVipVendidas <= concierto.getCantidadEntradasVip()) {
                        precioTotal = cantidadSeleccionada * concierto.getPrecioEntradasVip();
                        mostrarVentanaConfirmacion(concierto, tipoEntrada, cantidadSeleccionada, precioTotal);
                    } else {
                        mostrarError("No hay suficientes entradas VIP disponibles.");
                    }
                }
            });
            }

        entryRow.getChildren().addAll(tipoLabel, cantidadLabel, precioLabel, cantidadComboBox, comprarButton);
        return entryRow;
    }

    /**
     * Muestra la pantalla de añadir una canción a un grupo
     * @param grupo Grupo
     */
    private void mostrarAnyadirCancion(Grupo grupo){
        Stage confirmStage = new Stage();
        confirmStage.setTitle("Añadir canción");

        VBox vbox = new VBox(20);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));

        Label textoTitulo = new Label("Título");
        TextField tituloField = new TextField();
        tituloField.getStyleClass().add("search-field");

        Label texto = new Label("Enlace Youtube embebido ej (https://www.youtube.com/embed/ockzzfKbFOE)");
        TextField textField = new TextField();
        textField.getStyleClass().add("search-field");

        Button confirmarButton = new Button("Añadir");
        confirmarButton.getStyleClass().addAll("btn", "btn-success");
        confirmarButton.setOnAction(e -> controller.registrarCancion(grupo, tituloField.getText(), textField.getText()));
        Button cancelarButton = new Button("Cancelar");
        cancelarButton.getStyleClass().addAll("btn", "btn-danger");
        cancelarButton.setOnAction(e -> confirmStage.close());

        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(confirmarButton, cancelarButton);

        vbox.getChildren().addAll(textoTitulo, tituloField, texto, textField, buttonBox);

        Scene scene = new Scene(vbox, 500, 250);
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        confirmStage.setScene(scene);
        confirmStage.show();
    }

    /**
     * Muestra la ventana de confirmación de compra de entradas
     * @param concierto Concierto
     * @param tipoEntrada Tipo de entrada
     * @param cantidadSeleccionada Cantidad de entradas
     * @param precioTotal Precio total de la compra
     */
    private void mostrarVentanaConfirmacion(Concierto concierto, String tipoEntrada, int cantidadSeleccionada, double precioTotal) {
        Stage confirmStage = new Stage();
        confirmStage.setTitle("Confirmar Compra");

        VBox vbox = new VBox(20);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));

        Label confirmLabel = new Label("Precio Total: " + precioTotal + "€");
        confirmLabel.getStyleClass().add("h5");

        Button confirmarButton = new Button("Confirmar");
        confirmarButton.getStyleClass().addAll("btn", "btn-success");
        confirmarButton.setOnAction(e -> {
            if (tipoEntrada.equals("Entradas Normales")) {
                controller.comprarEntradasNormales(concierto, cantidadSeleccionada);
            } else {
                controller.comprarEntradasVip(concierto, cantidadSeleccionada);
            }
            confirmStage.close();
            mostrarInfoConcierto(concierto);
        });
        Button cancelarButton = new Button("Cancelar");
        cancelarButton.getStyleClass().addAll("btn", "btn-danger");
        cancelarButton.setOnAction(e -> confirmStage.close());

        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(confirmarButton, cancelarButton);

        vbox.getChildren().addAll(confirmLabel, buttonBox);

        Scene scene = new Scene(vbox, 300, 200);
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        confirmStage.setScene(scene);
        confirmStage.show();
    }

    /**
     * Muestra una alerta de error
     * @param mensaje Mensaje de error
     */
    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Muestra una pantalla que cambia sus elementos en función de si está o no logueado el usuario
     * @param concierto Concierto
     */
    public void mostrarMiPerfil(Concierto concierto) {
        HBox barraBotones = new HBox(10);
        barraBotones.setStyle("-fx-background-color: #c8eeff");
        barraBotones.getStyleClass().add("btn-toolbar");

        Button button1 = new Button("Inicio");
        button1.getStyleClass().addAll("btn", "btn-primary");
        Button button3;
        if (controller.usuarioActivo != null){
            button3 = new Button("Mi perfil");
        }else{
            button3 = new Button("Iniciar sesión");
        }
        button3.getStyleClass().addAll("btn", "btn-primary");
        button3.setOnAction(e -> mostrarMiPerfil(null));
        button1.setOnAction(e -> mostrarInicio());
        barraBotones.getChildren().addAll(button1, button3);
        if(controller.usuarioActivo != null && (controller.usuarioActivo.getGrupoUsuarios().getTipo().equals(Privilegios.EVENTO) ||
                controller.usuarioActivo.getGrupoUsuarios().getTipo().equals(Privilegios.ADMIN))){
            Button button2 = new Button("Añadir evento");
            button2.getStyleClass().addAll("btn", "btn-primary");
            button2.setOnAction(e -> mostrarAgregarConcierto());
            barraBotones.getChildren().add(button2);
        }
        barraBotones.setAlignment(Pos.CENTER);
        barraBotones.setPadding(new Insets(10, 0, 10, 0));

        if (controller.usuarioActivo != null){
            mostrarPerfilUsuario();
        }else{
           mostrarLogin(concierto);
        }
    }

    /**
     * Si en la pantalla anterior no hay usuario activo, se muestra la ventana de iniciar sesión
     * @param concierto Concierto
     */
    public void mostrarLogin(Concierto concierto){
        HBox barraBotones = new HBox(10);
        barraBotones.setStyle("-fx-background-color: #c8eeff");
        barraBotones.getStyleClass().add("btn-toolbar");

        Button button1 = new Button("Inicio");
        button1.getStyleClass().addAll("btn", "btn-primary");
        button1.setOnAction(e -> mostrarInicio());
        Button button3;
        if (controller.usuarioActivo != null){
            button3 = new Button("Mi perfil");
        }else{
            button3 = new Button("Iniciar sesión");
        }
        button3.getStyleClass().addAll("btn", "btn-primary");
        button3.setOnAction(e -> mostrarMiPerfil(null));
        barraBotones.getChildren().addAll(button1, button3);
        if(controller.usuarioActivo != null && (controller.usuarioActivo.getGrupoUsuarios().getTipo().equals(Privilegios.EVENTO) ||
                controller.usuarioActivo.getGrupoUsuarios().getTipo().equals(Privilegios.ADMIN))){
            Button button2 = new Button("Añadir evento");
            button2.getStyleClass().addAll("btn", "btn-primary");
            button2.setOnAction(e -> mostrarAgregarConcierto());
            barraBotones.getChildren().add(button2);
        }
        barraBotones.setAlignment(Pos.CENTER);
        barraBotones.setPadding(new Insets(10, 0, 10, 0));
        VBox detailBox = new VBox(20);
        VBox loginBox = new VBox(10);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setPadding(new Insets(20, 0, 20, 0));

        Label loginLabel = new Label("Iniciar sesión");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Nombre de usuario");
        usernameField.setMaxWidth(200);
        usernameField.setPrefWidth(200);
        usernameField.getStyleClass().add("search-field");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Contraseña");
        passwordField.setMaxWidth(200);
        passwordField.setPrefWidth(200);
        passwordField.getStyleClass().add("search-field");

        Label mensajeLogin = new Label("");
        mensajeLogin.setVisible(false);
        Button loginButton = new Button("Iniciar sesión");
        loginButton.getStyleClass().addAll("btn", "btn-primary");
        loginButton.setOnAction(e -> controller.iniciarSesion(usernameField.getText(), passwordField.getText(), mensajeLogin, concierto));
        passwordField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                controller.iniciarSesion(usernameField.getText(), passwordField.getText(), mensajeLogin, concierto);
            }
        });
        Button registerButton = new Button("Registrarse");
        registerButton.getStyleClass().addAll("btn", "btn-primary");
        registerButton.setOnAction(e -> mostrarRegistro());

        HBox loginRegistro = new HBox(10);
        loginRegistro.setAlignment(Pos.CENTER);
        loginRegistro.getChildren().addAll(loginButton, registerButton);
        loginBox.getChildren().addAll(loginLabel, usernameField, passwordField, loginRegistro, mensajeLogin);

        detailBox.getChildren().addAll(barraBotones, loginBox);

        Scene detailScene = new Scene(detailBox, Constantes.TAMANYO_VENTANA_HORIZONTAL, Constantes.TAMANYO_VENTANA_VERTICAL);
        detailScene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        detailScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        stage.setScene(detailScene);
    }

    /**
     * Muestra la pantalla de registro de usuario
     */
    public void mostrarRegistro() {
        HBox barraBotones = new HBox(10);
        barraBotones.setStyle("-fx-background-color: #c8eeff");
        barraBotones.getStyleClass().add("btn-toolbar");

        Button button1 = new Button("Inicio");
        button1.getStyleClass().addAll("btn", "btn-primary");
        button1.setOnAction(e -> mostrarInicio());
        Button button3;
        if (controller.usuarioActivo != null){
            button3 = new Button("Mi perfil");
        }else{
            button3 = new Button("Iniciar sesión");
        }
        button3.getStyleClass().addAll("btn", "btn-primary");
        button3.setOnAction(e -> mostrarMiPerfil(null));
        barraBotones.getChildren().addAll(button1, button3);
        if(controller.usuarioActivo != null && (controller.usuarioActivo.getGrupoUsuarios().getTipo().equals(Privilegios.EVENTO) ||
                controller.usuarioActivo.getGrupoUsuarios().getTipo().equals(Privilegios.ADMIN))){
            Button button2 = new Button("Añadir evento");
            button2.getStyleClass().addAll("btn", "btn-primary");
            button2.setOnAction(e -> mostrarAgregarConcierto());
            barraBotones.getChildren().add(button2);
        }
        barraBotones.setAlignment(Pos.CENTER);
        barraBotones.setPadding(new Insets(10, 0, 10, 0));
        VBox detailBox = new VBox(20);
        VBox registerBox = new VBox(10);
        registerBox.setAlignment(Pos.CENTER);
        registerBox.setPadding(new Insets(20, 0, 20, 0));

        Label registerLabel = new Label("Registrarse (* campos obligatorios)");
        TextField usernameField = new TextField();
        configurarTextLabel(usernameField, "*Nombre de usuario*");

        PasswordField contrasenyaField = new PasswordField();
        configurarTextLabel(contrasenyaField, "*Contraseña*");

        PasswordField confirmarContrasenyaField = new PasswordField();
        configurarTextLabel(confirmarContrasenyaField, "*Confirmar Contraseña*");

        TextField nombreField = new TextField();
        configurarTextLabel(nombreField, "Nombre");

        TextField apellidoField = new TextField();
        configurarTextLabel(apellidoField, "Apellido");

        TextField emailField = new TextField();
        configurarTextLabel(emailField, "*Email*");

        ComboBox<Privilegios> privilegiosComboBox = new ComboBox<>();
        privilegiosComboBox.getItems().addAll(Privilegios.EVENTO, Privilegios.USER);
        privilegiosComboBox.setPromptText("*Seleccione un tipo de usuario*");
        privilegiosComboBox.setMaxWidth(200);
        privilegiosComboBox.setPrefWidth(200);

        Label mensajeRegistro = new Label("");
        mensajeRegistro.setVisible(false);

        Button registerButton = new Button("Registrarse");
        registerButton.getStyleClass().addAll("btn", "btn-primary");
        registerButton.setOnAction(e ->
            controller.registrarUsuario(usernameField, contrasenyaField, confirmarContrasenyaField, nombreField, apellidoField, emailField, privilegiosComboBox, mensajeRegistro)
        );

        Button cancelButton = new Button("Cancelar");
        cancelButton.getStyleClass().addAll("btn", "btn-secondary");
        cancelButton.setOnAction(e -> mostrarLogin(null));

        HBox registerActions = new HBox(10);
        registerActions.setAlignment(Pos.CENTER);
        registerActions.getChildren().addAll(registerButton, cancelButton);
        registerBox.getChildren().addAll(registerLabel, usernameField, contrasenyaField, confirmarContrasenyaField, nombreField, apellidoField, emailField, privilegiosComboBox, registerActions, mensajeRegistro);

        detailBox.getChildren().addAll(barraBotones, registerBox);

        Scene registerScene = new Scene(detailBox, Constantes.TAMANYO_VENTANA_HORIZONTAL, Constantes.TAMANYO_VENTANA_VERTICAL);
        registerScene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        registerScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        stage.setScene(registerScene);
    }

    /**
     * Muestra la pantalla de perfil de usuario si hay un usuario activo
     */
    public void mostrarPerfilUsuario(){
        HBox barraBotones = new HBox(10);
        barraBotones.setStyle("-fx-background-color: #c8eeff");
        barraBotones.getStyleClass().add("btn-toolbar");

        Button button1 = new Button("Inicio");
        button1.getStyleClass().addAll("btn", "btn-primary");
        button1.setOnAction(e -> mostrarInicio());
        Button button3;
        if (controller.usuarioActivo != null){
            button3 = new Button("Mi perfil");
        }else{
            button3 = new Button("Iniciar sesión");
        }
        button3.getStyleClass().addAll("btn", "btn-primary");
        button3.setOnAction(e -> mostrarMiPerfil(null));
        barraBotones.getChildren().addAll(button1, button3);
        if(controller.usuarioActivo != null && (controller.usuarioActivo.getGrupoUsuarios().getTipo().equals(Privilegios.EVENTO) ||
                controller.usuarioActivo.getGrupoUsuarios().getTipo().equals(Privilegios.ADMIN))){
            Button button2 = new Button("Añadir evento");
            button2.getStyleClass().addAll("btn", "btn-primary");
            button2.setOnAction(e -> mostrarAgregarConcierto());
            barraBotones.getChildren().add(button2);
        }
        barraBotones.setAlignment(Pos.CENTER);
        barraBotones.setPadding(new Insets(10, 0, 10, 0));
        VBox detailBox = new VBox(20);

        Label nombreLabel = new Label("Nombre: " + controller.usuarioActivo.getNombre());
        Label apellidosLabel = new Label("Apellidos: " + controller.usuarioActivo.getApellidos());
        Label emailLabel = new Label("Email: " + controller.usuarioActivo.getEmail());

        nombreLabel.getStyleClass().addAll("h4", "text-primary");
        apellidosLabel.getStyleClass().addAll("h4", "text-primary");
        emailLabel.getStyleClass().addAll("h4", "text-primary");

        detailBox.getChildren().addAll(nombreLabel, apellidosLabel, emailLabel);

        Button cerrarSesionButton = new Button("Cerrar sesión");
        cerrarSesionButton.getStyleClass().addAll("btn", "btn-warning");
        cerrarSesionButton.setOnAction(event -> controller.cerrarSesion());

        detailBox.getChildren().add(cerrarSesionButton);
        VBox pagina = new VBox(10);
        VBox card = new VBox(20);
        card.setPadding(new Insets(20, 20, 20, 20));
        card.getStyleClass().addAll("card", "p-3");
        card.getChildren().addAll(detailBox);
        if (controller.usuarioActivo.getGrupoUsuarios().getTipo().name().equals("USER")){
            Label tituloEntradas = new Label("Mis entradas");
            tituloEntradas.getStyleClass().addAll("h4", "text-primary");
            Map<String, List<EntradaConcierto>> entradasPorConcierto = controller.obtenerEntradasConcierto();
            HBox entradasYGuardados = new HBox(10);
            VBox entradasBox = new VBox(10);
            entradasBox.setPadding(new Insets(20, 20, 20, 20));
            entradasBox.setAlignment(Pos.TOP_LEFT);
            entradasBox.getChildren().add(tituloEntradas);
            for (Map.Entry<String, List<EntradaConcierto>> entry : entradasPorConcierto.entrySet()) {
                String concierto = entry.getKey();
                List<EntradaConcierto> entradasConcierto = entry.getValue();

                VBox conciertoCard = new VBox(10);
                conciertoCard.getStyleClass().addAll("card", "p-3");
                conciertoCard.setPadding(new Insets(0, 20, 20, 20));

                HBox conciertoHeader = new HBox(10);
                Image image = new Image("file:src/main/resources/" + entradasConcierto.get(0).getConcierto().getImagen());
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(100);
                imageView.setPreserveRatio(true);

                Label conciertoLabel = new Label(concierto);
                conciertoLabel.getStyleClass().addAll("h4", "text-primary");
                Label cantidadLabel = new Label("Cantidad de entradas: " + entradasConcierto.size());
                cantidadLabel.getStyleClass().add("h5");
                Label fechaLabel = new Label(entradasConcierto.get(0).getConcierto().getFecha().toString());
                VBox datosConcierto = new VBox(10);

                Button exportarButton = new Button("Exportar entradas");
                exportarButton.getStyleClass().addAll("btn", "btn-primary");
                exportarButton.setOnAction(e -> controller.exportarEntradas(entradasConcierto));

                datosConcierto.getChildren().addAll(conciertoLabel, cantidadLabel, fechaLabel, exportarButton);
                conciertoHeader.getChildren().addAll(imageView, datosConcierto);
                conciertoCard.getChildren().addAll(conciertoHeader);
                entradasBox.getChildren().addAll(conciertoCard);
            }

            VBox guardadosBox = new VBox(10);
            Label tituloGuardados = new Label("Mis elementos guardados");
            guardadosBox.setPadding(new Insets(20, 20, 20, 20));
            guardadosBox.getChildren().add(tituloGuardados);
            tituloGuardados.getStyleClass().addAll("h4", "text-primary");
            List<Concierto> conciertos = controller.obtenerConciertosGuardadosPorUsuario();
            for (Concierto concierto : conciertos) {
                VBox conciertoCard = new VBox(10);
                conciertoCard.getStyleClass().addAll("card", "p-3");
                conciertoCard.setPadding(new Insets(0, 20, 20, 20));

                HBox conciertoHeader = new HBox(10);
                Image image = new Image("file:src/main/resources/" + concierto.getImagen());
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(100);
                imageView.setPreserveRatio(true);

                Label conciertoLabel = new Label(concierto.getNombre());
                conciertoLabel.getStyleClass().addAll("h4", "text-primary");

                Label fechaLabel = new Label(concierto.getFecha().toString());

                VBox datosConcierto = new VBox(10);

                Button irButton = new Button("Ir");
                irButton.getStyleClass().addAll("btn", "btn-primary");
                irButton.setOnAction(e -> mostrarInfoConcierto(concierto));

                Button eliminarButton = new Button("Dejar de seguir");
                eliminarButton.getStyleClass().addAll("btn", "btn-danger");
                eliminarButton.setOnAction(e -> {
                    controller.eliminarGuardado(concierto);
                    mostrarPerfilUsuario();
                });

                datosConcierto.getChildren().addAll(conciertoLabel, fechaLabel, irButton, eliminarButton);
                conciertoHeader.getChildren().addAll(imageView, datosConcierto);
                conciertoCard.getChildren().addAll(conciertoHeader);
                guardadosBox.getChildren().addAll(conciertoCard);
            }
            VBox comentariosBox = new VBox(10);
            Label tituloComentarios = new Label("Deja un comentario");
            comentariosBox.setPadding(new Insets(20, 20, 20, 20));
            comentariosBox.getChildren().add(tituloComentarios);
            tituloComentarios.getStyleClass().addAll("h4", "text-primary");
            Map<String, List<Concierto>> conciertosComentarios = controller.obtenerConciertosAnteriores();
            for (Map.Entry<String, List<Concierto>> entry : conciertosComentarios.entrySet()) {
                String concierto = entry.getKey();
                List<Concierto> entradasConcierto = entry.getValue();
                VBox conciertoCard = new VBox(10);
                conciertoCard.getStyleClass().addAll("card", "p-3");
                conciertoCard.setPadding(new Insets(0, 20, 20, 20));

                HBox conciertoHeader = new HBox(10);
                Image image = new Image("file:src/main/resources/" + entradasConcierto.get(0).getImagen());
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(100);
                imageView.setPreserveRatio(true);

                Label conciertoLabel = new Label(concierto);
                conciertoLabel.getStyleClass().addAll("h4", "text-primary");

                Label fechaLabel = new Label(entradasConcierto.get(0).getFecha().toString());

                VBox datosConcierto = new VBox(10);

                Button comentarButton = new Button("Comentar");
                comentarButton.getStyleClass().addAll("btn", "btn-primary");
                comentarButton.setOnAction(e -> mostrarAnyadirComentario(entry.getValue().get(0)));
                if (controller.comprobarComentarioPorUsuarioYConcierto(entry.getValue().get(0))){
                    comentarButton.setDisable(true);
                }
                datosConcierto.getChildren().addAll(conciertoLabel, fechaLabel, comentarButton);
                conciertoHeader.getChildren().addAll(imageView, datosConcierto);
                conciertoCard.getChildren().addAll(conciertoHeader);
                comentariosBox.getChildren().addAll(conciertoCard);
            }
            entradasYGuardados.getChildren().addAll(entradasBox, guardadosBox);
            card.getChildren().addAll(entradasYGuardados, comentariosBox);


        }else if(controller.usuarioActivo.getGrupoUsuarios().getTipo().name().equals("EVENTO") ||
                controller.usuarioActivo.getGrupoUsuarios().getTipo().name().equals("ADMIN")){
            Label tituloConciertos = new Label("Mis eventos");
            VBox conciertosBox = new VBox(10);
            conciertosBox.setPadding(new Insets(20, 20, 20, 20));
            conciertosBox.setAlignment(Pos.TOP_LEFT);
            conciertosBox.getChildren().add(tituloConciertos);
            tituloConciertos.getStyleClass().addAll("h4", "text-primary");
            List<Concierto> conciertos = controller.obtenerConciertosPorPromotor();
            for (Concierto concierto : conciertos) {
                VBox conciertoCard = new VBox(10);
                conciertoCard.getStyleClass().addAll("card", "p-3");
                conciertoCard.setPadding(new Insets(20, 20, 20, 20));

                HBox conciertoHeader = new HBox(10);
                Image image = new Image("file:src/main/resources/" + concierto.getImagen());
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(100);
                imageView.setPreserveRatio(true);

                Label conciertoLabel = new Label(concierto.getNombre());
                conciertoLabel.getStyleClass().addAll("h4", "text-primary");

                VBox datosConcierto = new VBox(10);

                Button irButton = new Button("Ir");
                irButton.getStyleClass().addAll("btn", "btn-primary");
                irButton.setOnAction(e -> mostrarInfoConcierto(concierto));
                Button eliminarButton = new Button("Eliminar");
                eliminarButton.getStyleClass().addAll("btn", "btn-danger");
                eliminarButton.setOnAction(e ->{
                    controller.eliminarConcierto(concierto.getId());
                    mostrarPerfilUsuario();
                });
                if (controller.usuarioActivo.getGrupoUsuarios().getTipo().name().equals("ADMIN")){
                    datosConcierto.getChildren().addAll(conciertoLabel, irButton, eliminarButton);
                }else{
                    datosConcierto.getChildren().addAll(conciertoLabel, irButton);
                }
                conciertoHeader.getChildren().addAll(imageView, datosConcierto);
                conciertoCard.getChildren().addAll(conciertoHeader);
                conciertosBox.getChildren().addAll(conciertoCard);
            }
            card.getChildren().addAll(tituloConciertos, conciertosBox);

        }

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(card);
        StackPane root = new StackPane();
        pagina.getChildren().addAll(barraBotones, scrollPane);
        root.getChildren().add(pagina);
        root.setAlignment(Pos.CENTER);

        Scene registerScene = new Scene(root, Constantes.TAMANYO_VENTANA_HORIZONTAL, Constantes.TAMANYO_VENTANA_VERTICAL);
        registerScene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        stage.setScene(registerScene);
    }

    /**
     * Muestra la ventana de añadir un comentario a un concierto
     * @param concierto Concierto
     */
    private void mostrarAnyadirComentario(Concierto concierto){
        Stage confirmStage = new Stage();
        confirmStage.setTitle("Añadir comentario");
        System.out.println(concierto.getNombre());
        VBox vbox = new VBox(20);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));

        Label textoTitulo = new Label("Comentario");
        TextArea comentarioArea = new TextArea();
        comentarioArea.setPrefRowCount(10);
        comentarioArea.getStyleClass().add("search-field");

        Label texto = new Label("Recomiendas este concierto");
        ComboBox<OpcionesOpinion> opinionComboBox = new ComboBox<>();
        opinionComboBox.getItems().addAll(OpcionesOpinion.values());
        opinionComboBox.setMaxWidth(200);
        opinionComboBox.setPrefWidth(200);
        opinionComboBox.getStyleClass().add("combo-box");

        Label mensaje = new Label();

        Button confirmarButton = new Button("Añadir");
        confirmarButton.getStyleClass().addAll("btn", "btn-success");
        confirmarButton.setOnAction(e -> {
            if(!comentarioArea.getText().isEmpty() && opinionComboBox.getValue() != null){
                controller.registrarComentario(concierto, comentarioArea.getText(), opinionComboBox.getValue());
            }else{
                mensaje.setVisible(true);
                mensaje.setText("Se deben completar todos los campos");
                mensaje.getStyleClass().add("text-warning");
                PauseTransition pause = new PauseTransition(Duration.seconds(3));
                pause.setOnFinished(event -> mensaje.setVisible(false));
                pause.play();
            }

        });
        Button cancelarButton = new Button("Cancelar");
        cancelarButton.getStyleClass().addAll("btn", "btn-danger");
        cancelarButton.setOnAction(e -> confirmStage.close());

        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(confirmarButton, cancelarButton);

        vbox.getChildren().addAll(textoTitulo, comentarioArea, texto, opinionComboBox, buttonBox, mensaje);

        Scene scene = new Scene(vbox, 500, 400);
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        confirmStage.setScene(scene);
        confirmStage.show();
    }

    /**
     * Muestra la pantalla de añadir concierto
     */
    public void mostrarAgregarConcierto(){
        HBox buttonBar = new HBox(10);
        buttonBar.setStyle("-fx-background-color: #c8eeff");
        buttonBar.getStyleClass().add("btn-toolbar");

        Button button1 = new Button("Inicio");
        button1.getStyleClass().addAll("btn", "btn-primary");
        button1.setOnAction(e -> mostrarInicio());
        Button button3;
        if (controller.usuarioActivo != null){
            button3 = new Button("Mi perfil");
        }else{
            button3 = new Button("Iniciar sesión");
        }
        button3.getStyleClass().addAll("btn", "btn-primary");
        button3.setOnAction(e -> mostrarMiPerfil(null));
        buttonBar.getChildren().addAll(button1, button3);
        if(controller.usuarioActivo != null && (controller.usuarioActivo.getGrupoUsuarios().getTipo().equals(Privilegios.EVENTO) ||
                controller.usuarioActivo.getGrupoUsuarios().getTipo().equals(Privilegios.ADMIN))){
            Button button2 = new Button("Añadir evento");
            button2.getStyleClass().addAll("btn", "btn-primary");
            button2.setOnAction(e -> mostrarAgregarConcierto());
            buttonBar.getChildren().add(button2);
        }
        buttonBar.setAlignment(Pos.CENTER);
        buttonBar.setPadding(new Insets(10, 0, 10, 0));
        VBox detailBox = new VBox(20);
        VBox agregarBox = new VBox(10);
        agregarBox.setAlignment(Pos.CENTER);
        agregarBox.setPadding(new Insets(20, 0, 20, 0));

        Label registerLabel = new Label("Agregar nuevo concierto");
        TextField recintoField = new TextField();
        configurarTextLabel(recintoField, "Recinto");

        TextField grupoField = new TextField();
        configurarTextLabel(grupoField, "Grupo");

        DatePicker fechaField = new DatePicker();
        fechaField.setPromptText("Fecha");
        fechaField.setMaxWidth(200);
        fechaField.setPrefWidth(200);
        fechaField.getStyleClass().add("search-field");

        TextField horaField = new TextField();
        configurarTextLabel(horaField, "Hora");

        TextField cantidadEntradasField = new TextField();
        configurarTextLabel(cantidadEntradasField, "Cantidad de entradas normales");

        TextField precioEntradasField = new TextField();
        configurarTextLabel(precioEntradasField, "Precio de entradas normales");

        TextField cantidadEntradasVipField = new TextField();
        configurarTextLabel(cantidadEntradasVipField, "Cantidad de entradas vip");

        TextField precioEntradasVipField = new TextField();
        configurarTextLabel(precioEntradasVipField, "Precio de entradas vip");

        Button seleccionarImagenBtn = new Button("Seleccionar imagen");
        Label imagenSeleccionada = new Label();
        AtomicReference<String> imagen = new AtomicReference<>("");
        seleccionarImagenBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File selectedFile = fileChooser.showOpenDialog(stage);
            imagen.set(controller.guardarImagenGrupo(selectedFile, imagenSeleccionada));
        });

        Label mensajeRegistro = new Label("");
        mensajeRegistro.setVisible(false);

        Button registerButton = new Button("Añadir");
        registerButton.getStyleClass().addAll("btn", "btn-primary");
        registerButton.setOnAction(e ->
            controller.registrarConcierto(grupoField, recintoField, fechaField,  horaField, cantidadEntradasField, precioEntradasField,
                    cantidadEntradasVipField, precioEntradasVipField, imagen.get(), mensajeRegistro)
        );

        HBox registerActions = new HBox(10);
        registerActions.setAlignment(Pos.CENTER);
        registerActions.getChildren().addAll(registerButton);
        agregarBox.getChildren().addAll(registerLabel, grupoField, recintoField, fechaField,  horaField, cantidadEntradasField,
                precioEntradasField, cantidadEntradasVipField, precioEntradasVipField, seleccionarImagenBtn, imagenSeleccionada,
                registerActions, mensajeRegistro);

        detailBox.getChildren().addAll(buttonBar, agregarBox);

        Scene registerScene = new Scene(detailBox, Constantes.TAMANYO_VENTANA_HORIZONTAL, Constantes.TAMANYO_VENTANA_VERTICAL);
        registerScene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        registerScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        stage.setScene(registerScene);
    }

    /**
     * Muestra la ventana de añadir nuevo recinto
     * @param nombre Nombre del recinto
     */
    public void mostrarVentanaNuevoRecinto(String nombre) {
        Stage agregarRecintoStage = new Stage();
        agregarRecintoStage.setTitle("Agregar Recinto");

        VBox vbox = new VBox(20);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));

        Label registerLabel = new Label("El recinto " + nombre + " no se encuentra en nuestra Base de datos");
        TextField nombreField = new TextField();
        configurarTextLabel(nombreField, "Nombre");

        TextField direccionField = new TextField();
        configurarTextLabel(direccionField, "Dirección");

        TextField ciudadField = new TextField();
        configurarTextLabel(ciudadField, "Ciudad");

        TextField telefonoField = new TextField();
        configurarTextLabel(telefonoField, "Teléfono");

        TextField emailField = new TextField();
        configurarTextLabel(emailField, "Email");

        TextField enlaceMapsField = new TextField();
        configurarTextLabel(enlaceMapsField, "Enlace Maps");

        Label mensajeRegistro = new Label("");
        mensajeRegistro.setVisible(false);

        Button registerButton = new Button("Añadir");
        registerButton.getStyleClass().addAll("btn", "btn-primary");
        registerButton.setOnAction(e -> {
            boolean creado = controller.registrarRecinto(nombreField, direccionField, ciudadField, telefonoField, emailField, enlaceMapsField, mensajeRegistro);
            if(creado){
                mensajeRegistro.setVisible(true);
                mensajeRegistro.setText("Recinto introducido con éxito");
                mensajeRegistro.getStyleClass().add("text-primary");
                PauseTransition pause = new PauseTransition(Duration.seconds(3));
                pause.setOnFinished(event -> {
                    mensajeRegistro.setVisible(false);
                    agregarRecintoStage.close();
                });
                pause.play();
            }
        });

        Button cancelarButton = new Button("Cancelar");
        cancelarButton.getStyleClass().addAll("btn", "btn-danger");
        cancelarButton.setOnAction(e -> agregarRecintoStage.close());

        HBox registerActions = new HBox(10);
        registerActions.setAlignment(Pos.CENTER);
        registerActions.getChildren().addAll(registerButton, cancelarButton);
        vbox.getChildren().addAll(registerLabel, nombreField, direccionField, ciudadField, telefonoField, emailField, enlaceMapsField, registerActions, mensajeRegistro);


        Scene scene = new Scene(vbox, 350, 500);
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        agregarRecintoStage.setScene(scene);
        agregarRecintoStage.show();
    }

    /**
     * Muestra la ventana de añadir nuevo recinto y grupo
     * @param recinto Nombre del recinto
     * @param grupo Nombre del grupo
     */
    public void mostrarVentanaNuevoGrupoYRecinto(String recinto, String grupo) {
        Stage agregarRecintoYGrupoStage = new Stage();
        agregarRecintoYGrupoStage.setTitle("Agregar Recinto y Grupo");

        VBox recintoVbox = new VBox(20);
        //recintoVbox.setAlignment(Pos.CENTER);
        recintoVbox.setPadding(new Insets(20));
        VBox grupoVbox = new VBox(20);
        //recintoVbox.setAlignment(Pos.CENTER);
        grupoVbox.setPadding(new Insets(20));

        HBox hbox = new HBox(10);
        hbox.getChildren().addAll(recintoVbox, grupoVbox);

        Label registerLabel = new Label("El recinto " + recinto + " no se encuentra en nuestra Base de datos");
        TextField nombreField = new TextField();
        configurarTextLabel(nombreField, "Nombre");

        TextField direccionField = new TextField();
        configurarTextLabel(direccionField, "Dirección");

        TextField ciudadField = new TextField();
        configurarTextLabel(ciudadField, "Ciudad");

        TextField telefonoField = new TextField();
        configurarTextLabel(telefonoField, "Teléfono");

        TextField emailField = new TextField();
        configurarTextLabel(emailField, "Email");

        TextField enlaceMapsField = new TextField();
        configurarTextLabel(enlaceMapsField, "Enlace Maps");

        Label grupoLabel = new Label("El grupo " + grupo + " no se encuentra en nuestra Base de datos");
        TextField nombreGrupoField = new TextField();
        configurarTextLabel(nombreGrupoField, "Nombre");

        TextField descripcionField = new TextField();
        configurarTextLabel(descripcionField, "Descripción");

        TextField generoField = new TextField();
        configurarTextLabel(generoField, "Género");

        TextField ciudadGrupoField = new TextField();
        configurarTextLabel(ciudadGrupoField, "Ciudad");

        TextField paisField = new TextField();
        configurarTextLabel(paisField, "País");

        TextField perfilSpotifyField = new TextField();
        configurarTextLabel(perfilSpotifyField, "Perfil Spotify");

        Button seleccionarImagenBtn = new Button("Seleccionar imagen");
        Label imagenSeleccionada = new Label();
        AtomicReference<String> imagen = new AtomicReference<>("");
        seleccionarImagenBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File selectedFile = fileChooser.showOpenDialog(stage);
            imagen.set(controller.guardarImagenGrupo(selectedFile, imagenSeleccionada));
        });

        Label mensajeRegistro = new Label("");
        mensajeRegistro.setVisible(false);

        Button registerButton = new Button("Añadir");
        registerButton.getStyleClass().addAll("btn", "btn-primary");
        registerButton.setOnAction(e -> {
            controller.registrarRecinto(nombreField, direccionField, ciudadField, telefonoField, emailField, enlaceMapsField, mensajeRegistro);
            controller.registrarGrupo(nombreGrupoField, descripcionField, generoField, ciudadGrupoField, paisField, imagen.get(), perfilSpotifyField);
            mensajeRegistro.setVisible(true);
            mensajeRegistro.setText("Recinto y grupo introducidos con éxito");
            mensajeRegistro.getStyleClass().add("text-primary");
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(event -> {
                mensajeRegistro.setVisible(false);
                agregarRecintoYGrupoStage.close();
            });
            pause.play();
        });

        Button cancelarButton = new Button("Cancelar");
        cancelarButton.getStyleClass().addAll("btn", "btn-danger");
        cancelarButton.setOnAction(e -> agregarRecintoYGrupoStage.close());

        HBox registerActions = new HBox(10);
        registerActions.setAlignment(Pos.CENTER);
        registerActions.getChildren().addAll(registerButton, cancelarButton);
        VBox registro = new VBox();
        registro.getChildren().addAll(hbox,registerActions, mensajeRegistro);
        recintoVbox.getChildren().addAll(registerLabel, nombreField, direccionField, ciudadField, telefonoField, emailField);
        grupoVbox.getChildren().addAll(grupoLabel, nombreGrupoField, descripcionField, generoField, ciudadGrupoField, paisField, perfilSpotifyField, seleccionarImagenBtn,
                imagenSeleccionada);


        Scene scene = new Scene(registro, 700, 500);
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        agregarRecintoYGrupoStage.setScene(scene);
        agregarRecintoYGrupoStage.show();
    }

    /**
     * Muestra la ventana de añadir un grupo
     * @param nombre Nombre del grupo
     */
    public void mostrarVentanaNuevoGrupo(String nombre) {
        Stage confirmStage = new Stage();
        confirmStage.setTitle("Agregar Recinto");

        VBox vbox = new VBox(20);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));

        Label grupoLabel = new Label("El grupo " + nombre + " no se encuentra en nuestra Base de datos");
        TextField nombreGrupoField = new TextField();
        configurarTextLabel(nombreGrupoField, "Nombre");

        TextField descripcionField = new TextField();
        configurarTextLabel(descripcionField, "Descripción");

        TextField generoField = new TextField();
        configurarTextLabel(generoField, "Género");

        TextField ciudadGrupoField = new TextField();
        configurarTextLabel(ciudadGrupoField, "Ciudad");

        TextField paisField = new TextField();
        configurarTextLabel(paisField, "País");

        TextField perfilSpotifyField = new TextField();
        configurarTextLabel(perfilSpotifyField, "Perfil Spotify");

        Button seleccionarImagenBtn = new Button("Seleccionar imagen");
        Label imagenSeleccionada = new Label();
        AtomicReference<String> imagen = new AtomicReference<>("");
        seleccionarImagenBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File selectedFile = fileChooser.showOpenDialog(stage);
            imagen.set(controller.guardarImagenGrupo(selectedFile, imagenSeleccionada));
        });


        Label mensajeRegistro = new Label("");
        mensajeRegistro.setVisible(false);

        Button registerButton = new Button("Añadir");
        registerButton.getStyleClass().addAll("btn", "btn-primary");
        registerButton.setOnAction(e -> {
            controller.registrarGrupo(nombreGrupoField, descripcionField, generoField, ciudadGrupoField, paisField, imagen.get(), perfilSpotifyField);
            confirmStage.close();
        });

        Button cancelarButton = new Button("Cancelar");
        cancelarButton.getStyleClass().addAll("btn", "btn-danger");
        cancelarButton.setOnAction(e -> confirmStage.close());

        HBox registerActions = new HBox(10);
        registerActions.setAlignment(Pos.CENTER);
        registerActions.getChildren().addAll(registerButton, cancelarButton);
        vbox.getChildren().addAll(grupoLabel, nombreGrupoField, descripcionField, generoField, ciudadGrupoField, paisField,
                perfilSpotifyField, seleccionarImagenBtn, imagenSeleccionada, registerActions, mensajeRegistro);


        Scene scene = new Scene(vbox, 350, 500);
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        confirmStage.setScene(scene);
        confirmStage.show();
    }

    /**
     * Añade el tamaño y los estilo a los textfield
     * @param field TextField
     * @param nombre Nombre del field
     */
    public void configurarTextLabel(TextField field, String nombre){
        field.setPromptText(nombre);
        field.setMaxWidth(200);
        field.setPrefWidth(200);
        field.getStyleClass().add("search-field");
    }

    public static void main(String[] args) {
        launch();
    }
}

