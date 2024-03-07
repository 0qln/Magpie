package Interface;

import Engine.IMoveDecoder;
import Engine.IMoveEncoder;

public class InfoResponse extends Response {

    private StringBuilder _result = new StringBuilder("info ");

    public static class Builder extends Misc.Builder<InfoResponse> {

        InfoResponse _instance = new InfoResponse();

        public Builder depth(int depth) {
            _instance._result.append("depth ").append(depth).append(" ");
            return this;
        }

        public Builder seldepth(int seldepth) {
            _instance._result.append("seldepth ").append(seldepth).append(" ");
            return this;
        }

        public Builder time(long time) {
            _instance._result.append("time ").append(time).append(" ");
            return this;
        }

        public Builder nodes(long nodes) {
            _instance._result.append("nodes ").append(nodes).append(" ");
            return this;
        }

        public Builder pv(short[] pvline, IMoveEncoder encoder) {
            StringBuilder pv = new StringBuilder();
            for (int i = 0; i < pvline.length; i++) {
                pv.append(encoder.encode(pvline[i])).append(' ');
            }
            _instance._result.append("pv ").append(pv).append(' ');
            return this;
        }

        public Builder multipv(int multipv) {
            _instance._result.append("multipv ").append(multipv).append(" ");
            return this;
        }

        public Builder score(int score, ScoreType type) {
            _instance._result
                .append("score ")
                .append(ScoreType.toString(type)).append(' ')
                .append(score)
                .append(' ');
            return this;
        }

        public Builder currmove(String currmove) {
            _instance._result.append("currmove ").append(currmove).append(" ");
            return this;
        }

        public Builder currmovenumber(int currmovenumber) {
            _instance._result.append("currmovenumber ").append(currmovenumber).append(" ");
            return this;
        }

        public Builder hashfull(int hashfull) {
            _instance._result.append("hashfull ").append(hashfull).append(" ");
            return this;
        }

        public Builder nps(long nps) {
            _instance._result.append("nps ").append(nps).append(" ");
            return this;
        }

        public Builder tbhits(int tbhits) {
            _instance._result.append("tbhits ").append(tbhits).append(" ");
            return this;
        }

        public Builder sbhits(int sbhits) {
            _instance._result.append("sbhits ").append(sbhits).append(" ");
            return this;
        }

        public Builder cpuload(int cpuload) {
            _instance._result.append("cpuload ").append(cpuload).append(" ");
            return this;
        }

        public Builder string(String str) {
            _instance._result.append("string ").append(str).append(" ");
            return this;
        }

        public Builder refutation(String refutation) {
            _instance._result.append("refutation ").append(refutation).append(" ");
            return this;
        }

        public Builder currline(String currline) {
            _instance._result.append("currline ").append(currline).append(" ");
            return this;
        }

        @Override
        protected InfoResponse _buildT() {
            return _instance;
        }

    }

    @Override
    protected void executeSend() {
        System.out.println(_result.toString());
    }
}
