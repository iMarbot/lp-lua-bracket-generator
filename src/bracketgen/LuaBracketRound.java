package bracketgen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.VLineTo;

class LuaBracketRound extends VBox {

	final static String[] types = { "combination", "dropdown", "passthrough", "singlepassthrough", "qualified",
			"blank" };
	ComboBox<String> typesBox;
	StackPane content_wrap;
	VBox content;
	String type = "";
	String name = "";
	static String game = "";
	boolean isInLowerBracket = false;
	int matchCount = 1;
	SimpleIntegerProperty this_maxMatchNr = new SimpleIntegerProperty(2);
	SimpleStringProperty roundNameProperty = new SimpleStringProperty();
	
	// visual parameters
	final static int MATCH_HEIGHT = 20;
	final static int MATCH_WIDTH = 150;
	final static int CON_WIDTH = 20;
	final static int MATCH_GAP = 20;
	
	// parameters for setting the correct height of a round
	final static SimpleIntegerProperty MAX_UPPER_HEIGHT = new SimpleIntegerProperty(0);
	final static SimpleIntegerProperty MAX_LOWER_HEIGHT = new SimpleIntegerProperty(0);
	final static SimpleIntegerProperty MAX_UPPER_MATCHCOUNT = new SimpleIntegerProperty(0);
	final static SimpleIntegerProperty MAX_LOWER_MATCHCOUNT = new SimpleIntegerProperty(0);
	final SimpleIntegerProperty wantedHeight = new SimpleIntegerProperty(0);
	static SimpleIntegerProperty currentMaxHeight = null;
	static int qualfiedTeamsCount = 0;

	// list links
	final static ArrayList<LuaBracketRound> ROUNDS_UPPER = new ArrayList<LuaBracketRound>();
	final static ArrayList<LuaBracketRound> ROUNDS_LOWER = new ArrayList<LuaBracketRound>();
	final static ArrayList<HashMap<String, SimpleIntegerProperty>> ROUNDS_GAMES = new ArrayList<HashMap<String, SimpleIntegerProperty>>();
	final static ArrayList<ArrayList<String>> ROUNDS_KEYS_UPPER = new ArrayList<ArrayList<String>>();
	final static ArrayList<ArrayList<String>> ROUNDS_KEYS_LOWER = new ArrayList<ArrayList<String>>();
	ArrayList<LuaBracketRound> this_list = null;

	LuaBracketRound() {
		super();
	}

