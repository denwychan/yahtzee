/*
 * File: Yahtzee.java
 * ------------------
 * This program will eventually play the Yahtzee game.
 */

import acm.io.*;
import acm.program.*;
import acm.util.*;
import java.util.*;

public class Yahtzee extends GraphicsProgram implements YahtzeeConstants {
	
	public static void main(String[] args) {
		new Yahtzee().start(args);
	}
	
	public void run() {
		IODialog dialog = getDialog();
		nPlayers = dialog.readInt("Enter number of players");
		playerNames = new String[nPlayers];
		for (int i = 1; i <= nPlayers; i++) {
			playerNames[i - 1] = dialog.readLine("Enter name for player " + i);
		}
		display = new YahtzeeDisplay(getGCanvas(), playerNames);
		playGame();
	}

	private void playGame() {
		//Create players score board
		int[][] playersScores = new int[nPlayers + 1][N_CATEGORIES + 1];
		//Create players list of previously used categories
		int[][] usedCategories = new int[nPlayers + 1][N_CATEGORIES + 1];
		//There are 13 rounds in a game
		for (int roundNum = 1; roundNum < rounds + 1; roundNum++) {
			playARound(playersScores, usedCategories);
			
		}
		//Update score board with final score
		updateScoreBoard(playersScores);
		//Announce the winner with the highest score
		annouceTheWinner(playersScores);
	}
		
	
	//A round consists of each player taking a turn
	private void playARound(int[][] playersScores, int[][]usedCategories) {
		for (int playerId = 1; playerId < nPlayers + 1; playerId++) {
			playATurn(playerId, playersScores, usedCategories);
		}
	}
	
	//A player takes a turn by rolling the dice up to 3 times and assign their score per turn to a category
	private void playATurn(int playerId, int[][] playersScores, int[][]usedCategories) {
		int[] diceValues = new int[N_DICE];
		rollDice(playerId, diceValues);
		display.printMessage(playerNames[playerId - 1].toString() + " you get to reroll the dice you fancy twice more");
		rerollDice(diceValues);
		display.printMessage(playerNames[playerId - 1].toString() + " you get to reroll the dice you fancy once more");
		rerollDice(diceValues);
		assignCat(playerId, diceValues, playersScores, usedCategories);
	}
	
	
	//The player rolls all 5 dice first of 3 times
	private void rollDice(int playerId, int[] diceValues) {
		display.printMessage( playerNames[playerId - 1].toString() + "\'s turn. Click \'Roll Dice\' to start");
		display.waitForPlayerToClickRoll(playerId);
		for (int i = 0; i < diceValues.length; i++) {
			diceValues[i] = rgen.nextInt(1, 6);
		}
		display.displayDice(diceValues);
	}
	
	//Roll a single die 
	private int rollDie() {
		int dieValue = rgen.nextInt(1, 6);
		return dieValue;
	}
	
	//If they wish, the player selects and rerolls the dice they select for the second and three of 3 times in a single turn
	private void rerollDice(int[] diceValues) {
		display.waitForPlayerToSelectDice();
		for (int i = 0; i < diceValues.length; i++) {
			if (display.isDieSelected(i)) {
				diceValues[i] = rollDie();
			} 
		}
		display.displayDice(diceValues);
	}
	
	//Assign the player's score for a turn to a category
	private void assignCat(int playerId, int[] diceValues, int[][] playersScores, int[][] usedCategories) {
		int turnScore = 0; 
		display.printMessage("Please select a valid category for your score. Careful, if it's invalid you will get 0 points");
		int category = checkUsedCategories(playerId, usedCategories);
		if (validateCategory(category,diceValues)) {
			turnScore = calcScore(category, diceValues);
		}
		display.updateScorecard(category, playerId, turnScore);
		playersScores[playerId][category] = turnScore;
	}
	
	//Check if a category has been used before. 
	//The user cannot re-use any previous categories and a warning appears if they try to do so
	private int checkUsedCategories(int playerId, int[][]usedCategories) {
		int category = display.waitForPlayerToSelectCategory();
		while (true) {
			//0 represents an unused category and 1 represents a previously used category
			if (usedCategories[playerId][category] == 0) {
				usedCategories[playerId][category] = 1;
				break;
			}
			display.printMessage("You cannot reuse a previous category. Please pick another one");
			category = display.waitForPlayerToSelectCategory();
		}
		return category; 
	}
	
