package sernet.gs.reveng;

// Generated Jun 5, 2015 1:28:30 PM by Hibernate Tools 3.4.0.CR1

/**
 * SysPropertiesId generated by hbm2java
 */
public class SysPropertiesId implements java.io.Serializable {

	private int impId;
	private String propName;
	private String host;

	public SysPropertiesId() {
	}

	public SysPropertiesId(int impId, String propName, String host) {
		this.impId = impId;
		this.propName = propName;
		this.host = host;
	}

	public int getImpId() {
		return this.impId;
	}

	public void setImpId(int impId) {
		this.impId = impId;
	}

	public String getPropName() {
		return this.propName;
	}

	public void setPropName(String propName) {
		this.propName = propName;
	}

	public String getHost() {
		return this.host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof SysPropertiesId))
			return false;
		SysPropertiesId castOther = (SysPropertiesId) other;

		return (this.getImpId() == castOther.getImpId())
				&& ((this.getPropName() == castOther.getPropName()) || (this
						.getPropName() != null
						&& castOther.getPropName() != null && this
						.getPropName().equals(castOther.getPropName())))
				&& ((this.getHost() == castOther.getHost()) || (this.getHost() != null
						&& castOther.getHost() != null && this.getHost()
						.equals(castOther.getHost())));
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + this.getImpId();
		result = 37 * result
				+ (getPropName() == null ? 0 : this.getPropName().hashCode());
		result = 37 * result
				+ (getHost() == null ? 0 : this.getHost().hashCode());
		return result;
	}

}