	public LuaBracketRound(boolean isInLowerBracket) {
		// initialize javafx object
		super();
		content = new VBox();
		content.setAlignment(Pos.CENTER_RIGHT);
		content_wrap = new StackPane();
		content_wrap.setAlignment(Pos.CENTER_RIGHT);
		content_wrap.getChildren().add(content);
		this.setPrefWidth(MATCH_WIDTH);
		this.setAlignment(Pos.CENTER_RIGHT);
		
		// add round to upper or lower bracket
		this.isInLowerBracket = isInLowerBracket;
		if (isInLowerBracket) {
			content_wrap.minHeightProperty().bind(MAX_LOWER_HEIGHT);
			wantedHeight.addListener((cl, o, n) -> {
				if (n.intValue() > MAX_LOWER_HEIGHT.intValue()) {
					MAX_LOWER_HEIGHT.set(n.intValue());
					currentMaxHeight = new SimpleIntegerProperty(n.intValue());
					currentMaxHeight.bind(wantedHeight);
					currentMaxHeight.addListener((cl1, o1, n1) -> {
						MAX_LOWER_HEIGHT.set(n1.intValue());
						MAX_LOWER_MATCHCOUNT.set(matchCount);
					});
					MAX_LOWER_MATCHCOUNT.set(matchCount);
				}
			});
			this_list = ROUNDS_LOWER;
			this_maxMatchNr = MAX_LOWER_MATCHCOUNT;
		} else {
			content_wrap.minHeightProperty().bind(MAX_UPPER_HEIGHT);
			wantedHeight.addListener((cl, o, n) -> {
				if (n.intValue() > MAX_UPPER_HEIGHT.intValue()) {
					MAX_UPPER_HEIGHT.set(n.intValue());
					currentMaxHeight = new SimpleIntegerProperty(n.intValue());
					currentMaxHeight.bind(wantedHeight);
					currentMaxHeight.addListener((cl1, o1, n1) -> {
						MAX_UPPER_HEIGHT.set(n1.intValue());
						MAX_UPPER_MATCHCOUNT.set(matchCount);
					});
					MAX_UPPER_MATCHCOUNT.set(matchCount);
				}
			});
			this_list = ROUNDS_UPPER;
			this_maxMatchNr = MAX_UPPER_MATCHCOUNT;
		}
		
		// initialization stuff
		this_list.add(this);

		// Type and name
		HBox typesWrap = new HBox();
		typesWrap.setAlignment(Pos.CENTER_RIGHT);
		typesWrap.setSpacing(3);
		typesBox = new ComboBox<String>();
		typesBox.getItems().addAll(Arrays.asList(types));
		typesBox.valueProperty().addListener((cl, o, n) -> {
			type = n;
			changeContentTo(type, matchCount, name);
		});
		typesBox.setValue("combination");
		typesBox.setMaxWidth(MATCH_WIDTH - CON_WIDTH);
		typesBox.setMinWidth(MATCH_WIDTH - CON_WIDTH);
		TextField nameField = new TextField();
		nameField.textProperty().addListener((cl, o, n) -> {
			name = n;
			changeContentTo(type, matchCount, name);
		});
		getChildren().addAll(typesBox);
		typesWrap.setMaxWidth(MATCH_WIDTH);

		// Number of rounds
		HBox infoWrap = new HBox();
		infoWrap.setAlignment(Pos.CENTER_RIGHT);
		infoWrap.setSpacing(3);
		Spinner<Integer> rnrSpinner = new Spinner<Integer>(1, 200, 1);
		rnrSpinner.setValueFactory(new SpinnerValueFactory<Integer>() {
			@Override
			public void increment(int steps) {
				int value = this.getValue();
				value = (int) Math.pow(2, steps) * value;
				if (value == 0)
					value = 1;
				this.setValue(value);
			}

			@Override
			public void decrement(int steps) {
				int value = this.getValue();
				value = (int) (1 / Math.pow(2, steps) * value);
				if (value == 0)
					value = 1;
				this.setValue(value);
			}
		});
		rnrSpinner.getValueFactory().setValue(1);
		rnrSpinner.valueProperty().addListener((cl, o, n) -> {
			matchCount = n;
			changeContentTo(type, matchCount, name);
		});
		rnrSpinner.setMaxWidth(MATCH_WIDTH - CON_WIDTH);
		getChildren().addAll(rnrSpinner);

		// round name
		TextField roundNameField = new TextField();
		roundNameField.setMaxWidth(MATCH_WIDTH - CON_WIDTH);
		roundNameField.setText("Round title");
		roundNameField.setPromptText("Round title");
		roundNameField.focusedProperty().addListener((cl, o, n) -> {
			if (n) {
				if (roundNameField.getText().equals("Round title")) {
					roundNameField.setText("");
				}
			} else {
				if (roundNameField.getText().equals("")) {
					roundNameField.setText("Round title");
				}
			}
		});
		roundNameProperty.bind(roundNameField.textProperty());
		getChildren().addAll(roundNameField);

		this.getChildren().add(content_wrap);
		this.setSpacing(5);
	}

	private void update() {
		changeContentTo(type, matchCount, name, false);
	}

	public void changeContentTo(String type, int nr, String name) {
		changeContentTo(type, nr, name, true);
	}

