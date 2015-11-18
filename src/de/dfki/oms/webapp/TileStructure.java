package de.dfki.oms.webapp;


/** A grid of tiles used to build a memory view on the OMS's web interface. */
public class TileStructure
{
	private Tile[][] m_data;
	private int m_rows, m_columns;
	
	/** Constructor.
	 * @param rows Number of rows in the grid.
	 * @param columns Number of columns in the grid.
	 */
	public TileStructure(int rows, int columns)
	{
		this.m_rows = rows;
		this.m_columns = columns;
		m_data = new Tile[rows][columns];
	}
	
	/** Retrieves the number of rows in the grid.
	 * @return Number of rows in the grid.
	 */
	public int getRows() { return m_rows; }
	
	/** Retrieves the number of columns in the grid.
	 * @return Number of columns in the grid.
	 */
	public int getColumns() { return m_columns; }
		
	/** Creates a new tile in the given grid cell. 
	 * 
	 * @param row Row in which to create the new tile. 
	 * @param column Column in which to create the new tile.  
	 * @param text The tile's main text.
	 * @param action The tile's action (should be an {@link URL} or {@link String}). 
	 */
	public void addTile(int row, int column, String text, Object action)
	{
		m_data[row][column] = new Tile(text, action);
	}
	
	/** Adds a new tile in the given grid cell. 
	 * 
	 * @param row Row to which to add the new tile. 
	 * @param column Column to which to add the new tile.  
	 * @param The {@link Tile} to add.
	 */
	public void addTile(int row, int column, Tile tile)
	{
		m_data[row][column] = tile;
	}
	
	/** Retrieves a tile from a specific grid cell.
	 * @param row The tile's row.
	 * @param column The tile's column.
	 * @return The retrieved {@link Tile}.
	 */
	public Tile getTile(int row, int column)
	{
		return m_data[row][column];
	}
}
