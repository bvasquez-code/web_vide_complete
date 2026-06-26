package com.ccadmin.app.desktop.ui;

import com.ccadmin.app.admin.controller.AdminAuthController;
import com.ccadmin.app.admin.model.dto.LoginDto;
import com.ccadmin.app.admin.model.dto.LoginResponseDto;
import com.ccadmin.app.desktop.controller.DesktopAppConfigController;
import com.ccadmin.app.shared.model.dto.ResponsePageSearchT;
import com.ccadmin.app.video.controller.AdminCatalogController;
import com.ccadmin.app.video.controller.AdminVideoController;
import com.ccadmin.app.video.controller.PublicVideoController;
import com.ccadmin.app.video.model.dto.VideoCardDto;
import com.ccadmin.app.video.model.dto.VideoDetailDto;
import com.ccadmin.app.video.model.dto.VideoRegisterDto;
import com.ccadmin.app.video.model.entity.ActorEntity;
import com.ccadmin.app.video.model.entity.TagEntity;
import com.ccadmin.app.video.model.entity.VideoCategoryEntity;
import com.ccadmin.app.video.model.entity.VideoEntity;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Component
public class DesktopMainWindow {
    private static final Logger log = LoggerFactory.getLogger(DesktopMainWindow.class);
    private final AdminAuthController authController;
    private final PublicVideoController publicVideoController;
    private final AdminVideoController adminVideoController;
    private final AdminCatalogController adminCatalogController;
    private final DesktopAppConfigController desktopAppConfigController;
    private final BorderPane root = new BorderPane();
    private final StackPane content = new StackPane();
    private LoginResponseDto session;
    private MediaPlayer currentPlayer;
    private Stage stage;

    public DesktopMainWindow(AdminAuthController authController, PublicVideoController publicVideoController, AdminVideoController adminVideoController, AdminCatalogController adminCatalogController, DesktopAppConfigController desktopAppConfigController) {
        this.authController = authController;
        this.publicVideoController = publicVideoController;
        this.adminVideoController = adminVideoController;
        this.adminCatalogController = adminCatalogController;
        this.desktopAppConfigController = desktopAppConfigController;
    }

    public Scene createScene(Stage stage) {
        this.stage = stage;
        root.getStyleClass().add("app-root");
        root.setCenter(createLoginView());
        return new Scene(root, 1280, 800);
    }

    private Parent createLoginView() {
        VBox box = new VBox(18);
        box.getStyleClass().add("login-panel");
        box.setMaxWidth(420);
        box.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Video Complete Desktop");
        title.getStyleClass().add("login-title");
        Label subtitle = new Label("Acceso obligatorio para catalogo publico y administracion.");
        subtitle.getStyleClass().add("muted");
        TextField userName = new TextField();
        userName.setPromptText("Usuario");
        PasswordField password = new PasswordField();
        password.setPromptText("Contrasena");
        Label message = new Label();
        message.getStyleClass().add("error-text");
        Button login = new Button("Ingresar");
        login.getStyleClass().add("primary-button");
        login.setMaxWidth(Double.MAX_VALUE);
        login.setDefaultButton(true);
        login.setOnAction(event -> {
            try {
                LoginDto dto = new LoginDto();
                dto.UserName = userName.getText();
                dto.Password = password.getText();
                session = (LoginResponseDto) DesktopResponse.data(authController.login(dto));
                showApplicationShell();
            } catch (Exception ex) {
                message.setText(ex.getMessage());
            }
        });

        box.getChildren().addAll(title, subtitle, userName, password, login, message);
        StackPane wrapper = new StackPane(box);
        wrapper.getStyleClass().add("login-wrapper");
        return wrapper;
    }

    private void showApplicationShell() {
        root.setTop(createTopBar());
        root.setLeft(createSidebar());
        root.setCenter(content);
        showPublicCatalog();
    }

