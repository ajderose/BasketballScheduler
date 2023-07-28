import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

public class App extends Application {
	private Stage primaryStage;
	private TextField numTeamsField;
	private TextField numFixturesField;
	private List<String> currentTeamNames;
	private VBox scheduleContainer;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Basketball Scheduler");

		GridPane grid = createInputGrid();
		Button generateButton = createGenerateButton();
		scheduleContainer = new VBox();
		scheduleContainer.setSpacing(10);
		scheduleContainer.setPadding(new Insets(10));

		VBox vbox = new VBox(10);
		vbox.getChildren().addAll(grid, generateButton, scheduleContainer);
		vbox.setAlignment(Pos.CENTER);
		vbox.setPrefWidth(475);
		vbox.setPrefHeight(175);
		
		Scene scene = new Scene(vbox, 475, 175);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private GridPane createInputGrid() {
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(10));

		Label numTeamsLabel = new Label("Number of Teams:");
		numTeamsField = new TextField();

		Label numFixturesLabel = new Label("Number of Fixtures:");
		numFixturesField = new TextField();

		grid.add(numTeamsLabel, 0, 0);
		grid.add(numTeamsField, 1, 0);
		grid.add(numFixturesLabel, 0, 1);
		grid.add(numFixturesField, 1, 1);

		currentTeamNames = new ArrayList<>();
		Button addTeamButton = new Button("Team Names");
		addTeamButton.setOnAction(e -> {
			showTeamNamesModal(Integer.parseInt(numTeamsField.getText()));
		});
		grid.add(addTeamButton, 2, 0);
		
		return grid;
	}

	private Button createGenerateButton() {
		Button generateButton = new Button("Generate Schedule");
		generateButton.setOnAction(e -> generateSchedule());
		return generateButton;
	}

	private void showTeamNamesModal(int numTeams) {
		Dialog<List<String>> dialog = new Dialog<>();
		dialog.setTitle("Enter Team Names");
		dialog.setHeaderText("Please enter the names of all teams.");

		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

		// Using a grid pane to manage all input fields
		GridPane gridPane = new GridPane();
		gridPane.setHgap(10);
		gridPane.setVgap(10);

		List<TextField> teamNameFields = new ArrayList<>();

		for (int i = 0; i < numTeams; i++) {
			Label label = new Label("Team " + (i + 1));
			TextField textField = new TextField();
			teamNameFields.add(textField);

			gridPane.add(label, 0, i);
			gridPane.add(textField, 1, i);
		}

		dialog.getDialogPane().setContent(gridPane);

		// Request focus on the first text field by default
		teamNameFields.get(0).requestFocus();

		// Convert the result to a list of team names when OK is clicked
		dialog.setResultConverter(dialogButton -> {
			List<String> teamNames = new ArrayList<>();
			if (dialogButton == ButtonType.OK) {
				for (TextField field : teamNameFields) {
					String teamName = field.getText().trim();
					if (!teamName.isEmpty()) {
						teamNames.add(teamName);
					}
				}
				return teamNames;
			}
			return null;
		});

		Optional<List<String>> result = dialog.showAndWait();

		result.ifPresent(teamNames -> {
			for (String teamName : teamNames) {
				currentTeamNames.add(teamName);
			}
		});

	}

	private void generateSchedule() {
		int numFixtures = Integer.parseInt(numFixturesField.getText());
		List<String> teamNames = currentTeamNames;

		List<List<Match>> schedule = generateSchedule(teamNames, numFixtures);

		// Clear any existing schedule
		scheduleContainer.getChildren().clear();

		TextArea scheduleTextArea = new TextArea();
		scheduleTextArea.setEditable(false);

		StringBuilder scheduleText = new StringBuilder();
		for (int i = 0; i < schedule.size(); i++) {
			List<Match> fixture = schedule.get(i);

			scheduleText.append("Fixture ").append(i + 1).append("\n");
			for (Match match : fixture) {
				scheduleText.append(match).append("\n");
			}
			scheduleText.append("\n");
		}

		scheduleTextArea.setText(scheduleText.toString());
		scheduleContainer.getChildren().add(scheduleTextArea);

		ScrollPane scrollPane = new ScrollPane(scheduleContainer);
		scrollPane.setFitToWidth(true);
		scrollPane.setFitToHeight(true);

		VBox vbox = new VBox(10);
		vbox.getChildren().addAll(createInputGrid(), createGenerateButton(), scrollPane);
		vbox.setAlignment(Pos.CENTER);

		Scene scene = new Scene(vbox, 475, 325);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private List<List<Match>> generateSchedule(List<String> teamNames, int numFixtures) {
		int numTeams = teamNames.size();
		if (numTeams % 2 == 1) {
			teamNames.add("bye");
			numTeams++;
		}

		List<List<Match>> schedule = new ArrayList<>(numFixtures);
		List<Match> matchHistory = new ArrayList<Match>();

		int maxWithoutDupes = numTeams * (numTeams - 1);

		while (schedule.size() < numFixtures) { // && schedule.size() != numTeams - 1) { // (numTeams*(numTeams-1))/2) {
			if (matchHistory.size() == maxWithoutDupes) {
				matchHistory = new ArrayList<Match>(maxWithoutDupes);
			}

			List<Match> possibleFix = generateFixture(teamNames);
			boolean fixWorks = true;

			for (Match match : possibleFix) {
				if (containsMatch(matchHistory, match)) {
					fixWorks = false;
				}
			}

			if (fixWorks) {
				schedule.add(possibleFix);

				for (Match match : possibleFix) {
					matchHistory.add(match);
					matchHistory.add(new Match(match.team2, match.team1));
				}
			}
		}
		return schedule;
	}

	public static List<Match> generateFixture(List<String> teamNames) {
		int numTeams = teamNames.size();
		boolean[] beenScheduled = new boolean[numTeams];

		List<Match> fixture = new ArrayList<Match>();

		Collections.shuffle(teamNames);

		for (int t1 = 0; t1 < numTeams; t1++) {
			for (int t2 = 0; t2 < numTeams; t2++) {

				if (t1 == t2 || beenScheduled[t1] || beenScheduled[t2]) {
					continue;
				} else {
					fixture.add(new Match(teamNames.get(t1), teamNames.get(t2)));
					beenScheduled[t1] = true;
					beenScheduled[t2] = true;
				}
			}
		}
		return fixture;
	}

	@SuppressWarnings("unused")
	private void showError(String message) {
		System.err.println("Error: " + message);
	}

	private static class Match {
		private String team1;
		private String team2;

		private Match(String team1, String team2) {
			this.team1 = team1;
			this.team2 = team2;
		}

		public String toString() {
			return team1 + " vs " + team2;
		}
	}

	private static boolean containsMatch(List<Match> list, Match c) {
		for (Match m : list) {
			if ((m.team1.equals(c.team1) && m.team2.equals(c.team2))
					|| (m.team1.equals(c.team2) && m.team2.equals(c.team1))) {
				return true;
			}
		}
		return false;
	}
}
