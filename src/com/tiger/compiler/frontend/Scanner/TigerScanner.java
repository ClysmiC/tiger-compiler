package com.tiger.compiler.frontend.scanner;

import com.tiger.compiler.Output;
import com.tiger.compiler.Tuple;
import com.tiger.compiler.frontend.Token;

import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.List;
import java.io.FileInputStream;

public class TigerScanner
{
	private List<DfaState> dfaTable;
	private int lineNum;
	private int charNum;
    private FileInputStream inputStream;

	private String partialPrefix; //keep track of each line as we parse it, to help w/ error message

	private List<Character> charList;
	
	public TigerScanner(String filename)
    {
		dfaTable = DfaTableGenerator.generateDfa();
		lineNum = 1;
		charNum = 0;
		partialPrefix = "";

		charList = new ArrayList<Character>();


		try {
			FileInputStream fileInput = new FileInputStream(filename);
			int r;
			while ((r = fileInput.read()) != -1) {
				char c = (char) r;
				charList.add(c);
			}

			fileInput.close();
		} catch(Exception e) {
			Output.println("File not found error: " + e.getMessage());
		}

        charList.add('\0'); //add null character at end to denote EOF
	}

	public Tuple<Token, String> nextToken() {
		
		StringBuilder sb = new StringBuilder("");
		DfaState currState = dfaTable.get(0);
		DfaState lastState = dfaTable.get(0);

        //first thing's first, check for EOF.
        //return null in case of EOF
        //do it outside of the loop, because EOF probably occurs
        //after valid token, which needs to detect EOF to know
        //to end the token
        if (charList.get(charNum) == '\0') {
            return new Tuple<Token, String>(Token.EOF, "EOF");
        }


		do {

            //should never happen, since we check for EOF
            //MIGHT happen in an un-closed block comment (since EOF will be considered an OTHER,
            //which is allowed in block comments)
            if(charNum >= charList.size())
				return new Tuple<Token, String>(Token.EOF, "EOF");

			char character = charList.get(charNum);

			if(!partialPrefix.isEmpty() || !Character.isWhitespace(character))
				partialPrefix += character;

			if(character == '\n')
			{
				lineNum++;
				partialPrefix = "";
			}


			charNum++;

			CharClass charClass = CharClass.classOf(character);

			lastState = currState;
			currState = currState.next(charClass);

			sb.append(character);
		} while(!currState.isError());
		charNum--;
		
		char charToDelete = sb.charAt(sb.length() - 1);
		sb.deleteCharAt(sb.length() - 1);

		if(!partialPrefix.isEmpty())
			partialPrefix = partialPrefix.substring(0, partialPrefix.length() - 1);

		if(charToDelete == '\n')
			lineNum--; //"unread" the new line character

		String tokenString = sb.toString();
		
		Token token = Token.classOf(lastState, tokenString);

        //skip over whitespace and block comments and return the next token
        if(token == Token.WHITESPACE || token == Token.BLOCKCOMMENT)
            return nextToken();

		if(token == Token.ERROR)
		{
			tokenString = "Error on line: " + lineNum + "\n" + partialPrefix + charToDelete + "<---";

			//don't reconsume the erroneous token
			charNum++;
		}

		return new Tuple<Token, String>(token, tokenString);
	}

}