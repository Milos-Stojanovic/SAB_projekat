/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.student;
import com.sun.istack.internal.NotNull;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.PackageOperations;

/**
 *
 * @author stoja
 */
public class sm180228_PackageOperations implements PackageOperations {

    @Override
    public int insertPackage(int addressFrom, int addressTo, @NotNull String username, int packageType, BigDecimal weight) {
        Connection conn = DB.getInstance().getConnection();
        // proveri da li obe adrese postoje, da li postoji korisnik, da li je tip 0/1/2/3, da tezina nije <0
        // i na li su adrese iste
        // na kraju kreiraj paket
        int xOd = -1, yOd = -1;
        int xDo = -1, yDo = -1;
        int idKor = -1;
        
        String proveraAdresaOdPostoji = "select x_koord, y_koord from adresa where idadr=?";
        try(PreparedStatement ps = conn.prepareStatement(proveraAdresaOdPostoji)){
            
            ps.setInt(1, addressFrom);
            ResultSet rs = ps.executeQuery();
            if(!rs.next()){
                System.out.println("Greska, ne postoji ta pocetna adresa!");
                return -1;
            }
            xOd = rs.getInt(1);
            yOd = rs.getInt(2);
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String proveraAdresaDoPostoji = "select x_koord, y_koord from adresa where idadr=?";
        try(PreparedStatement ps = conn.prepareStatement(proveraAdresaDoPostoji)){
            
            ps.setInt(1, addressTo);
            ResultSet rs = ps.executeQuery();
            if(!rs.next()){
                System.out.println("Greska, ne postoji ta ciljna adresa!");
                return -1;
            }
            xDo = rs.getInt(1);
            yDo = rs.getInt(2);
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // provera da li su adrese iste
        if (xOd == xDo && yOd == yDo){
            System.out.println("Greska, pocetna i ciljna adresa su iste!");
            return -1;
        }
        // provera da li je paket valjanog tipa
        if(!(packageType >= 0 && packageType <= 3)){
            System.out.println("Greska, naveden tip paketa ne postoji!");
            return -1;
        }
        
        String proveraKorisnikPostoji = "select idkor from korisnik where korisnickoime=?";
        try(PreparedStatement ps = conn.prepareStatement(proveraKorisnikPostoji)){
            
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if(!rs.next()){
                System.out.println("Greska, ne postoji korisnik sa tim korisnickim imenom!");
                return -1;
            }
            idKor = rs.getInt(1);
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // provera da li je navedena valjana tezina
        if (weight.longValue() <= 0){
            System.out.println("Greska, ovakva tezina paketa nije moguca!");
            return -1;
        }
        
        String kreirajPaket = "insert into ZahtevPaket (PocetnaAdresa, ZavrsnaAdresa, IdKor, Tip, Tezina, VremeKreiranjaZahteva) values(?,?,?,?,?,?)";
        String kreirajIsporuku = "insert into Isporuka(IdPak, StatusIsporuke, cenaIsporuke, Lokacija, VremePrihvatanjaPonude) values (?, 0, 0, ?, NULL)";
        try ( PreparedStatement ps = conn.prepareStatement(kreirajPaket, PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement ps1 = conn.prepareStatement(kreirajIsporuku)) {

            ps.setInt(1, addressFrom);
            ps.setInt(2, addressTo);
            ps.setInt(3, idKor);
            ps.setInt(4, packageType);
            ps.setBigDecimal(5, weight);

            //stm.setDate(6, Date.valueOf(LocalDate.now()));
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();

            ps.setString(6, dtf.format(now));
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();

            if (rs.next()) {
                
                ps1.setInt(1, rs.getInt(1));
                ps1.setInt(2, addressFrom);
                
                if(1 == ps1.executeUpdate()){
                    System.out.println("Paket je uspesno kreiran!");
                    return rs.getInt(1);
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(sm180228_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    @Override
    public boolean acceptAnOffer(int packageId) {
        Connection conn = DB.getInstance().getConnection();
        int status = -1;
        BigDecimal cena = BigDecimal.ZERO;
        String proveraPaketPostoji = "select StatusIsporuke, CenaIsporuke from Ponuda where IdPak=?";

        try ( PreparedStatement ps = conn.prepareStatement(proveraPaketPostoji)) {

            ps.setInt(1, packageId);

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                System.out.println("Greska, ne postoji ciljni paket!");
                return false;
            }
            status = rs.getInt(1);
            cena = rs.getBigDecimal(2);
        } catch (SQLException ex) {
            Logger.getLogger(sm180228_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (status != 0) { //status=0 paket kreiran
            System.out.println("Greska, paket se ne moze prihvatiti jer mu je status pogresan!");
            return false;
        }

        String query = "update Ponuda set StatusIsporuke=1 where IdPak=? "; //prihvacena ponuda

        try ( PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, packageId);

            ps.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(sm180228_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        query = "Update Ponuda set CenaIsporuke=?, VremePrihvatanjaPonude=? where IdPak=? "; //prihvacena ponuda

        try ( PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setBigDecimal(1, cena);
            
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();

            ps.setString(2, dtf.format(now));

            ps.setInt(3, packageId);

            ps.executeUpdate();

            System.out.println("Ponuda je uspesno prihvacena!");
            return true;

        } catch (SQLException ex) {
            Logger.getLogger(sm180228_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;

    }

    @Override
    public boolean rejectAnOffer(int packageId) {
        Connection conn = DB.getInstance().getConnection();
        int status = -1;
        String provera = "select Status from Ponuda where IdPak=?";

        try ( PreparedStatement ps = conn.prepareStatement(provera)) {

            ps.setInt(1, packageId);

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                System.out.println("Greska, ne postoji ciljni paket!");
                return false;
            }
            status = rs.getInt(1);
            
        } catch (SQLException ex) {
            Logger.getLogger(sm180228_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (status != 0) { //status=0 paket kreiran
            System.out.println("Greska, paket se ne moze odbiti jer nije u odgovarajucem statusu!");
            return false;
        }

        String query = "Update Ponuda set StatusIsporuke = 4 where IdPak=? "; //odbijena ponuda

        try ( PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, packageId);

            ps.executeUpdate();
            System.out.println("Ponuda je uspesno odbijena!");
            return true;

        } catch (SQLException ex) {
            Logger.getLogger(sm180228_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;

    }

    @Override
    public List<Integer> getAllPackages() {
        Connection conn = DB.getInstance().getConnection();
        List<Integer> list = new ArrayList<>();
        String query = "select IdPak from ZahtevPaket";
        
        try(PreparedStatement ps = conn.prepareStatement(query);
                ResultSet rs = ps.executeQuery();){
            
            while(rs.next()){
                list.add(rs.getInt(1));
            }
            return list;
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
        
    }

    @Override
    public List<Integer> getAllPackagesWithSpecificType(int type) {
        Connection conn = DB.getInstance().getConnection();
        List<Integer> list = new ArrayList<>();
        String query = "select IdPak from ZahtevPaket where Tip=?";
        
        try(PreparedStatement ps = conn.prepareStatement(query)){
            
            ps.setInt(1, type);
            ResultSet rs = ps.executeQuery();
            
            while(rs.next()){
                list.add(rs.getInt(1));
            }
            return list;
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public List<Integer> getAllUndeliveredPackages() {
        Connection conn = DB.getInstance().getConnection();
        List<Integer> list = new ArrayList<>();
        String query = "select IdPak from Ponuda where StatusIsporuke=1 or StatusIsporuke=2";
        
        try(PreparedStatement ps = conn.prepareStatement(query);
                ResultSet rs = ps.executeQuery();){
            
            while(rs.next()){
                list.add(rs.getInt(1));
            }
            return list;
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public List<Integer> getAllUndeliveredPackagesFromCity(int cityID) {
        Connection conn = DB.getInstance().getConnection();
        // prvo proveri da li postoji taj grad, pa onda sve po redu
        String proveraGradPostoji = "select IdGra from Grad where IdGra=?";
        
        try(PreparedStatement ps = conn.prepareStatement(proveraGradPostoji)){
            
            ps.setInt(1, cityID);
            ResultSet rs = ps.executeQuery();
            if(!rs.next()){
                System.out.println("Greska, takav grad ne postoji!");
                return null;
            }
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        List<Integer> list = new ArrayList<>();
        String query =  "select p.idpak\n" +
                        "from ponuda p join zahtevpaket z on (p.idpak=z.idpak)\n" +
                        "	join adresa a on(z.PocetnaAdresa=a.IdAdr)\n" +
                        "	join grad g on(a.idgra=g.idgra) " +
                        "where g.idgra=? and (StatusIsporuke=1 or StatusIsporuke=2)";
        
        try(PreparedStatement ps = conn.prepareStatement(query)){
            
            ps.setInt(1, cityID);
            ResultSet rs = ps.executeQuery();
            
            while(rs.next()){
                list.add(rs.getInt(1));
            }
            return list;
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
        
    }

    @Override
    public List<Integer> getAllPackagesCurrentlyAtCity(int cityID) {
        Connection conn = DB.getInstance().getConnection();
        List<Integer> list = new ArrayList<>();
        String query = "Select IdPak from Ponuda P join ZahtevPaket ZP on (P.IdPak=ZP.IdPak) join Adresa A "
                + "on (P.Lokacija=A.IdAdr) where (Lokacija=PocetnaAdresa or Lokacija=ZavrsnaAdresa) and IdGra=? "
                + " and StatusIsporuke = 1";
        
        try (PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, cityID);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(rs.getInt(1));
            }

        } catch (SQLException ex) {
            Logger.getLogger(sm180228_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        query = "Select IdPak from Ponuda P join Adresa A on (P.Lokacija=A.IdAdr) join LokacijaMagazina M on (A.IdAdr=M.IdAdr) where IdGra=? and StatusIsporuke=1";
        try ( PreparedStatement stm = conn.prepareStatement(query)) {

            stm.setInt(1, cityID);
            ResultSet rs = stm.executeQuery();

            while (rs.next()) {
                if (!list.contains(rs.getInt(1))) {
                    list.add(rs.getInt(1));
                }
            }
            return list;

        } catch (SQLException ex) {
            Logger.getLogger(sm180228_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
        
    }

    @Override
    public boolean deletePackage(int packageID) {
        Connection conn = DB.getInstance().getConnection();
        // proveri da li paket postoji, potom da li je u statusu 0 ili 4
        // ako jeste, brisi - najpre iz ponude, potom iz zahteva
        int status = -1;
        String proveraStatusa = "select StatusIsporuke from Ponuda where IdPak=?";
        
        try(PreparedStatement ps = conn.prepareStatement(proveraStatusa)){
            
            ps.setInt(1, packageID);
            ResultSet rs = ps.executeQuery();
            if(!rs.next()){
                System.out.println("Greska, ciljni paket ne postoji!");
                return false;
            }
            status = rs.getInt(1);
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(status!=0 && status!=4){
            System.out.println("Greska, ciljni paket je u losem statusu!");
            return false;
        }
        
        String brisiPonudu = "delete from Ponuda where IdPak=?";
        try(PreparedStatement ps = conn.prepareStatement(brisiPonudu)){
            
            ps.setInt(1, packageID);
            ps.executeUpdate();
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String brisiZahtev = "delete from ZahtevPaket where IdPak=?";
        try(PreparedStatement ps = conn.prepareStatement(brisiZahtev)){
            
            ps.setInt(1, packageID);
            System.out.println("Uspesno brisanje paketa!");
            return 1 == ps.executeUpdate();
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public boolean changeWeight(int packageId, BigDecimal tezina) {
        Connection conn = DB.getInstance().getConnection();
        // najpre proveri da li paket postoji, pa je li u pravom statusu (0)
        // na kraju azuriraj tezinu!
        int status = -1;
        String proveraPaketPostoji = "select StatusIsporuke from Ponuda where IdPak=?";
        
        try(PreparedStatement ps = conn.prepareStatement(proveraPaketPostoji)){
            
            ps.setInt(1, packageId);
            ResultSet rs = ps.executeQuery();
            if(!rs.next()){
                System.out.println("Greska, ciljni paket ne postoji!");
                return false;
            }
            status = rs.getInt(1);
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(status != 0){
            System.out.println("Greska, paket nije u pravom statusu!");
            return false;
        }
        
        String azurirajTezinu = "update ZahtevPaket set Tezina=? where IdPak=?";
        
        try(PreparedStatement ps = conn.prepareStatement(azurirajTezinu)){
            
            ps.setBigDecimal(1, tezina);
            ps.setInt(2, packageId);
            return 1 == ps.executeUpdate();
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public boolean changeType(int packageId, int newType) {
        Connection conn = DB.getInstance().getConnection();
        // najpre proveri da li paket postoji, pa je li u pravom statusu (0)
        // na kraju azuriraj tezinu!
        int status = -1;
        String proveraPaketPostoji = "select StatusIsporuke from Ponuda where IdPak=?";
        
        try(PreparedStatement ps = conn.prepareStatement(proveraPaketPostoji)){
            
            ps.setInt(1, packageId);
            ResultSet rs = ps.executeQuery();
            if(!rs.next()){
                System.out.println("Greska, ciljni paket ne postoji!");
                return false;
            }
            status = rs.getInt(1);
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(status != 0){
            System.out.println("Greska, paket nije u pravom statusu!");
            return false;
        }
        if(newType<0 || newType>3){
            System.out.println("Greska, naveden je nepostojeci tip paketa!");
            return false;
        }
        
        String azurirajTezinu = "update ZahtevPaket set Tip=? where IdPak=?";
        
        try(PreparedStatement ps = conn.prepareStatement(azurirajTezinu)){
            
            ps.setInt(1, newType);
            ps.setInt(2, packageId);
            return 1 == ps.executeUpdate();
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;}

    @Override
    public int getDeliveryStatus(int packageId) {
        Connection conn = DB.getInstance().getConnection();
        // proveri da li paket postoji pa ga dovati
        String proveraPostojanja = "select IdPak from Ponuda where IdPak=?";
        
        try(PreparedStatement ps = conn.prepareStatement(proveraPostojanja)){
            
            ps.setInt(1, packageId);
            ResultSet rs = ps.executeQuery();
            if(!rs.next()){
                System.out.println("Greska, ciljni paket ne postoji!");
                return -1;
            }
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String query = "select StatusIsporuke from Ponuda where IdPak=?";
        
        try(PreparedStatement ps = conn.prepareStatement(query)){
            
            ps.setInt(1, packageId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    @Override
    public BigDecimal getPriceOfDelivery(int packageId) {
        Connection conn = DB.getInstance().getConnection();
        // proveri da li paket postoji pa potom vrati cenu
        BigDecimal price = BigDecimal.ZERO;
        String query = "select CenaIsporuke from Ponuda where IdPak=?";
        
        try(PreparedStatement ps = conn.prepareStatement(query)){
            
            ps.setInt(1, packageId);
            ResultSet rs = ps.executeQuery();
            if(!rs.next()){
                System.out.println("Greska, ciljni paket ne postoji!");
                return BigDecimal.ZERO;
            }
            price = rs.getBigDecimal(1);
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return price;
    }

    @Override
    public int getCurrentLocationOfPackage(int packageId) {
        Connection conn = DB.getInstance().getConnection();
        // proveri da li paket postoji, pa da li je u statusu 2
        // ako ovo sve valja, vrati id grada u kome se paket nalazi
        int status = -1;
        String proveraPaketPostoji = "select StatusIsporuke from Ponuda where IdPak=?";
        
        try(PreparedStatement ps = conn.prepareStatement(proveraPaketPostoji)){
            
            ps.setInt(1, packageId);
            ResultSet rs = ps.executeQuery();
            if(!rs.next()){
                System.out.println("Greska, ciljni paket ne postoji!");
                return -1;
            }
            status = rs.getInt(1);
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(status == 2){
            System.out.println("Greska, paket se trenutno nalazi u vozilu!");
            return -1;
        }
        
         String proveraAdresa = "select A.IdGra from ZahtevPaket ZP join Ponuda P on (ZP.IdPak=P.IdPak) join Adresa A on (ZP.PocetnaAdresa=A.IdAdr) join Adresa A2 on (ZP.ZavrsnaAdresa=A2.IdAdr) where (Lokacija = PocetnaAdresa or Lokacija = ZavrsnaAdresa) and P.IdPak=? ";

        try ( PreparedStatement ps = conn.prepareStatement(proveraAdresa)) {

            ps.setInt(1, packageId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("Paket se nalazi na pocetnoj ili krajnjoj adresi!");
                return rs.getInt(1);
            }

        } catch (SQLException ex) {
            Logger.getLogger(sm180228_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        String proveraMagacin = "select IdGra from Ponuda P join LokacijaMagazina M on (P.Lokacija=M.IdAdr) join Adresa A on (M.IdAdr=A.IdAdr) where IdPak=? ";

        try ( PreparedStatement ps = conn.prepareStatement(proveraMagacin)) {

            ps.setInt(1, packageId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("Paket se nalazi u magacinu!");
                return rs.getInt(1);
            }

        } catch (SQLException ex) {
            Logger.getLogger(sm180228_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    @Override
    public Date getAcceptanceTime(int packageId) {
        Connection conn = DB.getInstance().getConnection();
        // proveri da li paket postoji, da nije u statusu 0/4 i onda vrati vreme
        int status = -1;
        Date datum = null;
        String query = "select VremePrihvatanjaPonude, StatusIsporuke from Ponuda where IdPak=?";
        
        try(PreparedStatement ps = conn.prepareStatement(query)){
            
            ps.setInt(1, packageId);
            ResultSet rs = ps.executeQuery();
            if(!rs.next()){
                System.out.println("Greska, ciljni paket ne postoji!");
                return null;
            }
            datum = rs.getDate(1);
            status = rs.getInt(2);
            
        } catch (SQLException ex) {
            Logger.getLogger(sm180228_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(status == 0 || status == 4){
            System.out.println("Greska, ponuda nije prihvacena!");
        }
        return datum;
        
    }
    
}
