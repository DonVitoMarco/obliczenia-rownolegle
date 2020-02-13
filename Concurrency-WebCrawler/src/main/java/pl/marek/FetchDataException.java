package pl.marek;

public class FetchDataException extends RuntimeException {

    public FetchDataException(String path) {
        super("Could not get data from : " + path);
    }
}
