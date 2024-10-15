package io.github.pigeonmuyz.pigeonapi.Entity;


import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement(name = "TitleInfoList")
public class TitleInfoList {

    private List<TitleInfo> titleInfo;

    @XmlElement(name = "TitleInfo")
    public List<TitleInfo> getTitleInfo() {
        return titleInfo;
    }

    public void setTitleInfo(List<TitleInfo> titleInfo) {
        this.titleInfo = titleInfo;
    }
}