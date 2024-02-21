public class UciIdResponse extends UciResponse
{
    private String _name;
    private String _author;
    
    public UciIdResponse(String name, String author)
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
