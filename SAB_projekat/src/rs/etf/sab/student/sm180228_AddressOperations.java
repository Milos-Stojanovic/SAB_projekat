/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.student;
import com.sun.istack.internal.NotNull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.AddressOperations;

/**
 *
 * @author stoja
 */
public class sm180228_AddressOperations implements AddressOperations {

    @Override
    public int deleteAddresses(@NotNull String name, int number) {
        Connection conn = DB.getInstance().getConnection();
        String query = "delete from adresa where Ulica = ? and Broj = ?";
        
        try(PreparedStatement ps = conn.prepareStatement(query)){
            ps.setString(1, name);
            ps.setInt(2, number);
            int cnt = 0;
            cnt = ps.executeUpdate();
            return cnt;
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    @Override
    public boolean deleteAdress(int idAddress) {
        Connection conn = DB.getInstance().getConnection();
        String query = "delete from Adresa where IdAdr = ?";
        
        try(PreparedStatement ps = conn.prepareStatement(query)){
            ps.setInt(1, idAddress);
            return 1 == ps.executeUpdate();
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public int deleteAllAddressesFromCity(int idCity) {
        Connection conn = DB.getInstance().getConnection();
        String query = "delete from Adresa where IdGra = ?";
        String query1 = "select Naziv from Grad where IdGra = ?"; 
        
        
        try(PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps1 = conn.prepareStatement(query1)){
            
            ps1.setInt(1, idCity);
            ResultSet rs = ps1.executeQuery();
            if(!rs.next()){
                return 0; // Nema takvog grada!
            }
            
            ps.setInt(1, idCity);
            int cnt = 0;
            cnt = ps.executeUpdate();
            return cnt;
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    @Override
    public List<Integer> getAllAddresses() {
        Connection conn = DB.getInstance().getConnection();
        String query = "select IdAdr from Adresa";
        List<Integer> list = new ArrayList<>();
        
        try(PreparedStatement ps = conn.prepareStatement(query);
                ResultSet rs = ps.executeQuery();){
        
            while(rs.next()){
                list.add(rs.getInt(1));
            }
            return list;
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public List<Integer> getAllAddressesFromCity(int idCity) {
        Connection conn = DB.getInstance().getConnection();
        String query = "select IdAdr from Adresa where IdGra = ?";
        String query1 = "select Naziv from Grad where IdGra = ?";
        List<Integer> list = new ArrayList<>();
        
        try(PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps1 = conn.prepareStatement(query1);){
            
            ps1.setInt(1, idCity);
            ResultSet rs1 = ps1.executeQuery();

            if (!rs1.next()) {
                return null; // Nema takvog grada!
            }
            
            ps.setInt(1, idCity);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                list.add(rs.getInt(1));
            }
            return list;
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public int insertAddress(@NotNull String street, int number, int cityId, int xCord, int yCord) {
        Connection conn = DB.getInstance().getConnection();
        String query = "insert into Adresa (Ulica, Broj, IdGra, x_koord, y_koord) values (?, ?, ?, ?, ?)";
        String query1 = "select Naziv from Grad where IdGra = ?";
        try(PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement ps1 = conn.prepareStatement(query1);){
            
            ps1.setInt(1, cityId);
            ResultSet rs1 = ps1.executeQuery();
            if(!rs1.next()){
                return -1; // Nema takvog grada!
            }
            
            ps.setString(1, street);
            ps.setInt(2, number);
            ps.setInt(3, cityId);
            ps.setInt(4, xCord);
            ps.setInt(5, yCord);
            ps.executeUpdate();
            
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()){
                return rs.getInt(1);
            }
            
        } catch (SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }
    
}
