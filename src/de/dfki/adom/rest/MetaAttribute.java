/**
 * 
 */
package de.dfki.adom.rest;

/** Enumerator describing all known metadata attributes an OMMBlock can have. 
 * 
 * @author Christian Hauck
 * @organization DFKI
 */
public enum MetaAttribute {
	
	NameSpace,
	Creation,
	Contribution,
	Title,
	Description,
	Format,
	Subject,
	Type,
	Link,
	PreviousBlock,
	Certificate;
	
	public static MetaAttribute fromString(String meta)
	{
		if (meta.equals("namespace"))    return NameSpace;
		if (meta.equals("creation"))     return Creation;
		if (meta.equals("contribution")) return Contribution;
		if (meta.equals("title"))        return Title;
		if (meta.equals("description"))  return Description;
		if (meta.equals("format"))       return Format;
		if (meta.equals("type"))         return Type;
		if (meta.equals("link"))         return Link;
		if (meta.equals("subject"))      return Subject;
		if (meta.equals("certificate"))      return Certificate;
		if (meta.equals("previousBlock"))      return PreviousBlock;

		return null;
	}
}

