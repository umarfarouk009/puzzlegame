package view;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.animation.Interpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.preference.PreferenceManager;

import com.eysoft.a8puzzle.GridSelectFragment;
import com.eysoft.a8puzzle.MainActivity;
import com.eysoft.a8puzzle.R;
import com.eysoft.a8puzzle.SettingsActivity;
import com.eysoft.a8puzzle.SettingsFragment;

import AI.AStar;
import AI.HeuristicManhattan;
import AI.SolverMemoDecorator;
import model.Board;
import model.CountUpTimer;
import model.Move;
import model.Path;


public class BoardView extends View implements OnClickListener {

        private static final String TAG = BoardView.class.getSimpleName();
        private MainActivity game;

        private Board board;
        protected int moves;
        private Path shortestPath;
        protected int perfectPlay = -1; // move benchmark set on first call to updateAI
        private final float playerDuration =50; // Piece animation duration on player touch.

        private Map<Character, Piece> pieces;
        private Piece touched;
        private Rect dirty;
        private int rowDelta, colDelta;
        private Animator an;

        private int solving; // Equal to n number of moves from goal when AI is animating solution to puzzle.  Counts down to 0.
        private final MotionEvent cpuTouch = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        private final float aiDuration = 100; // Piece animation duration on cpuTouch.

        private int counterValue = 0;
        SharedPreferences preferenceManager = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        final Handler handler = new Handler();
        public static String highScoreKey = "high_score";
        public static int highestScore = 200;
        boolean timerStart = false;
        boolean timerReset = false;
        boolean firstTouch = true;
        int seconds = 0;
        CountUpTimer timer;

        public BoardView(Context context) {
            super(context);
            game = (MainActivity) context;
        }

        public BoardView(Context context, AttributeSet attrs) {
            super(context, attrs);
            game = (MainActivity) context;
        }

        public void setupPuzzle() {
            setFocusable(true);
            setFocusableInTouchMode(true);

            board = game.solver.generateBoard();
            moves = -1; // incremented to 0 in updateMoves()
            solving = 0;
            an = new Animator(playerDuration, new AccelerateDecelerateInterpolator());

            pieces = createPieces();
            touched = null;
            dirty = new Rect();
            rowDelta = colDelta = 0;

            updateAI();
            updateMoves();
        }

        private Map<Character, Piece> createPieces() {
            Map<Character, Piece> result = new HashMap<Character, Piece>();
            for (int row = 0; row < board.getSize(); ++row)
                for (int col = 0; col < board.getSize(); ++col) {
                    char p = board.getChar(row, col);
                    if (p != Board.BLANK){
                        switch (p) {
                            case 'A':
                                result.put(p, new Piece(String.valueOf("10"), row, col));
                                break;
                            case 'B':
                                result.put(p, new Piece(String.valueOf("11"), row, col));
                                break;
                            case 'C':
                                result.put(p, new Piece(String.valueOf("12"), row, col));
                                break;
                            case 'D':
                                result.put(p, new Piece(String.valueOf("13"), row, col));
                                break;
                            case 'E':
                                result.put(p, new Piece(String.valueOf("14"), row, col));
                                break;
                            case 'F':
                                result.put(p, new Piece(String.valueOf("15"), row, col));
                                break;
                            default:
                                result.put(p, new Piece(String.valueOf(p), row, col));
                        }
                    }
                }
            return result;
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            Piece.game = game;
            Piece.width = w / (float) board.getSize();
            Piece.height = h / (float) board.getSize();

            // TODO: Speed up by only creating bitmaps on game startup rather than for each new puzzle?
            for (Piece piece : pieces.values()) piece.createBitmap();

            // Called only once on game startup
            if (game.outBoard == null) {
                game.outBoard = new TranslateAnimation(0, w, 0, 0);
                game.inBoard = new TranslateAnimation(-w, 0, 0, 0);
                game.outBoard.setDuration(300);
                game.inBoard.setDuration(300);
                game.outBoard.setStartTime(Animation.START_ON_FIRST_FRAME);
                game.inBoard.setStartTime(Animation.START_ON_FIRST_FRAME);
                Interpolator i = new DecelerateInterpolator();
                game.outBoard.setInterpolator(i);
                game.inBoard.setInterpolator(i);
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(Color.TRANSPARENT); // Draw background

            for (Piece piece : pieces.values()) // Draw all non-touched pieces
                if (!piece.equals(touched))
                    canvas.drawBitmap(piece.bitmap, piece.col * Piece.width, piece.row * Piece.height, null);

            // touched is null on game start, animation end, or new puzzle.  Otherwise,
            if (touched != null) { // Draw touched piece last
                float fraction = an.getInterpolatedFraction();
                float left = ((touched.col - colDelta) + (colDelta * fraction)) * Piece.width;
                float top = ((touched.row - rowDelta) + (rowDelta * fraction)) * Piece.height;
                canvas.drawBitmap(touched.bitmap, left, top, null);

                dirty.set((int) left, (int) top, (int) (left + Piece.width + 1.0f), (int) (top + Piece.height + 1.0f));
                if (an.isAnimating()) invalidate(dirty);
                else {
                    // Redraw board on animation end.  Fixes case when quick moves leave board incompletely drawn.
                    touched = null;
                    invalidate(dirty);

                    if (solving > 0) onTouchEvent(cpuTouch);
                    else {
                        checkWin();
                        an.setDuration(playerDuration);
                    }
                }
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            int col, row;
            // If AI is animating solution and event is a cpuTouch, simulate move.
            if (solving > 0 && event.equals(cpuTouch)) {
                --solving;
                Move move = shortestPath.removeFirstMove();
                col = board.getCol(move.fromIndex);
                row = board.getRow(move.fromIndex);

                timerStart = false;

                // If AI is NOT animating solution, then event is a player touch.
            } else if (solving == 0) {
                col = (int) (event.getX() / Piece.width);
                row = (int) (event.getY() / Piece.height);

                final boolean timerEnabled = preferenceManager.getBoolean(SettingsFragment.enableTimePrefKey, false);
                Log.d(TAG, "ENABLE TIME: "+ timerEnabled);
                timerStart = timerEnabled;
                if(firstTouch){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(timerStart){
                                seconds++;
                                counterValue = seconds;
                                game.timerTextView.setText("Time: "+seconds+" s");
                                if(highestScore != 0)
                                    highestScore--;
                                else
                                    highestScore = highestScore + 3;
                            }
                            if(timerReset){
                                seconds = 0;
                                timerReset = false;
                                handler.removeCallbacks(this);
                            }
                            handler.postDelayed(this, 1000);
                        }
                    });
                    firstTouch = false;
                }

                // If AI is animating solution but event is a player touch, then ignore event and return.
            } else return true;

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    Move.Action action = board.getAction(row, col);
                    if (action != null && !an.isAnimating()) {
                        touched = getPiece(row, col);
                        int blankRow = board.getRow(board.blankIndex);
                        int blankCol = board.getCol(board.blankIndex);
                        rowDelta = blankRow - touched.row;
                        colDelta = blankCol - touched.col;

                        float left = touched.col * Piece.width;
                        float top = touched.row * Piece.height;
                        dirty.set((int) left, (int) top, (int) (left + Piece.width + 1.0f), (int) (top + Piece.height + 1.0f));

                        touched.row = blankRow;
                        touched.col = blankCol;
                        board = new Board(board, new Move(board, action));

                        an.start();
                        updateMoves();
                        updateAI();
                        invalidate(dirty);
                    }
                    return true;
                default:
                    return false;
            }
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.shuffle:
                    pause();
                    if(GridSelectFragment.selection.equals("3x3"))
                        newPuzzle("123 456 780");
                    else if(GridSelectFragment.selection.equals("4x4"))
                        newPuzzle("1234 5678 9ABC DEF0");
                    break;
                case R.id.solveBtn:
                    moveAhead(shortestPath.getNumOfMoves());
                    break;
