import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class HashIndexCreator {
    private static final int HEADER_SIZE = 4;
    private static final int REG_NAME_SIZE = 20;
    private static final int BN_NAME_SIZE = 200;
    private static final int STATUS_SIZE = 15;
    private static final int DATE_SIZE = HEADER_SIZE * 3;
    private static final int STATE_NUM_SIZE = 20;
    private static final int STATE_SIZE = 3;
    private static final int ABN_SIZE = 13;
    private static final int COMMA_SIZE = 1;
    private static final int RECORD_SIZE_BEFORE_TARGET = REG_NAME_SIZE + BN_NAME_SIZE + STATUS_SIZE + 3 * DATE_SIZE;
    private static final int RECORD_SIZE = REG_NAME_SIZE + BN_NAME_SIZE + STATUS_SIZE + 3 * DATE_SIZE + STATE_NUM_SIZE + STATE_SIZE + ABN_SIZE + COMMA_SIZE;
    private final int NUM_RECORDS_PER_PAGE;

    private String fileIn;
    private final int pageSize;
    //private static CustomHash<String, Integer> index7 = new CustomHash<>();
    private static CustomHash<String, Integer> index9 = new CustomHash<>();
    private int numRec = 0;

    public HashIndexCreator(int pagesize) {
        this.pageSize = pagesize;
        this.fileIn = "heap." + Integer.toString(pagesize);
        this.NUM_RECORDS_PER_PAGE = Math.floorDiv(pageSize, RECORD_SIZE);
    }

    private int getPageSize(String fileIn) {
        String[] tokens = fileIn.split("\\.");
        if (tokens.length > 1)
            return Integer.parseInt(tokens[1]);
        else
            return -1;
    }

    public void create() {
        // read the source file
        try (FileInputStream fis = new FileInputStream(new File(fileIn))) {
            // allocate a channel to read the file
            FileChannel fc = fis.getChannel();
            // allocate a buffer, size of pageSize
            ByteBuffer buffer = ByteBuffer.allocate(pageSize);
            // read a page of pageSize bytes; -1 means eof.
            while (fc.read(buffer) != -1) {
                // flip from filling to emptying
                buffer.flip();

                String s;
                Byte header;
                for (int i = 0; i < NUM_RECORDS_PER_PAGE; i++) {
                    byte[] wrapperBefore = new byte[RECORD_SIZE_BEFORE_TARGET];
                    buffer.get(wrapperBefore);

                    // state number
                    byte[] target1 = new byte[STATE_NUM_SIZE];
                    buffer.get(target1);
                    /* header = target1[0];
                    if (header.intValue() != -1 && header.intValue() != 0) {
                        s = new String(target1, "US-ASCII");
                        //index7.put(s.substring(0, 5), numRec);
                    } */
                    byte[] wrapperBetween = new byte[STATE_SIZE];
                    buffer.get(wrapperBetween);
                    // abn
                    byte[] target2 = new byte[ABN_SIZE];
                    buffer.get(target2);
                    header = target2[0];
                    if (header.intValue() != -1 && header.intValue() != 0) {
                        s = new String(target2, "US-ASCII");
                        index9.put(s.substring(0, 11), numRec);
                    }
                    byte[] wrapperAfter = new byte[COMMA_SIZE];
                    buffer.get(wrapperAfter);
                    // numRec increment
                    numRec++;
                }
                buffer.clear();
            }
        } catch (FileNotFoundException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }

        // write to the destination file
        index9.save("hash." + Integer.toString(pageSize));
    }

    public void display() {
        for (String key : index9.keySet())
            System.out.println("key: " + key + ", value: " + index9.get(key).toString());
//        System.out.println("-----------------------------------------");
//        for (String key : index7.keySet())
//            System.out.println("key: " + key + ", value: " + index7.get(key).toString());
    }
}
