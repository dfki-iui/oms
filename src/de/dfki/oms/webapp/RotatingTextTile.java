package de.dfki.oms.webapp;

/** A {@link Tile} with additional alternative text. */
public class RotatingTextTile extends Tile
{
	protected String m_alternativeText = null;
	
	/** Constructor. 
	 * @param text The tile's main text.
	 * @param alternativeText The tile's alternative text.
	 * @param action The tile's action (should be an {@link URL} or {@link String}). 
	 */
	public RotatingTextTile(String text, String alternativeText, Object action)
	{
		super(text, action);
		this.m_alternativeText = alternativeText;
	}
	
	/** Retrieves the tile's alternative text. 
	 * @return The text as {@link String}. 
	 */
	public String getAlternativeText()
	{
		return m_alternativeText;
	}
	
	/** Retrieves the tile's text and alternative text as an HTML5 snippet with a scripted effect. 
	 * @return The text as an HTML {@link String}. 
	 */
	@Override
	public String toHtml5(String id)
	{
		StringBuilder sb = new StringBuilder();
		String time = "2.0";
				
		/*sb.append("<script type=\"text/javascript\">\n" +	          
		          "setText('"+id+"', '"+time+"', '"+m_mainText+"', '"+m_alternativeText+"');\n" +
		          "</script>\n");*/
		
		// two line text
		sb.append("<div style=\"\" id=\""+id+"_1\"><div>"+m_mainText+"</div></div>");
		sb.append("<div id=\""+id+"_2\" style=\"display:none;\"><span>"+m_alternativeText+"</span></div>");
		sb.append("<script>Effect.SlideDown('"+id+"_2', { duration: "+time+" }); </script>");
		
		
		/*sb.append("<div id=\""+id+"_1\" style=\"\"><span>"+m_mainText+"</span></div>");
		sb.append("<div id=\""+id+"_2\" style=\"display:none;\"><span>"+m_alternativeText+"</span></div>");
		sb.append("<script> Effect.Appear('"+id+"_2', { duration: "+time+" }); Effect.SlideDown('"+id+"_2', { duration: 1.0 }); Effect.SlideUp('"+id+"_1', { duration: 1.0 }); Effect.Fade('"+id+"_1', { duration: "+time+" });</script>");*/
		
		/*sb.append("<canvas style=\"\" id=\""+id+"_canvas\"></canvas> \n");
		sb.append("<script> \n" +
				"var canvas = document.getElementById(\""+id+"_canvas\"); \n" +
				"if (canvas.getContext) \n" +
				"{ " +
					"var ctx = canvas.getContext(\"2d\"); \n" +
					"ctx.fillStyle = \"rgb(255, 255, 255)\"; \n" +
					"ctx.font = \"40px Segoe UI, Verdana, Arial, Helvetica, sans-serif\"; \n" +
					"var size = ctx.measureText(\""+m_mainText+"\"); \n" +
					"ctx.fillText(\""+m_mainText+"\", canvas.width/2 - size.width/2, 98); \n" +
				"} \n" +
				"</script> \n");*/
		
		return sb.toString();
	}
}
