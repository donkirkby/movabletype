package com.andrewpmsmith.movabletype.test;

import java.util.List;

import com.andrewpmsmith.movabletype.model.AnagramsGameModel;
import com.andrewpmsmith.movabletype.model.AnagramsPlayer;
import com.andrewpmsmith.movabletype.model.InvalidWordException;

public class AnagramsGameModelTest extends AnagramsTestCase {
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

	public void testEmpty() {
		// SETUP
		AnagramsGameModel model = new AnagramsGameModel();
		model.setDeck("E");
		
		// EXEC
		boolean isEmptyBefore = model.isDeckEmpty();
		model.revealLetter();
		boolean isEmptyAfter = model.isDeckEmpty();
		
		// VERIFY
		assertFalse("empty before", isEmptyBefore);
		assertTrue("empty after", isEmptyAfter);
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

	public void testMakeWord() throws InvalidWordException {
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

		model.makeWord("FORE", player2);
		String unclaimedLetters = model.getUnclaimedLetters();
		int player1Score = player1.getScore();
		int player2Score = player2.getScore();
		
		// VERIFY
		assertEquals("unclaimed letters", "G", unclaimedLetters);
		assertEquals("score 1", 0, player1Score);
		assertEquals("score 2", 4, player2Score);
	}

	public void testMakeGoodWordAfterUnavailableWord() throws Exception {
		// SETUP
		String expectedMessage = "The letter K is not available to make FORK.";
		final AnagramsGameModel model = new AnagramsGameModel();
		model.setWordFinder(new WordFinderStub(new String[] {
			"FORE",
			"GORE",
			"PORE",
			"FORK"
		}));
		model.setDeck("ERFGOP");
		final AnagramsPlayer player1 = new AnagramsPlayer();
		final AnagramsPlayer player2 = new AnagramsPlayer();
		model.addPlayer(player1);
		model.addPlayer(player2);
		
		// EXEC
		model.revealLetter(); //E
		model.revealLetter(); //R
		model.revealLetter(); //F
		model.revealLetter(); //G
		model.revealLetter(); //O

		InvalidWordException ex = makeInvalidWord(model, "FORK", player2);
		model.makeWord("FORE", player2);
		
		// VERIFY
		assertEquals("message", expectedMessage, ex.getMessage());
	}

	private InvalidWordException makeInvalidWord(
			final AnagramsGameModel model, 
			final String word,
			final AnagramsPlayer player) throws Exception {
		InvalidWordException ex = assertThrows(
				InvalidWordException.class,
				new Testable() {
					@Override
					public void run() throws Exception {
						model.makeWord(word, player);
					}
				});
		return ex;
	}

	private InvalidWordException changeInvalidWord(
			final AnagramsGameModel model, 
			final String oldWord,
			final String newWord,
			final AnagramsPlayer player) throws Exception {
		InvalidWordException ex = assertThrows(
				InvalidWordException.class,
				new Testable() {
					@Override
					public void run() throws Exception {
						model.changeWord(oldWord, newWord, player);
					}
				});
		return ex;
	}

	public void testChangeWord() throws InvalidWordException {
		// SETUP
		AnagramsGameModel model = new AnagramsGameModel();
		model.setWordFinder(new WordFinderStub(new String[] {
			"FORE",
			"FORGE",
			"FORK",
			"GORE",
			"PORE",
		}));
		model.setDeck("ERFGOPN");
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
		model.revealLetter(); //P

		model.makeWord("FORE", player2);
		model.changeWord("FORE", "FORGE", player1);
		String unclaimedLetters = model.getUnclaimedLetters();
		int player1Score = player1.getScore();
		int player2Score = player2.getScore();
		
		// VERIFY
		assertEquals("unclaimed letters", "P", unclaimedLetters);
		assertEquals("score 1", 5, player1Score);
		assertEquals("score 2", 0, player2Score);
	}

	public void testChangeUnusedWord() throws Exception {
		// SETUP
		AnagramsGameModel model = new AnagramsGameModel();
		model.setWordFinder(new WordFinderStub(new String[] {
			"FORE",
			"FORGE",
			"FORK",
			"GORE",
			"PORE",
		}));
		model.setDeck("ERFGOPN");
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
		model.revealLetter(); //P

		InvalidWordException ex = 
				changeInvalidWord(model, "FORE", "FORGE", player1);
		String unclaimedLetters = model.getUnclaimedLetters();
		int player1Score = player1.getScore();
		
		// VERIFY
		assertEquals("message", "FORE is not a claimed word.", ex.getMessage());
		assertEquals("unclaimed letters", "ERFGOP", unclaimedLetters);
		assertEquals("score 1", 0, player1Score);
	}

	public void testChangeToUnavailableWord() throws Exception {
		// SETUP
		AnagramsGameModel model = new AnagramsGameModel();
		model.setWordFinder(new WordFinderStub(new String[] {
			"FORE",
			"FORCE",
			"FORGE",
			"FORK",
			"GORE",
			"PORE",
		}));
		model.setDeck("ERFGOPN");
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
		model.revealLetter(); //P

		model.makeWord("FORE", player1);
		InvalidWordException ex = 
				changeInvalidWord(model, "FORE", "FORCE", player1);
		model.changeWord("FORE", "FORGE", player1);
		String unclaimedLetters = model.getUnclaimedLetters();
		int player1Score = player1.getScore();
		
		// VERIFY
		assertEquals(
				"message",
				"C is not available to make FORCE.",
				ex.getMessage());
		assertEquals("unclaimed letters", "P", unclaimedLetters);
		assertEquals("score 1", 5, player1Score);
	}

	public void testIncompleteChange() throws Exception {
		// SETUP
		AnagramsGameModel model = new AnagramsGameModel();
		model.setWordFinder(new WordFinderStub(new String[] {
			"FORE",
			"FORGE",
			"FORK",
			"GORE",
			"PORE",
		}));
		model.setDeck("ERFGOPN");
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
		model.revealLetter(); //P

		model.makeWord("FORE", player1);
		InvalidWordException ex = 
				changeInvalidWord(model, "FORE", "PORE", player1);
		model.changeWord("FORE", "FORGE", player1);
		String unclaimedLetters = model.getUnclaimedLetters();
		int player1Score = player1.getScore();
		
		// VERIFY
		assertEquals(
				"message",
				"Some letters of FORE were not used in PORE.",
				ex.getMessage());
		assertEquals("unclaimed letters", "P", unclaimedLetters);
		assertEquals("score 1", 5, player1Score);
	}

	public void testMakeUnknownWord() throws Exception {
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

		InvalidWordException ex = makeInvalidWord(model, "FORG", player2);
		String unclaimedLetters = model.getUnclaimedLetters();
		int player1Score = player1.getScore();
		int player2Score = player2.getScore();
		
		// VERIFY
		assertEquals(
				"message", 
				"FORG is not in the dictionary.", 
				ex.getMessage());
		assertEquivalent("unclaimed letters", "ERFGO", unclaimedLetters);
		assertEquals("score 1", 0, player1Score);
		assertEquals("score 2", 0, player2Score);
	}

	public void testMakeUnavailableWord() throws Exception {
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

		InvalidWordException ex = makeInvalidWord(model, "FORK", player2);
		String unclaimedLetters = model.getUnclaimedLetters();
		int player1Score = player1.getScore();
		int player2Score = player2.getScore();
		
		// VERIFY
		assertEquals(
				"message",
				"The letter K is not available to make FORK.",
				ex.getMessage());
		assertEquivalent("unclaimed letters", "ERFGO", unclaimedLetters);
		assertEquals("score 1", 0, player1Score);
		assertEquals("score 2", 0, player2Score);
	}

	public void testGetWordsFromMake() throws InvalidWordException {
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

		model.makeWord("FORE", player2);
		List<AnagramsPlayer> players = model.getPlayers();
		List<String> player1Words = model.getWords(player1);
		List<String> player2Words = model.getWords(player2);
		
		// VERIFY
		assertTrue("player 1 found", players.contains(player1));
		assertTrue("player 2 found", players.contains(player2));
		assertEquals("player count", 2, players.size());
		assertEquals("player 1 word count", 0, player1Words.size());
		assertEquals("player 2 word count", 1, player2Words.size());
		assertEquals("player 2 word", "FORE", player2Words.get(0));
	}

	public void testGetWordsFromChange() throws InvalidWordException {
		// SETUP
		AnagramsGameModel model = new AnagramsGameModel();
		model.setWordFinder(new WordFinderStub(new String[] {
			"FORE",
			"FORGE",
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

		model.makeWord("FORE", player2);
		model.changeWord("FORE", "FORGE", player1);
		List<String> player1Words = model.getWords(player1);
		List<String> player2Words = model.getWords(player2);
		
		// VERIFY
		assertEquals("player 1 word count", 1, player1Words.size());
		assertEquals("player 2 word count", 0, player2Words.size());
		assertEquals("player 1 word", "FORGE", player1Words.get(0));
	}

}
