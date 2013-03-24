package com.andrewpmsmith.movabletype.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
	AnagramsGameModelTest.class,
	GameModelTest.class,
	LetterSetTest.class, 
	})
public class PureUnitSuite {

} 