package Engine;

import static Engine.Utils.*;

import java.util.Arrays;

public class Bishop extends SlidingPiece {

    public static final MoveGenerator generator = new Bishop.MoveGenerator();
    public static final int ID_Type = 3;
    public static final int ID_White = Piece.create(ID_Type, Color.White);
    public static final int ID_Black = Piece.create(ID_Type, Color.Black);

    @Override
    public MoveGenerator getGenerator() {
        return generator;
    }

    public static class MoveGenerator extends SlidingPiece.MoveGenerator {
        
        @Override
        protected Engine.SlidingPiece.MoveGenerator _getInstance() {
            return this;
        }

        /*
         * Generate in chunks
         * https://www.chessprogramming.org/Move_Generation#Chunk_Move_Generation
         */

        @Override
        int generate(short[] list, int index, Board board, int color, boolean capturesOnly) {
            index = captures(list, index, board, color);
            if (!capturesOnly)
                index = quiets(list, index, board, color);
            return index;
        }

        @Override
        int resolves(short[] list, int index, Board board, int color, boolean capturesOnly) {
            final long checkerBB = board.getCheckers();
            final int checker = lsb(checkerBB);
            final int king = lsb(board.getBitboard(King.ID_Type, color));
            final long mask = Masks.squaresBetween(king, checker);
            // quiet
            if (!capturesOnly)
                index = generate(
                        list,
                        index,
                        ~board.getOccupancy() & mask,
                        board.getOccupancy(),
                        board.getBitboard(Bishop.ID_Type, color),
                        Move.QUIET_MOVE_FLAG);
            // captures
            return generate(
                    list,
                    index,
                    checkerBB,
                    board.getOccupancy(),
                    board.getBitboard(Bishop.ID_Type, color),
                    Move.CAPTURE_FLAG);
        }

        public int quiets(short[] list, int index, Board board, int color) {
            return generate(
                    list,
                    index,
                    ~board.getOccupancy(),
                    board.getOccupancy(),
                    board.getBitboard(Bishop.ID_Type, color),
                    Move.QUIET_MOVE_FLAG);
        }

        public int captures(short[] list, int index, Board board, int color) {
            return generate(
                    list,
                    index,
                    board.getCBitboard(Color.NOT(color)),
                    board.getOccupancy(),
                    board.getBitboard(Bishop.ID_Type, color),
                    Move.CAPTURE_FLAG);
        }

        @Override
        public long attacks(int square, int color) {
            return attacks(square);
        }

        @Override
        public long attacks(int square, long occupied, int color) {
            return attacks(square, occupied);
        }

        @Override
        public long attacks(int square) {
            final long a1h8BB = Masks.Diags_A1H8[diagA1H8(square)], a8h1BB = Masks.Diags_A8H1[diagA8H1(square)];
            return (a1h8BB | a8h1BB) ^ target(square);
        }

        @Override
        public long attacks(int square, long occupied) {
            // return computeAttacks(square, occupied);
            return lookupAttacks(square, occupied);
        }
        
