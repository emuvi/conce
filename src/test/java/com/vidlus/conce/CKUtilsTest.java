package com.vidlus.conce;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CKUtilsTest {

    @Test
    public void testCleanBracketsLinks() {
        // Test null input
        Assertions.assertNull(CKUtils.cleanBracketsLinks(null));

        // Test empty input
        Assertions.assertEquals("", CKUtils.cleanBracketsLinks(""));
        
        // Test string without brackets
        Assertions.assertEquals("No brackets", CKUtils.cleanBracketsLinks("No brackets"));
        
        // Test string with simple bracket
        Assertions.assertEquals("See [[Link]]", CKUtils.cleanBracketsLinks("See [[Link]]"));
        
        // Test cleaning special characters inside brackets
        // / -> -
        // : -> ,
        // ? -> (removed)
        String input = "Check [[Path/To/File:Name?]]";
        String expected = "Check [[Path-To-File,Name]]";
        Assertions.assertEquals(expected, CKUtils.cleanBracketsLinks(input));
        
        // Test cleaning ": " -> " - "
        input = "Read [[Title: Subtitle]]";
        expected = "Read [[Title - Subtitle]]";
        Assertions.assertEquals(expected, CKUtils.cleanBracketsLinks(input));
        
        // Test cleaning multiple brackets
        input = "[[Link One]] and [[Link/Two]]";
        expected = "[[Link One]] and [[Link-Two]]";
        Assertions.assertEquals(expected, CKUtils.cleanBracketsLinks(input));
        
        // Test cleaning extra whitespace inside brackets
        input = "[[  Spaced   Out  ]]";
        expected = "[[Spaced Out]]";
        Assertions.assertEquals(expected, CKUtils.cleanBracketsLinks(input));
        
        // Test that content outside brackets is not affected
        input = "Outside/Slash [[Inside/Slash]]";
        expected = "Outside/Slash [[Inside-Slash]]";
        Assertions.assertEquals(expected, CKUtils.cleanBracketsLinks(input));
        
        // Test replace quotes
        input = "[[Title \"Quoted\"]]";
        expected = "[[Title ”Quoted”]]";
        Assertions.assertEquals(expected, CKUtils.cleanBracketsLinks(input));
    }
}
