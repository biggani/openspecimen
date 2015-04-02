
package com.krishagni.catissueplus.core.auth.events;

public class LoginDetail {
	private String loginId;

	private String password;

	private String domainName;
	
	private String ipAddress;
	
	private boolean doNotGenerateToken;

	public String getLoginId() {
		return loginId;
	}

	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public boolean isDoNotGenerateToken() {
		return doNotGenerateToken;
	}

	public void setDoNotGenerateToken(boolean doNotGenerateToken) {
		this.doNotGenerateToken = doNotGenerateToken;
	}

}
