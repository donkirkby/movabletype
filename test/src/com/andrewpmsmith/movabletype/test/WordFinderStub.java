package com.andrewpmsmith.movabletype.test;

import java.util.Arrays;

import com.andrewpmsmith.movabletype.model.WordFinder;

/**
 * Allows testing our game model without having to load up a full context to
 * get at the word database. Just specify a small set of test words with the
 * constructor.
 * @author don
 *
 */
public class WordFinderStub implements WordFinder {
	private String[] mWords;

	public WordFinderStub(String[] words) {
		mWords = new String[words.length];
		for (int i = 0; i < words.length; i++) {
			mWords[i] = words[i];
		}
		Arrays.sort(mWords);
	}

	@Override
	public boolean wordInDictionary(String word) {
		return Arrays.binarySearch(mWords, word) >= 0;
	}

}
