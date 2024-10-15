package io.github.pigeonmuyz.pigeonapi.Entity;

import jakarta.xml.bind.annotation.XmlElement;

public class TitleInfo {
    private String initialCode;
    private String titleName;
    private String makerName;
    private String price;
    private String salesDate;
    private String softType;
    private String platformID;
    private String linkURL;
    private String screenshotImgURL;
    private String localeCNName;
    private String localeCNMakerName;
    private String localeCNPrice;

    public String getLocaleCNMakerName() {
        return localeCNMakerName;
    }

    public void setLocaleCNMakerName(String localeCNMakerName) {
        this.localeCNMakerName = localeCNMakerName;
    }

    public String getLocaleCNPrice() {
        return localeCNPrice;
    }

    public void setLocaleCNPrice(String localeCNPrice) {
        this.localeCNPrice = localeCNPrice;
    }

    public String getLocaleCNName() {
        return localeCNName;
    }

    public void setLocaleCNName(String localeCNName) {
        this.localeCNName = localeCNName;
    }

    // Getters and Setters for all fields
    @XmlElement(name = "InitialCode")
    public String getInitialCode() {
        return initialCode;
    }

    public void setInitialCode(String initialCode) {
        this.initialCode = initialCode;
    }

    @XmlElement(name = "TitleName")
    public String getTitleName() {
        return titleName;
    }

    public void setTitleName(String titleName) {
        this.titleName = titleName;
    }

    @XmlElement(name = "MakerName")
    public String getMakerName() {
        return makerName;
    }

    public void setMakerName(String makerName) {
        this.makerName = makerName;
    }

    @XmlElement(name = "Price")
    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    @XmlElement(name = "SalesDate")
    public String getSalesDate() {
        return salesDate;
    }

    public void setSalesDate(String salesDate) {
        this.salesDate = salesDate;
    }

    @XmlElement(name = "SoftType")
    public String getSoftType() {
        return softType;
    }

    public void setSoftType(String softType) {
        this.softType = softType;
    }

    @XmlElement(name = "PlatformID")
    public String getPlatformID() {
        return platformID;
    }

    public void setPlatformID(String platformID) {
        this.platformID = platformID;
    }

    @XmlElement(name = "LinkURL")
    public String getLinkURL() {
        return linkURL;
    }

    public void setLinkURL(String linkURL) {
        this.linkURL = linkURL;
    }

    @XmlElement(name = "ScreenshotImgURL")
    public String getScreenshotImgURL() {
        return screenshotImgURL;
    }

    public void setScreenshotImgURL(String screenshotImgURL) {
        this.screenshotImgURL = screenshotImgURL;
    }
}