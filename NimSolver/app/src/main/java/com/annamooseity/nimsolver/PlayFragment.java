package com.annamooseity.nimsolver;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

import java.util.Arrays;


/**
 * PlayFragment.java
 * Anna Carrigan
 * Fragment to hold the gameplay view
 */
public class PlayFragment extends Fragment
{
    // Strings for the menu
    private static String saveStr = "Save Game";
    private static String helpStr = "Help!";
    private static String restartStr = "Restart";

    // Parameters for gameplay
    private NimGame game;
    private OnGamePlayListener mListener;
    private NimPileView nimPileView;
    private int numPiles = 5;
    private String[] takeOptions;
    private NimPileView pile1, pile2, pile3, pile4, pile5, pile6, currentHighlightView;
    private String[] selectionArgs;
    private boolean gameOver = false;

    private View thisView;
    private TextView advice;

    private Spinner takeChipsSpinner;
    private Button takeChipsButton;

    String yourMove = "Your Move.";
    String otherPlayerMove = "'s Move.";

    TextView whosMove;

    public boolean changed = false;

    public PlayFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        setHasOptionsMenu(true);
        thisView = inflater.inflate(R.layout.fragment_play, container, false);

        takeChipsSpinner = (Spinner) thisView.findViewById(R.id.takeOptionsSpinner);

