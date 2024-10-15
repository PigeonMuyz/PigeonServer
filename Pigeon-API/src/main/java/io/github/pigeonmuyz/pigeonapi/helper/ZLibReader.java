package io.github.pigeonmuyz.pigeonapi.helper;

import io.github.pigeonmuyz.pigeonapi.Entity.ZLibData;
import io.github.pigeonmuyz.pigeonapi.config.JXAPIConfig;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class ZLibReader {
    private final static Logger LOGGER = LoggerFactory.getLogger(ZLibReader.class);

    public List<ZLibData> getLinks(String url) throws IOException {
        LOGGER.info(url);
        Document doc = Jsoup.connect(url)
//                .proxy("pigeon-gy-server", 7899)
                .get();
        Elements links = doc.select("a[href]");

        List<ZLibData> linkDataList = new ArrayList<>();
        for (Element link : links) {
            String text = link.text();
            String href = link.attr("href");
            if (href.contains("/book/") && !text.isEmpty()) {
                linkDataList.add(new ZLibData(text, JXAPIConfig.zliburi+href));
//                linkDataList.add(new ZLibData(text, JXAPIConfig.zliburi+ URLDecoder.decode(href, "UTF-8")));
            }
        }
        return linkDataList;
    }
}
