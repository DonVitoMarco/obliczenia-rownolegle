package pl.marek;

import io.vavr.Tuple2;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class HtmlParser {

    private static Logger logger = Logger.getLogger(HtmlParser.class);

    private final CloseableHttpClient client;
    private final String word;

    public HtmlParser(final String word) {
        this.client = HttpClientBuilder.create().build();
        this.word = word;
    }

    public Tuple2<List<String>, Integer> getInfo(final String path) {
        try {
            return getLinks(path);
        } catch (Exception e) {
            logger.warn("Cannot parse : " + path + " : " + e.getMessage());
        }
        return new Tuple2<>(new ArrayList<>(), 0);
    }

    private Tuple2<List<String>, Integer> getLinks(final String path) {
        String pageContent = getPageContent(path);

        int occurrences = StringUtils.countMatches(pageContent, word);

        Document document = Jsoup.parse(pageContent);
        Elements hrefElements = document.select("a");

        ArrayList<String> links = new ArrayList<>();
        for (Element hrefElement : hrefElements) {
            links.add(hrefElement.attributes().get("href"));
        }

        return new Tuple2<>(links, occurrences);
    }

    private String getPageContent(final String path) {
        BufferedReader reader = null;
        try {
            HttpGet request = new HttpGet(path);
            HttpResponse response = client.execute(request);
            reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            String line;
            StringBuilder builder = new StringBuilder();
            while((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } catch (Exception e) {
            logger.warn("Cannot get page content : " + path + " : " + e.getMessage());
            throw new FetchDataException(path);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.error("Cannot close reader : " + path + " : " + e.getMessage());
                }
            }
        }
    }

    public void shutdown() {
        if (client != null) {
            try {
                client.close();
            } catch (IOException e) {
                logger.error("Shutdown : " + e.getMessage());
            }
        }
    }
}
