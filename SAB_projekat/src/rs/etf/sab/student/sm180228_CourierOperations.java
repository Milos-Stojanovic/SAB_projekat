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
import rs.etf.sab.operations.CourierOperations;

/**
 *
 * @author stoja
 */
public class sm180228_CourierOperations implements CourierOperations {

    @Override
    public boolean insertCourier(@NotNull String courierUsername, @NotNull String driverLicenceNumber) {
        // prvo provera da li postoji korisnik sa datim korisnickim imenom : da-vrati njegov id, ne-kraj
        // potom provera da li je taj korisnik vec kurir: da-kraj
        // potom provere da li postoji zahtev za tog korisnika da postane kurir (i da li je sav info jedinstven): ne-kraj
        // potom dodaj kurira u bazu i tjt
        Connection conn = DB.getInstance().getConnection();
        String proveraPostojiKorisnik = "select IdKor from Korisnik where KorisnickoIme = ?";
        int IdKor = 0;
        
        try(PreparedStatement ps = conn.prepareStatement(proveraPostojiKorisnik)){
            
            ps.setString(1, courierUsername);
            ResultSet rs = ps.executeQuery();
            if(!rs.next()){
                System.out.println("Greska, ne postoji korisnik sa tim korisnickim imenom!");
                return false;
            }
            
            IdKor = rs.getInt(1);
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CourierOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String proveraKurirVecPostojiVozacka = "select IdKor from Kurir where VozackaDozvola = ?";
        try(PreparedStatement ps = conn.prepareStatement(proveraKurirVecPostojiVozacka)){
            
            ps.setString(1, driverLicenceNumber);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                System.out.println("Greska, vec postoji kurir sa prilozenom vozackom dozvolom!");
                return false;
            }
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CourierOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String proveraKurirVecPostoji = "select ku.IdKor from Kurir ku join korisnik ko on (ku.idkor=ko.idkor) where ko.korisnickoime=? and ku.VozackaDozvola = ?";
        try(PreparedStatement ps = conn.prepareStatement(proveraKurirVecPostoji)){
            
            ps.setString(1, courierUsername);
            ps.setString(2, driverLicenceNumber);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                System.out.println("Greska, vec postoji kurir sa prilozenom korisnickim imenom i dozvolom!");
                return false;
            }
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CourierOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String proveraZahtevPostojiId = "select Podnosilac from ZahtevZaKurira where Podnosilac = ? and BrojVozacke != ?";
        try(PreparedStatement ps = conn.prepareStatement(proveraZahtevPostojiId)){
            
            ps.setInt(1, IdKor);
            ps.setString(2, driverLicenceNumber);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                System.out.println("Greska, vec postoji zahtev za datog kurira sa drugim brojem vozacke dozvole!");
                return false;
            }
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CourierOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String proveraZahtevPostojiVozacka = "select Podnosilac from ZahtevZaKurira where Podnosilac != ? and BrojVozacke = ?";
        try(PreparedStatement ps = conn.prepareStatement(proveraZahtevPostojiId)){
            
            ps.setInt(1, IdKor);
            ps.setString(2, driverLicenceNumber);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                System.out.println("Greska, vec postoji zahtev drugog kurira sa prilozenim brojem vozacke dozvole!");
                return false;
            }
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CourierOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //String proveraZahtevPostoji = "select Podnosilac from ZahtevZaKurira where Podnosilac = ? and BrojVozacke = ?";
        String query = "insert into Kurir(IdKor, BrIsporucenihPaketa, Status, OstvarenProfit, VozackaDozvola) values(?, 0, 0, 0, ?)";
        String query1 = "delete from ZahtevZaKurira where Podnosilac = ? and BrojVozacke = ?";
        try(//PreparedStatement ps = conn.prepareStatement(proveraZahtevPostoji);
                PreparedStatement ps1 = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query1);){
            
            /*ps.setInt(1, IdKor);
            ps.setString(2, driverLicenceNumber);
            ResultSet rs = ps.executeQuery();
            if(!rs.next()){
                System.out.println("Greska, zahtev sa prilozenim informacijama ne postoji!");
                return false;
            }*/
            
            ps1.setInt(1, IdKor);
            ps1.setString(2, driverLicenceNumber);
            ps2.setInt(1, IdKor);
            ps2.setString(2, driverLicenceNumber);
            
            int val = ps1.executeUpdate();
            ps2.executeUpdate();
            System.out.println("Kurir dodat i zahtev obrisan!");
            
            return 1 == val;
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CourierOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        return false;
    }

    @Override
    public boolean deleteCourier(@NotNull String username) {
        Connection conn = DB.getInstance().getConnection();
        String provera = "select KO.IdKor from Korisnik KO join Kurir KU on (KO.IdKor=KU.IdKor) where KO.KorisnickoIme = ?";
        int IdKor = 0;
        
        try(PreparedStatement ps = conn.prepareStatement(provera)){
            
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if(!rs.next()){
                System.out.println("Greska, ne postoji korisnik sa datim korisnickim imenom!");
            }
            IdKor = rs.getInt(1);
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CourierOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String query = "delete from Kurir where IdKor = ?";
        
        try(PreparedStatement ps = conn.prepareStatement(provera)){
            
            ps.setInt(1, IdKor);
            return 1 == ps.executeUpdate();
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CourierOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }

    @Override
    public List<String> getCouriersWithStatus(int status) {
        Connection conn = DB.getInstance().getConnection();
        List<String> list = new ArrayList<>();
        String query = "select KO.KorisnickoIme from Korisnik KO join Kurir KU on (KO.IdKor=KU.IdKor) where KU.Status = ?";
        
        try(PreparedStatement ps = conn.prepareStatement(query)){
            
            ps.setInt(1, status);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                list.add(rs.getString(1));
            }
            
            return list;
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CourierOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }

    @Override
    public List<String> getAllCouriers() {
        Connection conn = DB.getInstance().getConnection();
        List<String> list = new ArrayList<>();
        String query = "select KO.KorisnickoIme from Korisnik KO join Kurir KU on (KO.IdKor=KU.IdKor) order by KU.OstvarenProfit desc";
        
        try(PreparedStatement ps = conn.prepareStatement(query);
                ResultSet rs = ps.executeQuery()){
            
            while(rs.next()){
                list.add(rs.getString(1));
            }
            
            return list;
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CourierOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }

    @Override
    public BigDecimal getAverageCourierProfit(int numberOfDeliveries) {
        Connection conn = DB.getInstance().getConnection();
        String query = "";
        switch(numberOfDeliveries){
            case -1:
                query = "select avg(OstvarenProfit) from Kurir";
                break;
            default:
                query = "select avg(OstvarenProfit) from Kurir where BrIsporucenihPaketa = ?";
                break;
        }
        
        try(PreparedStatement ps = conn.prepareStatement(query)){
            
            if(numberOfDeliveries != -1){
                ps.setInt(1, numberOfDeliveries);
            }
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getBigDecimal(1);
            }
            
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CourierOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
}
