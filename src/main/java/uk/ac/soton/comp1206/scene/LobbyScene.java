package uk.ac.soton.comp1206.scene;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.Multimedia;

import java.util.Timer;
import java.util.TimerTask;

public class LobbyScene extends BaseScene{
    private static final Logger logger = LogManager.getLogger(ScoreScene.class);
    /**
     * Communicator variable
     */
    Communicator com;
    /**
     * Timer variable to keep requesting channels
     */
    Timer timer;
    /**
     * Checks if user is host, if they are they can start the game
     */
    Boolean isHost = false;

    /**
     * Stores list of channels
     */
    private ObservableList<String> channelList = FXCollections.observableArrayList();
    private SimpleStringProperty currentChannel = new SimpleStringProperty("");


    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public LobbyScene(GameWindow gameWindow) {
        super(gameWindow);
        com = gameWindow.getCommunicator();
        setCommunicator();
        requestCurrentChannels();
    }

    /**
     * Initialise this scene. Called after creation
     */
    @Override
    public void initialise() {
//        Multimedia.playMusic();
        scene.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ESCAPE)) {
                gameWindow.startMenu();
            }
        });
    }

    public void requestCurrentChannels() {
        logger.info("Requesting current channels");
        timer = new Timer();
        //Every 3 seconds it calls timer task (run)
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                requestChannels();
            }
        }, 0,3000);
    }

    /**
     * Sets communicator for lobby
     */
    public void setCommunicator() {
        com.addListener(this::incomingMessage);
    }

    /**
     * Handles a message received from the server
     * @param message message from server
     */
    public void incomingMessage(String message) {
        if (message.startsWith("QUIT")) {
            logger.info("Forced to quit");
            handleError();
        }
    }

    /**
     * Requests a list of available channels from server
     */
    public void requestChannels() {
        com.addListener(message -> {
            if (message.startsWith("CHANNELS")) {
                logger.info("CHANNELS have been requested");
                handleChannel(message);
            }
        });
        //Gets list of all channels
        com.send("LIST");
    }

    /**
     * Sends a join request for another channel
     * @param channelName Name of the channel to be joined
     */
    public void joinRequest(String channelName) {
        com.addListener(message -> {
            if (message.startsWith("JOIN")) {
                logger.info("Joining another channel");
                String channelNameReceived = message.substring(9);
                currentChannel.set(channelNameReceived);
            }
        });
        if (!channelName.isEmpty()) {
            logger.info("Joining channel {}" + channelName);
            com.send("JOIN " + channelName);
        }
    }

    /**
     * Request to join a given channel
     * @param channelName Name of the channel being joined
     */
    public void joinChannel(String channelName) {
        logger.info("Channel joined: " + channelName);
        channelList.add(channelName);
        currentChannel.set(channelName);
    }

    /**
     * Creates a channel
     * @param channelName Name of the channel to be created
     */
    public void createChannel(String channelName) {
        com.addListener(message -> {
            if (message.startsWith("HOST")) {
                isHost = true;
            }
        });
        if (!channelName.isEmpty()) {
            com.send("CREATE " + channelName);
        }
    }

    public void sendMessage(String message) {
        com.addListener(msg -> {

        });
    }

    /**
     * Deals with channels that have come from server
     */
    public void handleChannel(String message) {
    }

    public void handleError() {

    }

    /**
     * Build the layout of the scene
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("menu-background");
        root.getChildren().add(challengePane);
    }


}
