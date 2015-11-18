/**
 * 
 */
package de.dfki.adom.rest.nodes;

import org.restlet.routing.Router;

import de.dfki.adom.rest.ADOMeRestlet;

/** <p>A {@link Component} to handle the block storage of an object memory via the REST interface.</p>
 * <p>Represents the "st" node in the REST tree and handles HTTP requests going there.</p>
 * 
 * @author Christian Hauck
 * @organization DFKI
 */
public class Storage extends Component {

	protected Toc	     m_toc;
	protected BlockIDs m_blockIDs;
	protected Block    m_block;
	protected Header   m_header;
	
	/** Constructor.
	 * @param adom The {@link ADOMeRestlet} to which the storage belongs. 
	 */
	public Storage(ADOMeRestlet adom) {
		super(adom);
		m_toc      = new Toc(adom);
		m_blockIDs = new BlockIDs(adom);
		m_block    = new Block(adom);
		m_header   = new Header(adom);
	}

	
	/**
	 * Attach this object and its subcomponents to the given router.
	 * @param router
	 */
	@Override
	public void attach(Router router, String path) {

		super.attach(router, path);
		
		m_toc.attach(router, path + "/toc");
		m_blockIDs.attach(router, path + "/block_ids");
		m_block.attach(router, path + "/block");
		m_header.attach(router, path + "/header");
	}
	
	
	@Override
	public void detach(Router router) {
		m_toc.detach(router);
		m_blockIDs.detach(router);
		m_block.detach(router);
		m_header.detach(router);
		
		super.detach(router);
	}
	
	
	/**
	 * Delivers the Table of Contents object of the ADOM storage.
	 * @return table of contents object.
	 */
	public Toc getToc() {
		return m_toc;
	}
	
	
	/**
	 * Delivers the BlockIDs object of the ADOM storage.
	 * @return BlockIDs object.
	 */
	public BlockIDs getBlockIDs() {
		return m_blockIDs;
	}
	
		
	/**
	 * Delivers the Block object of the ADOM storage.
	 * @return Block object.
	 */
	public Block getBlock() {
		return m_block;
	}
}
