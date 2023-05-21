package com.traben.bidmaths

import com.traben.bidmaths.math.ParsedEquation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit testing made to check the validity of Math expression parsing into valid ParsedMathEquation objects
 * notes about tests, results and insight gained, are commented below tests
 *
 * This was run after writing the majority of the math parsing code as a test of the whole system
 */
class MathParserUnitTests {

    //generic method for testing environment to not be affected heavily by any changes in the process
    private fun testAnswer(expression: String): Float {
        return ParsedEquation.parseExpressionAndPrepare(expression).getAnswer().toFloat()
    }


    @Test
    fun addition_isCorrect() {
        assertEquals(4f, testAnswer("2+2"))
        assertEquals(22f, testAnswer("2+20"))
        assertEquals(100f, testAnswer("50+49+1"))
        assertEquals(4f, testAnswer("1+1+1+1"))
        assertEquals(4f, testAnswer("-2+6"))
        //test helped resolve 1 logical error with the last number not being tokenized
        // test passed second attempt
    }

    @Test
    fun floating_point_isCorrect() {
        assertEquals(5f, testAnswer("2.5+2.5"))
        assertEquals(23f, testAnswer("2.8+20.2"))
        assertEquals(100.5f, testAnswer("50+49+1.5"))
        assertEquals(4.4f, testAnswer("1.1+1.1+1.1+1.1"))
        assertEquals(4.5f, testAnswer("-2+6.5"))
        // floating point test passed on first attempt
    }

    @Test
    fun subtraction_isCorrect() {
        assertEquals(-4f, testAnswer("2-6"))
        assertEquals(4f, testAnswer("10-6"))
        assertEquals(4f, testAnswer("2--2"))
        assertEquals(-100f, testAnswer("100-200"))
        assertEquals(0f, testAnswer("-2--2"))
        //subtraction test passed on first attempt
    }

    @Test
    fun multiplication_isCorrect() {
        assertEquals(4f, testAnswer("2*2"))
        assertEquals(25f, testAnswer("5*5"))
        assertEquals(35f, testAnswer("10+5*5"))
        // test passed on first attempt
    }

    @Test
    fun division_isCorrect() {
        assertEquals(3f, testAnswer("6/2"))
        assertEquals(1f, testAnswer("5/5"))
        assertEquals(11f, testAnswer("10+5/5"))
        // test passed on first attempt
    }

    @Test
    fun power_isCorrect() {
        assertEquals(4f, testAnswer("2^2"))
        assertEquals(27f, testAnswer("3^3"))
        assertEquals(54f, testAnswer("2*3^3"))
        assertEquals(-0.25f, testAnswer("-2^-2"))

        assertEquals(-0f, testAnswer("-150^-147"))
        // test failed first attempt, this was actually a result of treating -2^-2 as (-2)^-2 when it is technically -(2^-2)
        // test passed on second attempt
    }

    @Test
    fun bidmas_isCorrect() {
        //sourced common bidmas trouble problems from the internet
        assertEquals(9f, testAnswer("6/2(1+2)"))
        assertEquals(41f, testAnswer("10*4-2*(4^2/4)/2/0.5+9"))
        assertEquals(-18f, testAnswer("-10/(20/4*5/5)*8-2"))
        assertEquals(16f, testAnswer("8/2(2+2)"))
        //test failed on first attempt as I had failed to consider that common notation 2(2) needs to be understood as 2*(2)
        //test passed on second attempt
    }

    private fun doesExpressionFail(expression: String): Boolean {
        return ParsedEquation.parseExpressionAndPrepare(expression).getAnswer().isNaN()
    }

    @Test
    fun invalid_expression_detection_isCorrect() {
        assertTrue(doesExpressionFail("2+"))// incomplete
        assertTrue(doesExpressionFail("2/0")) //   /0
        assertTrue(doesExpressionFail("2/-0")) //   /-0
        assertTrue(doesExpressionFail("two+1"))// words
        assertTrue(doesExpressionFail("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."))
        assertTrue(doesExpressionFail("10*4-2*(4^2/4)/2/0.5+9)"))//extra closing bracket
        assertTrue(doesExpressionFail("10*4-2*(4^2/4)/(2/0.5+9"))//extra open bracket
        assertTrue(doesExpressionFail("2/(99-(100-1))"))//more complex /0 problem
        //test passed on first attempt, error messages for failure tests might need a better look at in future
//        FAILED: [InvalidValue: ADD, doesn't have componenets either side: [[[#:2.0], ADD]]:[[[#:2.0], ADD]]]
//        FAILED: com.traben.bidmaths.maths.MathBinaryExpressionComponent@1807f5a7
//        FAILED: [InvalidValue: two, is not a valid number]
//        FAILED: [InvalidValue: Loremipsumdolorsitamet,consecteturadipiscingelit,seddoeiusmodtemporincididuntutlaboreetdoloremagnaaliqua.Utenimadminimveniam,quisnostrudexercitationullamcolaborisnisiutaliquipexeacommodoconsequat.Duisauteiruredolorinreprehenderitinvoluptatevelitessecillumdoloreeufugiatnullapariatur.Excepteursintoccaecatcupidatatnonproident,suntinculpaquiofficiadeseruntmollitanimidestlaborum., is not a valid number]
//        FAILED: [InvalidValue: nesting did not synchronise with )]
//        FAILED: [InvalidValue: nesting did not synchronise with (]
//        FAILED: com.traben.bidmaths.maths.MathBinaryExpressionComponent@626abbd0
    }


    @Test
    fun random_expression_creation_isCorrect() {
        for (i in 1..250) {
            // note that a soft difficulty for the equations should increase from loop 1 to loop 250
            // its not a hard defined difficulty but some complexity/length/number size differences should be visible
            val exp = ParsedEquation.createRandomExpression((i/12.5).toInt())
            // the print allows a sort of spot check to make sure they look good and they seem to
            // in general increase in (complexity/length/number size) as expected
            println("random output was: ${exp.validExpression}")
            assertTrue(exp.isValid())
        }
    }

}