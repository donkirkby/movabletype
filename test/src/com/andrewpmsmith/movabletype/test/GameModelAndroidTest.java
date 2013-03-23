package com.andrewpmsmith.movabletype.test;

import junit.framework.Assert;
import android.test.AndroidTestCase;

import com.andrewpmsmith.movabletype.model.GameModel;


public class GameModelAndroidTest extends AndroidTestCase {
	
	/*
	 * Test that instances can be serialized and restored
	 */
	public void test_serialization() {
		
		// Serialize our game model
		GameModel gm = new GameModel(getContext());
		byte[] s = gm.serialize();
		Assert.assertNotNull(s);
		
		// Delete it
		gm = null;
		System.gc();
		
		// Restore it
		GameModel gm2 = GameModel.deserialize(s, getContext());
		Assert.assertNotNull(gm2);
		
	}
	
	
}
