package com.ccadmin.app.desktop;

import com.ccadmin.app.ApplicationCcadminApplication;
import com.ccadmin.app.desktop.ui.DesktopMainWindow;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class DesktopLauncher extends Application {
    private ConfigurableApplicationContext context;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        context = new SpringApplicationBuilder(ApplicationCcadminApplication.class)
                .web(WebApplicationType.NONE)
                .profiles("desktop")
                .run();
    }

    @Override
    public void start(Stage stage) {
        DesktopMainWindow mainWindow = context.getBean(DesktopMainWindow.class);
        Scene scene = mainWindow.createScene(stage);
        scene.getStylesheets().add(getClass().getResource("/desktop.css").toExternalForm());
        stage.setTitle("Video Complete Desktop");
        stage.setMinWidth(1180);
        stage.setMinHeight(760);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        if (context != null) {
            context.close();
        }
    }
}
