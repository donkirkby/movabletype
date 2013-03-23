package com.andrewpmsmith.movabletype.model;

import java.io.Serializable;

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
	private char[] mDeck;
	private int mNextTileToReveal;
	private int mNextTileToClaim;

	/**
	 * Set the deck of letters to a given list. Resets the game state.
	 * @param deck
	 */
	public void setDeck(String deck) {
		mDeck = deck.toCharArray();
		mNextTileToReveal = 0;
	}
	
	/**
	 * Reveal the next letter from the deck.
	 * @return the letter
	 */
	public char revealLetter() {
		return mDeck[mNextTileToReveal++];
	}
	
	/**
	 * Get the list of currently unclaimed letters. Letters are add to this
	 * list by calling revealLetter() and removed by calls to makeWord()
	 * or changeWord().
	 * @return
	 */
	public String getUnclaimedLetters() {
		return new String(
				mDeck, 
				mNextTileToClaim, 
				mNextTileToReveal-mNextTileToClaim);
	}

	public void addPlayer(AnagramsPlayer player) {
		
	}

	public boolean makeWord(String word, AnagramsPlayer player) {
		if ( ! mWordFinder.wordInDictionary(word)) {
			return false;
		}
		for (int i = 0; i < word.length(); i++) {
			char letter = word.charAt(i);
			for (int j = mNextTileToClaim; j < mNextTileToReveal; j++) {
				if (letter == mDeck[j]) {
					mDeck[j] = mDeck[mNextTileToClaim++];
				}
			}
		}
		player.setScore(player.getScore() + word.length());
		return true;
	}

	public WordFinder getWordFinder() {
		return mWordFinder;
	}

	public void setWordFinder(WordFinder mWordFinder) {
		this.mWordFinder = mWordFinder;
	}

}
