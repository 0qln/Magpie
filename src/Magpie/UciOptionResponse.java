public class UciOptionResponse extends UciResponse
{
    public UciOptionResponse()
    {
    }

    @Override
    protected void executeSend() {
        for (IUciOption option : Config.Options) {
            String varStr = "";
            for (int i = 0; i <= option.getVars().length; i++) {
                varStr += " var " + option.getVars()[i];
            }
            System.out.println("option " + 
                " name " + option.getName() + 
                " type " + option.getType() + 
                " default " + option.getDefault() + 
                " min " + option.getMin() + 
                " max " + option.getMax() +
                " var" + varStr);
        }
    }
}
