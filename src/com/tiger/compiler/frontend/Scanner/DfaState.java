package com.tiger.compiler.frontend.scanner;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;


public class DfaState
{
	private int id;
	private Map<CharClass, DfaState> transitions;

	public static final DfaState ERROR_STATE = new DfaState(-1);

	public DfaState(int id)
	{
		this.id = id;
		transitions = new HashMap<>();
	}

	public void addTransition(CharClass character, DfaState newState)
	{
		if(id == -1)
			return; //can't add transitions out of the error state

		transitions.put(character, newState);
	}

	/**
	 * For testing generator
	 */
	public String toString()
	{
		String str = (id + "\t");

		Set<CharClass> keys = transitions.keySet();

		for(CharClass key: keys)
		{
			int transitionTo = transitions.get(key).id;

			str += (key + ":" + transitionTo + " ");
		}

		return str;
	}

	public DfaState next(CharClass charClass) {
		return transitions.get(charClass);
	}

	public boolean isError() {
		return id == -1;
	}

	public int id() {
		return id;
	}
}