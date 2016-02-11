/**
 
 */
package com.igt.fsp.game;

import com.igt.fsp.generated.xml.inp.Game;

/**
 * Game ELEGIDOS
 * 
 *@author lblum@menhir.com.ar
 */
public class FSPGameElegidos extends FSPGame {

	/**
	 * Empty contructor
	 */
	public FSPGameElegidos() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * Contructor
	 * 
	 * @param g The game from XML input
	 */
	public FSPGameElegidos(Game g) {
		super(g);
	}

	/* (non-Javadoc)
	 * @see com.igt.fsp.game.FSPGame#getTitle()
	 */
	public String getCode() {
		return "E";
	}

	/* (non-Javadoc)
	 * @see com.igt.fsp.game.FSPGame#getTitle()
	 */
	public String getTitle() {
		return "ELEGIDOS";
	}

}
