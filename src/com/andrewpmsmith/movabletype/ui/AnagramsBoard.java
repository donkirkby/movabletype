package com.andrewpmsmith.movabletype.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.andrewpmsmith.movabletype.R;
import com.andrewpmsmith.movabletype.gameframework.ExpandContractAnimation;
import com.andrewpmsmith.movabletype.gameframework.RenderSurface;
import com.andrewpmsmith.movabletype.gameframework.RotationAnimation;
import com.andrewpmsmith.movabletype.gameframework.TextWidget;
import com.andrewpmsmith.movabletype.gameframework.TranslationAnimation;
import com.andrewpmsmith.movabletype.gameframework.Widget;
import com.andrewpmsmith.movabletype.gameframework.WidgetClickListener;
import com.andrewpmsmith.movabletype.gameframework.WidgetDragListener;
import com.andrewpmsmith.movabletype.model.AnagramsGameModel;
import com.andrewpmsmith.movabletype.model.AnagramsPlayer;
import com.andrewpmsmith.movabletype.model.InvalidWordException;

/**
 * Controls the rendering of the board, including the letter deck, the words, 
 * the scores, and the "clear" and "submit" buttons.
 * 
 * The state of the game is managed by a GameModel object passed into the
 * Board's constructor. The Board will render the game contained in the
 * GameModel. It will manage the user interaction, passing turn details to the
 * GameModel.
 * 
 * @author Andrew Smith
 */
