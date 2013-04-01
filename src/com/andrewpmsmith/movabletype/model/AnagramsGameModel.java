package com.andrewpmsmith.movabletype.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

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
	private ArrayList<AnagramsPlayer> mPlayers;

	/**
	 * Set the deck of letters to a given list. Resets the game state.
	 * @param deck
	 */
	public void setDeck(String deck) {
		mLetterSet = new LetterSet(deck);
		mWordOwners = new HashMap<String, AnagramsPlayer>();
		mPlayers = new ArrayList<AnagramsPlayer>();
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

	/**
	 * Add a player to the game.
	 * @param player The player to add.
	 */
	public void addPlayer(AnagramsPlayer player) {
		mPlayers.add(player);
	}

	/**
	 * Get all the players that were added by addPlayer().
	 * @return a list of players.
	 */
	public List<AnagramsPlayer> getPlayers() {
		return mPlayers;
	}

	/**
	 * Get all words claimed by a player.
	 * @param player The player who owns the words.
	 * @return A list of words.
	 */
	public List<String> getWords(AnagramsPlayer player) {
		ArrayList<String> words = new ArrayList<String>();
		for (Entry<String, AnagramsPlayer> entry : mWordOwners.entrySet()) {
			if (entry.getValue() == player) {
				words.add(entry.getKey());
			}
		}
		return words;
	}

	/**
	 * Make a word from unclaimed letters, and assign it to a player.
	 * @param word The word to make.
	 * @param player The player claiming the word.
	 * @throws InvalidWordException When word is not in the dictionary, or
	 * cannot be made from unclaimed letters.
	 */
	public void makeWord(String word, AnagramsPlayer player) throws InvalidWordException {
		if ( ! mWordFinder.wordInDictionary(word)) {
			throw new InvalidWordException(String.format(
					"%s is not in the dictionary.", 
					word));
		}
		for (int i = 0; i < word.length(); i++) {
			char letter = word.charAt(i);
			if ( ! mLetterSet.reserveLetter(letter)) {
				mLetterSet.releaseReservedLetters();
				throw new InvalidWordException(String.format(
						"The letter %c is not available to make %s.", 
						letter,
						word));
			}
		}
		mLetterSet.hideReservedLetters();
		player.setScore(player.getScore() + word.length());
		mWordOwners.put(word, player);
	}

	public WordFinder getWordFinder() {
		return mWordFinder;
	}

	public void setWordFinder(WordFinder mWordFinder) {
		this.mWordFinder = mWordFinder;
	}

	/**
	 * Make a new word from a claimed word, and assign it to a player.
	 * @param oldWord The existing word.
	 * @param newWord The word to make.
	 * @param player The player claiming the word.
	 * @throws InvalidWordException When oldWord has not been claimed, newWord
	 * 		is not in the dictionary or newWord is not makeable from all the 
	 * 		letters in oldWord, plus zero or more unclaimed letters.
	 */
	public void changeWord(
			String oldWord, 
			String newWord,
			AnagramsPlayer player) throws InvalidWordException {
		AnagramsPlayer oldPlayer = mWordOwners.get(oldWord);
		if (oldPlayer == null) {
			throw new InvalidWordException(String.format(
					"%s is not a claimed word.", 
					oldWord));
		}
		
		LetterSet oldSet = new LetterSet(oldWord);
		oldSet.showAllRemaining();
		try
		{
			for (int i = 0; 
					i < newWord.length(); i++) {
				char letter = newWord.charAt(i);
				if ( ! oldSet.reserveLetter(letter) && 
						! mLetterSet.reserveLetter(letter)) {
					throw new InvalidWordException(String.format(
							"%s is not available to make %s.",
							letter,
							newWord));
				}
			}
			oldSet.hideReservedLetters();
			if (oldSet.getVisibleLetters().length() > 0) {
				throw new InvalidWordException(String.format(
						"Some letters of %s were not used in %s.", 
						oldWord,
						newWord));
			}
			mLetterSet.hideReservedLetters();
			mWordOwners.put(newWord, player);
			mWordOwners.remove(oldWord);
		}
		finally {
			mLetterSet.releaseReservedLetters();
		}
		player.setScore(player.getScore() + newWord.length());
		oldPlayer.setScore(oldPlayer.getScore() - oldWord.length());
	}

	public boolean isDeckEmpty() {
		return mLetterSet.getRemainingCount() <= 0;
	}
}
