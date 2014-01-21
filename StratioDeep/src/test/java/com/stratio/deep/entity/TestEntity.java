package com.stratio.deep.entity;

import com.stratio.deep.annotations.DeepEntity;
import com.stratio.deep.annotations.DeepField;
import org.apache.cassandra.db.marshal.Int32Type;
import org.apache.cassandra.db.marshal.LongType;

@DeepEntity
public class TestEntity implements IDeepType {
  private static final long serialVersionUID = -6242942929275890323L;

  @DeepField(isPartOfPartitionKey = true)
  private String id;

  @DeepField(fieldName = "domain_name")
  private String domain;

  @DeepField
  private String url;

  @DeepField(validationClass = Int32Type.class, fieldName = "response_time")
  private Integer responseTime;

  @DeepField(validationClass = Int32Type.class, fieldName = "response_code")
  private Integer responseCode;

  @DeepField(validationClass = LongType.class, fieldName = "download_time")
  private Long downloadTime;

  private String notMappedField;

  public String getNotMappedField() {
    return notMappedField;
  }

  public void setNotMappedField(String notMappedField) {
    this.notMappedField = notMappedField;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Integer getResponseTime() {
    return responseTime;
  }

  public void setResponseTime(Integer responseTime) {
    this.responseTime = responseTime;
  }

  public Long getDownloadTime() {
    return downloadTime;
  }

  public void setDownloadTime(Long downloadTime) {
    this.downloadTime = downloadTime;
  }

  public Integer getResponseCode() {
    return responseCode;
  }

  public void setResponseCode(Integer responseCode) {
    this.responseCode = responseCode;
  }

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  @Override
  public String toString() {
    return "TestEntity ["
        + (id != null ? "id=" + id + ", " : "")
        + (domain != null ? "domain=" + domain + ", " : "")
        + (url != null ? "url=" + url + ", " : "")
        + (responseTime != null ? "responseTime=" + responseTime + ", "
        : "")
        + (responseCode != null ? "responseCode=" + responseCode + ", "
        : "")
        + (downloadTime != null ? "downloadTime=" + downloadTime + ", "
        : "")
        + (notMappedField != null ? "notMappedField=" + notMappedField
        : "") + "]\n";
  }


  public TestEntity(){
    super();
  }

  public TestEntity(String id, String domain, String url, Integer responseTime,
                    Integer responseCode, Long downloadTime, String notMappedField) {
    this.id = id;
    this.domain = domain;
    this.url = url;
    this.responseTime = responseTime;
    this.responseCode = responseCode;
    this.downloadTime = downloadTime;
    this.notMappedField = notMappedField;
  }
}