public class hashload {
    private static int pagesize = 0;
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis(); // timer for calculating program execution time

        try {
            if (args.length == 1) {
                pagesize = Integer.parseInt(args[0]);

                HashIndexCreator hashIndex = new HashIndexCreator(pagesize);
                hashIndex.create();
            } else {
                throw new Exception("Need 1 argument: java hashload pagesize (eg. 4096)");
            }
        } catch (NumberFormatException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }

        long stopTime = System.currentTimeMillis();
        double elapsedTime = (stopTime - startTime) / 1000.0; // execution time of the program in seconds
        System.out.println("Execution time:           " + elapsedTime + " seconds");
    }
}