    private Parent createTopBar() {
        HBox bar = new HBox(12);
        bar.getStyleClass().add("top-bar");
        bar.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Video Complete");
        title.getStyleClass().add("top-title");
        Label user = new Label(session.Names + " (" + session.UserName + ")");
        user.getStyleClass().add("user-pill");
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button logout = new Button("Salir");
        logout.getStyleClass().add("ghost-button");
        logout.setOnAction(event -> {
            stopPlayer();
            session = null;
            root.setTop(null);
            root.setLeft(null);
            root.setCenter(createLoginView());
        });
        bar.getChildren().addAll(title, spacer, user, logout);
        return bar;
    }

    private Parent createSidebar() {
        VBox menu = new VBox(10);
        menu.getStyleClass().add("sidebar");
        menu.getChildren().addAll(
                navButton("Explorar videos", this::showPublicCatalog),
                navButton("Administrar videos", this::showAdminVideos),
                navButton("Catalogos", this::showCatalogs),
                navButton("Procesos", this::showProcesses),
                navButton("Configuracion", this::showDesktopConfiguration)
        );
        return menu;
    }

    private Button navButton(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("nav-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(event -> action.run());
        return button;
    }

    private void setContent(Parent view) {
        stopPlayer();
        content.getChildren().setAll(view);
    }

    private void showPublicCatalog() {
        VBox view = page("Explorar videos", "Busqueda publica integrada dentro de la aplicacion de escritorio.");
        HBox filters = new HBox(10);
        filters.setAlignment(Pos.CENTER_LEFT);
        TextField query = new TextField();
        query.setPromptText("Buscar por titulo, descripcion, categoria, actor o tag");
        query.setPrefWidth(420);
        ComboBox<String> sort = new ComboBox<>(FXCollections.observableArrayList("recent", "views", "title"));
        sort.setValue("recent");
        Button search = new Button("Buscar");
        filters.getChildren().addAll(query, sort, search);

        TableView<VideoCardDto> table = createVideoCardTable();
        Button play = new Button("Ver video");
        play.getStyleClass().add("primary-button");
        play.setOnAction(event -> {
            VideoCardDto selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showVideoPlayer(selected.VideoCod);
            }
        });
        search.setOnAction(event -> loadPublicVideos(table, query.getText(), sort.getValue()));
        query.setOnAction(event -> loadPublicVideos(table, query.getText(), sort.getValue()));
        loadPublicVideos(table, "", "recent");

        view.getChildren().addAll(filters, table, play);
        VBox.setVgrow(table, Priority.ALWAYS);
        setContent(view);
    }

    private void loadPublicVideos(TableView<VideoCardDto> table, String query, String sort) {
        try {
            Object data = DesktopResponse.data(publicVideoController.search(query == null ? "" : query, sort == null ? "recent" : sort, 1, 80));
            ResponsePageSearchT<VideoCardDto> page = castPage(data);
            table.setItems(FXCollections.observableArrayList(page.Data));
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private TableView<VideoCardDto> createVideoCardTable() {
        TableView<VideoCardDto> table = new TableView<>();
        table.getStyleClass().add("data-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        TableColumn<VideoCardDto, VideoCardDto> thumbnail = thumbnailColumn();
        TableColumn<VideoCardDto, String> cod = column("Codigo", item -> item.VideoCod);
        TableColumn<VideoCardDto, String> title = column("Titulo", item -> item.Title);
        TableColumn<VideoCardDto, String> quality = column("Calidad", item -> qualityLabel(item.ResolutionWidth, item.ResolutionHeight));
        TableColumn<VideoCardDto, String> duration = column("Duracion", item -> safe(item.Duration));
        TableColumn<VideoCardDto, String> views = column("Vistas", item -> String.valueOf(item.ViewCount == null ? 0 : item.ViewCount));
        table.getColumns().addAll(thumbnail, cod, title, quality, duration, views);
        table.setRowFactory(tv -> {
            javafx.scene.control.TableRow<VideoCardDto> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showVideoPlayer(row.getItem().VideoCod);
                }
            });
            return row;
        });
        return table;
    }

