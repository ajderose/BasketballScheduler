import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class BasketballScheduler {

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);

		System.out.print("Enter the number of teams: ");
		int numTeams = sc.nextInt();

		System.out.print("Enter the number of fixtures: ");
		int numFixtures = sc.nextInt();

		List<String> teamNames = new ArrayList<>();
		for (int i = 0; i < numTeams; i++) {
			System.out.print("Enter the name of Team " + (i + 1) + ": ");
			String teamName = sc.next();
			teamNames.add(teamName);
		}

		sc.close();

		List<List<Match>> schedule = generateSchedule(teamNames, numFixtures);

		for (int i = 0; i < schedule.size(); i++) {
			List<Match> fixture = schedule.get(i);
			System.out.println("\nFixture " + (i + 1));

			for (Match match : fixture) {
				System.out.println(match);
			}
		}
	}

	public static List<List<Match>> generateSchedule(List<String> teamNames, int numFixtures) {
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

//formulaic aproach with random list shuffle or order shuffle
// generate day, check conflicts, accept or reshuffle day
