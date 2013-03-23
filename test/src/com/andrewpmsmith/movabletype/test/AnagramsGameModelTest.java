package com.andrewpmsmith.movabletype.test;

import java.util.Arrays;

import junit.framework.TestCase;

import com.andrewpmsmith.movabletype.model.AnagramsGameModel;
import com.andrewpmsmith.movabletype.model.AnagramsPlayer;

public class AnagramsGameModelTest extends TestCase {
	public void testReveal() {
		// SETUP
		AnagramsGameModel model = new AnagramsGameModel();
		model.setDeck("ERFTOP");
		
		// EXEC
		char letter1 = model.revealLetter();
		String unclaimedLetters1 = model.getUnclaimedLetters();
		char letter2 = model.revealLetter();
		String unclaimedLetters2 = model.getUnclaimedLetters();
		
		// VERIFY
		assertEquals("letter 1", 'E', letter1);
		assertEquals("unclaimed letters 1", "E", unclaimedLetters1);
		assertEquals("letter 2", 'R', letter2);
		assertEquals("unclaimed letters 2", "ER", unclaimedLetters2);
	}

	public void testRestart() {
		// SETUP
		AnagramsGameModel model = new AnagramsGameModel();
		model.setDeck("ERFTOP");
		
		// EXEC
		model.revealLetter();
		model.getUnclaimedLetters();
		
		model.setDeck("XYZ");
		char letter = model.revealLetter();
		String unclaimedLetters = model.getUnclaimedLetters();
		
		// VERIFY
		assertEquals("letter", 'X', letter);
		assertEquals("unclaimed letters", "X", unclaimedLetters);
	}

	public void testMakeWord() {
		// SETUP
		AnagramsGameModel model = new AnagramsGameModel();
		model.setWordFinder(new WordFinderStub(new String[] {
			"FORE",
			"GORE",
			"PORE",
			"FORK"
		}));
		model.setDeck("ERFGOP");
		AnagramsPlayer player1 = new AnagramsPlayer();
		AnagramsPlayer player2 = new AnagramsPlayer();
		model.addPlayer(player1);
		model.addPlayer(player2);
		
		// EXEC
		model.revealLetter(); //E
		model.revealLetter(); //R
		model.revealLetter(); //F
		model.revealLetter(); //G
		model.revealLetter(); //O

		boolean isSuccess = model.makeWord("FORE", player2);
		String unclaimedLetters = model.getUnclaimedLetters();
		int player1Score = player1.getScore();
		int player2Score = player2.getScore();
		
		// VERIFY
		assertTrue("success", isSuccess);
		assertEquals("unclaimed letters", "G", unclaimedLetters);
		assertEquals("score 1", 0, player1Score);
		assertEquals("score 2", 4, player2Score);
	}

	public void testMakeUnknownWord() {
		// SETUP
		AnagramsGameModel model = new AnagramsGameModel();
		model.setWordFinder(new WordFinderStub(new String[] {
				"FORE",
				"GORE",
				"PORE",
				"FORK"
			}));
		model.setDeck("ERFGOP");
		AnagramsPlayer player1 = new AnagramsPlayer();
		AnagramsPlayer player2 = new AnagramsPlayer();
		model.addPlayer(player1);
		model.addPlayer(player2);
		
		// EXEC
		model.revealLetter(); //E
		model.revealLetter(); //R
		model.revealLetter(); //F
		model.revealLetter(); //G
		model.revealLetter(); //O

		boolean isSuccess = model.makeWord("FORG", player2);
		String unclaimedLetters = model.getUnclaimedLetters();
		int player1Score = player1.getScore();
		int player2Score = player2.getScore();
		
		// VERIFY
		assertFalse("success", isSuccess);
		assertEquivalent("unclaimed letters", "ERFGO", unclaimedLetters);
		assertEquals("score 1", 0, player1Score);
		assertEquals("score 2", 0, player2Score);
	}

	/**
	 * Assert that two strings contain the same set of letters, where order
	 * doesn't matter.
	 * @param message A description of the contents
	 * @param expected The expected set of letters
	 * @param actual The actual set of letters
	 */
	private void assertEquivalent(
			String message, 
			String expected,
			String actual) {
		char[] expectedLetters = expected.toCharArray();
		char[] actualLetters = actual.toCharArray();
		Arrays.sort(expectedLetters);
		Arrays.sort(actualLetters);
		assertEquals(message, expected, actual);
	}
}
