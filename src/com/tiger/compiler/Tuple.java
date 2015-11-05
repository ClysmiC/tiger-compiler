package com.tiger.compiler;

public class Tuple<X, Y> {
	public final X x; 
	public final Y y; 
	
	public Tuple(X x, Y y) {
		this.x = x; 
		this.y = y; 
	} 

	@Override
	public String toString()
	{
		return "< " + x.toString() + " , \"" + y.toString() + "\" >";
	}

	@Override
	public boolean equals(Object obj)
	{
		if(obj == null || !(obj instanceof Tuple))
		{
			return false;
		}

		Tuple<?, ?> other = (Tuple<?, ?>)obj;

		return x.equals(other.x) && y.equals(other.y);
	}

	@Override
	public int hashCode()
	{
		return x.hashCode() + y.hashCode();
	}
}