	public void changeContentTo(String type, int nr, String name, boolean updateSiblings) {
		// apply new values (for method calls from outside)
		this.type = type;
		this.matchCount = nr;
		this.name = name;
		
		// find the index of the round
		int this_round = 0;
		for (int i = 0; i < this_list.size(); i++) {
			if (this_list.get(i) == this) {
				this_round = i;
			}
		}
		String roundkey = "R" + (this_round + 1);
		boolean firstround = this_round == 0;
		if (firstround && !isInLowerBracket) {
			qualfiedTeamsCount = 0;
		}

		ArrayList<String> teams = isInLowerBracket ? ROUNDS_KEYS_LOWER.get(this_round) : ROUNDS_KEYS_UPPER.get(this_round);
		if (!isInLowerBracket) {
			ROUNDS_GAMES.get(this_round).get("W").set(0);
			ROUNDS_GAMES.get(this_round).get("D").set(0);
		}
		teams.clear();

		content.getChildren().clear();
		wantedHeight.set(2 * (nr + 1) * MATCH_HEIGHT + (nr - 1) * MATCH_GAP);
		// combination
		if (type.equals(types[0])) {
			int gap = this.this_maxMatchNr.get() > nr
					? (int) Math.round((this_maxMatchNr.get() / nr - 2) * 1.5 * MATCH_HEIGHT)
					: 0;
			content.setSpacing((this_maxMatchNr.get() / nr - 1) * MATCH_GAP
					+ (this_maxMatchNr.get() / nr - 1) * 2 * MATCH_HEIGHT - gap);
			boolean doublepass = false;
			for (int i = 1; i < this_list.size(); i++) {
				if (this_list.get(i) == this) {
					if (this_list.get(i - 1).type.equals(types[2])) {
						doublepass = true;
					}
				}
			}
			SimpleIntegerProperty g = null;
			if (firstround) {
				g = ROUNDS_GAMES.get(this_round).get("D");
			} else {
				g = ROUNDS_GAMES.get(this_round).get("W");
			}
			for (int i = 0; i < nr; i++) {
				g.set(g.get() + 1);
				String team1 = roundkey + (firstround ? "D" : "W") + g.get();
				g.set(g.get() + 1);
				String team2 = roundkey + (firstround ? "D" : "W") + g.get();
				teams.add(team1);
				teams.add(team2);
				content.getChildren().add(TeamField.makeMatch(team1, team2, firstround, false, gap, doublepass));
			}
		}
		// dropdown
		else if (type.equals(types[1])) {
			int gap = this.this_maxMatchNr.get() > nr
					? (int) Math.round((this_maxMatchNr.get() / nr - 2) * 1.5 * MATCH_HEIGHT)
					: 0;
			content.setSpacing((this_maxMatchNr.get() / nr - 1) * MATCH_GAP
					+ (this_maxMatchNr.get() / nr - 1) * 2 * MATCH_HEIGHT - gap);
			SimpleIntegerProperty d = ROUNDS_GAMES.get(this_round).get("D");
			SimpleIntegerProperty w = ROUNDS_GAMES.get(this_round).get("W");
			for (int i = 0; i < nr; i++) {
				d.set(d.get() + 1);
				String team1 = roundkey + "D" + d.get();
				w.set(w.get() + 1);
				String team2 = roundkey + "W" + w.get();
				teams.add(team1);
				teams.add(team2);
				content.getChildren().add(TeamField.makeMatch(team1, team2, firstround, true, gap, false));
			}
		}
		// passthrough
		else if (type.equals(types[2])) {
			int gap = this.this_maxMatchNr.get() > nr
					? (int) Math.round((this_maxMatchNr.get() / nr - 2) * 1.5 * MATCH_HEIGHT)
					: 0;
			content.setSpacing((this_maxMatchNr.get() / nr - 1) * MATCH_GAP
					+ (this_maxMatchNr.get() / nr - 1) * 2 * MATCH_HEIGHT - gap);
			for (int i = 0; i < nr; i++) {
				content.getChildren().add(Passthrough.make(firstround, false, gap));
			}
		}
		// singlepassthrough
		else if (type.equals(types[3])) {
			int gap = this.this_maxMatchNr.get() > nr
					? (int) Math.round((this_maxMatchNr.get() / nr - 2) * 1.5 * MATCH_HEIGHT)
					: 0;
			content.setSpacing((this_maxMatchNr.get() / nr - 1) * MATCH_GAP
					+ (this_maxMatchNr.get() / nr - 1) * 2 * MATCH_HEIGHT - gap);
			for (int i = 0; i < nr; i++) {
				content.getChildren().add(Passthrough.make(firstround, true, gap));
			}
		}
		// qualified
		else if (type.equals(types[4])) {
			content.setSpacing(
					(this_maxMatchNr.get() / nr - 1) * MATCH_GAP + (this_maxMatchNr.get() / nr) * 2 * MATCH_HEIGHT);
			for (int i = 0; i < nr; i++) {
				qualfiedTeamsCount++;
				teams.add("Q" + qualfiedTeamsCount);
				content.getChildren().add(TeamField.makeQualified(qualfiedTeamsCount));
			}
		}

		if (updateSiblings) {
			for (LuaBracketRound r : ROUNDS_UPPER) {
				r.update();
			}
			for (LuaBracketRound r : ROUNDS_LOWER) {
				r.update();
			}
		}
	}

