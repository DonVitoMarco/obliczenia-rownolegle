package pl.marek;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

public class App {

    private static Logger logger = Logger.getLogger(App.class);

    public static void main(String[] args) {
        String basePathOptionName = "basePath";
        String threadsOptionName = "threads";
        String wordOptionName = "word";

        Options options = new Options();

        Option word = new Option("w", wordOptionName, true, "Search word");
        word.setRequired(true);

        Option basePath = new Option("b", basePathOptionName, true, "Base path to the webpage");
        basePath.setRequired(true);

        Option numberOfThreads = new Option("t", threadsOptionName, true, "Number of threads");
        numberOfThreads.setRequired(true);

        options.addOption(word);
        options.addOption(basePath);
        options.addOption(numberOfThreads);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        CommandLine cmd;
        String parsedBasePath = "";
        String parsedWord = "";
        int parsedThreads = 1;


        try {
            cmd = parser.parse(options, args);

            parsedBasePath = cmd.getOptionValue(basePathOptionName);
            parsedThreads = Integer.parseInt(cmd.getOptionValue(threadsOptionName));
            parsedWord = cmd.getOptionValue(wordOptionName);
        } catch (ParseException e) {
            logger.error(e.getMessage());
            formatter.printHelp("web-crawler", options);

            System.exit(1);
        }

        logger.info("==== WebCrawler ====");
        logger.info("Web base path : " + parsedBasePath);
        logger.info("Threads : " + parsedThreads);
        logger.info("Search word : " + parsedWord);

        final WebCrawler webCrawler = new WebCrawler(parsedThreads, parsedBasePath, parsedWord);

        final long startTime = System.currentTimeMillis();
        webCrawler.start();
        webCrawler.join();
        final long endTime = System.currentTimeMillis() - startTime;
        webCrawler.shutdown();
        logger.info("Work time : " + endTime / 1000.00);
        logger.info("Visited links : " + webCrawler.getNumberOfVisitedPages());
        logger.info("Number of word occurrences : " + webCrawler.getWordCounter());
    }
}
