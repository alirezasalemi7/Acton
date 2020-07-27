package acton.compiler.main.symbolTable;

public abstract class SymbolTableItem {
	protected String name;

	public SymbolTableItem() {
	}

	public abstract String getKey();
}