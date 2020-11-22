package edu.mit.simile.tools;

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.ModelRDB;


/**
 * This class drops a model from a Jena RDB instance
 *
 * @author Ryan Lee
 * @see LoadDb
 */
public class DropModelDb {
    /**
     * Drop a model in a Jena RDB instance
     *
     * @param args Commandline arguments.
     */
    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("java DropModelDb <dbtype> <databasename> <modelname> <username>");
            System.out.println("Drops <modelname> from specified <dbtype> database");
            System.exit(0);
        }
        
        String dbtype = args[0];
        String db = args[1];
        String model = args[2];
        String username = args[3];
        
        System.out.println("Dropping model from " + dbtype + " database");
        System.out.println("Database: " + db);
        System.out.println("Model: " + model);
        System.out.println("Username: " + username);
                
        try {
            String className = null, DB_URL = null, DB_USER, DB_PASSWD, DB;
            // Load the Driver
            if (dbtype.equals("PostgreSQL")) { 
                className = "org.postgresql.Driver"; // path of driver class 
                DB_URL = "jdbc:postgresql://localhost/" + db; // URL of database server 
            } else if (dbtype.equals("MySQL")) {
                className = "com.mysql.jdbc.Driver";
                DB_URL = "jdbc:mysql://localhost/" + db;
            }
            
            DB_USER = username; // database user id 
            DB_PASSWD = ""; // database password 
            DB = dbtype; // database type             
            Class.forName(className); // load driver 
            
            // Create database connection 
            IDBConnection conn = new DBConnection(DB_URL, DB_USER, DB_PASSWD, DB);
            
            // Remove model
            ModelRDB m = ModelRDB.createModel(conn, model);
            m.remove();
            
            conn.close();
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }
}
