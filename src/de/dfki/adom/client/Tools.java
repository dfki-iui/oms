package de.dfki.adom.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/** Helpful methods to perform frequent tasks. */
public class Tools {
	
	/** Reads a file and returns its contents as String. 
	 * @param path Path to the file, without its name.
	 * @param filename The file's name.
	 * @return The file's content.
	 */
	public static String Read(String path, String filename) {
		File file = new File(path + filename);
		StringBuffer contents = new StringBuffer();
		BufferedReader reader = null;
		
		try {           
			reader = new BufferedReader(new FileReader(file));     
			String text = null;     
			while ((text = reader.readLine()) != null) {         
				contents.append(text).append(System.getProperty("line.separator")); 
			}       
		} catch (FileNotFoundException e) {         
			e.printStackTrace();      
		} catch (IOException e) {   
			e.printStackTrace();    
		} finally {          
			try {             
				if (reader != null) {   
					reader.close();     
				}          
			} catch (IOException e) {  
				e.printStackTrace();     
			}     
		}
		
		return contents.toString();
	}

	
}
