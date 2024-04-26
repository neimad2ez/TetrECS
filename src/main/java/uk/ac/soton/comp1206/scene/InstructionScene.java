package uk.ac.soton.comp1206.scene;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * Instruction scene that displays the instructions of the game

 */
public class InstructionScene extends BaseScene{
    private static final Logger logger = LogManager.getLogger(InstructionScene.class);


    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     * @param gameWindow the game window
     */
    public InstructionScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating instruction scene");
    }

    /**
     * Initialise this scene. Called after creation
     */
    @Override
    public void initialise() {
        logger.info("Initialising instructions");
        scene.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ESCAPE)) {
                gameWindow.startMenu();
            }
        });
    }

    /**
     * Build the layout of the scene
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var instructionPane = new BorderPane();
        instructionPane.setMaxWidth(gameWindow.getWidth());
        instructionPane.setMaxHeight(gameWindow.getHeight());
        instructionPane.getStyleClass().add("instruction-background");
        root.getChildren().add(instructionPane);

        //Title
        var top = new VBox();
        top.setAlignment(Pos.CENTER);
        instructionPane.setTop(top);
        var instructionsTitle = new Text("Instructions");
        instructionsTitle.getStyleClass().add("title");
        top.getChildren().add(instructionsTitle);

        //Text description
        var description = new Text("A fast-paced block placement game where you have a 5x5 grid, and you must place pieces in that grid, scoring points by clearing lines, " + "\n" + " horizontally or vertically.");
        description.getStyleClass().add("instructions");
        description.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        top.getChildren().add(description);

        //Content of Instructions
        var middle = new VBox();
        middle.setAlignment(Pos.CENTER);
        instructionPane.setCenter(middle);

        //Default pieces text
        var piecesText = new Text("Default Pieces");
        piecesText.getStyleClass().add("heading");

        //Creating the grids of the 15 default pieces
        var pieces = new GridPane();
        pieces.setHgap(10);
        pieces.setVgap(10);
        pieces.setAlignment(Pos.CENTER);

        //Image
        Image instructions = new Image("C:\\Users\\neimad\\Documents\\COMP1206cwk\\coursework\\src\\main\\resources\\images\\Instructions.png");
        ImageView instructionView = new ImageView(instructions);
        instructionView.setPreserveRatio(true);
        instructionView.setFitHeight(360);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 5; j++) {
                //Creates new pieceboard
                var pb = new PieceBoard(40, 40);
                //Retreives created GamePiece from 15 default pieces which has the 3x3 grid containing where blocks are placed
                GamePiece gp = GamePiece.createPiece(j + (i * 5));
                //Places GamePiece into a grid
                pb.setPiece(gp);
                //Adds grid onto one of the GridPanes
                pieces.add(pb, j, i);
            }
        }

        middle.getChildren().addAll(instructionView,piecesText,pieces);
    }

}