//                case R.id.skipAhead:
//                    moveAhead(5);
//                    break;
            }
        }

        public void newPuzzle(String boardString) {
            game.boardView = new BoardView(game);
            game.goal = new Board(boardString);

            game.solver = new SolverMemoDecorator(new AStar(game.goal, new HeuristicManhattan(game.goal)));

            game.setupBoardView();
            this.startAnimation(game.outBoard);
            game.boardView.startAnimation(game.inBoard);

            ViewGroup parent = (ViewGroup) getParent();
            int index = parent.indexOfChild(this);
            parent.removeView(this);
            parent.addView(game.boardView, index, getLayoutParams());

            handler.removeCallbacksAndMessages(null);
        }

        private void moveAhead(int moves) {
            Log.d(TAG, "moveAhead: moves = " + moves);
            Log.d(TAG, "moveAhead: shortestPath = " + shortestPath);
            Log.d(TAG, "moveAhead: solving = " + solving);

            if (shortestPath != null && solving == 0 && shortestPath.getNumOfMoves() > 0) {
                solving = Math.min(moves, shortestPath.getNumOfMoves());
                an.setDuration(aiDuration);
                onTouchEvent(cpuTouch);
            }
        }

        private Piece getPiece(int row, int col) {
            char p = board.getChar(row, col);
            return pieces.get(p);
        }

        private void checkWin() {
            if (board.equals(game.goal)) {
                game.showMyDialog(MainActivity.DIALOG_WIN);
                timerReset = true;
                Log.d(TAG, "HIGH SCORE: "+ (highestScore));
                int priorHighScore  = preferenceManager.getInt(highScoreKey, 0);
                if (priorHighScore > -1) {
                    if (highestScore > priorHighScore) {
                        preferenceManager.edit().putInt(highScoreKey, (highestScore)).apply();
                        game.highScoreView.setText("High Score: " + (highestScore));
                    }
                }
            }
        }

        private void updateAI() {
            if (solving > 0) setGoalText();
                // TODO: Interrupt previous thread before calling new thread?  For when player moves are faster than solver.
            else new Thread(new Runnable() {
                @Override
                public void run() {
                    shortestPath = game.solver.shortestPath(board);
                    if (perfectPlay == -1) perfectPlay = shortestPath.getNumOfMoves();
                    game.goalView.post(new Runnable() {
                        @Override
                        public void run() { setGoalText(); }
                    });
                }
            }).start();
        }

        private void setGoalText() {
            String spacing = " ";
            int count = shortestPath.getNumOfMoves();
            if (count < 10) spacing = "   ";
            game.goalView.setText(game.getString(R.string.goal) + spacing + count);
        }

        private void updateMoves() {
            ++moves;
            String message = game.getString(R.string.moves) + " " + moves;
            Log.d(TAG, message);
            game.moveView.setText(message);
        }

        public void pause() {
            solving = 0;
        }

    }
