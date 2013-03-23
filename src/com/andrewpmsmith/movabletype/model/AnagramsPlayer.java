package com.andrewpmsmith.movabletype.model;

import java.io.Serializable;

public class AnagramsPlayer implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int mScore;

	public int getScore() {
		return mScore;
	}

	public void setScore(int score) {
		mScore = score;
	}

}
