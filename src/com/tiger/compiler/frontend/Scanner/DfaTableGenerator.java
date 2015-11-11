package com.tiger.compiler.frontend.scanner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DfaTableGenerator
{
	public final static String TABLE_FILENAME = "res/DfaTable.csv";

	public static List<DfaState> generateDfa()
	{
		List<DfaState> dfa = new ArrayList<>();
		Scanner input = null;

		try
		{
			input = new Scanner(new File(TABLE_FILENAME));
		}
		catch(Exception e)
		{
			return null;
		}

		//this row lists all the character classes
		String row0 = input.nextLine();
		Scanner rowScanner = new Scanner(row0).useDelimiter(",");

		//record which column each character class corresponds with
		//so when we see transitions, we know what character class to map them to
		List<CharClass> charClasses = new ArrayList<>();

		while(rowScanner.hasNext())
		{
			String columnClass = rowScanner.next();
			charClasses.add(CharClass.valueOf(columnClass));
		}

		List<DfaState> states = new ArrayList<>();


		/*************
		Next, count # of DFA states so we can initialize one for each state, since
		we need a reference to a state to add it as a transition to another state 
		*************/
		int numStates = 0;

		while(input.hasNext())
		{
			input.nextLine();

			states.add(new DfaState(numStates));
			numStates++;
		}


		//reinitialize scanner, and discard first row
		try
		{
			input = new Scanner(new File(TABLE_FILENAME));
		}
		catch(Exception e)
		{
			return null;
		}

		input.nextLine();


		/***************Each row from here below is a DFA state***************/
		while(input.hasNext())
		{
			String row = input.nextLine();
			rowScanner = new Scanner(row).useDelimiter(",");

			int id = new Integer(rowScanner.next());

			DfaState state = states.get(id);

			int column = 0;
			while(rowScanner.hasNext())
			{
				String transitionToString = rowScanner.next();
				DfaState transitionTo;

				if(transitionToString.equals("e"))
				{
					transitionTo = DfaState.ERROR_STATE;
				}
				else
				{
					transitionTo = states.get(new Integer(transitionToString));
				}

				state.addTransition(charClasses.get(column), transitionTo);

				column++;
			}

			dfa.add(state);
		}


		rowScanner.close();
		input.close();
		return dfa;
	}

	/**
	 * This main method is purely for testing. Not used by the compiler.
	 *
	 */
	// public static void main(String[] args)
	// {
	// 	List<DfaState> dfa = generateDfa();

	// 	for(DfaState state: dfa)
	// 	{
	// 		System.out.println(state);
	// 	}

	// 	System.out.println("Complete");
	// }
}