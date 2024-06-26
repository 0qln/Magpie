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

        private static final long[] _magics = new long[] { 324277453625393152L, 594475288257102016L, 792642537205547266L, 36037593380424832L, -4467568631191957371L, 72066403018081280L, 6052842309060923394L, 72067489644707906L, 576601508603234432L, 315322361182167048L, 2533412230463560L, -8466485682744851960L, 4611967566419066948L, 290622922052337792L, 18577352791426056L, 4909064352798105984L, 10274386485445681L, 141288317919232L, 7602097063332954368L, -9187201952323205116L, 6194649117018128L, 4620975792204283912L, 141287260963328L, 1729947406428572932L, 1337042646630944L, 153123213039110177L, 49548392531108864L, 576496456367604992L, 3386500109567233L, 3603442660447094788L, 3749264299151983048L, 613690516667667748L, 2323862356607631400L, 144291129288835080L, 326598940365430784L, 153139981672515585L, 144255959949116416L, 10997272151040L, -9223363171975167670L, 567367360839749L, 141975512842246L, 2344475587474636800L, 4504046371086336L, 1187541294448672L, 146240012615696L, -8934578675583156204L, 18375391856295973L, 9583363484680195L, 756745320334592L, 54044020707493120L, -9223293970992033536L, 2278209836056704L, 864697725668693504L, 36169551687843968L, 72339352486936832L, 36169638710608256L, 158880506183938L, 18014626151153703L, 35189006797057L, 6473959363317761L, 4756364363152230406L, 5769955556248782849L, -9221595191695488764L, 18014618643366914L, };

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
