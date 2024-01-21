package com.benayoub.superettebozar;

public class DataProduct {
    private String ProductName,ProductCode,expereddate,preexpireddate,uriPic;
    private long ExpiredTime,preexpiredtime;

    public DataProduct(String productName, String productCode, String expereddate, String preexpireddate, long expiredTime, long preexpiredtime,String uriPic) {
        ProductName = productName;
        ProductCode = productCode;
        this.expereddate = expereddate;
        this.preexpireddate = preexpireddate;
        ExpiredTime = expiredTime;
        this.preexpiredtime = preexpiredtime;
        this.uriPic=uriPic;
    }

    public String getProductName() {
        return ProductName;
    }

    public void setProductName(String productName) {
        ProductName = productName;
    }

    public String getProductCode() {
        return ProductCode;
    }

    public void setProductCode(String productCode) {
        ProductCode = productCode;
    }

    public String getExpereddate() {
        return expereddate;
    }

    public void setExpereddate(String expereddate) {
        this.expereddate = expereddate;
    }

    public String getPreexpireddate() {
        return preexpireddate;
    }

    public void setPreexpireddate(String preexpireddate) {
        this.preexpireddate = preexpireddate;
    }

    public long getExpiredTime() {
        return ExpiredTime;
    }

    public void setExpiredTime(long expiredTime) {
        ExpiredTime = expiredTime;
    }

    public long getPreexpiredtime() {
        return preexpiredtime;
    }

    public void setPreexpiredtime(long preexpiredtime) {
        this.preexpiredtime = preexpiredtime;
    }

    public String getUriPic() {
        return uriPic;
    }

    public void setUriPic(String uriPic) {
        this.uriPic = uriPic;
    }
}
