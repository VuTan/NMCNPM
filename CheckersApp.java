package com.game.app;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CheckersApp extends Application {

    private Board board = Board.getInstance();
    private Scene scene;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent content = board.createContent();
        scene = new Scene(content);
        board.setScene(scene);
        board.setCheckersApp(this);
        primaryStage.setTitle("CheckersApp");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

}
