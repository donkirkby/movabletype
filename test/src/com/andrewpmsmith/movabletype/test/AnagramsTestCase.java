package com.andrewpmsmith.movabletype.test;

import java.util.Arrays;

import junit.framework.TestCase;

public abstract class AnagramsTestCase extends TestCase {

	public AnagramsTestCase() {
		super();
	}

	public AnagramsTestCase(String name) {
		super(name);
	}

	/**
	 * Assert that two strings contain the same set of letters, where order
	 * doesn't matter.
	 * @param message A description of the contents
	 * @param expected The expected set of letters
	 * @param actual The actual set of letters
	 */
	public void assertEquivalent(String message, String expected, String actual) {
		char[] expectedLetters = expected.toCharArray();
		char[] actualLetters = actual.toCharArray();
		Arrays.sort(expectedLetters);
		Arrays.sort(actualLetters);
		assertEquals(
				message, 
				new String(expectedLetters), 
				new String(actualLetters));
	}
	
	public interface Testable {
		public void run() throws Exception;
	}

	public <T extends Exception> T assertThrows(
			final Class<T> expected, 
			final Testable codeUnderTest) throws Exception {
		T result = null;
        try {
            codeUnderTest.run();
            fail("Expecting exception but none was thrown.");
        } catch(final Exception actual) {
            if (expected.isInstance(actual)) {
            	result = expected.cast(actual);
            }
            else {
                throw actual;
            }
        }
        return result;
    }
}