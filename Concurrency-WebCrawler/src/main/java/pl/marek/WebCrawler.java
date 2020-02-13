package pl.marek;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.vavr.Tuple2;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WebCrawler {

    private static Logger logger = Logger.getLogger(WebCrawler.class);

    private final ExecutorService executorService;
    private final String basePath;
    private final ConcurrentHashMap<String, Boolean> visited;
    private final AtomicInteger wordCounter;
    private final AtomicInteger processing;
    private final HtmlParser htmlParser;
    private final Object lock = new Object();

    public WebCrawler(int numberOfThreads, String basePath, String word) {
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("Web-Crawler-Worker-%d")
                .setDaemon(true)
                .build();

        this.executorService = Executors.newFixedThreadPool(numberOfThreads, threadFactory);
        this.basePath = basePath;
        this.visited = new ConcurrentHashMap<>();
        this.processing = new AtomicInteger(0);
        this.wordCounter = new AtomicInteger(0);
        this.htmlParser = new HtmlParser(word);
    }

    public void start() {
        searchLink(basePath);
    }

    public void join() {
        synchronized (this.lock) {
            try {
                this.lock.wait();
            } catch (InterruptedException e) {
                logger.error("Interrupted Exception : " + e.getMessage());
            }
        }
    }

    public void shutdown() {
        htmlParser.shutdown();

        executorService.shutdown();
        try {
            executorService.awaitTermination(2, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            logger.error("Shutdown : " + e.getMessage());
        }
    }

    private void searchLink(final String path) {
        if (visited.containsKey(path)) {
            return;
        }
        visited.put(path, Boolean.TRUE);
        processing.incrementAndGet();

        executorService.execute(() -> {
            logger.info("Search Link : " + path);
            Tuple2<List<String>, Integer> info = htmlParser.getInfo(path);
            wordCounter.addAndGet(info._2);

            info._1.forEach(p -> {
                String toVisitPath = p.contains("http") ? p : basePath + p;
                if (toVisitPath.contains(basePath)) {
                    searchLink(toVisitPath);
                } else {
                    logger.debug("External resources : " + p);
                }
            });

            processing.decrementAndGet();

            if (processing.get() == 0) {
                synchronized (lock) {
                    lock.notify();
                }
            }
        });
    }

    public Integer getNumberOfVisitedPages() {
        return visited.size();
    }

    public Integer getWordCounter() {
        return wordCounter.get();
    }
}
