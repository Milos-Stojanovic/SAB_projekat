/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.student;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author stoja
 */
public class DB {
    private static final String username = "sa";
    private static final String password = "password";
    private static final String database = "KurirskaSluzba";
    private static final int port = 1433;
    private static final String serverName = "localhost";

    private static final String connectionString="jdbc:sqlserver://"
            + serverName + ":" + port + ";"
            + "database=" + database + ";"
            +"user=" + username 
            + ";password=" + password;


    private Connection connection;

    public Connection getConnection(){
        return connection;
    }
    

    private DB(){
        try {
            connection=DriverManager.getConnection(connectionString);
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    

    private static DB db=null;
    public static DB getInstance(){
        if(db==null)
            db=new DB();
        return db;
    }
}

