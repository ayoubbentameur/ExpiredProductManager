package com.benayoub.superettebozar;

public class getDataNotification {
   String expdate;
   String productname;

    public getDataNotification(String expdate, String productname) {
        this.expdate = expdate;
        this.productname = productname;
    }

    public String getExpdate() {
        return expdate;
    }

    public void setExpdate(String expdate) {
        this.expdate = expdate;
    }

    public String getProductname() {
        return productname;
    }

    public void setProductname(String productname) {
        this.productname = productname;
    }
}
