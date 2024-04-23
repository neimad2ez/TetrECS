package uk.ac.soton.comp1206.scene;

import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.Multimedia;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    /**
     * Create a new menu scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    /**
     * Build the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);

        //Title layout
        var top = new HBox();
        top.setAlignment(Pos.CENTER);
        top.setPadding(new Insets(50, 0, 0, 0));
        mainPane.setTop(top);

        //Menu layout
        var menu = new VBox(5);
        menu.setAlignment(Pos.CENTER);
        menu.setPadding(new Insets(100,0,500,0));
        mainPane.setCenter(menu);

        //Title
        Image image = new Image(getClass().getResourceAsStream("/images/TetrECS.png"));
        ImageView title = new ImageView(image);
        title.setPreserveRatio(true);
        title.setFitHeight(140);
        top.getChildren().add(title);

        //Title animation
        RotateTransition rt = new RotateTransition(Duration.millis(2000), title);
        rt.setToAngle(5);
        rt.setFromAngle(-5);
        rt.setCycleCount(Animation.INDEFINITE);
        rt.setAutoReverse(true);
        rt.play();

        //Play button
        var play = new Text("Play");
        play.getStyleClass().add("menuItem");
        play.setOnMouseClicked(event -> {
            gameWindow.startChallenge();
        });

        //Multiplayer button
        var multiplayer = new Text("Multiplayer");
        multiplayer.getStyleClass().add("menuItem");
        multiplayer.setOnMouseClicked(event -> {
            gameWindow.startMultiplayer();
        });

        //Instruction button
        var instruction = new Text("Instructions");
        instruction.getStyleClass().add("menuItem");
        instruction.setOnMouseClicked(event -> {
            gameWindow.startInstructions();
        });

        //Quit button
        var quit = new Text("Quit");
        quit.getStyleClass().add("menuItem");
        quit.setOnMouseClicked(event -> {
            Platform.exit();
        });

        menu.getChildren().addAll(play, multiplayer, instruction, quit);
    }

    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {
        logger.info("Initialising menu");
        Multimedia.playMusic("menu.mp3");
    }

    /**
     * Handle when the Start Game button is pressed
     * @param event event
     */
    private void startGame(ActionEvent event) {
        gameWindow.startChallenge();
    }

}
