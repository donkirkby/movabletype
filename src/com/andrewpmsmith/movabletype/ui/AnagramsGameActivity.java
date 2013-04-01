package com.andrewpmsmith.movabletype.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import com.andrewpmsmith.movabletype.R;
import com.andrewpmsmith.movabletype.model.AnagramsGameModel;
import com.andrewpmsmith.movabletype.model.AnagramsPlayer;
import com.andrewpmsmith.movabletype.model.WordList;

/**
 * The activity that presents the game board for Anagrams.
 *
 * @author Andrew Smith
 */
public class AnagramsGameActivity extends Activity {

	public final static String EXTRA_GAME_ID = "gameId";

	AnagramsBoard mBoard;
	AnagramsGameModel mGameModel;
	long mSavedGameId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_game);

		Intent intent = getIntent();
		mSavedGameId = intent.getLongExtra(EXTRA_GAME_ID, -1);

//		if (mSavedGameId >= 0) {
//			GameDataBase gdb = new GameDataBase(this);
//			mGameModel = gdb.getGame(mSavedGameId);
//		} else {
		mGameModel = new AnagramsGameModel();
		mGameModel.setWordFinder(new WordList(this));
		
		String letterSource = 
				"AAAAAAAAAAAAAAAABBBBCCCCDDDDDDDDEEEEEEEEEEEEEEEEEEEEEEFFFF" +
				"GGGGGGHHHHHHIIIIIIIIIIIIIIJJKKLLLLLLLLMMMMNNNNNNNNNN" +
				"OOOOOOOOOOOOOOPPPPQQRRRRRRRRRRRRSSSSSSSSTTTTTTTTTTUUUUUUUU" +
				"VVWWXXYYYYZZ";
		char[] letters = letterSource.toCharArray();
        // shuffle
        for (int i = letters.length - 1; i > 0; i--) {
            // int from remainder of deck
            int r = (int) (Math.random() * (i + 1));
            char swap = letters[r];
            letters[r] = letters[i];
            letters[i] = swap;
        }

        mGameModel.setDeck(new String(letters));
		mGameModel.addPlayer(new AnagramsPlayer());
		mGameModel.addPlayer(new AnagramsPlayer());
		for (int i = 0; i < 4; i++) {
			mGameModel.revealLetter();
		}
//		}

		mBoard = new AnagramsBoard(this, mGameModel);
		setContentView(mBoard);

	}

	@Override
	public void onPause() {
		super.onPause();

// TODO:
//		GameDataBase gdb = new GameDataBase(this);
//		if (mGameModel.getGameState() == GameModel.GameState.GAME_OVER) {
//			// Game has finished. Clean up the DB
//			gdb.deleteGame(mSavedGameId);
//		} else if (mSavedGameId < 0) {
//			// Add a new saved game entry
//			mSavedGameId = gdb.addGame(mGameModel);
//			getIntent().putExtra(EXTRA_GAME_ID, mSavedGameId);
//		} else {
//			// Update current saved game
//			gdb.updateGame(mSavedGameId, mGameModel);
//		}
	}

}
