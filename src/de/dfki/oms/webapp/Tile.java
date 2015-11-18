package de.dfki.oms.webapp;

/** A tile to be used in the web interface of the OMS. */
public class Tile
{
	protected String m_mainText = null;
	protected Object m_action = null;
	
	/** Constructor. 
	 * @param text The tile's main text.
	 * @param action The tile's action (should be an {@link URL} or {@link String}). 
	 */
	public Tile (String text, Object action)
	{
		this.m_mainText = text;
		this.m_action = action;
	}
	
	/** Retrieves the tile's action. 
	 * @return The action as an {@link Object}. 
	 */
	public Object getAction()
	{
		return m_action;
	}
	
	/** Retrieves the tile's text. 
	 * @return The text as {@link String}. 
	 */
	public String getMainText()
	{
		return m_mainText;
	}
	
	/** Retrieves the tile's text. 
	 * @return The text as {@link String}. 
	 */
	public String toHtml5(String id)
	{
		return m_mainText;
	}
}
