/**
 * 
 */
package com.igt.fsp.game;

import com.igt.fsp.generated.xml.inp.Game;

/**
 * @author lblum
 *
 */
public class FSPGameElegidosTeeTe extends FSPGame {
	
	/**
	 * Empty contructor
	 */
	public FSPGameElegidosTeeTe() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * Contructor
	 * 
	 * @param g The game from XML input
	 */
	public FSPGameElegidosTeeTe(Game g) {
		super(g);
	}

	/* (non-Javadoc)
	 * @see com.igt.fsp.game.FSPGame#getTitle()
	 */
	public String getCode() {
		return "T";
	}

	/* (non-Javadoc)
	 * @see com.igt.fsp.game.FSPGame#getTitle()
	 */
	public String getTitle() {
		return "ELEGIDOS TEETE";
	}

}
