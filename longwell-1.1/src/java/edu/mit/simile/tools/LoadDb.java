package edu.mit.simile.tools;

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.ModelRDB;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import java.util.Date;


/**
 * This class can upload data into a persistant Postgres database.
 *
 * @author Mark H. Butler
 */
public class LoadDb {
    /**
     * Load the data into a persistant Postgres database.
     *
     * @param args Commandline arguments.
     */
    public static void main(String[] args) {
        if (args.length < 5) {
            System.out.println("java LoadDb <dbtype> <file> <databasename> <modelname> <username>");
            System.out.println("Loads <file> into the specified <dbtype> database");
            System.exit(0);
        }

		String dbtype = args[0];
		String filename = args[1];
		String db = args[2];
		String model = args[3];
		String username = args[4];

        System.out.println("Loading data into the " + dbtype + " database");
        System.out.println("File: " + filename);
        System.out.println("Database: " + db);
        System.out.println("Model: " + model);
        System.out.println("Username: " + username);

        long startTime = (new Date()).getTime();
        
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

            boolean inTransaction = false;

            // Create database connection 
            IDBConnection conn = new DBConnection(DB_URL, DB_USER, DB_PASSWD, DB);

            // Create a model in the database 
            ModelRDB m = ModelRDB.createModel(conn, model);
            m.begin();

            File file = new File(filename);

            String name = file.toString();

            InputStream in = new FileInputStream(file);
            System.out.println("Begin loading");
            m.read(in, "");
            System.out.println("Finished loading " + name);

            m.commit();
            conn.close();

            long endTime = (new Date()).getTime();
            System.out.println("Time to read in RDF data and schemas: " + (endTime - startTime) + " milliseconds");
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }
}
