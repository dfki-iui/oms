package de.dfki.oms.tools;

import java.util.List;

/** Provides a method that creates a SOAP XML text containing a list of {@link Asset}s. */
public class AssetSoapCreator { // TODO This class is not used anywhere
	
	/** Creates a SOAP XML String containing the {@link Asset}s of interest.
	 * @param assetList A {@link List} of {@link Asset}s to include in the SOAP XML text. 
	 * @return SOAP XML text as {@link String}. 
	 */
	public static String create(List<Asset> assetList)
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">");
		sb.append("\t<soap:Body>");
		sb.append("\t\t<AssetsOfInterest>");
		
		for(Asset asset : assetList)
		{
			sb.append("\t\t\t<Asset>");
			sb.append("\t\t\t\t<ManufacturerName>"+asset.getManufacturerName()+"</ManufacturerName>");
			sb.append("\t\t\t\t<ManufacturerPartNo>"+asset.getManufacturerPartNo()+"</ManufacturerPartNo>");
			sb.append("\t\t\t\t<SerialNo>"+asset.getSerialNo()+"</SerialNo>");
			sb.append("\t\t\t</Asset>");
		}
		
		sb.append("\t\t</AssetsOfInterest>");
		sb.append("\t</soap:Body>");
		sb.append("</soap:Envelope>");
		
		return sb.toString();
	}
}
