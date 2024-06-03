package tmt.realtimechartservice.common;

import lombok.Getter;

@Getter
public enum KisUrls {
	BASE_URL("ws://ops.koreainvestment.com:31000"),
	REAL_BASE_URL("ws://ops.koreainvestment.com:21000"),
	REAL_TIME_EXECUTION_PRICE_PATH("/tryitout/H0STCNT0"),
	REAL_TIME_ASKING_PRICE_PATH("/tryitout/H0STASP0");

	private final String path;

	KisUrls(String path) {
		this.path = path;
	}

	public String getFullUrl() {
		return BASE_URL.path + this.path;
	}

	public String getRealFullUrl() {
		return REAL_BASE_URL.path + this.path;
	}
}
