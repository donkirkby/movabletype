package com.andrewpmsmith.movabletype.model;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Manage the state of a game, including the state of the board, current word, 
 * previously played words, and player scores.
 * 
 * An AnagramsGameModel provides the data that is rendered on an AnagramsBoard 
 * object, and accepts user input via an AnagramsBoard object.
 * 
 * @author Don Kirkby
 */
public class AnagramsGameModel implements Serializable {

	private static final long serialVersionUID = 1L;

//	private static final int NUMBER_OF_LETTERS_IN_DECK = 50;
//	private static final int NUMBER_OF_VOWELS_IN_DECK = 10;
//	private static final String CONSONANTS = "BCDFHJKLMNPQRSTVWXYZ";
//	private static final String VOWELS = "AEIOU";
	
	private WordFinder mWordFinder;
	private LetterSet mLetterSet;
	private HashMap<String, AnagramsPlayer> mWordOwners;

	/**
	 * Set the deck of letters to a given list. Resets the game state.
	 * @param deck
	 */
	public void setDeck(String deck) {
		mLetterSet = new LetterSet(deck);
		mWordOwners = new HashMap<String, AnagramsPlayer>();
	}
	
	/**
	 * Reveal the next letter from the deck.
	 * @return the letter
	 */
	public char revealLetter() {
		return mLetterSet.showNextLetter();
	}
	
	/**
	 * Get the list of currently unclaimed letters. Letters are added to this
	 * list by calling revealLetter() and removed by calling makeWord()
	 * or changeWord().
	 * @return
	 */
	public String getUnclaimedLetters() {
		return mLetterSet.getVisibleLetters();
	}

	public void addPlayer(AnagramsPlayer player) {
		
	}

	/**
	 * Make a word from unclaimed letters, and assign it to a player.
	 * @param word The word to make: must be in the dictionary, and makeable
	 * from unclaimed letters.
	 * @param player The player claiming the word.
	 * @return true if the word is accepted.
	 */
	public boolean makeWord(String word, AnagramsPlayer player) {
		if ( ! mWordFinder.wordInDictionary(word)) {
			return false;
		}
		for (int i = 0; i < word.length(); i++) {
			char letter = word.charAt(i);
			if ( ! mLetterSet.reserveLetter(letter)) {
				mLetterSet.releaseReservedLetters();
				return false;
			}
		}
		mLetterSet.hideReservedLetters();
		player.setScore(player.getScore() + word.length());
		mWordOwners.put(word, player);
		return true;
	}

	public WordFinder getWordFinder() {
		return mWordFinder;
	}

	public void setWordFinder(WordFinder mWordFinder) {
		this.mWordFinder = mWordFinder;
	}

	/**
	 * Make a word from a claimed word, and assign it to a player.
	 * @param oldWord The existing word: must be claimed by one of the players.
	 * @param newWord The word to make: must be in the dictionary, and makeable
	 * from all the letters in oldWord, plus zero or more unclaimed letters.
	 * @param player The player claiming the word.
	 * @return true if the word is accepted.
	 */
	public boolean changeWord(
			String oldWord, 
			String newWord,
			AnagramsPlayer player) {
		AnagramsPlayer oldPlayer = mWordOwners.get(oldWord);
		if (oldPlayer == null) {
			return false;
		}
		
		LetterSet oldSet = new LetterSet(oldWord);
		oldSet.showAllRemaining();
		for (int i = 0; 
				i < newWord.length(); i++) {
			char letter = newWord.charAt(i);
			if ( ! oldSet.reserveLetter(letter) && 
					! mLetterSet.reserveLetter(letter)) {
				mLetterSet.releaseReservedLetters();
				return false;
			}
		}
		oldSet.hideReservedLetters();
		if (oldSet.getVisibleLetters().length() > 0) {
			mLetterSet.releaseReservedLetters();
			return false;
		}
		mLetterSet.hideReservedLetters();
		player.setScore(player.getScore() + newWord.length());
		oldPlayer.setScore(oldPlayer.getScore() - oldWord.length());
		return true;
	}
}
