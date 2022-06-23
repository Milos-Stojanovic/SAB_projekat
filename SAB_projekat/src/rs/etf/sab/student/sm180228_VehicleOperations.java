/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.student;
import com.sun.istack.internal.NotNull;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.VehicleOperations;

/**
 *
 * @author stoja
 */
public class sm180228_VehicleOperations implements VehicleOperations {

    @Override
    public boolean insertVehicle(@NotNull String licencePlateNumber, int fuelType, BigDecimal fuelConsumption, BigDecimal capacity) {
        Connection conn = DB.getInstance().getConnection();
        if(!(fuelType>=0 && fuelType<=2)){
            System.out.println("Nepodrzani tip goriva!");
            return false;
        }
        String proveraPostojiRegBr = "select Idvoz from vozilo where RegBr=?";
        
        try(PreparedStatement ps = conn.prepareStatement(proveraPostojiRegBr)){
            
            ps.setString(1, licencePlateNumber);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                System.out.println("Greska, vec postoji vozilo sa datom registracijom!");
                return false;
            }
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String dodajVozilo = "insert into vozilo(TipGoriva, Potrosnja, Nosivost, RegBr) values(?, ?, ?, ?)";
        try(PreparedStatement ps = conn.prepareStatement(dodajVozilo)){
            
            ps.setInt(1, fuelType);
            ps.setBigDecimal(2, fuelConsumption);
            ps.setBigDecimal(3, capacity);
            ps.setString(4, licencePlateNumber);
            return 1 == ps.executeUpdate();
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public int deleteVehicles(String... strings) {
        Connection conn = DB.getInstance().getConnection();
        int brObrisanih = 0;
        String obrisiVozilo = "delete from vozilo where RegBr=?";
        try(PreparedStatement ps = conn.prepareStatement(obrisiVozilo)){
            
            for(int i = 0; i < strings.length; i++){
                ps.setString(1, strings[i]);
                brObrisanih += ps.executeUpdate();
            }
            return brObrisanih;
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    @Override
    public List<String> getAllVehichles() {
        Connection conn = DB.getInstance().getConnection();
        List<String> list = new ArrayList<>();
        String query = "select regbr from vozilo";
        
        try(PreparedStatement ps = conn.prepareStatement(query);
                ResultSet rs = ps.executeQuery()){
            
            while(rs.next()){
                list.add(rs.getString(1));
            }
            return list;
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public boolean changeFuelType(@NotNull String licencePlateNumber, int fuelType) {
        Connection conn = DB.getInstance().getConnection();
        // najpre proveri da li postoji vozilo te registracije, potom da li je u magacinu i potom radi!
        int IdVoz = -1;
        if(!(fuelType>=0 && fuelType<=2)){
            System.out.println("Nepodrzani tip goriva!");
            return false;
        }
        String proveraVoziloPostoji = "select idvoz from vozilo where regbr=?";
        
        try(PreparedStatement ps = conn.prepareStatement(proveraVoziloPostoji)){
            
            ps.setString(1, licencePlateNumber);
            ResultSet rs = ps.executeQuery();
            if(!rs.next()){
                System.out.println("Greska, vozilo sa ovom registracijom ne postoji!");
                return false;
            }
            IdVoz = rs.getInt(1);
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
            
        String proveraVoziloParkirano = "select idvoz from parkirano where idvoz=?";
        try(PreparedStatement ps = conn.prepareStatement(proveraVoziloParkirano)){
            
            ps.setInt(1, IdVoz);
            ResultSet rs = ps.executeQuery();
            if(!rs.next()){
                System.out.println("Greska, ne moze se menjati tip goriva jer vozilo nije parkirano!");
                return false;
            }
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String azurirajTipGoriva = "update Vozilo set TipGoriva=? where idVoz=?";
        try(PreparedStatement ps = conn.prepareStatement(azurirajTipGoriva)){
            
            ps.setInt(1, fuelType);
            ps.setInt(2, IdVoz);
            return 1 == ps.executeUpdate();
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;   
    }

    @Override
    public boolean changeConsumption(@NotNull String licencePlateNumber, BigDecimal fuelConsumption) {
        Connection conn = DB.getInstance().getConnection();
        // najpre proveri da li postoji vozilo te registracije, potom da li je u magacinu i potom radi!
        int IdVoz = -1;
        String proveraVoziloPostoji = "select idvoz from vozilo where regbr=?";
        
        try(PreparedStatement ps = conn.prepareStatement(proveraVoziloPostoji)){
            
            ps.setString(1, licencePlateNumber);
            ResultSet rs = ps.executeQuery();
            if(!rs.next()){
                System.out.println("Greska, vozilo sa ovom registracijom ne postoji!");
                return false;
            }
            IdVoz = rs.getInt(1);
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
            
        String proveraVoziloParkirano = "select idvoz from parkirano where idvoz=?";
        try(PreparedStatement ps = conn.prepareStatement(proveraVoziloParkirano)){
            
            ps.setInt(1, IdVoz);
            ResultSet rs = ps.executeQuery();
            if(!rs.next()){
                System.out.println("Greska, ne moze se menjati potrosnja jer vozilo nije parkirano!");
                return false;
            }
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String azurirajTipGoriva = "update Vozilo set Potrosnja=? where idVoz=?";
        try(PreparedStatement ps = conn.prepareStatement(azurirajTipGoriva)){
            
            ps.setBigDecimal(1, fuelConsumption);
            ps.setInt(2, IdVoz);
            return 1 == ps.executeUpdate();
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public boolean changeCapacity(@NotNull String licencePlateNumber, BigDecimal capacity) {
        Connection conn = DB.getInstance().getConnection();
        // najpre proveri da li postoji vozilo te registracije, potom da li je u magacinu i potom radi!
        int IdVoz = -1;
        String proveraVoziloPostoji = "select idvoz from vozilo where regbr=?";
        
        try(PreparedStatement ps = conn.prepareStatement(proveraVoziloPostoji)){
            
            ps.setString(1, licencePlateNumber);
            ResultSet rs = ps.executeQuery();
            if(!rs.next()){
                System.out.println("Greska, vozilo sa ovom registracijom ne postoji!");
                return false;
            }
            IdVoz = rs.getInt(1);
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
            
        String proveraVoziloParkirano = "select idvoz from parkirano where idvoz=?";
        try(PreparedStatement ps = conn.prepareStatement(proveraVoziloParkirano)){
            
            ps.setInt(1, IdVoz);
            ResultSet rs = ps.executeQuery();
            if(!rs.next()){
                System.out.println("Greska, ne moze se menjati nosivost jer vozilo nije parkirano!");
                return false;
            }
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String azurirajTipGoriva = "update Vozilo set Nosivost=? where idVoz=?";
        try(PreparedStatement ps = conn.prepareStatement(azurirajTipGoriva)){
            
            ps.setBigDecimal(1, capacity);
            ps.setInt(2, IdVoz);
            return 1 == ps.executeUpdate();
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public boolean parkVehicle(@NotNull String licencePlateNumber, int idStockroom) {
        Connection conn = DB.getInstance().getConnection();
        // najpre proveri da li postoji vozilo te registracije, potom da li je u magacinu,
        // potom da li je u toku voznja i da li magacin postoji i na kraju radi
        int IdVoz = -1;
        String proveraVoziloPostoji = "select idvoz from vozilo where regbr=?";
        
        try(PreparedStatement ps = conn.prepareStatement(proveraVoziloPostoji)){
            
            ps.setString(1, licencePlateNumber);
            ResultSet rs = ps.executeQuery();
            if(!rs.next()){
                System.out.println("Greska, vozilo sa ovom registracijom ne postoji!");
                return false;
            }
            IdVoz = rs.getInt(1);
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String proveraVoziloParkirano = "select idvoz from parkirano where idvoz=?";
        try(PreparedStatement ps = conn.prepareStatement(proveraVoziloParkirano)){
            
            ps.setInt(1, IdVoz);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                System.out.println("Greska, vozilo je vec parkirano!");
                return false;
            }
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String proveraVoznjaUToku = "select idvoz from vozi where idvoz=?";
        try(PreparedStatement ps = conn.prepareStatement(proveraVoznjaUToku)){
            
            ps.setInt(1, IdVoz);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                System.out.println("Greska, vozilo je u toku dostave paketa!");
                return false;
            }
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String proveraMagacinPostoji = "select idlok from lokacijamagazina where idlok=?";
        try(PreparedStatement ps = conn.prepareStatement(proveraMagacinPostoji)){
            
            ps.setInt(1, idStockroom);
            ResultSet rs = ps.executeQuery();
            if(!rs.next()){
                System.out.println("Greska, magacin u koji je potrebno parkirati ne postoji!");
                return false;
            }
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String parkirajVozilo = "insert into Parkirano(IdVoz, IdLok) values (?, ?)";
        try(PreparedStatement ps = conn.prepareStatement(parkirajVozilo)){
            
            ps.setInt(1, IdVoz);
            ps.setInt(2, idStockroom);
            return 1 == ps.executeUpdate();
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }
    
}
