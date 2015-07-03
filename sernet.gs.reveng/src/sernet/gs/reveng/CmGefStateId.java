package sernet.gs.reveng;

// Generated Jun 5, 2015 1:28:30 PM by Hibernate Tools 3.4.0.CR1

import java.util.Date;

/**
 * CmGefStateId generated by hbm2java
 */
public class CmGefStateId implements java.io.Serializable {

	private int gefImpId;
	private int gefId;
	private Date cmTimestamp;

	public CmGefStateId() {
	}

	public CmGefStateId(int gefImpId, int gefId, Date cmTimestamp) {
		this.gefImpId = gefImpId;
		this.gefId = gefId;
		this.cmTimestamp = cmTimestamp;
	}

	public int getGefImpId() {
		return this.gefImpId;
	}

	public void setGefImpId(int gefImpId) {
		this.gefImpId = gefImpId;
	}

	public int getGefId() {
		return this.gefId;
	}

	public void setGefId(int gefId) {
		this.gefId = gefId;
	}

	public Date getCmTimestamp() {
		return this.cmTimestamp;
	}

	public void setCmTimestamp(Date cmTimestamp) {
		this.cmTimestamp = cmTimestamp;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof CmGefStateId))
			return false;
		CmGefStateId castOther = (CmGefStateId) other;

		return (this.getGefImpId() == castOther.getGefImpId())
				&& (this.getGefId() == castOther.getGefId())
				&& ((this.getCmTimestamp() == castOther.getCmTimestamp()) || (this
						.getCmTimestamp() != null
						&& castOther.getCmTimestamp() != null && this
						.getCmTimestamp().equals(castOther.getCmTimestamp())));
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + this.getGefImpId();
		result = 37 * result + this.getGefId();
		result = 37
				* result
				+ (getCmTimestamp() == null ? 0 : this.getCmTimestamp()
						.hashCode());
		return result;
	}

}