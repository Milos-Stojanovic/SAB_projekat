/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.student;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.CityOperations;

/**
 *
 * @author stoja
 */
public class sm180228_CityOperations implements CityOperations{

    @Override
    public int insertCity(String Naziv, String PostBr){
        Connection conn = DB.getInstance().getConnection();
        String query1 = "select IdGra from Grad where PostanskiBroj=?";
        
        try ( PreparedStatement stm = conn.prepareStatement(query1)) {
            stm.setString(1, PostBr);
            ResultSet rs = stm.executeQuery();

            if (rs.next()) {
                System.out.println("Vec postoji grad sa navedenim postanskim brojem!");
                return -1;
            }

        } catch (SQLException ex) {
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        String query2 = "insert into Grad (PostanskiBroj, Naziv) values(?,?)";
        try ( PreparedStatement stm = conn.prepareStatement(query2, PreparedStatement.RETURN_GENERATED_KEYS);) {
            stm.setString(1, PostBr);
            stm.setString(2, Naziv);
            stm.executeUpdate();

            ResultSet rs = stm.getGeneratedKeys();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException ex) {
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return -1;
    }

    @Override
    public int deleteCity(String... strings) {
        Connection conn = DB.getInstance().getConnection();
        String query = "delete from Grad where Naziv = ?";
        int cnt = 0;

        for (int i = 0; i < strings.length; i++) {
            try ( PreparedStatement stm = conn.prepareStatement(query);) {
                stm.setString(1, strings[i]);
                cnt += stm.executeUpdate();

            } catch (SQLException ex) {
                Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        return cnt;
    }

    @Override
    public boolean deleteCity(int i) {
        
        Connection conn = DB.getInstance().getConnection();
        String query = "delete from Grad where IdGra=?";

        try ( PreparedStatement stm = conn.prepareStatement(query);) {
            stm.setInt(1, i);
            int i1 = stm.executeUpdate();

            if(i1 == 1)
                return true;

        } catch (SQLException ex) {
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    @Override
    public List<Integer> getAllCities() {
        
        Connection conn = DB.getInstance().getConnection();
        String query = "select IdGra from Grad";
        try ( PreparedStatement ps = conn.prepareStatement(query);
              ResultSet rs = ps.executeQuery()) {
            
            List<Integer> list = new ArrayList<>();
            while (rs.next()) {

                list.add(rs.getInt(1));

            }
            return list;

        } catch (SQLException ex) {
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
}