        // https://www.chessprogramming.org/Magic_Bitboards
        private static final int[] BBits = {
  6, 5, 5, 5, 5, 5, 5, 6,
  5, 5, 5, 5, 5, 5, 5, 5,
  5, 5, 7, 7, 7, 7, 5, 5,
  5, 5, 7, 9, 9, 7, 5, 5,
  5, 5, 7, 9, 9, 7, 5, 5,
  5, 5, 7, 7, 7, 7, 5, 5,
  5, 5, 5, 5, 5, 5, 5, 5,
  6, 5, 5, 5, 5, 5, 5, 6
};
        // public static final int _magicShift = 12;
        public static final long[][] _attacks = new long[64][];// ~0 >>> (64 - _magicShift) 
        public static void Initialize() {
            var gen = Bishop.generator;
            long[] used = new long[4960];
            for (int square = 0; square < 64; square++) {
                // for (long occ, occIdx = 0; (occ = gen.nextRelevantOccupied(square, occIdx)) != -1; occIdx++) {
                //     _attacks[square][(int)occIdx] = gen.computeAttacks(square, occ);
                // }            
                 
                Arrays.setAll(used, i -> 0);

                long mask, magic, blockers;
                int i, j, jMax, n, magicShift;

                magicShift = BBits[square];
                magic = _magics[square];
                mask = gen.relevantOccupancy(square);
                n = countBits(mask);

                for (i = 0, jMax = 0; i < (1 << n); i++) {
                    blockers = gen.mapBits(i, mask);
                    j = transform(blockers, magic, magicShift);
                    used[j] = gen.computeAttacks(square, blockers);
                    jMax = Math.max(jMax, j);
                }
                
                _attacks[square] = Arrays.copyOfRange(used, 0, jMax + 1);
            }
        }
        static int transform(long blockers, long magic, int bits) {
            return (int)((blockers * magic) >>> (64 - bits));        
        }
        public static final long[] _magics = new long[] {
72220391556256258L,
1157425381259874816L,
2428031046811648L,
218460067047950369L,
9154813527064704L,
72705363675200L,
-9222567185618917072L,
1553197747802112L,
1477202704920215584L,
-9218868145167507392L,
8796428568580L,
19917378527756320L,
3503800785257365568L,
23925407426281506L,
18190870260236416L,
180209972989269008L,
4982529918727421960L,
2614409954714471680L,
182976329737111556L,
2308167396189340673L,
9367839215456256L,
76601051096043523L,
36310358432088192L,
324295478683111946L,
-4611650692052678900L,
1176003555971104914L,
17669562606624L,
1134696016646146L,
-7493980433961320320L,
82190848221118592L,
180284791304356418L,
577024816941371904L,
72092781365168640L,
299067836211793L,
162131787219734816L,
4702919099566526468L,
-4575514009951535084L,
1585372761537511936L,
-9061152822885604096L,
1447907289060089864L,
-9205355301580783104L,
-6890506051186064384L,
9071247886480L,
-9213801868316114432L,
36099170192591936L,
130605905387241600L,
-8644658346531487696L,
579005022226940200L,
2304597880799232L,
2450029666669692928L,
-7752870600115746560L,
403183648L,
1126040114072576L,
288268379236598913L,
4785315122397440L,
288248123498201216L,
90074298970030097L,
2485987030815868961L,
3463268389022204032L,
2267811020866L,
2382404203952801920L,
2392560925409282L,
144398864357556480L,
73751134136705200L,
        };
        
        public int key(int square, long occupied) {
            // temp
            var _magicShift = 0;
            long relevantOccupancy = occupied & relevantOccupancy(square);
            long key = (relevantOccupancy * _magics[square]) >>> (64 - _magicShift);
            return Math.abs((int)key);
        }

        public long lookupAttacks(int square, long occupied) {
            // Get the magic key.
            // int key = key(square, occupied);
            int key = transform(relevantOccupancy(square) & occupied, _magics[square], BBits[square]);

            // Return the lookup.
            return _attacks[square][(int)key];
        }

        public long computeAttacks(int square, long occupied) {
            final long a1h8BB = Masks.Diags_A1H8[diagA1H8(square)], a8h1BB = Masks.Diags_A8H1[diagA8H1(square)];
            final long nortBB = Utils.splitBBNorth(square), soutBB = Utils.splitBBSouth(square);

            long occupands, moves, result = 0, ray;
            int nearest;

            // South East
            ray = a8h1BB & soutBB;
            occupands = occupied & ray;
            nearest = msb(occupands);
            moves = shift(splitBBNorth(nearest), CompassRose.SoEa) & ray;
            result |= moves;

            // South West
            ray = a1h8BB & soutBB;
            occupands = occupied & ray;
            nearest = msb(occupands);
            moves = shift(splitBBNorth(nearest), CompassRose.SoWe) & ray;
            result |= moves;

            // North West
            ray = a8h1BB & nortBB;
            occupands = occupied & ray;
            nearest = lsb(occupands);
            moves = shift(splitBBSouth(nearest), CompassRose.NoWe) & ray;
            result |= moves;

            // North East
            ray = a1h8BB & nortBB;
            occupands = occupied & ray;
            nearest = lsb(occupands);
            moves = shift(splitBBSouth(nearest), CompassRose.NoEa) & ray;
            result |= moves;

            return result;
        }

    }

}