public class AnagramsBoard extends RenderSurface implements WidgetClickListener,
		WidgetDragListener {

	private static final double TILE_IN_WORD_SCALE_FACTOR = 0.6;
	private static final float MIN_DRAG_ROTATION = -8f; // degrees
	private static final float MAX_DRAG_ROTATION = 8f;
	private static final int ANIMATION_DURATION = 400; // milliseconds
	private static final int TILE_SHADOW_RADIUS = 10;
	private static final int COLOR_INVISIBLE = 0x00000000;
	private static final int PLACEHOLDER_UNUSED = -1;
//	private final static double MIN_HEIGHT_TO_WIDTH = 1.4;

	private AnagramsGameModel mGameModel;

	private List<Tile> mUnclaimed = new ArrayList<Tile>();
	private List<Tile> mActiveWord = new ArrayList<Tile>();
	private List<Tile> mCapturedWord;
	private HashMap<AnagramsPlayer, Rect> mPlayerBuildingAreas =
			new HashMap<AnagramsPlayer, Rect>();
	private HashMap<AnagramsPlayer, List<List<Tile>>> mPlayerWords =
			new HashMap<AnagramsPlayer, List<List<Tile>>>();
	private AnagramsPlayer mActivePlayer;
	private int mLongestWordSize;
	private int mMaxWordCount; // most words owned by a single player
	private Tile mPlaceHolderTile;
	private int mPlaceHolderIndex = PLACEHOLDER_UNUSED;

	private int mTileWidthInWord;

	private int mGridTop;
	private int mWordTop;
	private int mAddToWordThreshold;

	private static Random mRand = new Random();

	private enum DragAnimation {
		NONE, EXPANDING, CONTRACTING
	};

	private DragAnimation mDragAnimation;
	private ExpandContractAnimation mExpandContractAnimation;

	int mEvenTileColor;
	int mOddTileColor;
	int mPlayer1Color;
	int mPlayer2Color;
	int mPlayer1SurroundedColor;
	int mPlayer2SurroundedColor;
	int mTextColor;
	int mNextColor;
	int mClearColor;
	int mBackgroundColor;
	int mDropShadowColor;

	TextWidget mSubmitButton;
	TextWidget mNextButton;
	TextWidget mClearButton;
	TextWidget mPlayer1Button;
	TextWidget mPlayer2Button;
	TextWidget mPlayer1Score;
	TextWidget mPlayer2Score;

	public AnagramsBoard(Context context, AnagramsGameModel gameModel) {
		super(context);
		mGameModel = gameModel;
		init();
	}

	public AnagramsBoard(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public AnagramsBoard(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public void init() {

		Resources res = getResources();
		mEvenTileColor = res.getColor(R.color.tile_even);
		mOddTileColor = res.getColor(R.color.tile_odd);
		mPlayer1Color = res.getColor(R.color.player1);
		mPlayer2Color = res.getColor(R.color.player2);
		mPlayer1SurroundedColor = res.getColor(R.color.player1_surrounded);
		mPlayer2SurroundedColor = res.getColor(R.color.player2_surrounded);
		mTextColor = res.getColor(R.color.tile_text);
		mNextColor = Color.GREEN;
		mClearColor = Color.rgb(200, 0, 200);
		mBackgroundColor = res.getColor(R.color.background);
		mDropShadowColor = res.getColor(R.color.tile_dropshadow);

		setBackgroundColor(mBackgroundColor);

		String unclaimed = mGameModel.getUnclaimedLetters();
		final List<AnagramsPlayer> players = mGameModel.getPlayers();
		for (AnagramsPlayer player : players) {
			List<List<Tile>> playerWordList = new ArrayList<List<Tile>>();
			mPlayerWords.put(player, playerWordList);
			List<String> words = mGameModel.getWords(player);
			for (String word : words) {
				List<Tile> wordTiles = new ArrayList<Tile>();
				playerWordList.add(wordTiles);
				for (int i = 0; i < word.length(); i++) {
					Tile tile = addTile(word.substring(i, i+1));
					wordTiles.add(tile);
				}
			}
		}
		
		for (int i = 0; i < unclaimed.length(); i++) {
			Tile tile = new Tile(
					-1,
					mEvenTileColor,
					unclaimed.substring(i, i+1),
					mTextColor);
			tile.setClickListener(this);
			tile.setDragListener(this);
			mUnclaimed.add(tile);
			addWidget(tile);
		}
		
		mPlaceHolderTile = new Tile(-1, COLOR_INVISIBLE, null, COLOR_INVISIBLE);
		addWidget(mPlaceHolderTile);

		mPlayer1Button = new TextWidget(
				COLOR_INVISIBLE, 
				"Alice",
				Color.BLACK);
		mPlayer1Button.setClickListener(new WidgetClickListener() {

			@Override
			public void onClick(Widget w) {
				if (mActivePlayer == null) {
					setActivePlayer(players.get(0));
				}
			}

		});
		mPlayer1Button.setColor(mPlayer1Color);
		addWidget(mPlayer1Button);

		mPlayer2Button = new TextWidget(
				mPlayer2Color, 
				"Bob",
				Color.BLACK);
		mPlayer2Button.setClickListener(new WidgetClickListener() {

			@Override
			public void onClick(Widget widget) {
				if (mActivePlayer == null) {
					setActivePlayer(players.get(1));
				}
			}

		});
		addWidget(mPlayer2Button);
		
		mSubmitButton = new TextWidget(
				Color.TRANSPARENT,
				"Submit",
				mTextColor);
		mSubmitButton.setClickListener(new WidgetClickListener() {

			@Override
			public void onClick(Widget widget) {
				if (mActivePlayer != null) {
					submitWord();
				}
			}

		});
		addWidget(mSubmitButton);
		
		mNextButton = new TextWidget(
				mNextColor,
				"Next",
				mTextColor);
		mNextButton.setClickListener(new WidgetClickListener() {

			@Override
			public void onClick(Widget widget) {
				if (mActivePlayer == null) {
					revealLetter();
				}
			}

		});
		addWidget(mNextButton);
		
		mClearButton = new TextWidget(
				Color.TRANSPARENT, 
				"Clear", 
				mTextColor);
		mClearButton.setClickListener(new WidgetClickListener() {
			
			@Override
			public void onClick(Widget widget) {
				if (mActivePlayer != null) {
					setActivePlayer(null);
				}
			}
		});
		addWidget(mClearButton);

		String player1Score = String.valueOf(
				players.get(0).getScore());
		String player2Score = String.valueOf(
				players.get(1).getScore());

		mPlayer1Score = new TextWidget(COLOR_INVISIBLE, player1Score,
				mPlayer1SurroundedColor);
		mPlayer2Score = new TextWidget(COLOR_INVISIBLE, player2Score,
				mPlayer2SurroundedColor);
		addWidget(mPlayer1Score);
		addWidget(mPlayer2Score);
	}

	private Tile addTile(String letter) {
		Tile tile = new Tile(
				-1, 
				mEvenTileColor, 
				letter, 
				mTextColor);
		tile.setClickListener(this);
		tile.setDragListener(this);
		addWidget(tile);
		return tile;
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		// Enforce a minimum height to width ratio for the board

		int measuredWidth = getDefaultSize(getSuggestedMinimumWidth(),
				widthMeasureSpec);
		int measuredHeight = getDefaultSize(getSuggestedMinimumHeight(),
				heightMeasureSpec);

//		final double measuredHeightToWidthRatio = (double) measuredHeight
//				/ measuredWidth;
//
//		if (measuredHeightToWidthRatio < MIN_HEIGHT_TO_WIDTH) {
//			setMeasuredDimension((int) (measuredHeight / MIN_HEIGHT_TO_WIDTH),
//					measuredHeight);
//		} else {
			setMeasuredDimension(measuredWidth, measuredHeight);
//		}

	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		layoutBoard();
	}

	private void layoutBoard() {
		mLongestWordSize = 7; // Always leave room for minimum letters.
		mMaxWordCount = Math.max(
				5, // Always leave room for minimum words.
				(mUnclaimed.size()+2)/3); // leave room for all unclaimed letters.

		for (List<List<Tile>> words : mPlayerWords.values()) {
			// leave room for building a new word.
			mMaxWordCount = Math.max(mMaxWordCount, words.size()+1);
			for (List<Tile> word : words) {
				mLongestWordSize = Math.max(mLongestWordSize, word.size());
			}
		}

		int tileWidth = getWidth() / 2 / 
				(int)Math.round(mLongestWordSize + 2/TILE_IN_WORD_SCALE_FACTOR);
		int tileHeight = getHeight() / (mMaxWordCount + 1);
		if (tileHeight > (int) (tileWidth / TILE_IN_WORD_SCALE_FACTOR)) {
			tileHeight = (int) (tileWidth / TILE_IN_WORD_SCALE_FACTOR);
		}
		else {
			tileWidth = (int) (tileHeight * TILE_IN_WORD_SCALE_FACTOR);
		}

		Tile.widthInGrid = tileHeight;
		Tile.widthInWord = mTileWidthInWord = tileWidth;
		
		AnagramsPlayer player1 = mGameModel.getPlayers().get(0);
		AnagramsPlayer player2 = mGameModel.getPlayers().get(1);
		mPlayerBuildingAreas.put(
				player1, 
				new Rect(0, 0, getWidth()/2-2*tileHeight, tileHeight));
		mPlayerBuildingAreas.put(
				player2, 
				new Rect(getWidth()/2+2*tileHeight, 0, getWidth(), tileHeight));

		for (int i = 0; i < mUnclaimed.size(); ++i) {
			int row = i/3;
			int col = i%3;
			int x = (getWidth() + (2*col-3)*Tile.widthInGrid)/2;
			int y = tileHeight*row;
			mUnclaimed.get(i).applyLayout(x, y, Tile.widthInGrid, tileHeight);
		}
		
		layoutPlayerWords(mGameModel.getPlayers().get(0), 0, tileHeight);
		layoutPlayerWords(
				mGameModel.getPlayers().get(1), 
				getWidth()/2 + 2*tileHeight, 
				tileHeight);

		mPlaceHolderTile.applyLayout(0, mWordTop, Tile.widthInWord, tileHeight);

		mPlayer1Button.applyLayout(
				0, 
				getHeight()-tileHeight, 
				tileHeight, 
				tileHeight);
		mPlayer2Button.applyLayout(
				getWidth() - tileHeight, 
				getHeight()-tileHeight,
				tileHeight, 
				tileHeight);
		mPlayer1Score.applyLayout(
				getWidth() / 4 - tileHeight, 
				getHeight() - tileHeight,
				tileHeight*2, 
				tileHeight);
		mPlayer2Score.applyLayout(
				getWidth() * 3/4,
				getHeight()-tileHeight, 
				tileHeight*2,
				tileHeight);
		mSubmitButton.applyLayout(
				(getWidth() - 3*tileHeight)/2,
				getHeight()-tileHeight,
				tileHeight,
				tileHeight);
		mNextButton.applyLayout(
				(getWidth() - tileHeight)/2,
				getHeight()-tileHeight,
				tileHeight,
				tileHeight);
		mClearButton.applyLayout(
				(getWidth() + tileHeight)/2,
				getHeight()-tileHeight,
				tileHeight,
				tileHeight);
	}

	private void layoutPlayerWords(
			AnagramsPlayer player, 
			int left,
			int tileHeight) {
		int y = 0;
		for (List<Tile> word : mPlayerWords.get(player)) {
			y += tileHeight;
			int x = left;
			for (Tile tile : word) {
				tile.applyLayout(x, y, Tile.widthInWord, tileHeight);
				x += Tile.widthInWord;
			}
		}
	}

	@Override
	public void onClick(Widget w) {
		Tile t = (Tile) w;
		Rect buildingArea = getActiveBuildingArea();
		if (buildingArea == null)
		{
			List<Tile> word = findOwningWord(t);
			for (AnagramsPlayer player : mGameModel.getPlayers()) {
				List<List<Tile>> words = mPlayerWords.get(player);
				if (words.contains(word)) {
					setActivePlayer(player);
					return;
				}
			}
			return;
		}
		
		if (buildingArea.contains(t.getX(), t.getY())) {
			removeTileFromWord(t);
			animateToPosition(
					t, 
					t.mPositionInGrid_x, 
					t.mPositionInGrid_y,
					Tile.widthInGrid);
		} else {
			addTileToWord(t);
			List<Tile> owningWord = findOwningWord(t);
			if (owningWord != null) {
				mCapturedWord = owningWord;
			}
		}
	}

	/**
	 * Pass touch event to widgets, but if that's not a hit, see if a player
	 * is ringing in by touching the background.
	 */
	@Override
	public boolean onTouchEvent(MotionEvent m) {
		if (super.onTouchEvent(m)) {
			return true;
		}
		else {
			if (mActivePlayer == null) {
				for (AnagramsPlayer player : mGameModel.getPlayers()) {
					Rect buildingArea = mPlayerBuildingAreas.get(player);
					if (buildingArea.contains(
							(int) m.getX(), 
							buildingArea.centerY())) {
						setActivePlayer(player);
						return true;
					}
				}
			}
			return false;
		}
	}

	private List<Tile> findOwningWord(Tile tile) {
		for (AnagramsPlayer player : mGameModel.getPlayers()) {
			for (List<Tile> word : mPlayerWords.get(player)) {
				if (word.contains(tile)) {
					return word;
				}
			}
		}
		return null;
	}

	@Override
	public void onDragStart(Widget widget) {

		mDragAnimation = DragAnimation.NONE;
		mPlaceHolderIndex = PLACEHOLDER_UNUSED;

		Tile tile = (Tile) widget;

		removeTileFromWord(tile);

		tile.setShadow(TILE_SHADOW_RADIUS, mDropShadowColor);

		float angle = (mRand.nextFloat() * (Math.abs(MIN_DRAG_ROTATION) + Math
				.abs(MAX_DRAG_ROTATION))) + MIN_DRAG_ROTATION;
		tile.cancelAllAnimations();
		tile.addAnimation(new RotationAnimation(tile, ANIMATION_DURATION, angle));
	}

	@Override
	public void onDragEnd(Widget widget) {

		Tile tile = (Tile) widget;

		tile.setShadow(0, 0);

		tile.cancelAllAnimations();
		tile.addAnimation(new RotationAnimation(tile, ANIMATION_DURATION, 0));

		if (widget.getY() <= mAddToWordThreshold
				&& mPlaceHolderIndex != PLACEHOLDER_UNUSED) {
			mActiveWord.add(mPlaceHolderIndex, tile);
		} else {
			animateToPosition(tile, tile.mPositionInGrid_x,
					tile.mPositionInGrid_y, Tile.widthInWord);
		}

		// remove the place holder tile
		mActiveWord.remove(mPlaceHolderTile);
		mPlaceHolderIndex = PLACEHOLDER_UNUSED;
		presentWord();

	}

	@Override
	public void onDrag(Widget widget, int x, int y) {

		Tile tile = (Tile) widget;

		// Reposition tile according to drag coordinates
		tile.setX(x);
		tile.setY(y);

		// When dragged above the grid, resize the tile to fit in the word
		if (y < mGridTop && mDragAnimation != DragAnimation.CONTRACTING) {

			mDragAnimation = DragAnimation.CONTRACTING;
			expandContractTile(tile, Tile.widthInWord);

		} else if (y >= mGridTop && mDragAnimation != DragAnimation.EXPANDING) {

			mDragAnimation = DragAnimation.EXPANDING;
			expandContractTile(tile, Tile.widthInGrid);

		}

		// When dragged above the grid, make space in the word for the tile
		if (y < mGridTop) {

			int word_begin = (int) (getWidth() / 2 - (Tile.widthInWord / 2.0)
					* mActiveWord.size());
			int word_end = word_begin + Tile.widthInWord * mActiveWord.size();

			int index;
			if (x < word_begin) {
				index = 0;
			} else if (x > word_end) {
				index = mActiveWord.size();
			} else {
				index = (int) (x - (word_begin - Tile.widthInWord / 2.0))
						/ Tile.widthInWord;
			}

			if (index != mPlaceHolderIndex) {
				mPlaceHolderIndex = index;
				mActiveWord.remove(mPlaceHolderTile);

				mActiveWord.add(Math.min(index, mActiveWord.size()), mPlaceHolderTile);
				presentWord();
			}
		}
	}

	private void expandContractTile(Tile tile, int newWidth) {
		if (mExpandContractAnimation != null)
			tile.cancelAnimation(mExpandContractAnimation);
		mExpandContractAnimation = new ExpandContractAnimation(tile,
				ANIMATION_DURATION, newWidth);
		tile.addAnimation(mExpandContractAnimation);
	}

	private void animateToPosition(Tile tile, int x, int y, int width) {
		tile.cancelAllAnimations();
		tile.addAnimation(new RotationAnimation(tile, ANIMATION_DURATION, 0));
		tile.addAnimation(new TranslationAnimation(tile, ANIMATION_DURATION, x,
				y));
		tile.addAnimation(new ExpandContractAnimation(tile, ANIMATION_DURATION,
				width));
	}

	private void addTileToWord(Tile tile) {
		mActiveWord.remove(tile);
		mActiveWord.add(tile);
		presentWord();
	}

	private void removeTileFromWord(Tile tile) {
		mActiveWord.remove(tile);
		presentWord();
	}

	private void returnAllTilesToGrid() {

		ListIterator<Tile> it = mActiveWord.listIterator();
		while (it.hasNext()) {
			Tile t = it.next();
			it.remove();
			animateToPosition(t, t.mPositionInGrid_x, t.mPositionInGrid_y,
					Tile.widthInGrid);
		}

	}

	private void presentWord() {
		Rect buildingArea = getActiveBuildingArea();
		if (buildingArea == null) {
			return;
		}
		if (mActiveWord.size() > mLongestWordSize)
		{
			mLongestWordSize = mActiveWord.size();
			layoutBoard();
		}

		for (int i = 0; i < mActiveWord.size(); ++i) {

			Tile t = mActiveWord.get(i);
			int x = buildingArea.left + Tile.widthInWord * i;
			int y = buildingArea.top;

			animateToPosition(t, x, y, Tile.widthInWord);
		}
	}

	private Rect getActiveBuildingArea() {
		Rect buildingArea =
				mActivePlayer == null
				? null
				: mPlayerBuildingAreas.get(mActivePlayer);
		return buildingArea;
	}
	
	private void setActivePlayer(AnagramsPlayer player) {
		mActivePlayer = player;
		if (player == null) {
			returnAllTilesToGrid();
			mCapturedWord = null;
			mPlayer1Button.setColor(mPlayer1Color);
			mPlayer2Button.setColor(mPlayer2Color);
			mSubmitButton.setColor(COLOR_INVISIBLE);
			mNextButton.setColor(mNextColor);
			mClearButton.setColor(COLOR_INVISIBLE);
		}
		else {
			if (player == mGameModel.getPlayers().get(0)) {
				mPlayer2Button.setColor(COLOR_INVISIBLE);
			}
			else {
				mPlayer1Button.setColor(COLOR_INVISIBLE);
			}
			mSubmitButton.setColor(mNextColor);
			mNextButton.setColor(COLOR_INVISIBLE);
			mClearButton.setColor(Color.rgb(200, 0, 200));
		}
	}

	private void submitWord() {

		final Resources res = getResources();
		final String dismiss = res.getString(R.string.dismiss_message);
		try {
			if (mCapturedWord != null) {
				mGameModel.changeWord(
						buildWord(mCapturedWord), 
						buildWord(mActiveWord), 
						mActivePlayer);
				for (AnagramsPlayer player : mGameModel.getPlayers()) {
					mPlayerWords.get(player).remove(mCapturedWord);
				}
			}
			else {
				mGameModel.makeWord(buildWord(mActiveWord), mActivePlayer);
			}
			mPlayerWords.get(mActivePlayer).add(mActiveWord);

			for (Tile tile : mActiveWord) {
				mUnclaimed.remove(tile);
			}
			mActiveWord = new ArrayList<Tile>();
			setActivePlayer(null);

			mPlayer1Score.setText(String.valueOf(
					mGameModel.getPlayers().get(0).getScore()));
			mPlayer2Score.setText(String.valueOf(
					mGameModel.getPlayers().get(1).getScore()));
			
			layoutBoard();

		}
		catch (InvalidWordException ex) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

			builder.setMessage(ex.getMessage())
				.setPositiveButton(dismiss, null)
				.show();
		}

	}

	private String buildWord(List<Tile> tiles) {
		StringBuilder word = new StringBuilder();
		for (Tile tile : tiles) {
			word.append(tile.getText());
		}
		String wordText = word.toString();
		return wordText;
	}
	
	private void revealLetter() {
		if (mGameModel.isDeckEmpty()) {
			return;
		}
		char letter = mGameModel.revealLetter();
		Tile tile = addTile(String.valueOf(letter));
		mUnclaimed.add(tile);
		mMaxWordCount = Math.max(mMaxWordCount, mUnclaimed.size());
		layoutBoard();
		if (mGameModel.isDeckEmpty()) {
			mNextButton.setColor(Color.TRANSPARENT);
		}
	}
}
