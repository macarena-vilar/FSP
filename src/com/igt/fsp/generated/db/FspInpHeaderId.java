package com.igt.fsp.generated.db;

// Generated 11/06/2015 18:14:23 by Hibernate Tools 4.0.0

/**
 * FspInpHeaderId generated by hbm2java
 */
public class FspInpHeaderId implements java.io.Serializable {

	private char gameid;
	private long drawnr;

	public FspInpHeaderId() {
	}

	public FspInpHeaderId(char gameid, long drawnr) {
		this.gameid = gameid;
		this.drawnr = drawnr;
	}

	public char getGameid() {
		return this.gameid;
	}

	public void setGameid(char gameid) {
		this.gameid = gameid;
	}

	public long getDrawnr() {
		return this.drawnr;
	}

	public void setDrawnr(long drawnr) {
		this.drawnr = drawnr;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof FspInpHeaderId))
			return false;
		FspInpHeaderId castOther = (FspInpHeaderId) other;

		return (this.getGameid() == castOther.getGameid())
				&& (this.getDrawnr() == castOther.getDrawnr());
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + this.getGameid();
		result = 37 * result + (int) this.getDrawnr();
		return result;
	}

}
