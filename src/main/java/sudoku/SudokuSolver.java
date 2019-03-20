package sudoku;

import java.util.LinkedList;
import java.util.List;

public class SudokuSolver {
    public static final int SIZE = 9;
    public static final int SECTOR_SIZE = 3;
    public static final int ALL = 0b111111111;
    class Cell {
        int value;
        int options;
        Cell(int v) {
            value = v;
            options = 0;
        }

        private Cell(int value, int options) {
            this.value = value;
            this.options = options;
        }

        boolean isSet() {
            return value >= 1 && value <= 9;
        }
        int optCount() {
            int out = 0;
            int test = options;
            while (test > 0) {
                out += (test & 1);
                test >>= 1;
            }
            return out;
        }
        int getFirstOpt() {
            int out = 1;
            int test = options;
            while (test > 0) {
                if ((test & 1) == 1) {
                    return out;
                }
                test >>= 1;
                out++;
            }
            return out;
        }
        String printTabbed() {
            String out = " ";
            if (value > 0) {
                out = ""+value;
            }
            out+=printOpts();
            while (out.length() < 15) {
                out+=" ";
            }
            return out;
        }
        String printOpts() {
            if (options == 0) return "()";
            String out = "(";
            int i=1;
            int test = options;
            while (test > 0) {
                if ((test & 1)==1) {
                    out+=i+",";
                }
                test >>= 1;
                i++;
            }
            return out.substring(0,out.length()-1) + ")";
        }
        List<Move> fetchMoves(int x, int y) {
            List<Move> l = new LinkedList<>();
            int i=1;
            int test = options;
            while (test > 0) {
                if ((test & 1)==1) {
                    l.add(new Move(x, y, i));
                }
                test >>= 1;
                i++;
            }
            return l;
        }
        Cell copy() {
            return new Cell(value,options);
        }
    }
    class Board {
        Cell [][] board;
        Board(Cell [][] b) {
            board = b;
        }

        int row(int y) {
            int out = 0;//bit array of SIZE, other bits assumed to be zero
            for (int x = 0; x < SIZE; x++) {
                if (board[x][y].isSet()) {
                    out |= (1 << (board[x][y].value-1));
                }
            }
            return out;
        }
        int column(int x) {
            int out = 0;//bit array of SIZE, other bits assumed to be zero
            for (int y = 0; y < SIZE; y++) {
                if (board[x][y].isSet()) {
                    out |= (1 << (board[x][y].value-1));
                }
            }
            return out;
        }
        int sector(int x1,int y1) {
            x1 = x1 - x1%SECTOR_SIZE;//align x1
            y1 = y1 - y1%SECTOR_SIZE;//align y1
            int out = 0;//bit array of SIZE, other bits assumed to be zero
            for (int x2 = 0; x2 < SECTOR_SIZE; x2++) {//iterate trough sector
                for (int y2 = 0; y2 < SECTOR_SIZE; y2++) {//iterate trough sector
                    int x = x1+x2;
                    int y = y1+y2;
                    if (board[x][y].isSet()) {
                        out |= (1 << (board[x][y].value - 1));
                    }
                }
            }
            return out;
        }

        boolean updateRow(int y, int valueBit) {
            boolean out = true;
            for (int x = 0; x < SIZE; x++) {
                if (!board[x][y].isSet()) {
                    if ((board[x][y].options & valueBit) > 0) {
                        board[x][y].options -= valueBit;
                    }
                    if (board[x][y].options == 0) {
                        out = false;
                    }
                }
            }
            return out;
        }
        boolean updateColumn(int x, int valueBit) {
            boolean out = true;
            for (int y = 0; y < SIZE; y++) {
                if (!board[x][y].isSet()) {
                    if ((board[x][y].options & valueBit) > 0) {
                        board[x][y].options -= valueBit;
                    }
                    if (board[x][y].options == 0) {
                        out = false;
                    }
                }
            }
            return out;
        }
        boolean updateSector(int x1,int y1, int valueBit) {
            x1 = x1 - x1%SECTOR_SIZE;//align x1
            y1 = y1 - y1%SECTOR_SIZE;//align y1
            boolean out = true;
            for (int x2 = 0; x2 < SECTOR_SIZE; x2++) {//iterate trough sector
                for (int y2 = 0; y2 < SECTOR_SIZE; y2++) {//iterate trough sector
                    int x = x1+x2;
                    int y = y1+y2;
                    if (!board[x][y].isSet()) {
                        if ((board[x][y].options & valueBit) > 0) {
                            board[x][y].options -= valueBit;
                        }
                        if (board[x][y].options == 0) {
                            out = false;
                        }
                    }
                }
            }
            return out;
        }

