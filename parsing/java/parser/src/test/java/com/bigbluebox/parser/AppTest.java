package com.bigbluebox.parser;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
        String path1 = "C:\\globalhack\\articles";
        String path2 = "C:\\globalhack";
        assertEquals("/articles", getPath(path1, path2));
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     */
    public void testApp()
    {
        assertTrue( true );
    }
    private String getPath(String canonicalPath, String basePath) {
	String path = canonicalPath.substring(basePath.length());
	if (path.indexOf("\\") != -1) {
	    path = path.replaceAll("\\\\", "/");
	}
	return path;
    }
}
