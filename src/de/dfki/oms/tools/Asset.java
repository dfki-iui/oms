package de.dfki.oms.tools;

/** Models a product with manufacturing information. */
public class Asset { // TODO This class is only used in AssetSoapCreator, which is not used anywhere
	
	String m_manufacturerName, m_manufacturerPartNo, m_serialNo;
	
	public Asset(String manufacturerName, String manufacturerPartNo, String serialNo)
	{
		m_manufacturerName = manufacturerName;
		m_manufacturerPartNo = manufacturerPartNo;
		m_serialNo = serialNo;
	}
	
	/** Retrieves the name of this asset's manufacturer. 
	 * @return The name as {@link String}.
	 */
	public String getManufacturerName()
	{
		return m_manufacturerName;
	}
	
	/** Retrieves the asset's manufacturer part number. 
	 * @return The number as {@link String}.
	 */
	public String getManufacturerPartNo()
	{
		return m_manufacturerPartNo;
	}
	
	/** Retrieves the asset's serial number. 
	 * @return The number as {@link String}.
	 */
	public String getSerialNo()
	{
		return m_serialNo;
	}
}
