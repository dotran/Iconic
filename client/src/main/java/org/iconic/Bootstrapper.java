package org.iconic;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.iconic.config.InMemoryModule;
import org.iconic.project.search.SearchService;

/**
 * {@inheritDoc}
 * <p>
 * The bootstrapper defines the entry point for the graphical user interface and performs all the necessary setup
 * to link views to their controllers and models.
 * </p>
 */
@Log4j2
public class Bootstrapper extends Application {
    private final Injector injector;
    private final SearchService searchService;

    /**
     * <p>
     * Launches the application with the provided arguments.
     * </p>
     *
     * @param args The arguments to pass to the application
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * <p>Constructs a new Bootstrapper</p>
     */
    public Bootstrapper() {
        super();
        injector = Guice.createInjector(new InMemoryModule());
        searchService = injector.getInstance(SearchService.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage primaryStage) {
        // Create the primary stage
        primaryStage.setTitle("- Iconic Workbench");

        // Create the root node for placing all the other components
        val root = new BorderPane();

        // Load the child UI elements from FXML resources
        val menuLoader = load("views/menu/MenuView.fxml");
        val projectLoader = load("views/project/ProjectTreeView.fxml");
        val workspaceLoader = load("views/workspace/WorkspaceView.fxml");

        try {
            root.setTop(menuLoader.load());
            root.setLeft(projectLoader.load());
            root.setCenter(workspaceLoader.load());
        } catch (IOException ex) {
            log.debug(ex.getMessage());
        }

        val scene = new Scene(root, 720, 480);

        // Load our stylesheets
        val stylesheet = getClass().getClassLoader().getResource("stylesheet.css");

        if (stylesheet != null) {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        }

        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private FXMLLoader load(final String uri) throws UnsupportedOperationException {
        val defaultLocale = "en";
        val defaultLocaleResource = "localisation.en_au";
        val loader = new FXMLLoader(getClass().getClassLoader().getResource(uri));
        loader.setControllerFactory(getInjector()::getInstance);

        // Provide a localisation resource to use for i18n strings
        if (Locale.getDefault().getLanguage().startsWith(defaultLocale)) {
            loader.setResources(ResourceBundle.getBundle(defaultLocaleResource));
        }
        // Just throw an exception for other languages (for now)
        else {
            throw new UnsupportedOperationException("Your language is not yet supported");
        }

        return loader;
    }


    @Override
    public void stop() {
        for (val search : getSearchService().searchesProperty().entrySet()) {
            search.getValue().stop();
        }
    }

    /**
     * <p>
     * Returns the default injector of this bootstrapper
     * </p>
     *
     * @return the default injector of the bootstrapper
     */
    private Injector getInjector() {
        return injector;
    }

    /**
     * <p>
     * Returns the search service of this bootstrapper
     * </p>
     *
     * @return the search service of the bootstrapper
     */
    private SearchService getSearchService() {
        return searchService;
    }

}