	//Determine if the dice configuration is valid for a category
	private boolean validateCategory(int category, int[] diceValues){
	boolean valid = false;
	switch(category) {	
		case ONES:
		case TWOS:
		case THREES:
		case FOURS:
		case FIVES:
		case SIXES:
		case CHANCE:
			valid = true;
			break;
		//Three of a kind
		case THREE_OF_A_KIND:
			valid = validateKindsYahtzee(3, diceValues);
			break;
		//Four of a kind
		case FOUR_OF_A_KIND:
			valid = validateKindsYahtzee(4, diceValues);
			break;
		//Full House
		case FULL_HOUSE :
			valid = validateFullHouse(diceValues);
			break;
		//Small straight
		case SMALL_STRAIGHT:
			valid = validateStraights(4, diceValues);
			break;
		//Large straight
		case LARGE_STRAIGHT:
			valid = validateStraights(5, diceValues);
			break;
		//Yahtzee
		case YAHTZEE:
			valid = validateKindsYahtzee(5, diceValues);
			break;
		default: 
			break;
	}
	return valid;
	}
	
	//Validate Three of a Kind, Four of a Kind and Yahtzee
	private boolean validateKindsYahtzee(int numMatchingDice, int[] diceValues) {//MatchingDice represents e.g. 3 for Three of a Kind, 5 for Yachzee etc.
		Map<Integer, Integer> diceNumCounter = diceNumCount(diceValues);
		//Return true if any of Three of a Kind, Four of a Kind and Yahtzee is found
		for (Integer count: diceNumCounter.values()) {
			if (count >= numMatchingDice) {
				return true; 
			}
		}
		return false;
	}
	
	//Validate Full House
	private boolean validateFullHouse(int[] diceValues) {
		Map<Integer, Integer> diceNumCounter = diceNumCount(diceValues);
		int counter = 0; 
		for (Integer count: diceNumCounter.values()) {
			if (count != 3 && count != 2) {
				return false;
			} else {
				counter++;
			}
		}
		return counter ==2;
	}
	
	//Create a hashmap to count the times 1 - 6 appear on the dice roll
	private Map<Integer, Integer> diceNumCount(int[] diceValues) {
		Map<Integer, Integer> diceNumCount = new HashMap<Integer, Integer>();
		for (int i = 0; i < diceValues.length; i++) {
			if (!diceNumCount.containsKey(diceValues[i])){
				diceNumCount.put(diceValues[i], 0);
			}
			diceNumCount.put(diceValues[i], diceNumCount.get(diceValues[i]) + 1);
		}
		return diceNumCount;
	}
	
	//Validate Small and Large Straights
	private boolean validateStraights(int streakSize, int[] diceValues) { //4 represents a small straight and 5 represents a large straight
		int streakCounter = 1; 
		Arrays.sort(diceValues);
		int j = diceValues[0];
		for (int i = 1; i < diceValues.length; i++) {
			//Stop counting as soon as the straight is found
			if (streakCounter == streakSize) {
				break;
			//Increment the counter if the values of 2 dices are incremental by 1
			} else if (diceValues[i] - j == 1){
				streakCounter ++;
			//Reset the counter if the dice values are not same or do not go up by 1
			} else if (diceValues[i] != j){
				streakCounter = 1;
			}
			j = diceValues[i];
		}
		if (streakCounter >= streakSize) {
			return true;
		}
		return false;
	}
	
	//Calculate the score based on the category selected
	private int calcScore(int category, int[]diceValues){
		int turnScore = 0;
		switch(category) {
			case ONES:
			case TWOS:
			case THREES:
			case FOURS:
			case FIVES:
			case SIXES:	
				turnScore = sumNumbers(category, diceValues);
				break;
			//Three of a kind, four of a kind, chance
			case THREE_OF_A_KIND: 
			case FOUR_OF_A_KIND:
			case CHANCE:
				turnScore = sumAll(diceValues);
				break;
			//Full House (25 points)
			case FULL_HOUSE:
				turnScore = 25;
				break;
			//Small straight (30 points)	
			case SMALL_STRAIGHT:
				turnScore = 30;
				break;
			//Large straight (40 points)
			case LARGE_STRAIGHT:
				turnScore = 40;
				break;
			//Yahtzee (50 points)
			case YAHTZEE:
				turnScore = 50;
				break;
			//Dice configuration assigned to a category it doesn't meet receives 0
			default: 
				turnScore = 0;
				break;
		}
		return turnScore;
	}