	public static GridPane newBracket(int rounds, boolean DE, String game) {
		LuaBracketRound.game = game;
		ROUNDS_LOWER.clear();
		ROUNDS_UPPER.clear();
		ROUNDS_GAMES.clear();
		MAX_LOWER_HEIGHT.set(0);
		MAX_UPPER_HEIGHT.set(0);
		MAX_LOWER_MATCHCOUNT.set(0);
		MAX_UPPER_MATCHCOUNT.set(0);
		GridPane grid = new GridPane();
		grid.setVgap(10);
		for (int i = 0; i < rounds; i++) {
			HashMap<String, SimpleIntegerProperty> games = new HashMap<String, SimpleIntegerProperty>();
			games.put("D", new SimpleIntegerProperty(0));
			games.put("W", new SimpleIntegerProperty(0));
			games.put("Q", new SimpleIntegerProperty(0));
			ROUNDS_GAMES.add(games);
			ROUNDS_KEYS_UPPER.add(new ArrayList<String>());
			grid.addRow(0, new LuaBracketRound(false));
			if (DE) {
				ROUNDS_KEYS_LOWER.add(new ArrayList<String>());
				grid.addRow(1, new LuaBracketRound(true));
			}
		}
		if (DE) {
			int column = grid.getChildren().size() / 2 + 1;
			grid.add(new LuaBracketFinal(), column, 0, 1, 2);
		}
		grid.setAlignment(Pos.TOP_CENTER);
		return grid;
	}

	static class Gap extends Pane {
		Gap(int gap) {
			super();
			this.setMaxHeight(gap);
			this.setMinHeight(gap);
		}
	}

	static class Passthrough {
		public static GridPane make(boolean firstround, boolean single, int gap) {
			GridPane pathWrap = new GridPane();
			pathWrap.setMinWidth(MATCH_WIDTH);
			pathWrap.setMaxWidth(MATCH_WIDTH);
			pathWrap.setAlignment(Pos.BASELINE_RIGHT);
			pathWrap.setMinHeight(MATCH_HEIGHT * 3 + gap);
			pathWrap.setMaxHeight(MATCH_HEIGHT * 3 + gap);
			pathWrap.setVgap(MATCH_HEIGHT - 2);
			Path p1 = new Path();
			Path p2 = new Path();
			p1.setStyle("-fx-border-color: black");
			if (!single) {
				p1.getElements().addAll(new MoveTo(0, 0), new HLineTo(10), new VLineTo(MATCH_HEIGHT * 1 + gap / 2),
						new HLineTo(MATCH_WIDTH));
				p2.getElements().addAll(new MoveTo(0, MATCH_HEIGHT * 3 + gap), new HLineTo(10),
						new VLineTo(MATCH_HEIGHT * 2 + gap / 2), new HLineTo(MATCH_WIDTH));
			} else {
				p1.getElements().addAll(new MoveTo(0, 0), new HLineTo(MATCH_WIDTH));
				p1.setTranslateY(MATCH_HEIGHT * 1.5 + gap / 2);
			}
			pathWrap.addColumn(0, p1, p2);
			if (firstround) {
				pathWrap.setVisible(false);
			}

			return pathWrap;
		}
	}

	static class TeamField extends TextField {

		TeamField(String s, int width, int height) {
			super(s);
			this.setMaxHeight(height);
			this.setMinHeight(height);
			this.setMaxWidth(width);
			this.setMinWidth(width);
		}