        boolean invalid(int x, int y) {
            Cell c = board[x][y];
            if (!c.isSet()) {
                int out = row(y) | column(x) | sector(x, y);//calculate all used
                c.options = out ^ ALL;//inverse
                return c.options == 0;
            }
            return false;
        }

        int fetchIfTheOnlyOption(int x, int y) {
            Cell c = board[x][y];
            if ((!c.isSet()) && c.optCount() == 1) {
                return c.getFirstOpt();
            }
            return 0;
        }

        public String print() {
            String out = "";
            for (int x = 0; x < SIZE; x++) {//iterate trough board
                for (int y = 0; y < SIZE; y++) {//iterate trough board
                    out += board[x][y].printTabbed();
                }
                out += "\n";
            }
            return out;
        }

        boolean setValue(int x, int y, int v) {
            Cell c = board[x][y];
            c.value = v;
            c.options = 0;
            int valueBit = 1 << (v-1);
            return updateRow(y,valueBit) & updateColumn(x,valueBit) & updateSector(x,y,valueBit);
        }

        List<Move> fetchBestMoves() throws IllegalStateException{
            //first find the cell with smallest number of moves available
            int maxOpts = 10;
            int tx = 0;
            int ty = 0;
            for (int x = 0; x < SIZE; x++) {
                for (int y = 0; y < SIZE; y++) {
                    int c = board[x][y].optCount();
                    if (c > 0 && c < maxOpts) {
                        maxOpts = c;
                        tx = x;
                        ty = y;
                    }
                }
            }
            //fill the list with moves
            if (maxOpts < 10) {
                return board[tx][ty].fetchMoves(tx, ty);
            }
            return new LinkedList<>();
        }

        boolean step() throws IllegalStateException{
            for (int x = 0; x < SIZE; x++) {//iterate trough board
                for (int y = 0; y < SIZE; y++) {//iterate trough board
                    int v = fetchIfTheOnlyOption(x,y);
                    if (v > 0) {
                        if (setValue(x,y,v)) {
                            return true;
                        } else {
                            throw new IllegalStateException();
                        }
                    }
                }
            }
            return false;
        }
        public Board copy() {
            Cell [][] b = new Cell[SIZE][];

            for (int i = 0; i < board.length; i++) {
                b[i] = new Cell[SIZE];
                for (int j = 0; j < board[i].length; j++) {
                    b[i][j] = board[i][j].copy();
                }
            }

            return new Board(b);
        }
    }
    class Move {
        int x,y,v;
        public Move(int x, int y, int v) {
            this.x = x;
            this.y = y;
            this.v = v;
        }
    }

    private Board board;
    private int recCount = 0;

    public static int parseChar(char v) throws IllegalArgumentException {
        if (v == ' ') {
            return 0;
        }
        if (v >= '1' && v <= '9') {
            return v-'0';
        }
        throw new IllegalArgumentException();
    }

    public SudokuSolver(String[] input) throws IllegalArgumentException {
        if (input.length != SIZE) {
            throw new IllegalArgumentException();
        }

        Cell [][] b = new Cell[SIZE][];

        for (int i = 0; i < input.length; i++) {
            String s = input[i];
            if (s == null || s.length() != SIZE) {
                throw new IllegalArgumentException();
            }

            b[i] = new Cell[SIZE];

            for (int j = 0; j < s.length(); j++) {
                b[i][j] = new Cell(parseChar(s.charAt(j)));
            }
        }

        board = new Board(b);
    }

    public void init() throws IllegalStateException {
        for (int x = 0; x < SIZE; x++) {//iterate trough board
            for (int y = 0; y < SIZE; y++) {//iterate trough board
                if (board.invalid(x,y)) {
                    throw new IllegalStateException();
                }
            }
        }
    }

    public String print() {
        return board.print();
    }

    public boolean microStep() {
        return board.step();
    }

    private void solve(Board b, List<Move> ms) {
        recCount++;
        for(Move m:ms) {
            Board board = b.copy();
            try {
                board.setValue(m.x, m.y, m.v);
                while (board.step()) {
                    //do all micro steps
                }
            } catch (IllegalStateException e) {
                continue;
            }
            List<Move> moves = board.fetchBestMoves();
            if (moves.size() > 0) {
                solve(board, moves);
            } else {
                System.out.println(board.print());
                System.out.println("total steps - "+recCount);
            }
        }
    }

    public void solve() {
        while (board.step()) {
            //do all micro steps
        }
        List<Move> moves = board.fetchBestMoves();
        if (moves.size() > 0) {
            solve(board, moves);
        } else {
            System.out.println(board.print());
        }
    }
}
