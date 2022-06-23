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
import rs.etf.sab.operations.CourierRequestOperation;

/**
 *
 * @author stoja
 */
public class sm180228_CourierRequestOperation implements CourierRequestOperation {

    @Override
    public boolean insertCourierRequest(@NotNull String username, @NotNull String driverLicenceNumber) {
        Connection conn = DB.getInstance().getConnection();
        // prvo proveri da li taj kurir vec postoji (najpre username, a potom da li je vozacka zauzeta) - ako postoji, to je kraj
        // potom proveri da li u zahtevima vec postoji ()
        String proveraKurirPostoji = "select ku.idkor from kurir ku join korisnik ko on (ku.idkor=ko.idkor) where ko.korisnickoime=? or ku.vozackadozvola=?";
        try(PreparedStatement ps = conn.prepareStatement(proveraKurirPostoji)){
            
            ps.setString(1, username);
            ps.setString(2, driverLicenceNumber);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                System.out.println("Greska, vec postoji kurir sa ovim korisnickim imenom ili za ovom vozackom dozvolom!");
                return false;
            }
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String proveraZahtevPostoji = "select ko.idkor from korisnik ko "
                + " join ZahtevZaKurira zzk on (ko.idkor=zzk.podnosilac) where ko.korisnickoime=? or zzk.brojvozacke=?";
        try(PreparedStatement ps = conn.prepareStatement(proveraZahtevPostoji)){
            
            ps.setString(1, username);
            ps.setString(2, driverLicenceNumber);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                System.out.println("Greska, zahtev za ovog kurira ili za ovu vozacku vec postoji!");
                return false;
            }
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String query = "insert into ZahtevZaKurira (Podnosilac, BrojVozacke) values (?, ?)";
        String query1 = "select Idkor from korisnik where korisnickoime=?";
        String queryCheckDuplicate = "select podnosilac from zahtevzakurira where podnosilac=?";
        try(PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps1 = conn.prepareStatement(query1);
                PreparedStatement ps2 = conn.prepareStatement(queryCheckDuplicate);){
            
            ps1.setString(1, username);
            ResultSet rs1 = ps1.executeQuery();
            if(!rs1.next()){
                System.out.println("Greska, korisnik za koga se podnosi zahtev ne postoji!");
                return false;
            }
            
            int IdKor = rs1.getInt(1);
            ps2.setInt(1, IdKor);
            ResultSet check = ps2.executeQuery();
            if(check.next()){
                System.out.println("Greska, ne moze se duplirati PK!");
                return false;
            }
            
            ps.setInt(1, IdKor);
            ps.setString(2, driverLicenceNumber);
            return 1 == ps.executeUpdate();
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public boolean deleteCourierRequest(@NotNull String username) {
        Connection conn = DB.getInstance().getConnection();
        String query = "delete from ZahtevZaKurira where Podnosilac = (select IdKor from korisnik where korisnickoime=?)";
        try(PreparedStatement ps = conn.prepareStatement(query)){
            
            ps.setString(1, username);
            if (1 == ps.executeUpdate()){
                System.out.println("Uspesno obrisan zahtev za kurira!");
                return true;
            }
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public boolean changeDriverLicenceNumberInCourierRequest(@NotNull String username, @NotNull String licencePlateNumber) {
        Connection conn = DB.getInstance().getConnection();
        String proveraZahtevPostoji = "select ko.idkor from korisnik ko join zahtevzakurira zzk on (ko.idkor=zzk.podnosilac) where ko.korisnickoime=?";
        int IdKor = -1;
        try(PreparedStatement ps = conn.prepareStatement(proveraZahtevPostoji)){
            
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if(!rs.next()){
                System.out.println("Greska, ne postoji zahtev za korisnika sa datim korisnickim imenom!");
                return false;
            }
            IdKor = rs.getInt(1);
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String proveraRegBrojVecUZahtevima = "select ko.idkor from korisnik ko join zahtevzakurira zzk on (ko.idkor=zzk.podnosilac) where zzk.brojvozacke=? and ko.korisnickoime!=?";
        String query = "select idkor from korisnik where korisnickoime=?";
        try(PreparedStatement ps = conn.prepareStatement(proveraRegBrojVecUZahtevima);
                PreparedStatement ps1 = conn.prepareStatement(query)){
            
            ps.setString(1, licencePlateNumber);
            ps.setString(2, username);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                System.out.println("Greska, ovaj broj vozacke se vec nalazi u zahtevima!");
                return false;
            }
            
            ps1.setString(1, username);
            ResultSet rs1 = ps1.executeQuery();
            if(rs1.next())
                IdKor = rs1.getInt(1);
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String proveraRegBrojVecUKuririma = "select ko.idkor from korisnik ko join kurir ku on (ko.idkor=ku.idkor) where ku.vozackadozvola=?";
        try(PreparedStatement ps = conn.prepareStatement(proveraRegBrojVecUKuririma)){
            
            ps.setString(1, licencePlateNumber);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                System.out.println("Greska, ova vozacka se vec nalazi kod nekog od kurira!");
                return false;
            }
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String promeniBrojVozacke = "update ZahtevZaKurira set BrojVozacke=? where Podnosilac=?";
        try(PreparedStatement ps = conn.prepareStatement(promeniBrojVozacke)){
            
            ps.setString(1, licencePlateNumber);
            ps.setInt(2, IdKor);
            return 1==ps.executeUpdate();
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
        
    }

    @Override
    public List<String> getAllCourierRequests() {
        Connection conn = DB.getInstance().getConnection();
        String query = "select ko.korisnickoime from korisnik ko join zahtevzakurira zzk on (ko.idkor=zzk.podnosilac)";
        List<String> list = new ArrayList<>();
        
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
    public boolean grantRequest(@NotNull String username) {
        // prvo proveri da li taj korisnik postoji
        // potom proveri da li je kojim slucajem on vec kurir
        // potom proveri da li ima svoj zahtev
        // na kraju napravi novog kurira u bazi
        Connection conn = DB.getInstance().getConnection();
        String proveraKorisnikPostoji = "select IdKor from korisnik where korisnickoime=?";
        int IdKor = -1;
        try(PreparedStatement ps = conn.prepareStatement(proveraKorisnikPostoji)){
            
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()){
                System.out.println("Greska, korisnik sa tim korisnickim imenom ne postoji!");
                return false;
            }
            IdKor = rs.getInt(1);
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        /*String proveraKurirVecPostoji = "select idkor from kurir where idkor=?";
        try(PreparedStatement ps = conn.prepareStatement(proveraKurirVecPostoji)){
            
            ps.setInt(1, IdKor);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                System.out.println("Greska, kurir za kojeg se podnosi zahtev vec postoji!");
                return false;
            }
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        
        String proveraZahteva = "select podnosilac from ZahtevZaKurira where podnosilac = ?";
        try(PreparedStatement ps = conn.prepareStatement(proveraZahteva)){
            
            ps.setInt(1, IdKor);
            ResultSet rs = ps.executeQuery();
            if(!rs.next()){
                System.out.println("Greska, ne postoji zahtev za korisnika sa datim korisnickim imenom!");
                return false;
            }
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String napraviKurira = "insert into kurir(IdKor, BrIsporucenihPaketa, Status, OstvarenProfit, VozackaDozvola) values(?, 0, 0, 0, ?)";
        String dohvatiVozacku = "select brojvozacke from zahtevzakurira where podnosilac = ?";
        String brisniZahtev = "delete from ZahtevZaKurira where podnosilac = ?";
        
        try(PreparedStatement ps = conn.prepareStatement(napraviKurira);
                PreparedStatement ps1 = conn.prepareStatement(dohvatiVozacku);
                PreparedStatement ps2 = conn.prepareStatement(brisniZahtev);){
            
            ps1.setInt(1, IdKor);
            ResultSet rs = ps1.executeQuery();
            if(rs.next()){
                ps.setInt(1, IdKor);
                ps.setString(2, rs.getString(1));
                ps2.setInt(1, IdKor);
                ps2.executeUpdate();
                return 1 == ps.executeUpdate();
            }
            return false;
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
}
