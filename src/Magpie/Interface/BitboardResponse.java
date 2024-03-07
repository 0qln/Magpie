package Interface;

import static Engine.Utils.printBB;

public class BitboardResponse extends Response {

    private final long _bb;

    public BitboardResponse(long[] bb) {
        _bb = bb[0];
    }

   public BitboardResponse(long bb) {
        _bb = bb;
    }

    @Override
    protected void executeSend() {
        printBB(_bb);
    }

}