		static VBox makeMatch(String team1, String team2) {
			VBox wrap = new VBox();
			wrap.getChildren().addAll(new TeamField(team1, MATCH_WIDTH - CON_WIDTH, MATCH_HEIGHT),
					new TeamField(team2, MATCH_WIDTH - CON_WIDTH, MATCH_HEIGHT));
			return wrap;
		}

		static HBox makeMatch(String team1, String team2, boolean firstround, boolean dropdown, int gap,
				boolean doublepass) {
			HBox wrap = new HBox();
			wrap.setAlignment(Pos.CENTER);
			VBox teams = makeMatch(team1, team2);
			teams.setMaxHeight(MATCH_HEIGHT * 2);

			GridPane pathWrap = new GridPane();
			pathWrap.setMinWidth(CON_WIDTH);
			pathWrap.setMaxWidth(CON_WIDTH);
			pathWrap.setAlignment(Pos.BASELINE_RIGHT);
			pathWrap.setMinHeight(MATCH_HEIGHT * 3 + gap);
			pathWrap.setMaxHeight(MATCH_HEIGHT * 3 + gap);
			pathWrap.setVgap(MATCH_HEIGHT - 2);
			Path p1 = new Path();
			Path p2 = new Path();
			p1.setStyle("-fx-border-color: black");
			if (!dropdown && !doublepass) {
				p1.getElements().addAll(new MoveTo(0, 0), new HLineTo(10), new VLineTo(MATCH_HEIGHT * 1 + gap / 2),
						new HLineTo(20));
				p2.getElements().addAll(new MoveTo(0, MATCH_HEIGHT * 3 + gap), new HLineTo(10),
						new VLineTo(MATCH_HEIGHT * 2 + gap / 2), new HLineTo(20));
			} else if (!dropdown && doublepass) {
				p1.getElements().addAll(new MoveTo(0, 0), new HLineTo(20));
				p1.setTranslateY(MATCH_HEIGHT * 1 + gap / 2);
				p2.getElements().addAll(new MoveTo(0, 0), new HLineTo(20));
				p2.setTranslateY(MATCH_HEIGHT * 1 + gap / 2);
			} else {
				p1.getElements().addAll(new MoveTo(0, MATCH_HEIGHT * 1), new VLineTo(MATCH_HEIGHT * 1.5),
						new HLineTo(20));
				p1.setTranslateX(15);
				p1.setTranslateY(MATCH_HEIGHT * 0.5 + gap / 2);
				p2.getElements().addAll(new MoveTo(0, MATCH_HEIGHT * 1.5), new HLineTo(10),
						new VLineTo(MATCH_HEIGHT * 2), new HLineTo(20));
				p2.setTranslateY(gap / 2);
			}
			pathWrap.addColumn(0, p1, p2);
			if (firstround) {
				pathWrap.setVisible(false);
			}

			wrap.getChildren().addAll(pathWrap, teams);

			return wrap;
		}

		static StackPane makeQualified(int nr) {
			StackPane wrap = new StackPane();

			HBox match_wrap = new HBox();

			Path p1 = new Path();
			p1.getElements().addAll(new MoveTo(0, 0), new HLineTo(20));
			p1.setTranslateY(MATCH_HEIGHT / 2);
			match_wrap.getChildren().addAll(p1, new TeamField("Q" + nr, MATCH_WIDTH - CON_WIDTH, MATCH_HEIGHT));
			wrap.getChildren().add(match_wrap);

			return wrap;
		}

	}

