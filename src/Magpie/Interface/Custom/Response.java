package Interface.Custom;

public class Response extends Interface.Response {

    private String _value;

    public Response(String value) {
        _value = value;
    }

    @Override
    protected void executeSend() {
        System.out.println(_value);
    }

}
