package main.java.model.db;

/**
 * A simple class to store databse authentication information
 * in package-private static fields
 *
 * @version 1.0
 * @author Martin Todorov
 */
public class DBConnection {
    // package - private database connection data - change for personal database
    static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String URL = "jdbc:mysql://localhost:3306/shazampoc";
    static final String USER = "root";
    static final String PASS = "TestDBS123#@!";
}