	public String toWikiCode(int offset) {
		String s = "";
		int this_round = 0;
		for (int i = 0; i < this_list.size(); i++) {
			if (this_list.get(i) == this) {
				this_round = i;
			}
		}
		boolean de = ROUNDS_LOWER.size() > 0;

		if (this_round == 0) {
			if (!isInLowerBracket) {
				s += "{{#invoke:Bracket|BracketStart\n" + "|cell-type=team\n" + "|column-width={{{column-width|180}}}\n"
						+ "|column-width-finals={{{column-width|180}}}\n" + "|columns="
						+ (ROUNDS_UPPER.size() + (de ? 1 : 0)) + (de ? "|debracket=true\n" : "") + "\n}}\n"
						+ "{{#invoke:WinnersBracketStructure|WinnersBracketStructure\n" + "|cell-type=team\n"
						+ "|column-width={{{column-width|180}}}\n" + "|space-width={{{space-width|20}}}\n"
						+ "|score-width={{{score-width|21}}}\n" + "|columns=" + (ROUNDS_UPPER.size()) + "\n\n";
			} else {
				s += "{{#invoke:LosersBracketStructure|LosersBracketStructure\n" + "|cell-type=team\n"
						+ "|column-width={{{column-width|180}}}\n" + "|space-width={{{space-width|20}}}\n"
						+ "|score-width={{{score-width|21}}}\n" + "|columns=" + (ROUNDS_LOWER.size() - 1) + "\n\n";
			}
		}

		String roundname = roundNameProperty.get();
		if (roundname.equals("Round title")) {
			roundname = "";
		}

		String roundkey = "R" + (this_round + 1);
		s += "|" + roundkey + "={{{" + roundkey + "|" + roundname + "}}}" + name + "|" + roundkey + "matches=" + matchCount
				+ "|" + roundkey + "type=" + type + "\n";

		ArrayList<String> teams = isInLowerBracket ? ROUNDS_KEYS_LOWER.get(this_round) : ROUNDS_KEYS_UPPER.get(this_round);

		if (type.equals(types[0]) || type.equals(types[1])) {
			for (int i = 0; i < matchCount; i++) {
				String gamekey = roundkey + "G" + (i + 1);
				String detailskey = roundkey + "G" + (i + 1 + offset);
				String team1 = teams.get(2 * i);
				String team2 = teams.get(2 * i + 1);
				s += "|" + gamekey + "_p1box={{#if:{{{" + team1 + "team|}}}|{{TeamBracket/{{{" + team1
						+ "team}}}}}|{{#if:{{{" + team1 + "|}}}|{{TeamBracket/" + game
						+ "}}<span style=\"vertical-align:-1px;\">{{{" + team1 + "}}}</span>|{{#if:{{{" + team1
						+ "literal|}}}|{{{" + team1 + "literal}}}|&nbsp;}}}}}}\n";
				s += "|" + gamekey + "_p2box={{#if:{{{" + team2 + "team|}}}|{{TeamBracket/{{{" + team2
						+ "team}}}}}|{{#if:{{{" + team2 + "|}}}|{{TeamBracket/" + game
						+ "}}<span style=\"vertical-align:-1px;\">{{{" + team2 + "}}}</span>|{{#if:{{{" + team2
						+ "literal|}}}|{{{" + team2 + "literal}}}|&nbsp;}}}}}}\n";
				s += "|" + gamekey + "_p1score={{{" + team1 + "score|}}}\n";
				s += "|" + gamekey + "_p2score={{{" + team2 + "score|}}}\n";
				s += "|" + gamekey + "_win={{#if:{{{" + team1 + "win|}}}|1|{{#if:{{{" + team2 + "win|}}}|2|}}}}\n";
				s += "|" + gamekey + "_details={{#if:{{{" + detailskey
						+ "details|}}}|<div class=\"bracket-popup-wrapper bracket-popup-team\" style=\"margin-left:{{{column-width|190}}}px;\"><div class=\"bracket-popup\">{{BracketMatchTeams|\n|team1={{{"
						+ team1 + "team|}}}\n|team2={{{" + team2 + "team|}}}\n|team1" + game + "={{{" + team1
						+ "|}}}\n|team2" + game + "={{{" + team2 + "|}}}\n|team1literal={{{" + team1
						+ "literal|}}}\n|team2literal={{{" + team2 + "literal|}}}\n|details={{{" + detailskey
						+ "details|}}}\n}}</div></div>}}\n";
			}
		} else if (type.equals("qualified")) {
			for (int i = 0; i < matchCount; i++) {
				String gamekey = roundkey + "G" + (i + 1);
				String team1 = teams.get(i);
				s += "|" + gamekey + "_p1box={{#if:{{{" + team1 + "team|}}}|{{TeamBracket/{{{" + team1
						+ "team}}}}}|{{#if:{{{" + team1
						+ "|}}}|{{TeamBracket/rocketleague}}}}<span style=\"vertical-align:-1px;\">{{{" + team1
						+ "|}}}</span>|{{#if:{{{" + team1 + "literal|}}}|{{{" + team1 + "literal}}}|&nbsp;}}}}\n|"
						+ gamekey + "_p1score={{{" + team1 + "score|}}}\n";
			}
		}

		if (this_round == ROUNDS_UPPER.size() - 1) {
			s += "}}\n";
		}

		return s;
	}

}