	/*Sum of the numbers (i.e. 1, 2, 3, 4, 5, 6) if they match the category number
	 * e.g. if the category is 1, sum all the 1s; if the category is 2, sum all the 2s
	 */
	private int sumNumbers(int num, int[] diceValues) {
		int turnScore = 0; 
		for (int i = 0; i < diceValues.length; i++) {
			if (diceValues[i] == num) {
				turnScore += diceValues[i];
			}
		}
		return turnScore;
	}
	
	//Sum of all the values showing on the dice
	private int sumAll(int[] diceValues) {
		int turnScore = 0;
		for (int i = 0; i < diceValues.length; i++) {
			turnScore += diceValues[i];
		}
		return turnScore;
	}
	
	//Sum up the upper score, upper bonus (if any), lower score, and final total based on these for each player
		private void updateScoreBoard(int[][] playersScores) {
			for (int playerId = 1; playerId < nPlayers + 1; playerId++) {
				//Update the upper score based on the upper section for the scoreboard (1 - 6)
				playersScores[playerId][UPPER_SCORE] = playersScores[playerId][ONES] 
						+ playersScores[playerId][TWOS] 
						+ playersScores[playerId][THREES] 
						+ playersScores[playerId][FOURS] 
						+ playersScores[playerId][FIVES] 
						+ playersScores[playerId][SIXES];
				display.updateScorecard(UPPER_SCORE, playerId, playersScores[playerId][UPPER_SCORE]);
				//Calculate if the player gets an upper bonus if the upper score is equal to or above 63
				if (playersScores[playerId][UPPER_SCORE] >= 63) {
					playersScores[playerId][UPPER_BONUS] = 35;
				} else {
					playersScores[playerId][UPPER_BONUS] = 0;
				}
				display.updateScorecard(UPPER_BONUS, playerId, playersScores[playerId][UPPER_BONUS]);
				//Update the lower score based on the lower section of the scoreboard
				playersScores[playerId][LOWER_SCORE] = playersScores[playerId][THREE_OF_A_KIND] 
						+ playersScores[playerId][FOUR_OF_A_KIND] 
						+ playersScores[playerId][FULL_HOUSE] 
						+ playersScores[playerId][SMALL_STRAIGHT] 
						+ playersScores[playerId][LARGE_STRAIGHT] 
						+ playersScores[playerId][YAHTZEE]
						+ playersScores[playerId][CHANCE];
				display.updateScorecard(LOWER_SCORE, playerId, playersScores[playerId][LOWER_SCORE]);
				//Update the total score
				playersScores[playerId][TOTAL] = playersScores[playerId][UPPER_SCORE]
						+ playersScores[playerId][UPPER_BONUS]
						+ playersScores[playerId][LOWER_SCORE];
				display.updateScorecard(TOTAL, playerId, playersScores[playerId][TOTAL]);
			}	
		}
		
		//Annouce the winner of the game with the highest total score
		private void annouceTheWinner(int[][] playersScores) {
			int maxScore = 0;
			String winner = "";
			for (int playerId = 1; playerId < nPlayers + 1; playerId++) {
				if (playersScores[playerId][TOTAL] > maxScore) {
					maxScore = playersScores[playerId][TOTAL]; 
					winner = playerNames[playerId - 1];
				} else if (playersScores[playerId][TOTAL] == maxScore) {
					winner = winner + " and " + playerNames[playerId - 1];
				}
			}
			display.printMessage("Congratulations " + winner + "! You won with a total score of " + maxScore);
		}
		
/* Private instance variables */
	private int nPlayers;
	private String[] playerNames;
	private YahtzeeDisplay display;
	private RandomGenerator rgen = new RandomGenerator();
	private int rounds = 13;

}