        takeChipsSpinner.setAdapter(new ArrayAdapter<String>(getContext(), R.layout.spinner_item, takeOptions));
        takeChipsButton = (Button) thisView.findViewById(R.id.takeTheChipsButton);
        takeChipsButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                takeChips(Integer.parseInt(takeChipsSpinner.getSelectedItem().toString()));
            }
        });

        whosMove = (TextView) thisView.findViewById(R.id.turnDisplay);
        advice = (TextView) thisView.findViewById(R.id.outcomeInfo);

        gameOver = game.checkIfOver();
        selectionArgs = new String[]{Integer.toString(game.getMove()), game.getOtherPlayerName(), Arrays.toString(game.getPiles()), Integer.toString(game.getRulesIndex())};


        // If moveMod is zero, first player's turn
        // Otherwise it's the second player's turn
        setUpPiles();

        int moveMod = game.getMove() % 2;

        if(game.getRules().getFirstPlayer() == 1)
        {
            if(moveMod == 1)
            {
                if(gameOver)
                {
                    whosMove.setText("You won!");
                }
                else
                {
                    whosMove.setText(otherPlayerMove);
                }

            }
            else
            {
                if(gameOver)
                {
                    whosMove.setText(game.getOtherPlayerName() + " won!");
                }
                else
                {
                    whosMove.setText(yourMove);
                }

            }
        }
        else
        {
            if(moveMod == 1)
            {
                if(gameOver)
                {
                    whosMove.setText(game.getOtherPlayerName() + " won!");
                }
                else
                {
                    whosMove.setText(yourMove);
                }
            }
            else
            {
                if(gameOver)
                {
                    whosMove.setText("You won!");
                }
                else
                {
                    whosMove.setText(otherPlayerMove);
                }
            }
        }

        displaySolverInfo();

        return thisView;
    }

    public void setGame(NimGame game)
    {
        this.game = game;
        int[] piles = game.getPiles();
        int numPiles = 6;

        for (int i = 1; i < 7; i++)
        {
            if(piles[i - 1] == 0)
            {
                numPiles = i - 1;
                break;
            }
        }

        this.numPiles = numPiles;

        int[] rawOptions = game.getRules().getTakeOptions();
        takeOptions = new String[rawOptions.length];

        for (int i = 0; i < takeOptions.length; i++)
        {
            takeOptions[i] = Integer.toString(rawOptions[i]);
        }

        otherPlayerMove = game.getOtherPlayerName() + otherPlayerMove;

    }

    private void setHighlighted(NimPileView view)
    {
        if (currentHighlightView != null)
        {
            currentHighlightView.setPileHighlighted(false);



        }

        if(!view.isEmpty())
        {
            view.setPileHighlighted(true);

            currentHighlightView = view;
        }



    }

    /**
     * Method for taking chips from pile
     */

    private void takeChips(int chipsToTake)
    {
        if(currentHighlightView == null)
        {
            Toast.makeText(getActivity(), "Please select a pile to take from.", Toast.LENGTH_SHORT).show();
            return;
        }

        int whichPile = 0;

        if(currentHighlightView.equals(pile1))
        {
            whichPile = 1;
        }
        else if(currentHighlightView.equals(pile2))
        {
            whichPile = 2;
        }
        else if(currentHighlightView.equals(pile3))
        {
            whichPile = 3;
        }
        else if(currentHighlightView.equals(pile4))
        {
            whichPile = 4;
        }
        else if(currentHighlightView.equals(pile5))
        {
            whichPile = 5;
        }
        else if(currentHighlightView.equals(pile6))
        {
            whichPile = 6;
        }

        boolean moveSuccess = game.move(chipsToTake, whichPile - 1);

        if(moveSuccess)
        {
            gameOver = game.checkIfOver();

            if (game.getPiles()[whichPile - 1] == -1)
            {
                currentHighlightView.setCount(-1);
                currentHighlightView.setEmpty();
                currentHighlightView.invalidate();
            }
            else
            {
                currentHighlightView.setCount(game.getPiles()[whichPile - 1]);
            }

            currentHighlightView.setPileHighlighted(false);
            currentHighlightView = null;


            if (whosMove.getText().equals(yourMove))
            {
                if (!gameOver)
                {
                    whosMove.setText(otherPlayerMove);
                }
                else
                {
                    whosMove.setText("You won!");
                }
            }
            else
            {
                if (!gameOver)
                {
                    whosMove.setText(yourMove);
                }
                else
                {
                    whosMove.setText(game.getOtherPlayerName() + " won!");
                }
            }


            if (!gameOver)
            {
                displaySolverInfo();
            }

            changed = true;
        }
        else
        {
            Toast.makeText(getContext(), "Invalid move. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void displaySolverInfo()
    {
        if(gameOver)
        {
            advice.setText("");
            return;
        }
        Solver solver = new Solver(game);

        boolean currentPlayerWins = solver.currentPlayerWins();

        Pair<Integer, Integer> optimalMove = solver.nextMove();

        String str = "With optimal play, ";

        int currentPlayer = 1;
        if(!whosMove.getText().equals(yourMove))
        {
            currentPlayer = 2;
        }

        if(currentPlayer == 1)
        {
            // prep for players next move
            if (currentPlayerWins)
            {
                str = str + "you will win!";
            }
            else
            {
                str = str + game.getOtherPlayerName() + " will win!";
            }
        }
        else
        {
            str = "";
        }

        if(currentPlayerWins && currentPlayer == 1 && !gameOver)
        {
            str = str + "\nTo continue winning, take " + optimalMove.second + " chip(s) from Pile " + (optimalMove.first + 1 + ".");
        }
        else if (gameOver)
        {
            str = "";
        }
        advice.setText(str);
    }

    /**
     * Lays out our nim piles neatly for us
     */

    private void setUpPiles()
    {
        pile1 = (NimPileView) thisView.findViewById(R.id.pile1);
        pile2 = (NimPileView) thisView.findViewById(R.id.pile2);
        pile3 = (NimPileView) thisView.findViewById(R.id.pile3);
        pile4 = (NimPileView) thisView.findViewById(R.id.pile4);
        pile5 = (NimPileView) thisView.findViewById(R.id.pile5);
        pile6 = (NimPileView) thisView.findViewById(R.id.pile6);

        LinearLayout.LayoutParams all = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 3);
        LinearLayout.LayoutParams half = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.5f);

        View.OnClickListener pileListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                NimPileView nimView = (NimPileView) view;
                setHighlighted(nimView);
            }
        };

        switch (numPiles)
        {
            case 1:
                pile2.setVisibility(View.GONE);
                pile3.setVisibility(View.GONE);
                pile4.setVisibility(View.GONE);
                pile5.setVisibility(View.GONE);
                pile6.setVisibility(View.GONE);

                pile1.setOnClickListener(pileListener);

                pile1.setLayoutParams(all);
                break;
            case 2:
                pile3.setVisibility(View.GONE);
                pile4.setVisibility(View.GONE);
                pile5.setVisibility(View.GONE);
                pile6.setVisibility(View.GONE);

                pile1.setLayoutParams(half);
                pile2.setLayoutParams(half);

                pile1.setOnClickListener(pileListener);
                pile2.setOnClickListener(pileListener);

                break;
            case 3:
                pile4.setVisibility(View.GONE);
                pile5.setVisibility(View.GONE);
                pile6.setVisibility(View.GONE);

                pile1.setOnClickListener(pileListener);
                pile2.setOnClickListener(pileListener);
                pile3.setOnClickListener(pileListener);
                break;
            case 4:
                pile5.setVisibility(View.GONE);
                pile6.setVisibility(View.GONE);
                pile1.setOnClickListener(pileListener);
                pile2.setOnClickListener(pileListener);
                pile3.setOnClickListener(pileListener);
                pile4.setOnClickListener(pileListener);
                pile4.setLayoutParams(all);
                break;
            case 5:
                pile6.setVisibility(View.GONE);
                pile4.setLayoutParams(half);
                pile5.setLayoutParams(half);
                pile1.setOnClickListener(pileListener);
                pile2.setOnClickListener(pileListener);
                pile3.setOnClickListener(pileListener);
                pile4.setOnClickListener(pileListener);
                pile5.setOnClickListener(pileListener);
                break;
            default:
                pile1.setOnClickListener(pileListener);
                pile2.setOnClickListener(pileListener);
                pile3.setOnClickListener(pileListener);
                pile4.setOnClickListener(pileListener);
                pile5.setOnClickListener(pileListener);
                pile6.setOnClickListener(pileListener);
                break;
        }

        switch(numPiles)
        {
            case 6:
                if(game.getPiles()[5] == -1)
                {
                    pile6.setEmpty();
                }
                pile6.setCount(game.getPiles()[5]);
            case 5:
                if(game.getPiles()[4] == -1)
                {
                    pile5.setEmpty();
                }
                pile5.setCount(game.getPiles()[4]);
            case 4:
                if(game.getPiles()[3] == -1)
                {
                    pile4.setEmpty();
                }
                pile4.setCount(game.getPiles()[3]);
            case 3:
                if(game.getPiles()[2] == -1)
                {
                    pile3.setEmpty();
                }
                pile3.setCount(game.getPiles()[2]);
            case 2:
                if(game.getPiles()[1] == -1)
                {
                    pile2.setEmpty();
                }
                pile2.setCount(game.getPiles()[1]);
            case 1:
                if(game.getPiles()[0] == -1)
                {
                    pile1.setEmpty();
                }
                pile1.setCount(game.getPiles()[0]);
            default:
                break;
        }
    }

    /**
     * Creates custom options menu
     *
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        MenuItem saveGame = menu.add(saveStr);
        MenuItem help = menu.add(helpStr);
    }

    /**
     * Handles behavior of our custom menu
     *
     * @param item item selected from options
     * @return whether or not the item was in the main dropdown
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        String title = item.getTitle().toString();

        if (title.equals(saveStr))
        {
            saveGame();
            changed = false;
            return false;
        }
        else if (title.equals(helpStr))
        {
            MainActivity.displayHelpDialog(getContext());
            return false;
        }
        else
        {
            return true;
        }
    }

    /**
     * Restarts the game
     */
    private void restart()
    {

    }

    public void saveGame()
    {
        String[] values = {Arrays.toString(game.getPiles()),
                Integer.toString(game.getRulesIndex()),
                game.getOtherPlayerName(),
                Integer.toString(game.getMove())};
        ContentValues cv = MainActivity.createData(NimGame.no_id_projection, values);

        // TODO check if this is right
        // Goes and looks for the game and updates it


        getActivity().getContentResolver().update(NimGame.CONTENT_URI_game, cv,
                NimGame.MOVE + "=? AND " +
                        NimGame.OPPONENT + "=? AND " +
                        NimGame.PILES + "=? AND " +
                        NimGame.RULES_INDEX + "=?", selectionArgs);
        changed = false;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof OnGamePlayListener)
        {
            mListener = (OnGamePlayListener) context;
        }
        else
        {
            throw new RuntimeException(context.toString()
                    + " must implement OnGamePlayListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }


    public interface OnGamePlayListener
    {
        void onGameOver();
    }

    public void checkIfWantToSave()
    {
        if(changed == true)
        {

        }
    }
}