class LuaBracketFinal extends LuaBracketRound {
	private final static String[] types = { "No Grand Final", "Grand Final" };
	final static int MIDDLE_SIZE = 50;

	public LuaBracketFinal() {
		super();
		content = new VBox();
		content.setAlignment(Pos.CENTER_RIGHT);
		content_wrap = new StackPane();
		content_wrap.setAlignment(Pos.CENTER_RIGHT);
		content_wrap.getChildren().add(content);
		this.setPrefWidth(MATCH_WIDTH);
		this.setAlignment(Pos.TOP_CENTER);

		isInLowerBracket = true;

		this.minHeightProperty().bind(Bindings.add(MAX_UPPER_HEIGHT, MAX_LOWER_HEIGHT).add(MIDDLE_SIZE + 30));
		this_list = ROUNDS_LOWER;
		this_maxMatchNr = MAX_LOWER_MATCHCOUNT;

		// initialization stuff
		this_list.add(this);

		// Type and name
		HBox typesWrap = new HBox();
		typesWrap.setAlignment(Pos.CENTER_RIGHT);
		typesWrap.setSpacing(3);
		typesBox = new ComboBox<String>();
		typesBox.getItems().addAll(Arrays.asList(types));
		typesBox.valueProperty().addListener((cl, o, n) -> {
			type = n;
			changeContentTo(type, matchCount, name);
		});
		typesBox.setValue(types[0]);
		typesBox.setMaxWidth(MATCH_WIDTH - CON_WIDTH);
		typesBox.setMinWidth(MATCH_WIDTH - CON_WIDTH);
		TextField nameField = new TextField();
		nameField.textProperty().addListener((cl, o, n) -> {
			name = n;
			changeContentTo(type, matchCount, name);
		});
		getChildren().addAll(typesBox);
		typesWrap.setMaxWidth(MATCH_WIDTH);

		// Number of rounds and apply button
		HBox infoWrap = new HBox();
		infoWrap.setAlignment(Pos.CENTER_RIGHT);
		infoWrap.setSpacing(3);
		Spinner<Integer> rnrSpinner = new Spinner<Integer>(1, 200, 1);
		rnrSpinner.setValueFactory(new SpinnerValueFactory<Integer>() {

			@Override
			public void increment(int steps) {
				int value = this.getValue();
				value = (int) Math.pow(2, steps) * value;
				if (value == 0)
					value = 1;
				this.setValue(value);
			}

			@Override
			public void decrement(int steps) {
				int value = this.getValue();
				value = (int) (1 / Math.pow(2, steps) * value);
				if (value == 0)
					value = 1;
				this.setValue(value);
			}
		});
		rnrSpinner.getValueFactory().setValue(1);
		rnrSpinner.valueProperty().addListener((cl, o, n) -> {
			matchCount = n;
			changeContentTo(type, matchCount, name);
		});
		rnrSpinner.setMaxWidth(MATCH_WIDTH - CON_WIDTH);
		rnrSpinner.setVisible(false);
		getChildren().addAll(rnrSpinner);

		this.getChildren().add(content_wrap);
		this.setSpacing(5);
	}

