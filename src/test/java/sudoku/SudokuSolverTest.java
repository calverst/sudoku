package sudoku;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class SudokuSolverTest
{
    @Test
    public void test1() {
        SudokuSolver s = new SudokuSolver(new String[]
                {
                        "4        ",
                        " 8     4 ",
                        " 3 2 81 5",
                        "3  91 2  ",
                        "64   59  ",
                        "7  38 4  ",
                        " 7 6 13 4",
                        " 1     2 ",
                        "5        "});
        s.init();
        System.out.println(s.print());
        while (s.microStep()) {
            System.out.println(s.print());
        }
    }
    @Test
    public void test2() {
        SudokuSolver s = new SudokuSolver(new String[]
                {
                        "8        ",
                        "  36     ",
                        " 7  9 2  ",
                        " 5   7   ",
                        "    457  ",
                        "   1   3 ",
                        "  1    68",
                        "  85   1 ",
                        " 9    4  "});
        s.init();
        s.solve();
    }
}