    private TableColumn<VideoCardDto, VideoCardDto> thumbnailColumn() {
        TableColumn<VideoCardDto, VideoCardDto> column = new TableColumn<>("Miniatura");
        column.setPrefWidth(138);
        column.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        column.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            private final Label empty = new Label("Sin imagen");

            {
                imageView.setFitWidth(120);
                imageView.setFitHeight(68);
                imageView.setPreserveRatio(true);
                empty.getStyleClass().add("muted");
            }

            @Override
            protected void updateItem(VideoCardDto video, boolean emptyRow) {
                super.updateItem(video, emptyRow);
                if (emptyRow || video == null) {
                    setGraphic(null);
                    return;
                }
                String imageUri = resolveLocalImageUri(video.ThumbnailUrl);
                if (imageUri == null) {
                    setGraphic(empty);
                    return;
                }
                imageView.setImage(new Image(imageUri, 120, 68, true, true, true));
                setGraphic(imageView);
            }
        });
        return column;
    }

    private void showVideoPlayer(String videoCod) {
        try {
            stopPlayer();
            VideoDetailDto detail = (VideoDetailDto) DesktopResponse.data(publicVideoController.detail(videoCod));
            VideoEntity video = detail.Video;
            VBox view = page(video.Title, video.ShortDescription == null ? "" : video.ShortDescription);
            Label meta = new Label("Resolucion " + qualityLabel(video.ResolutionWidth, video.ResolutionHeight) + " " + resolutionLabel(video)
                    + " - Peso " + fileSizeLabel(video.FileSizeBytes)
                    + " - Duracion " + safe(video.Duration)
                    + " - Origen " + video.SourceType);
            meta.getStyleClass().add("muted");
            Parent player = createPlayer(video);
            TextArea description = new TextArea(video.LongDescription == null ? "" : video.LongDescription);
            description.setEditable(false);
            description.setPrefRowCount(4);
            Button back = new Button("Volver");
            back.setOnAction(event -> showPublicCatalog());
            view.getChildren().addAll(meta, player, description, back);
            content.getChildren().setAll(view);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private Parent createPlayer(VideoEntity video) {
        if (!"PATH".equals(video.SourceType) && !"URL".equals(video.SourceType)) {
            Label label = new Label("Este origen EMBED no se reproduce directamente en escritorio.");
            label.getStyleClass().add("empty-state");
            return new StackPane(label);
        }
        String source = videoSourceUri(video);
        if (source == null) {
            Label label = new Label("La referencia del video no es valida.");
            label.getStyleClass().add("empty-state");
            return new StackPane(label);
        }
        Label playerMessage = new Label();
        playerMessage.getStyleClass().add("error-text");
        playerMessage.setWrapText(true);
        playerMessage.setMaxWidth(660);
        Button openExternal = new Button("Abrir externo");
        openExternal.getStyleClass().add("ghost-button");
        openExternal.setOnAction(event -> openExternal(video));
        Button fullScreen = new Button("Pantalla completa");
        fullScreen.getStyleClass().add("ghost-button");
        if ("PATH".equals(video.SourceType)) {
            Path path = Path.of(video.SourceValue).normalize();
            if (!Files.exists(path)) {
                Label label = new Label("Archivo no encontrado: " + path);
                label.getStyleClass().add("empty-state");
                return new StackPane(label);
            }
        }
        try {
            Media media = new Media(source);
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            currentPlayer = mediaPlayer;
            MediaView mediaView = new MediaView(mediaPlayer);
            mediaView.setPreserveRatio(true);
            mediaView.setFitWidth(640);
            mediaView.setFitHeight(360);
            mediaPlayer.setOnError(() -> {
                String message = mediaErrorMessage("JavaFX no pudo reproducir el video", mediaPlayer.getError());
                log.error(message, mediaPlayer.getError());
                playerMessage.setText(message);
            });
            media.setOnError(() -> {
                String message = mediaErrorMessage("JavaFX no pudo cargar el video", media.getError());
                log.error(message, media.getError());
                playerMessage.setText(message);
            });
            Button play = new Button("Play");
            Button pause = new Button("Pause");
            play.setOnAction(event -> {
                if (mediaPlayer.getError() != null || media.getError() != null) {
                    String message = mediaErrorMessage("JavaFX no pudo reproducir el video", mediaPlayer.getError() != null ? mediaPlayer.getError() : media.getError());
                    log.error(message, mediaPlayer.getError() != null ? mediaPlayer.getError() : media.getError());
                    playerMessage.setText(message);
                    return;
                }
                mediaPlayer.play();
            });
            pause.setOnAction(event -> mediaPlayer.pause());
            fullScreen.setOnAction(event -> openFullScreenPlayer());
            HBox controls = new HBox(10, play, pause, fullScreen, openExternal);
            controls.setAlignment(Pos.CENTER_LEFT);
            StackPane videoBox = new StackPane(mediaView);
            videoBox.getStyleClass().add("player-video-box");
            VBox box = new VBox(10, videoBox, controls, playerMessage);
            box.getStyleClass().add("player-panel");
            box.setMaxWidth(700);
            return box;
        } catch (Exception ex) {
            String message = mediaErrorMessage("JavaFX no pudo inicializar el reproductor", ex);
            log.error(message, ex);
            playerMessage.setText(message);
            VBox box = new VBox(10, playerMessage, openExternal);
            box.getStyleClass().add("player-panel");
            box.setMaxWidth(700);
            return box;
        }
    }

    private void showAdminVideos() {
        VBox view = page("Administrar videos", "Gestion local de videos, usando controllers del backend copiado.");
        HBox filters = new HBox(10);
        TextField query = new TextField();
        query.setPromptText("Buscar");
        ComboBox<String> status = new ComboBox<>(FXCollections.observableArrayList("", "A", "I"));
        status.setValue("");
        Button search = new Button("Buscar");
        Button newVideo = new Button("Nuevo video");
        filters.getChildren().addAll(query, status, search, newVideo);

        TableView<VideoEntity> table = createVideoEntityTable();
        HBox actions = new HBox(10);
        Button edit = new Button("Editar");
        Button enable = new Button("Activar");
        Button disable = new Button("Inactivar");
        actions.getChildren().addAll(edit, enable, disable);
        search.setOnAction(event -> loadAdminVideos(table, query.getText(), status.getValue()));
        query.setOnAction(event -> loadAdminVideos(table, query.getText(), status.getValue()));
        newVideo.setOnAction(event -> showVideoForm(null));
        edit.setOnAction(event -> {
            VideoEntity selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) showVideoForm(selected.VideoCod);
        });
        enable.setOnAction(event -> changeVideoStatus(table, query.getText(), status.getValue(), true));
        disable.setOnAction(event -> changeVideoStatus(table, query.getText(), status.getValue(), false));
        loadAdminVideos(table, "", "");

        view.getChildren().addAll(filters, table, actions);
        VBox.setVgrow(table, Priority.ALWAYS);
        setContent(view);
    }

    private TableView<VideoEntity> createVideoEntityTable() {
        TableView<VideoEntity> table = new TableView<>();
        table.getStyleClass().add("data-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.getColumns().addAll(
                column("Codigo", item -> item.VideoCod),
                column("Titulo", item -> item.Title),
                column("Calidad", item -> qualityLabel(item.ResolutionWidth, item.ResolutionHeight)),
                column("Duracion", item -> safe(item.Duration)),
                column("Origen", item -> safe(item.SourceType)),
                column("Estado", item -> safe(item.Status)),
                column("Vistas", item -> String.valueOf(item.ViewCount == null ? 0 : item.ViewCount))
        );
        table.setRowFactory(tv -> {
            javafx.scene.control.TableRow<VideoEntity> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showVideoForm(row.getItem().VideoCod);
                }
            });
            return row;
        });
        return table;
    }

    private void loadAdminVideos(TableView<VideoEntity> table, String query, String status) {
        try {
            Object data = DesktopResponse.data(adminVideoController.findAll(safe(query), safe(status), "", "", "", "", 1, 100));
            ResponsePageSearchT<VideoEntity> page = castPage(data);
            table.setItems(FXCollections.observableArrayList(page.Data));
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void changeVideoStatus(TableView<VideoEntity> table, String query, String status, boolean active) {
        try {
            VideoEntity selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            VideoRegisterDto dto = new VideoRegisterDto();
            dto.Video = new VideoEntity();
            dto.Video.VideoCod = selected.VideoCod;
            DesktopResponse.data(active ? adminVideoController.enable(dto) : adminVideoController.disable(dto));
            loadAdminVideos(table, query, status);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void showVideoForm(String videoCod) {
        try {
            VideoEntity video = new VideoEntity();
            if (videoCod != null) {
                VideoDetailDto detail = (VideoDetailDto) DesktopResponse.data(adminVideoController.findById(videoCod));
                video = detail.Video;
            }
            VideoEntity formVideo = video;
            VBox view = page(videoCod == null ? "Nuevo video" : "Editar video", "Alta y edicion basica de videos locales.");
            TextField title = new TextField(safe(formVideo.Title));
            TextArea shortDescription = new TextArea(safe(formVideo.ShortDescription));
            shortDescription.setPrefRowCount(2);
            TextArea longDescription = new TextArea(safe(formVideo.LongDescription));
            longDescription.setPrefRowCount(5);
            ComboBox<String> sourceType = new ComboBox<>(FXCollections.observableArrayList("PATH", "URL", "EMBED"));
            sourceType.setValue(formVideo.SourceType == null ? "PATH" : formVideo.SourceType);
            TextField sourceValue = new TextField(safe(formVideo.SourceValue));
            Button chooseFile = new Button("Seleccionar archivo");
            chooseFile.setOnAction(event -> {
                FileChooser chooser = new FileChooser();
                File file = chooser.showOpenDialog(stage);
                if (file != null) {
                    sourceType.setValue("PATH");
                    sourceValue.setText(file.getAbsolutePath());
                }
            });
            TextField duration = new TextField(safe(formVideo.Duration));
            HBox sourceRow = new HBox(10, sourceType, sourceValue, chooseFile);
            HBox.setHgrow(sourceValue, Priority.ALWAYS);
            GridPane form = formGrid();
            form.addRow(0, new Label("Titulo"), title);
            form.addRow(1, new Label("Descripcion corta"), shortDescription);
            form.addRow(2, new Label("Descripcion larga"), longDescription);
            form.addRow(3, new Label("Origen"), sourceRow);
            form.addRow(4, new Label("Duracion"), duration);
            Button save = new Button("Guardar");
            save.getStyleClass().add("primary-button");
            Button back = new Button("Volver");
            back.setOnAction(event -> showAdminVideos());
            save.setOnAction(event -> {
                try {
                    VideoRegisterDto dto = new VideoRegisterDto();
                    dto.Video = formVideo;
                    dto.Video.Title = title.getText();
                    dto.Video.ShortDescription = shortDescription.getText();
                    dto.Video.LongDescription = longDescription.getText();
                    dto.Video.SourceType = sourceType.getValue();
                    dto.Video.SourceValue = sourceValue.getText();
                    dto.Video.Duration = duration.getText();
                    DesktopResponse.data(adminVideoController.save(dto));
                    showAdminVideos();
                } catch (Exception ex) {
                    showError(ex);
                }
            });
            view.getChildren().addAll(form, new HBox(10, save, back));
            setContent(new ScrollPane(view));
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void showCatalogs() {
        TabPane tabs = new TabPane();
        tabs.getTabs().addAll(
                new Tab("Categorias", createCategoryCatalog()),
                new Tab("Actores", createActorCatalog()),
                new Tab("Tags", createTagCatalog())
        );
        tabs.getTabs().forEach(tab -> tab.setClosable(false));
        VBox view = page("Catalogos", "Mantenimiento de categorias, actores y tags.");
        view.getChildren().add(tabs);
        VBox.setVgrow(tabs, Priority.ALWAYS);
        setContent(view);
    }

    private Parent createCategoryCatalog() {
        TableView<VideoCategoryEntity> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.getColumns().addAll(
                column("Codigo", item -> item.CategoryCod),
                column("Nombre", item -> item.Name),
                column("Estado", item -> item.Status)
        );
        TextField name = new TextField();
        name.setPromptText("Nombre de categoria");
        Button save = new Button("Guardar");
        save.setOnAction(event -> {
            try {
                VideoCategoryEntity entity = new VideoCategoryEntity();
                entity.Name = name.getText();
                DesktopResponse.data(adminCatalogController.saveCategory(entity));
                name.clear();
                loadCategories(table);
            } catch (Exception ex) {
                showError(ex);
            }
        });
        loadCategories(table);
        return catalogPane(table, name, save);
    }

    private Parent createActorCatalog() {
        TableView<ActorEntity> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.getColumns().addAll(
                column("Codigo", item -> item.ActorCod),
                column("Nombre", item -> item.Name),
                column("Estado", item -> item.Status)
        );
        TextField name = new TextField();
        name.setPromptText("Nombre de actor");
        Button save = new Button("Guardar");
        save.setOnAction(event -> {
            try {
                ActorEntity entity = new ActorEntity();
                entity.Name = name.getText();
                DesktopResponse.data(adminCatalogController.saveActor(entity));
                name.clear();
                loadActors(table);
            } catch (Exception ex) {
                showError(ex);
            }
        });
        loadActors(table);
        return catalogPane(table, name, save);
    }

    private Parent createTagCatalog() {
        TableView<TagEntity> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.getColumns().addAll(
                column("Codigo", item -> item.TagCod),
                column("Nombre", item -> item.Name),
                column("Estado", item -> item.Status)
        );
        TextField name = new TextField();
        name.setPromptText("Nombre de tag");
        Button save = new Button("Guardar");
        save.setOnAction(event -> {
            try {
                TagEntity entity = new TagEntity();
                entity.Name = name.getText();
                DesktopResponse.data(adminCatalogController.saveTag(entity));
                name.clear();
                loadTags(table);
            } catch (Exception ex) {
                showError(ex);
            }
        });
        loadTags(table);
        return catalogPane(table, name, save);
    }

    private <T> Parent catalogPane(TableView<T> table, TextField name, Button save) {
        VBox box = new VBox(12, new HBox(10, name, save), table);
        box.setPadding(new Insets(16));
        VBox.setVgrow(table, Priority.ALWAYS);
        HBox.setHgrow(name, Priority.ALWAYS);
        return box;
    }

    private void loadCategories(TableView<VideoCategoryEntity> table) {
        ResponsePageSearchT<VideoCategoryEntity> page = castPage(DesktopResponse.data(adminCatalogController.categories("", "", 1, 100)));
        table.setItems(FXCollections.observableArrayList(page.Data));
    }

    private void loadActors(TableView<ActorEntity> table) {
        ResponsePageSearchT<ActorEntity> page = castPage(DesktopResponse.data(adminCatalogController.actors("", "", 1, 100)));
        table.setItems(FXCollections.observableArrayList(page.Data));
    }

    private void loadTags(TableView<TagEntity> table) {
        ResponsePageSearchT<TagEntity> page = castPage(DesktopResponse.data(adminCatalogController.tags("", "", 1, 100)));
        table.setItems(FXCollections.observableArrayList(page.Data));
    }

    private void showProcesses() {
        VBox view = page("Procesos", "Operaciones administrativas locales para videos existentes.");
        TextField videoCod = new TextField();
        videoCod.setPromptText("VideoCod opcional");
        ComboBox<String> overwrite = new ComboBox<>(FXCollections.observableArrayList("false", "true"));
        overwrite.setValue("false");
        Button fileMetadata = new Button("Procesar peso y resolucion");
        Label result = new Label();
        result.getStyleClass().add("muted");
        fileMetadata.setOnAction(event -> {
            try {
                Object data = DesktopResponse.data(adminVideoController.processFileMetadata(videoCod.getText(), Boolean.valueOf(overwrite.getValue())));
                result.setText("Proceso terminado: " + data);
            } catch (Exception ex) {
                showError(ex);
            }
        });
        view.getChildren().addAll(new HBox(10, videoCod, overwrite, fileMetadata), result);
        setContent(view);
    }

    private void showDesktopConfiguration() {
        VBox view = page("Configuracion", "Rutas locales usadas por la aplicacion de escritorio.");
        TextField uploadRoot = new TextField(getConfiguredUploadRoot());
        uploadRoot.setPromptText("Ruta base de uploads. Ejemplo: C:\\proyectos\\multiple\\web_vide_complete\\uploads");
        uploadRoot.setPrefWidth(620);
        Button choose = new Button("Seleccionar carpeta");
        Button save = new Button("Guardar");
        save.getStyleClass().add("primary-button");
        Label help = new Label("La carpeta debe contener subcarpetas como thumbnails y captures. Despues de guardar, vuelve a Explorar videos para refrescar miniaturas.");
        help.getStyleClass().add("muted");
        Label message = new Label();
        message.getStyleClass().add("muted");
        choose.setOnAction(event -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Seleccionar carpeta uploads");
            String current = uploadRoot.getText();
            if (current != null && !current.isBlank() && Files.isDirectory(Path.of(current))) {
                chooser.setInitialDirectory(Path.of(current).toFile());
            }
            File selected = chooser.showDialog(stage);
            if (selected != null) {
                uploadRoot.setText(selected.getAbsolutePath());
            }
        });
        save.setOnAction(event -> {
            try {
                DesktopResponse.data(desktopAppConfigController.saveUploadRoot(uploadRoot.getText()));
                message.setText("Configuracion guardada.");
            } catch (Exception ex) {
                showError(ex);
            }
        });
        view.getChildren().addAll(new HBox(10, uploadRoot, choose, save), help, message);
        setContent(view);
    }

    private VBox page(String title, String subtitle) {
        VBox view = new VBox(16);
        view.getStyleClass().add("page");
        Label h1 = new Label(title);
        h1.getStyleClass().add("page-title");
        Label sub = new Label(subtitle);
        sub.getStyleClass().add("muted");
        view.getChildren().addAll(h1, sub);
        return view;
    }

    private GridPane formGrid() {
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.getStyleClass().add("form-grid");
        return form;
    }

    private <T> TableColumn<T, String> column(String title, FieldReader<T> reader) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(reader.read(data.getValue()))));
        return column;
    }

    @SuppressWarnings("unchecked")
    private <T> ResponsePageSearchT<T> castPage(Object data) {
        return (ResponsePageSearchT<T>) data;
    }

    private void stopPlayer() {
        if (currentPlayer != null) {
            currentPlayer.stop();
            currentPlayer.dispose();
            currentPlayer = null;
        }
    }

    private String resolveLocalImageUri(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }
        try {
            if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://") || imageUrl.startsWith("file:")) {
                return imageUrl;
            }
            String normalized = imageUrl.replace("\\", "/");
            String thumbnailPrefix = "/api/v1/public/thumbnails/";
            if (normalized.startsWith(thumbnailPrefix)) {
                String fileName = normalized.substring(thumbnailPrefix.length());
                Path path = Path.of(getConfiguredUploadRoot(), "thumbnails", fileName).normalize();
                return Files.exists(path) ? path.toUri().toString() : null;
            }
            Path path = Path.of(imageUrl).normalize();
            return Files.exists(path) ? path.toUri().toString() : null;
        } catch (Exception ex) {
            return null;
        }
    }

    private String getConfiguredUploadRoot() {
        try {
            Object data = DesktopResponse.data(desktopAppConfigController.getUploadRoot());
            String value = data == null ? "" : data.toString();
            return value.isBlank() ? "uploads" : value;
        } catch (Exception ex) {
            return "uploads";
        }
    }

    private String videoSourceUri(VideoEntity video) {
        try {
            if ("PATH".equals(video.SourceType)) {
                Path path = Path.of(video.SourceValue).normalize();
                return Files.exists(path) ? path.toUri().toString() : null;
            }
            if ("URL".equals(video.SourceType)) {
                return video.SourceValue;
            }
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    private void openFullScreenPlayer() {
        if (currentPlayer == null) {
            return;
        }
        Stage fullStage = new Stage();
        MediaView mediaView = new MediaView(currentPlayer);
        mediaView.setPreserveRatio(true);
        StackPane wrapper = new StackPane(mediaView);
        wrapper.getStyleClass().add("fullscreen-player");
        Scene scene = new Scene(wrapper, 1200, 760);
        scene.getStylesheets().add(getClass().getResource("/desktop.css").toExternalForm());
        mediaView.fitWidthProperty().bind(scene.widthProperty());
        mediaView.fitHeightProperty().bind(scene.heightProperty());
        fullStage.setScene(scene);
        fullStage.setFullScreen(true);
        fullStage.setTitle("Video - Pantalla completa");
        fullStage.show();
    }

    private void openExternal(VideoEntity video) {
        try {
            if (!Desktop.isDesktopSupported()) {
                showError(new IllegalStateException("El sistema no permite abrir reproductores externos."));
                return;
            }
            if ("PATH".equals(video.SourceType)) {
                Desktop.getDesktop().open(Path.of(video.SourceValue).normalize().toFile());
                return;
            }
            if ("URL".equals(video.SourceType)) {
                Desktop.getDesktop().browse(URI.create(video.SourceValue));
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private String mediaErrorMessage(String prefix, Throwable error) {
        if (error == null) {
            return prefix + ": error no especificado. Si el archivo abre con otro reproductor, el codec probablemente no es compatible con JavaFX Media.";
        }
        String detail = error.getMessage();
        if (detail == null || detail.isBlank()) {
            detail = error.toString();
        }
        return prefix + ": " + detail + ". Puedes usar Abrir externo si el codec no es compatible con JavaFX.";
    }

    private void showError(Exception ex) {
        log.error("Error en la aplicacion desktop", ex);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("No se pudo completar la operacion");
        alert.setContentText(ex.getMessage());
        alert.showAndWait();
    }

    private String qualityLabel(Integer width, Integer height) {
        if (width == null || height == null || width <= 0 || height <= 0) return "SD";
        if (width >= 3840 || height >= 2160) return "4K";
        if (width >= 2560 || height >= 1440) return "2K";
        if (width >= 1920 || height >= 1080) return "Full HD";
        if (width >= 1280 || height >= 720) return "HD";
        return "SD";
    }

    private String resolutionLabel(VideoEntity video) {
        return video.ResolutionWidth != null && video.ResolutionHeight != null
                ? video.ResolutionWidth + "x" + video.ResolutionHeight
                : "No registrada";
    }

    private String fileSizeLabel(Long bytes) {
        if (bytes == null || bytes <= 0) return "No registrado";
        double value = bytes;
        List<String> units = List.of("B", "KB", "MB", "GB", "TB");
        int unit = 0;
        while (value >= 1024 && unit < units.size() - 1) {
            value = value / 1024;
            unit++;
        }
        return (value >= 10 ? String.format("%.0f", value) : String.format("%.1f", value)) + " " + units.get(unit);
    }

    private String safe(Object value) {
        return value == null ? "" : value.toString();
    }

    @FunctionalInterface
    private interface FieldReader<T> {
        String read(T value);
    }
}
