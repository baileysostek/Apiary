package exceptions;

public class JsonMissingKeyException extends Throwable {
    public JsonMissingKeyException(String key_name) {
        super(String.format("This JSON Object was expected to have the key [%s] however it was not found", key_name));
    }
}
