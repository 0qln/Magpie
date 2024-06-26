package Engine;

import static Engine.Utils.*;

public class Rook extends SlidingPiece {

    public static final MoveGenerator generator = new Rook.MoveGenerator();

    public static final int ID_Type = 4;
    // public static final int ID_White = Piece.create(ID_Type, Color.White);
    // public static final int ID_Black = Piece.create(ID_Type, Color.Black);
    // Manually inlining the static final function bc java is a little crybaby
    public static final int ID_White = ID_Type << 1 | Color.White; 
    public static final int ID_Black = ID_Type << 1 | Color.Black;

    @Override
    public MoveGenerator getGenerator() {
        return generator;
    }

    public static class MoveGenerator extends SlidingPiece.MoveGenerator implements IMoveLookup {
        
        /*
         * Generate in chunks
         * https://www.chessprogramming.org/Move_Generation#Chunk_Move_Generation
         */
        @Override
        int generate(short[] list, int index, Board board, int color, boolean capturesOnly) {
            index = generateCaptures(list, index, board, color);
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
                        board.getBitboard(Rook.ID_Type, color),
                        Move.QUIET_MOVE_FLAG);
            // captures
            return generate(
                    list,
                    index,
                    checkerBB,
                    board.getOccupancy(),
                    board.getBitboard(Rook.ID_Type, color),
                    Move.CAPTURE_FLAG);
        }

        public int quiets(short[] list, int index, Board board, int color) {

            // [Ignored case] in case of a double check, this function should not get called
            // assert (board.isInDoubleCheck() == false);

            return generate(
                    list,
                    index,
                    // only non-occupied squares
                    ~board.getOccupancy()
                    // [General restriction] when in check, allow only check blocking moves
                    // & (board.isInSingleCheck() ? board.getCheckBlockSqs() : ~0x0L)
                    ,
                    board.getOccupancy(),
                    board.getBitboard(Rook.ID_Type, color),
                    Move.QUIET_MOVE_FLAG);
        }

        public int generateCaptures(short[] list, int index, Board board, int color) {

            // [Ignored case] in case of a double check, this function should not get called
            // assert (board.isInDoubleCheck() == false);

            return generate(
                    list,
                    index,
                    // only squares with enemy pieces
                    board.getCBitboard(Color.NOT(color))
                    // [General restriction] when in check, allow only check resolving captures
                    // & (board.isInSingleCheck() ? board.getCheckers() : ~0x0L)
                    ,
                    board.getOccupancy(),
                    board.getBitboard(Rook.ID_Type, color),
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
            return (Masks.Files[file(square)] | Masks.Ranks[rank(square)]) ^ target(square);
        }

        @Override
        public long attacks(int square, long occupied) {
            var lookup = lookupAttacks(square, occupied);
            return lookup;
        }
        

        // https://www.chessprogramming.org/Magic_Bitboards
        
        private static final int[] _magicBits = {
          12, 11, 11, 11, 11, 11, 11, 12,
          11, 10, 10, 10, 10, 10, 10, 11,
          11, 10, 10, 10, 10, 10, 10, 11,
          11, 10, 10, 10, 10, 10, 10, 11,
          11, 10, 10, 10, 10, 10, 10, 11,
          11, 10, 10, 10, 10, 10, 10, 11,
          11, 10, 10, 10, 10, 10, 10, 11,
          12, 11, 11, 11, 11, 11, 11, 12        
        };

        private static final long[] _magics = new long[] { 144134980375683200L, 198193571466100736L, 5800671507789123584L, -4251366159715139576L, 8791061949192342016L, 8214574671053455872L, 36101365071609984L, 3602879977680307201L, 140877074808834L, 2343208813714481153L, 281750125150464L, 6918092115335448896L, 4785108968427776L, 308637861786157568L, 180425468661530628L, 2305983748849561856L, 158879434407968L, 2319354083510599697L, 153122939234943008L, 38291592236368064L, 141287311296512L, 2306406508990038656L, 5550831676370724963L, -8881096265985653759L, 2603645682091170L, 40542568202641408L, 18578525286056064L, 4504703436067080L, 2533846021570832L, 1729947408034693248L, 235462631291421264L, 4512679188496513L, 324329544607596672L, 5665785582206985L, -5683507543196823552L, 4540158690986496L, 2251840624264192L, 145289468650193408L, 1697852447261090L, 4684887658709647428L, 600175053996457984L, 40532534089498625L, 54151506018041872L, 1152939930022969368L, 577023874593587204L, 563104580665354L, 20275020458885121L, 578889028049436676L, 36029621990328384L, 1154364338757206272L, 2305994744339109376L, 4611844349041344640L, 2314863540214700544L, 5263019149165330944L, -9222809052474441216L, -8934855700681520640L, 36310480301599335L, 2432295668556005409L, 2312609404312252546L, 9306267004708357L, 1171217412453436465L, 2416744219160117570L, 6990149589216067786L, 4399262925026L, };

        private static final long[][] _attacks = new long[64][];


        @Override
        public long[] getMagics() {
            return _magics;
        }

        @Override
        public int[] getMagicBits() {
            return _magicBits;
        }

        @Override
        public long[][] getAttacks() {
            return _attacks;
        }



        public static void Initialize() {
            SlidingPiece.MoveGenerator.Initialize(Rook.generator, Rook.generator);
        }
        
        public long lookupAttacks(int square, long occupied) {
            // Get the magic key.
            int key = getKey(occupied & relevantOccupancy(square), _magics[square], _magicBits[square]);

            // Return the lookup.
            return _attacks[square][key];
        }
        
        public long relevantOccupancy(int square) {
            long result = Masks.RelevantFiles[file(square)] | Masks.RelevantRanks[rank(square)];
            result &= ~target(square);
            return result;
        }


        @Override
        public long computeAttacks(int square, long occupied) {
            final long fileBB = Masks.Files[square % 8];
            final long rankBB = Masks.Ranks[square / 8];
            final long nortBB = Utils.splitBBNorth(square);
            final long soutBB = Utils.splitBBSouth(square);

            long occupands, moves, result = 0, ray;
            int nearest;

            // South
            ray = fileBB & soutBB;
            occupands = occupied & ray;
            nearest = msb(occupands);
            moves = shift(splitBBNorth(nearest), CompassRose.Sout) & ray;
            result |= moves;

            // North
            ray = fileBB & nortBB;
            occupands = occupied & ray;
            nearest = lsb(occupands);
            moves = shift(splitBBSouth(nearest), CompassRose.Nort) & ray;
            result |= moves;

            // West
            ray = rankBB & soutBB;
            occupands = occupied & ray;
            nearest = msb(occupands);
            moves = shift(splitBBNorth(nearest), CompassRose.West) & ray;
            result |= moves;

            // East
            ray = rankBB & nortBB;
            occupands = occupied & ray;
            nearest = lsb(occupands);
            moves = shift(splitBBSouth(nearest), CompassRose.East) & ray;
            result |= moves;

            return result;
        }

    }

}
