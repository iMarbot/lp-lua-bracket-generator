package bracketgen;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class BracketGeneratorGUI extends Application {

	int rounds = 0;
	boolean DE = false;
	String game = "";

	public static void main(String[] args) {
		launch();
	}

	@Override
	public void start(Stage primaryStage) {
		VBox wrap = new VBox();
		wrap.setSpacing(10);
		wrap.setPadding(new Insets(10));
		BorderPane header_wrap = new BorderPane();
		HBox header = new HBox();
		header.setAlignment(Pos.CENTER_LEFT);
		header.setSpacing(10);
		Button apply = new Button("Apply");
		TextField gameField = new TextField();
		gameField.setMinWidth(200);
		gameField.setPromptText("Game (e.g. \"rocketleague\")");
		Button copyToClipboard = new Button("Copy to Clipboard");
		copyToClipboard.setDisable(true);
		Spinner<Integer> spinner = new Spinner<>(1, 10, 1);
		CheckBox deBox = new CheckBox();
		header.getChildren().addAll(new Label("Number of rounds:"), spinner, new Label("Double Elimination"), deBox,
				gameField, apply, copyToClipboard);
		ScrollPane content = new ScrollPane();
		content.setPadding(new Insets(5, 0, 5, 0));
		content.setStyle("-fx-background-color:transparent");
		header_wrap.setLeft(header);
		wrap.getChildren().addAll(header_wrap, content);

		// handle apply
		apply.setOnAction(e -> {
			if (gameField.getText().equals("")) {
				Timeline t = new Timeline(60,
						new KeyFrame(Duration.seconds(0), new KeyValue(apply.textProperty(), "Please set a game first"),
								new KeyValue(apply.disableProperty(), true)),
						new KeyFrame(Duration.seconds(0.5), new KeyValue(apply.textProperty(), "Apply"),
								new KeyValue(apply.disableProperty(), false)));
				t.play();
			} else {
				rounds = spinner.getValue();
				DE = deBox.isSelected();
				game = gameField.getText();
				copyToClipboard.setDisable(false);
				content.setContent(LuaBracketRound.newBracket(rounds, DE, game));
			}
		});

		// handle copy to Clipboard
		copyToClipboard.setOnAction(e -> {
			StringBuilder s = new StringBuilder();
			int[] offset = new int[LuaBracketRound.ROUNDS_UPPER.size()];
			int i = 0;
			for (LuaBracketRound r : LuaBracketRound.ROUNDS_UPPER) {
				s.append(r.toWikiCode(0));
				offset[i] = r.matchCount;
				i++;
			}
			i = 0;
			for (LuaBracketRound r : LuaBracketRound.ROUNDS_LOWER) {
				if (i >= LuaBracketRound.ROUNDS_UPPER.size()) {
					break;
				}
				s.append(r.toWikiCode(i < offset.length ? offset[i] : 0));
				i++;
			}
			if (!LuaBracketRound.ROUNDS_LOWER.isEmpty()) {
				s.append((LuaBracketRound.ROUNDS_LOWER.get(LuaBracketRound.ROUNDS_LOWER.size() - 1))
						.toWikiCode(i + 1));
			} else {
				s.append("{{#invoke:Bracket|BracketEnd}}\n");
			}
			StringSelection stringSelection = new StringSelection(s.toString());
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(stringSelection, null);
		});

		//initialize stage and scene
		primaryStage.setTitle("Lua Bracket Helper v0.3");

		Scene scene = new Scene(wrap, 1280, 720);
		scene.getStylesheets().add(getClass().getResource("bracketstyle.css").toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("bracketgen.png")));
		primaryStage.show();

		header_wrap.setPadding(new Insets(30, 0, 0, 0));
		content.setPrefHeight(10000);
		content.setPannable(true);
		wrap.setPrefHeight(10000);
		wrap.setPrefWidth(10000);
		wrap.setStyle("-fx-background-color: -fx-background;");
	}

}
