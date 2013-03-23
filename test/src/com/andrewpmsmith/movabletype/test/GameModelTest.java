package com.andrewpmsmith.movabletype.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.andrewpmsmith.movabletype.model.GameModel;
import com.andrewpmsmith.movabletype.model.GameModel.GameResult;
import com.andrewpmsmith.movabletype.model.GameModel.GameState;
import com.andrewpmsmith.movabletype.model.GameModel.LetterState;
import com.andrewpmsmith.movabletype.model.GameModel.TurnResult;

public class GameModelTest extends TestCase {
	
	private List<Integer> generateIndexListFromWord(String word, char[] grid) {
		
		if (word==null) return null;
		
		List<Integer> ret = new ArrayList<Integer>(word.length());
		
		for (int i=0; i<word.length(); ++i) {
			
			char c = word.charAt(i);
			
			for (int j=0; j<grid.length; ++j) {
				if (c==grid[j]) {
					ret.add(j);
					break;
				}
			}
			
		}
		
		return ret;
		
	}
	
	/*
	 * Play a full game using sample data. Test that results are calculated
	 * correctly. Test that rules are enforced for invalid turns.
	 */
	public void test_fullGame() {
		
		// Initialise a new game with sample data
		
		final char[] testGrid =
				("ABCDE" +
				 "FGHIJ" +
				 "KLMNO" +
				 "PQRST" +
				 "UVWXY").toCharArray();
		
		final LetterState UNPLAYED = LetterState.UNPLAYED;
		final LetterState[] states = {
				UNPLAYED,UNPLAYED,UNPLAYED,UNPLAYED,UNPLAYED,
				UNPLAYED,UNPLAYED,UNPLAYED,UNPLAYED,UNPLAYED,
				UNPLAYED,UNPLAYED,UNPLAYED,UNPLAYED,UNPLAYED,
				UNPLAYED,UNPLAYED,UNPLAYED,UNPLAYED,UNPLAYED,
				UNPLAYED,UNPLAYED,UNPLAYED,UNPLAYED,UNPLAYED,};
		
		GameState gameState = GameState.PLAYER1_TURN;
		int p1Points = 0;
		int p2Points = 0;
		
		
		// Define some sample turns
		
		final String[] turns = {
				"THE",		//*p1=3   p2=0
				"QUICK",	// p1=3  *p2=5
				"BROWN",	//*p1=8   p2=5
				"FOX",		// p1=7  *p2=8
				"JUMPS",	//*p1=11  p2=7
				"OVER",		// p1=9  *p2=10
				"LAY",		//*p1=12  p2=10
				"DOG"		// p1=12 *p2=11
				};
		
		final String[] invalidTurns = {
				null,
				"",		// too short
				"A",	// too short
				"QY",	// Word not in dictionary
				"THE",	// Already played
				"BROW"	// Prefix of previously played word
		};
		
		final int[][] scores = {
				{3,0},
				{3,5},
				{8,5},
				{7,8},
				{11,7},
				{9,10},
				{12,10},
				{12,11}};
		
		
		// Create the GameModel instance
		
		GameModel gm = new GameModel(testGrid,
				states,
				gameState,
				p1Points,
				p2Points,
				new WordFinderStub(turns));
		
		assertNotNull(gm);
		
		// Initial score is 0,0
		assertEquals(0, gm.getPoints(GameModel.PLAYER1));
		assertEquals(0, gm.getPoints(GameModel.PLAYER2));
		
		
		for (int i = 0; i < turns.length; i++) {
			
			if (i==3) {
				
				// Attempt some invalid moves
				// Play them after the third move so we can test duplicate and
				// prefix checking
				
				for (int j=0; j<invalidTurns.length; ++j) {
					gm.setWord(generateIndexListFromWord(invalidTurns[j], testGrid));
					TurnResult tr = gm.playTurn();
					
					Assert.assertFalse(tr==TurnResult.SUCCESS);
					
				}
			}
			
			gm.setWord(generateIndexListFromWord(turns[i], testGrid));
			TurnResult tr = gm.playTurn();
			
			// Test the word was played successfully
			assertEquals(tr,TurnResult.SUCCESS);
			
			// Test the score was correctly updated
			assertEquals(scores[i][0], gm.getPoints(GameModel.PLAYER1));
			assertEquals(scores[i][1], gm.getPoints(GameModel.PLAYER2));
			
			// Test that player turns switch correctly
			GameState gs = gm.getGameState();
			if (gs!=GameState.GAME_OVER) {
			
				if (i%2==0) {
					Assert.assertEquals(gs, GameState.PLAYER2_TURN);
				} else {
					Assert.assertEquals(gs, GameState.PLAYER1_TURN);
				}
			}
			
		}
		
		// Test game is marked as over
		GameState gs = gm.getGameState();
		Assert.assertTrue(gs==GameState.GAME_OVER);
		
		// Test the correct winner was announced
		GameResult gr = gm.getResult();
		Assert.assertEquals(gr, GameResult.PLAYER1_WIN);
		
		
	}
	
	
	
}
