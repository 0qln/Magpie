package Interface;

public class TextResponse extends Response {

    private final String _value;

    public TextResponse(Object value) {
        _value = value.toString();
    }
    public TextResponse(String value) {
        _value = value;
    }

    @Override
    protected void executeSend() {
        System.out.println(_value);
    }

}
