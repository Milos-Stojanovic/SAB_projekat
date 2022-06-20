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
import java.util.regex.Pattern;
import rs.etf.sab.operations.UserOperations;

/**
 *
 * @author stoja
 */
public class sm180228_UserOperations implements UserOperations {

    @Override
    public boolean insertUser(String username, String firstName, String lastName, String password, int idAddress) {
        // najpre odradi provere regex-a za lozinku i da li su za fN i lN pocetna slova velika
        // potom da li vec postoji taj username u bazi
        // potom da li postoji da adresa u bazi
        // ako je sve ok, ubaci u bazu
        if (!Pattern.matches("[A-Z][a-z]*", firstName)){
            System.out.println("Greska, ime mora poceti velikim slovom!");
            return false;
        }
        if (!Pattern.matches("[A-Z][a-z]*", lastName)){
            System.out.println("Greska, prezime mora poceti velikim slovom!");
            return false;
        }
        if (!Pattern.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()[{}]:;',?/*~$^+=<>]).{8,}$", password)){
            System.out.println("Greska, lozinka je nepravilnog formata!");
            return false;
        }
        
        Connection conn = DB.getInstance().getConnection();
        String proveraUsernameZauzet = "select idkor from korisnik where korisnickoime=?";
        try(PreparedStatement ps = conn.prepareStatement(proveraUsernameZauzet)){
            
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                System.out.println("Greska, vec postoji korisnik sa ovim korisnickim imenom!");
                return false;
            }
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String proveraAdresaPostoji = "select idadr from adresa where idadr=?";
        try(PreparedStatement ps = conn.prepareStatement(proveraAdresaPostoji)){
            
            ps.setInt(1, idAddress);
            ResultSet rs = ps.executeQuery();
            if(!rs.next()){
                System.out.println("Greska, ne postoji ciljna adresa!");
                return false;
            }
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String query = "insert into korisnik (Ime, Prezime, KorisnickoIme, Sifra, IdAdr) values (?, ?, ?, ?, ?)";
        try(PreparedStatement ps = conn.prepareStatement(query)){
            
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, username);
            ps.setString(4, password);
            ps.setInt(5, idAddress);
            ps.executeUpdate();
            System.out.println("Korisnik uspesno dodat!");
            return true;
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }

    @Override
    public boolean declareAdmin(@NotNull String username) {
        // proveri prvo da korisnik nije vec admin
        // ako nije, ubaci ga kao admina
        Connection conn = DB.getInstance().getConnection();
        String provera = "select a.idkor from administrator a join korisnik k on (a.idkor=k.idkor)"
                + " where k.korisnickoime=?";
        int IdKor = -1;
        
        try(PreparedStatement ps = conn.prepareStatement(provera)){
            
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                System.out.println("Greska, ovaj korisnik je vec administrator!");
                return false;
            }
            IdKor = rs.getInt(1);
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String query = "insert into Administrator (IdKor) values (?)";
        try(PreparedStatement ps = conn.prepareStatement(query)){
            
            ps.setInt(1, IdKor);
            return 1 == ps.executeUpdate();
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
        
    }

    @Override
    public int getSentPackages(String... strings) {
        Connection conn = DB.getInstance().getConnection();
        String query = "select z.idpak from zahtevpaket z join korisnik k on (z.idkor=k.idkor) where k.korisnickoime=?";
        int cnt = 0;
        int noUser = 0;
        String proveriPostojiKorisnik = "select idkor from korisnik where korisnickoime=?";
        
        try(PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps1 = conn.prepareStatement(proveriPostojiKorisnik)){
            
            for (int i = 0; i < strings.length; i++){
                ps1.setString(1, strings[i]);
                ResultSet rs1 = ps1.executeQuery();
                if(!rs1.next()){
                    noUser += 1;
                    continue;
                }
                
                ps.setString(1, strings[i]);
                ResultSet rs = ps.executeQuery();
                while (rs.next()){
                    if(cnt == -1) cnt = 0;
                    cnt += 1;
                }
            }
            
            if(noUser==strings.length)
                return -1;
            else
                return cnt;
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return cnt;
    }

    @Override
    public int deleteUsers(String... strings) {
        Connection conn = DB.getInstance().getConnection();
        int brObrisanih = 0;
        String query = "select idkor from korisnik where korisnickoime=?";
        String deleteUser = "delete from korisnik where idkor=?";
        String deleteKurir = "delete from kurir where idkor=?";
        String deleteAdmin = "delete from administrator where idkor=?";
        
        try(PreparedStatement ps1 = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(deleteUser);
                PreparedStatement ps3 = conn.prepareStatement(deleteKurir);
                PreparedStatement ps4 = conn.prepareStatement(deleteAdmin);){
            
            for (int i = 0; i < strings.length; i++){
                ps1.setString(1, strings[i]);
                ResultSet rs = ps1.executeQuery();
                if(rs.next()){
                    int IdKor = rs.getInt(1);
                    ps2.setInt(1, IdKor);
                    ps3.setInt(1, IdKor);
                    ps4.setInt(1, IdKor);
                    brObrisanih += ps2.executeUpdate();
                    brObrisanih += ps3.executeUpdate();
                    brObrisanih += ps4.executeUpdate();
                }
            }
            System.out.println("Kraj brisanja");
            return brObrisanih;
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return brObrisanih;
    }

    @Override
    public List<String> getAllUsers() {
        Connection conn = DB.getInstance().getConnection();
        String query = "select KorisnickoIme from Korisnik";
        List<String> list = new ArrayList<>();
        
        try(PreparedStatement ps = conn.prepareStatement(query);
                ResultSet rs = ps.executeQuery();){
            
            while(rs.next()){
                list.add(rs.getString(1));
            }
            return list;
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
}
