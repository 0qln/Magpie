package Interface;

public class IdResponse extends Response
{
    private String _name;
    private String _author;
    
    public IdResponse(String name, String author)
    {
        _name = name;
        _author = author;
    }
    
    @Override
    protected void executeSend() {
        System.out.println("id name " + _name);
        System.out.println("id author " + _author);
    }
}
