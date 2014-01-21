package com.stratio.deep.entity;

import org.apache.cassandra.db.marshal.Int32Type;
import org.apache.cassandra.db.marshal.LongType;

import com.stratio.deep.annotations.DeepEntity;
import com.stratio.deep.annotations.DeepField;

@DeepEntity
public class PageEntity implements IDeepType {
	
	private static final long serialVersionUID = -9213306241759793383L;

	@DeepField(fieldName="key", isPartOfPartitionKey = true)
	private String id;
	
	@DeepField(fieldName="domainName")
	private String domain;
	
	@DeepField
	private String url;
	
	@DeepField
	private String charset;
	
	@DeepField
	private String content;

	@DeepField(fieldName="downloadTime", validationClass=LongType.class)
	private Long downloadTime;
	
	@DeepField(fieldName="firstDownloadTime", validationClass=LongType.class)
	private Long firstDownloadTime;
	
	@DeepField(fieldName="responseCode", validationClass=Int32Type.class)
	private Integer responseCode;
	
	@DeepField(fieldName="responseTime", validationClass=LongType.class)
	private Long responseTime;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Long getDownloadTime() {
		return downloadTime;
	}

	public void setDownloadTime(Long downloadTime) {
		this.downloadTime = downloadTime;
	}

	public Long getFirstDownloadTime() {
		return firstDownloadTime;
	}

	public void setFirstDownloadTime(Long firstDownloadTime) {
		this.firstDownloadTime = firstDownloadTime;
	}

	public Integer getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(Integer responseCode) {
		this.responseCode = responseCode;
	}

	public Long getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(Long responseTime) {
		this.responseTime = responseTime;
	}
}
