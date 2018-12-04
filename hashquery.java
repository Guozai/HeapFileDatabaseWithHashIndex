public class hashquery {
    private static String queryKey = "";
    private static int pageSize = 0;

    public static void main(String[] args) {
        try {
            if (args.length == 2) {
                queryKey = args[0];
                pageSize = Integer.parseInt(args[1]);

                SearchIndex query = new SearchIndex(queryKey, pageSize);
                query.loadIndex();

                long startTime = System.currentTimeMillis(); // timer for calculating program execution time
                query.execute();
                long stopTime = System.currentTimeMillis();
                double elapsedTime = (stopTime - startTime) / 1000.0; // execution time of the program in seconds
                System.out.println("Execution time:           " + elapsedTime + " seconds");

            } else {
                throw new Exception("Need 3 arguments: java hashquery text pagesize");
            }
        } catch (NumberFormatException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }
}
