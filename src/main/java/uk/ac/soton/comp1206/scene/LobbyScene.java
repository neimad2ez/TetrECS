package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.Multimedia;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Lobby scene which displays the lobby of a multiplayer game
 */
public class LobbyScene extends BaseScene{
    private static final Logger logger = LogManager.getLogger(ScoreScene.class);
    /**
     * Nickname for user
     */
    public static final StringProperty nickname = new SimpleStringProperty();
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
     * VBox for joining channels
     */
    VBox channels;
    /**
     * VBox for the chat area
     */
    VBox chatBox;

    /**
     * Stores list of channels
     */
    private ObservableList<String> channelList = FXCollections.observableArrayList();
    /**
     * Name of current channel user is in
     */
    private SimpleStringProperty currentChannel = new SimpleStringProperty("");
    /**
     * List of users in the current channel, can be replaced if user changes nickname
     */
    private ObservableList<String> userList = FXCollections.observableArrayList();
    /**
     * Title of the current channel user is in
     */
    Text joinedChannel;
    /**
     * VBox for current lobby

     */
    VBox currentLobby;
    /**
     * VBox for right side of the screen which contains all the

     */
    VBox right;
    /**
     * Textfield to send messages across server
     */
    TextField sendChat;
    /**
     * TextArea where chat is stored
     */
    TextArea chat;
    /**
     * Button for leaving channel
     */
    Text leaveChannel;
    /**
     * Bottom box that contains the start button
     */
    HBox bottomBox;
    /**
     * Count ensuring theres only 1 chat box
     */
    int count = 0;


    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public LobbyScene(GameWindow gameWindow) {
        super(gameWindow);
        com = gameWindow.getCommunicator();
//        setCommunicator();
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

    /**
     * Requests current channels from the server
     */
    public void requestCurrentChannels() {
        logger.info("Requesting current channels");
        timer = new Timer();
        requestChannels();
        error();
        //Every 3 seconds it calls timer task (run)
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                com.send("LIST");
                logger.info(channelList.toString());
                gameStart();
            }
        }, 0,2000);
    }

    /**
     * Handles error message received from the server
     */
    public void error() {
        com.addListener(message -> {
            logger.info("Error message: " + message);
            if (currentChannel.get() != "") {
                if (message.startsWith("ERROR")) {
                    logger.info("Forced to quit");
                    Platform.runLater(() -> handleError(message));
                }
            }
        });
    }

    /**
     * Requests a list of available channels from server
     */
    public void requestChannels() {
        com.addListener(message -> {
            if (message.startsWith("CHANNELS")) {
                logger.info("CHANNELS have been requested");
                channelList.clear();
                handleChannel(message.substring(9));

                Platform.runLater(() -> channelHandler());
                Platform.runLater(() -> setUpChannel());
                Platform.runLater(() -> handleUsers());
                Platform.runLater(() -> setUpChat());
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
                String channel = message.substring(5);
                logger.info(channel);
                joinChannel(channel);
                handleUsers();
                handleNick();
            }
        });
        if (!channelName.isEmpty()) {
            logger.info("Joining channel " + channelName);
            com.send("JOIN " + channelName);
        }
    }

    /**
     * Game started by host
     */
    public void gameStart() {
        com.addListener(message -> {
            if (message.startsWith("START")) {
                Multimedia.stopMusic();
                gameWindow.startMultiplayerScene();
            }
        });
    }

    /**
     * Handles users in the current channel, adds it to userList
     */
    public void handleUsers() {
        com.addListener(message -> {
            if (message.startsWith("USERS")) {
                userList.clear();
                message = message.substring(6);
                String[] users = message.split("\n");
                userList.addAll(users);
            }
        });
        com.send("USERS");
    }

    /**
     * Request to join a given channel
     * @param channelName Name of the channel being joined
     */
    public void joinChannel(String channelName) {
        logger.info("Channel joined: " + channelName);
        channelList.add(channelName);
        Platform.runLater(() -> currentChannel.set(channelName));

    }

    /**
     * Creates a channel
     * @param channelName Name of the channel to be created
     */
    public void createChannel(String channelName) {
        logger.info("New channel has been created called: " + channelName);
        com.addListener(message -> {
            if (message.startsWith("HOST")) {
                isHost = true;
            }
        });
        if (!channelName.isEmpty()) {
            com.send("CREATE " + channelName);
            Platform.runLater(() -> joinChannel(channelName));
        }
    }

    /**
     * Handles nickname changes from other users
     */
    public void handleNick() {
        com.addListener(message -> {
            if (message.startsWith("NICK")) {
                message = message.substring(5);
                String[] nicks = message.split(":");
                int nickCount = 1;
                if (nicks.length > 1) {
                    for (String s: userList) {
                        if (s == nicks[0]) {
                            logger.info(nickCount + " and name is: " + nicks[1]);
                            userList.set(nickCount, nicks[1]);
                        } else {
                            nickCount++;
                        }
                    }
                }

            }
        });
    }

    /**
     * Handles UI for when user joins a channel
     */
    public void setUpChannel() {
        currentLobby.getChildren().clear();
        right.getChildren().clear();
        if (currentChannel.get() != "") {
            bottomBox.getChildren().clear();

            right.getChildren().addAll(currentLobby, chatBox);

            //Channel name
            joinedChannel = new Text(currentChannel.get());
            joinedChannel.getStyleClass().add("title");

            //User list
            var userBox = new VBox();
            userBox.getChildren().clear();
            for (String s: userList) {
                var users = new Text(s);
                users.getStyleClass().add("heading");
                userBox.getChildren().add(users);
            }

            //Leave channel
            leaveChannel = new Text("Leave Channel");
            leaveChannel.getStyleClass().add("heading");
            leaveChannel.setOnMouseClicked(event -> {
            leaveChannel();
            });

            //Start game
            Text startGame = new Text("Start");
            startGame.getStyleClass().add("menuItem");
            startGame.setOnMouseClicked(event -> {
                if (isHost) {
                    com.send("START");
                    Multimedia.stopMusic();
                    gameWindow.startMultiplayerScene();
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("ERROR - Not host");
                    alert.setHeaderText("You are not the host of this channel");
                    alert.setContentText("Only the host can start the game");
                    alert.showAndWait();
                }
            });

            bottomBox.getChildren().add(startGame);
            currentLobby.getChildren().addAll(joinedChannel, userBox);
            right.getChildren().add(leaveChannel);

        }
    }

    /**
     * Sets up chat for user
     */
    public void setUpChat() {
        if (currentChannel.get() != "") {
            if (count == 0) {
                count = count + 1;
                chat = new TextArea();
                chat.setEditable(false);
                sendChat = new TextField();
                sendChat.setPromptText("Send chat message");
                sendChat.setOnAction(event -> sendMessage(sendChat.getText()));
                chatBox.getChildren().addAll(chat, sendChat);
            }
        }
    }

    /**
     * Send message on chat
     * @param message message to be sent on chat
     */
    public void sendMessage(String message) {
        if (!message.isEmpty()) {
            com.send("MSG " + message);
        }
        sendChat.clear();
    }

    /**
     * Leaving channel user is currently in
     */
    public void leaveChannel() {
        com.addListener(message -> {
            if (message.startsWith("PARTED")) {
                currentChannel.set("");
            }
        });

        //Removing all the elements when you leave current lobby
        chatBox.getChildren().clear();
        count = 0;

        //Remove all the elements when you leave current lobby
        right.getChildren().clear();

        bottomBox.getChildren().clear();

        com.send("PART");
    }

    /**
     * Handles UI for available channels
     */
    public void channelHandler() {
        channels.getChildren().clear();
        //Available Channels text
        Text availableChannels = new Text("Available Channels");
        availableChannels.getStyleClass().add("heading");
        channels.getChildren().add(availableChannels);
        for (String s : channelList) {
            //Each channelName has their own final variable which can tell difference between texts for joinRequest
            final String channelName = s;
            var channel = new Text(channelName);
            channel.getStyleClass().add("heading");
            channels.getChildren().add(channel);

            channel.setOnMouseClicked(event -> {
                logger.info("Joined game " + channelName);
                joinRequest(channelName);
            });
        }
    }

    /**
     * Deals with channels that have come from server
     */
    public void handleChannel(String message) {
        if (message.contains("\n")) {
            String[] messages = message.split("\n");
            for(String s: messages) {
                channelList.add(s);
            }
        } else {
            channelList.add(message);
        }
    }

    /**
     * Handles error messages
     * @param message error message
     */
    public void handleError(String message) {
        logger.info("ERROR IS: " + message);
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setContentText(message);
        errorAlert.showAndWait();
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

        //Borderpane
        var borderPane = new BorderPane();
        challengePane.getChildren().add(borderPane);

        //Top box
        var top = new HBox();
        top.setSpacing(105);
        borderPane.setTop(top);

        //Top left box
        var topLeft = new VBox();
        topLeft.setAlignment(Pos.TOP_LEFT);
        topLeft.setSpacing(50);
        top.getChildren().add(topLeft);

        //Available channels box
        channels = new VBox();
        channels.setAlignment(Pos.TOP_LEFT);
        channels.setMaxHeight(100);
        topLeft.getChildren().add(channels);

        //Left top box
        var left = new VBox();
        left.setSpacing(10);
        left.setAlignment(Pos.CENTER);
        topLeft.getChildren().add(left);

        //Create channel box
        var createBox = new VBox();
        createBox.setSpacing(10);
        createBox.setAlignment(Pos.CENTER);

        //Set nickname box
        var nicknameBox = new VBox();
        nicknameBox.setSpacing(10);
        nicknameBox.setAlignment(Pos.CENTER);
        left.getChildren().addAll(createBox, nicknameBox);

        //Set nickname
        Text setNickname = new Text("Set nickname");
        setNickname.getStyleClass().add("heading");
        AtomicBoolean nicknameAdded = new AtomicBoolean(false);
        setNickname.setOnMouseClicked(event -> {
            if (!nicknameAdded.get()) {
                TextField enteredNickname = new TextField();
                nicknameBox.getChildren().add(enteredNickname);
                nicknameAdded.set(true);
                enteredNickname.setOnAction(e -> {
                    String nick = enteredNickname.getText();
                    nickname.set(nick);
                    logger.info("Nickname is: " + nickname);
                    com.send("NICK " + nick);
                });
            } else {
                nicknameAdded.set(false);
                nicknameBox.getChildren().clear();
                nicknameBox.getChildren().add(setNickname);
            }
        });
        nicknameBox.getChildren().add(setNickname);
;
        //Create channel
        Text createChannel = new Text("Create Channel");
        createChannel.getStyleClass().add("heading");
        AtomicBoolean createAdded = new AtomicBoolean(false);
        createChannel.setOnMouseClicked(event -> {
            if (!createAdded.get()) {
                TextField create = new TextField();
                createBox.getChildren().add(create);
                createAdded.set(true);
                create.setOnAction(e -> createChannel(create.getText()));
            } else {
                createAdded.set(false);
                createBox.getChildren().clear();
                createBox.getChildren().add(createChannel);
            }
        });
        createBox.getChildren().add(createChannel);

        right = new VBox();
        right.setAlignment(Pos.CENTER_RIGHT);
        top.getChildren().add(right);

        //Lobby
        currentLobby = new VBox();
        right.getChildren().add(currentLobby);

        //Chat box
        chatBox = new VBox();
        chatBox.setAlignment(Pos.CENTER);
        right.getChildren().add(chatBox);

        channelHandler();
        setUpChannel();
        setUpChat();
        requestCurrentChannels();

        //Listens for messages
        com.addListener(msg -> {
            if (msg.startsWith("MSG")) {
                chat.appendText(msg.substring(4) + "\n");
            }
        });

        //Bottom box to start game
        bottomBox = new HBox();
        bottomBox.setAlignment(Pos.CENTER);
        borderPane.setBottom(bottomBox);
    }


}