	public void changeContentTo(String type, int nr, String name, boolean updateSiblings) {
		// apply new values (for method calls from outside)
		this.type = type;
		this.matchCount = nr;
		this.name = name;

		int this_round = 0;
		for (int i = 0; i < this_list.size(); i++) {
			if (this_list.get(i) == this) {
				this_round = i;
			}
		}
		String roundkey = "R" + (this_round + 1);

		content.getChildren().clear();
		// no grandfinal
		if (type.equals(types[0])) {
		}
		// grandfinal
		else if (type.equals(types[1])) {
			content.setSpacing(0);
			int gap = (MAX_UPPER_MATCHCOUNT.get() * (2 * MATCH_HEIGHT) + (MAX_UPPER_MATCHCOUNT.get() - 1) * MATCH_GAP) / 2
					+ (MAX_LOWER_MATCHCOUNT.get() * (2 * MATCH_HEIGHT) + (MAX_LOWER_MATCHCOUNT.get() - 1) * MATCH_GAP) / 2
					+ MIDDLE_SIZE;
			content.getChildren().add(
					new Gap((MAX_UPPER_MATCHCOUNT.get() * (2 * MATCH_HEIGHT) + (MAX_UPPER_MATCHCOUNT.get() - 1) * MATCH_GAP) / 2 + 20));
			for (int i = 0; i < nr; i++) {
				content.getChildren()
						.add(TeamField.makeMatch(roundkey + "W1", roundkey + "W2", false, false, gap, false));
			}
		}
	}

	public String toWikiCode(int round) {
		String s = "";
		int this_round = 0;
		for (int i = 0; i < this_list.size(); i++) {
			if (this_list.get(i) == this) {
				this_round = i;
			}
		}
		if (type.equals(types[1])) {
			s += "{{#invoke:Bracket|DESectionEnd}}\n" + "{{#invoke:Bracket|GrandFinals\n" + "|cell-type=team\n"
					+ "|column-width={{{column-width|180}}}\n" + "|space-width={{{space-width|20}}}\n"
					+ "|score-width={{{score-width|21}}}\n" + "|columns=" + (ROUNDS_LOWER.size()) + "\n"
					+ "|offset=71\n" + "|line-height=40\n";

			String roundkey = "R" + (this_round + 1);
			String gamekey = roundkey + "G1";
			String detailskey = roundkey + "G1";
			String team1 = roundkey + "W1";
			String team2 = roundkey + "W2";
			s += "|" + gamekey + "_p1box={{#if:{{{" + team1 + "team|}}}|{{TeamBracket/{{{" + team1
					+ "team}}}}}|{{#if:{{{" + team1 + "|}}}|{{TeamBracket/" + game
					+ "}}<span style=\"vertical-align:-1px;\">{{{" + team1 + "}}}</span>|{{#if:{{{" + team1
					+ "literal|}}}|{{{" + team1 + "literal}}}|&nbsp;}}}}}}\n";
			s += "|" + gamekey + "_p2box={{#if:{{{" + team2 + "team|}}}|{{TeamBracket/{{{" + team2
					+ "team}}}}}|{{#if:{{{" + team2 + "|}}}|{{TeamBracket/" + game
					+ "}}<span style=\"vertical-align:-1px;\">{{{" + team2 + "}}}</span>|{{#if:{{{" + team2
					+ "literal|}}}|{{{" + team2 + "literal}}}|&nbsp;}}}}}}\n";
			s += "|" + gamekey + "_p1score={{{" + team1 + "score|}}}\n";
			s += "|" + gamekey + "_p2score={{{" + team2 + "score|}}}\n";
			s += "|" + gamekey + "_win={{#if:{{{" + team1 + "win|}}}|1|{{#if:{{{" + team2 + "win|}}}|2|}}}}\n";
			s += "|" + gamekey + "_details={{#if:{{{" + detailskey
					+ "details|}}}|<div class=\"bracket-popup-wrapper bracket-popup-team\" style=\"margin-left:{{{column-width|190}}}px;\"><div class=\"bracket-popup\">{{BracketMatchTeams|\n|team1={{{"
					+ team1 + "team|}}}\n|team2={{{" + team2 + "team|}}}\n|team1" + game + "={{{" + team1
					+ "|}}}\n|team2" + game + "={{{" + team2 + "|}}}\n|team1literal={{{" + team1
					+ "literal|}}}\n|team2literal={{{" + team2 + "literal|}}}\n|details={{{" + detailskey
					+ "details|}}}\n}}</div></div>}}\n";

			s += "}}\n";
		}

		s += "{{#invoke:Bracket|BracketEnd}}\n";

		return s;
	}
}
