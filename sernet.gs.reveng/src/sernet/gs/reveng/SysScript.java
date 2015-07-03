package sernet.gs.reveng;

// Generated Jun 5, 2015 1:28:30 PM by Hibernate Tools 3.4.0.CR1

import java.math.BigDecimal;
import java.util.Date;

/**
 * SysScript generated by hbm2java
 */
public class SysScript implements java.io.Serializable {

	private String scriptname;
	private BigDecimal version;
	private Date scriptdate;
	private Date timestamp;

	public SysScript() {
	}

	public SysScript(String scriptname, Date scriptdate, Date timestamp) {
		this.scriptname = scriptname;
		this.scriptdate = scriptdate;
		this.timestamp = timestamp;
	}

	public String getScriptname() {
		return this.scriptname;
	}

	public void setScriptname(String scriptname) {
		this.scriptname = scriptname;
	}

	public BigDecimal getVersion() {
		return this.version;
	}

	public void setVersion(BigDecimal version) {
		this.version = version;
	}

	public Date getScriptdate() {
		return this.scriptdate;
	}

	public void setScriptdate(Date scriptdate) {
		this.scriptdate = scriptdate;
	}

	public Date getTimestamp() {
		return this.timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

}