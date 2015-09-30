public class DfaState
{
	private int id;
	private Map<CharClass, DfaState> transitions;

	public DfaTableState(int id)
	{
		this.id = id;
	}
}