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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.StockroomOperations;

/**
 *
 * @author stoja
 */
public class sm180228_StockroomOperations implements StockroomOperations {

    @Override
    public int insertStockroom(int address) {
        Connection conn = DB.getInstance().getConnection();
        // prvo proveri da li u tom gradu vec postoji magacin
        // i potom proveri da li postoji adresa sa tim id-em
        String proveraAdrese = "select idgra from adresa where idadr=?";
        String provera = "select a.idgra from adresa a join lokacijamagazina m on (m.idadr=a.idadr) where a.idgra=?";
        int IdGra = -1;
        
        try(PreparedStatement ps = conn.prepareStatement(provera);
               PreparedStatement ps1 = conn.prepareStatement(proveraAdrese); ){
            
            ps1.setInt(1, address);
            ResultSet rs1 = ps1.executeQuery();
            if(!rs1.next()){
                System.out.println("Greska, ova adresa ne postoji!");
                return -1;
            }
            IdGra = rs1.getInt(1);
            
            ps.setInt(1, IdGra);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
            System.out.println("Greska, u ovom gradu vec postoji magacin!");
            return -1;
            }
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_StockroomOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String query = "insert into lokacijamagazina (IdAdr) values(?)";
        try(PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)){
            
            ps.setInt(1, address);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if(rs.next()){
                System.out.println("Magacin ubacen!");
                return rs.getInt(1);
            }
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_StockroomOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return -1;
    }

    @Override
    public boolean deleteStockroom(int idStockroom) {
        Connection conn = DB.getInstance().getConnection();
        // proveri da li magacin postoji i da li je prazan
        // ako je to slucaj, brisni ga
        
        String provera1 = "select IdLok from lokacijamagazina where IdLok=?";

        try ( PreparedStatement stm = conn.prepareStatement(provera1)) {

            stm.setInt(1, idStockroom);

            ResultSet rs = stm.executeQuery();

            if (!rs.next()) {
                System.out.println("Ne postoji magacin za zadatim ID-em");
                return false;
            }

        } catch (SQLException ex) {
            Logger.getLogger(sm180228_StockroomOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String provera = "select count(*) from lokacijamagazina lm join adresa a"
                + " on (lm.idadr=a.idadr) join zahtevpaket zp on (a.idadr=zp.pocetnaadresa)"
                + " where lm.idlok=?";
        
        try(PreparedStatement ps = conn.prepareStatement(provera)){
            
            ps.setInt(1, idStockroom);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                if(rs.getInt(1) > 0){
                    System.out.println("Greska, u magacinu ima robe!");
                    return false;
                }
            }
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_StockroomOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String brisanje = "delete from lokacijamagazina where idlok=?";
        try(PreparedStatement ps = conn.prepareStatement(brisanje)){
            
            ps.setInt(1, idStockroom);
            System.out.println("Magacin uspesno obrisan");
            return 1 == ps.executeUpdate();
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_StockroomOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }

    @Override
    public int deleteStockroomFromCity(int idCity) {
        Connection conn = DB.getInstance().getConnection();
        // najpre proveri da li postoji grad
        // potom dohvati id tog magacina i pozovi metodu iznad, a vrati id magacina
        String provera = "select IdGra from Grad where IdGra=?";
        String getIdMag = "select m.idlok from lokacijamagazina m join adresa a on (a.idadr=m.idadr) where a.idgra=?";
        int IdMag = -1;
        
        try(PreparedStatement ps = conn.prepareStatement(provera);
                PreparedStatement ps1 = conn.prepareStatement(getIdMag);){
 
            ps.setInt(1, idCity);
            ResultSet rs = ps.executeQuery();
            if(!rs.next()){
                System.out.println("Greska, grad ne postoji!");
            } else {
                ps1.setInt(1, idCity);
                ResultSet rs1 = ps1.executeQuery();
                
                if(rs1.next()){
                    IdMag = rs1.getInt(1);
                    this.deleteStockroom(IdMag);
                    return IdMag;
                }
            }
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_StockroomOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    @Override
    public List<Integer> getAllStockrooms() {
        Connection conn = DB.getInstance().getConnection();
        String query = "select idlok from lokacijamagazina";
        List<Integer> list = new ArrayList<>();
        
        try(PreparedStatement ps = conn.prepareStatement(query);
                ResultSet rs = ps.executeQuery()){
            
            while(rs.next()){
                list.add(rs.getInt(1));
            }
            return list;
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_StockroomOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
}
