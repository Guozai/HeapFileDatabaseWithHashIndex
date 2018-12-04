import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class SearchIndex {
    private static final int REG_NAME_SIZE = 20;
    private static final int BN_NAME_SIZE = 200;
    private static final int STATUS_SIZE = 15;
    private static final int STATE_NUM_SIZE = 20;
    private static final int STATE_SIZE = 3;
    private static final int ABN_SIZE = 13;
    private static final int RECORD_SIZE = 308;

    private final int NUM_RECORDS_PER_PAGE;
    private final String queryKey;
    private final int pageSize;
    private final String heapName;
    private static CustomHash<String,Integer> index9 = new CustomHash<>();
    private int numPage = 0; // page counter
    private int numRec = -1;
    public SearchIndex(String queryKey, int pageSize) {
        this.queryKey = queryKey;
        this.pageSize = pageSize;
        this.heapName = "heap." + Integer.toString(pageSize);
        this.NUM_RECORDS_PER_PAGE = Math.floorDiv(pageSize, RECORD_SIZE);
    }

    // load the hash index
    public void loadIndex() {
        index9.load("hash." + Integer.toString(pageSize));
    }

    public void execute() {
        // Column does not contain queryKey
        if (index9.get(queryKey) == null) {
            System.out.println("Column BN_ABN does not contain " + queryKey + ".");
            return;
        }

        // read the heap file
        try (FileInputStream fis = new FileInputStream(new File(heapName))) {
            // allocate a channel to read the file
            FileChannel fc = fis.getChannel();
            // allocate a buffer, size of pageSize
            ByteBuffer buffer = ByteBuffer.allocate(pageSize);
            // read a page of pageSize bytes; -1 means eof.
            // Math.floorDiv(numRec, NUM_RECORDS_PER_PAGE) is the number of page the target record is on
            while (fc.read(buffer) != -1 && numPage < Math.floorDiv(index9.get(queryKey), NUM_RECORDS_PER_PAGE)) {
                buffer.flip();
                numPage++;
                numRec += NUM_RECORDS_PER_PAGE;
            }
            if (fc.read(buffer) != -1) {
                // flip from filling to emptying
                buffer.flip();

                byte[] wrapper = new byte[RECORD_SIZE * (index9.get(queryKey) - numRec - 1)];
                buffer.get(wrapper);

                // display the target record column by column
                byte[] content = new byte[REG_NAME_SIZE];
                buffer.get(content);
                System.out.println("REGISTER_NAME: " + displayCol(content));
                content = new byte[BN_NAME_SIZE];
                buffer.get(content);
                System.out.println("BN_NAME: " + displayCol(content));
                content = new byte[STATUS_SIZE];
                buffer.get(content);
                System.out.println("BN_STATUS: " + displayCol(content));
                int date = buffer.getInt();
                if (date != -1)
                    System.out.println("BN_REG_DT: " + date + "/" + buffer.getInt() + "/" + buffer.getInt());
                else {
                    System.out.println("BN_REG_DT: NULL");
                    buffer.getInt();
                    buffer.getInt();
                }
                date = buffer.getInt();
                if (date != -1)
                    System.out.println("BN_CANCEL_DT: " + date + "/" + buffer.getInt() + "/" + buffer.getInt());
                else {
                    System.out.println("BN_CANCEL_DT: NULL");
                    buffer.getInt();
                    buffer.getInt();
                }
                date = buffer.getInt();
                if (date != -1)
                    System.out.println("BN_RENEW_DT: " + date + "/" + buffer.getInt() + "/" + buffer.getInt());
                else {
                    System.out.println("BN_RENEW_DT: NULL");
                    buffer.getInt();
                    buffer.getInt();
                }
                content = new byte[STATE_NUM_SIZE];
                buffer.get(content);
                System.out.println("BN_STATE_NUM: " + displayCol(content));
                content = new byte[STATE_SIZE];
                buffer.get(content);
                System.out.println("BN_STATE_OF_REG: " + displayCol(content));
                content = new byte[ABN_SIZE];
                buffer.get(content);
                System.out.println("BN_ABN: " + displayCol(content));

                buffer.clear();
            }
            fc.close();
            fis.close();
        } catch (FileNotFoundException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private String displayCol(byte[] content) {
        String s = "";
        Byte head = content[0];
        if (head.intValue() != -1) {
            try {
                s = new String(content, "US-ASCII");
            } catch (UnsupportedEncodingException e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }
            return s;
        } else {
            return "NULL";
        }
    }

    public void displayIndex() {
        int i = 0;
        for (String key : index9.keySet()) {
            i++;
            if (i > 100 && i < 150) {
                System.out.println("key: " + key + ", value: " + index9.get(key).toString());
            }
        }
    }
}
