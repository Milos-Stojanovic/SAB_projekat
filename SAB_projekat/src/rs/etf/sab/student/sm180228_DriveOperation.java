/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.student;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.DriveOperation;

/**
 *
 * @author stoja
 */
public class sm180228_DriveOperation implements DriveOperation {

    public static class Paket {
        
        public int IdPak, X_koord, Y_koord, PocetnaAdresa, ZavrsnaAdresa;
        public double Tezina;
        
        public Paket(int IdPak, int X_koord, int Y_koord, int PocetnaAdresa, int ZavrsnaAdresa, double Tezina){
            this.IdPak = IdPak;
            this.X_koord = X_koord;
            this.Y_koord = Y_koord;
            this.PocetnaAdresa = PocetnaAdresa;
            this.ZavrsnaAdresa = ZavrsnaAdresa;
            this.Tezina = Tezina;
        }
    }
    
    
    public List<Paket> pomeri_pakete(List<Paket> lista, Connection conn) {

        List<Paket> list = new ArrayList<>(lista);

        for (int i = 0; i < list.size(); i++) {
            try(PreparedStatement ps = conn.prepareStatement("select x_koord, y_koord from Adresa where IdAdr=?")) {

                int adr = list.get(i).ZavrsnaAdresa;
                ps.setInt(1, adr);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    list.get(i).X_koord = rs.getInt(1);
                    list.get(i).Y_koord = rs.getInt(2);
                }

            } catch (SQLException ex) {
                Logger.getLogger(sm180228_DriveOperation.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return list;
    }
    
  
    public void sortiraj_pakete_po_euklidskoj_distanci(List<Paket> lista, int X, int Y) {

        int index = 0, X1 = X, Y1 = Y;
        
        while (index != lista.size()-1) {

            for (int i = index; i < lista.size()-1; i++) {
                for (int j = i + 1; j < lista.size(); j++) {

                    double i1 = Math.sqrt(Math.pow(lista.get(i).X_koord - X1, 2) + Math.pow(lista.get(i).Y_koord - Y1, 2));
                    double j1 = Math.sqrt(Math.pow(lista.get(j).X_koord - X1, 2) + Math.pow(lista.get(j).Y_koord - Y1, 2));

                    if (i1 > j1) {
                        Paket pom = lista.get(i);
                        lista.set(i, lista.get(j));
                        lista.set(j, pom);
                    }
                }
            }
            X1 = lista.get(index).X_koord;
            Y1 = lista.get(index).Y_koord;
            index++;
        }
    }
    
    
    @Override
    public boolean planingDrive(String username) {
        // proveris da li postoji kurir tog username-a sa statusom 0,
        // potom dohvatis id grada sa njegove adrese,
        // potom iz njegovog grada dohvatis id magacina, id njegove adrese i koordinate njegove adrese
        
        // potom dohvatis vozila koja su parkirana u tom magacinu (uhvatis 1 od njih i vratis njegove id i nosivost)
        // potom proveris da li treba pokupiti pakete iz grada u kojem se nalazi magacin
        // potom dodas vozaca u Vozi entitet
        
        // potom dodas i one pakete iz magacina i ako ima paketa za isporuku nastavljas da pravis plan
        
        // obrises vozilo iz Parkiranih
        // promenis status kuriru
        Connection conn = DB.getInstance().getConnection();
        List<Paket> listaPaketa = new ArrayList<>();
        int IdKor = -1;
        String proveraPostojiSlobodanKurir = "select k.idkor from kurir k join korisnik ko on (k.idkor=ko.idkor) where korisnickoime=? and status=0";
        
        try(PreparedStatement ps = conn.prepareStatement(proveraPostojiSlobodanKurir)){
            
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if(!rs.next()){
                System.out.println("Greska, kurir tog korisnickog imena ili ne postoji ili trenutno vozi!");
                return false;
            }
            IdKor = rs.getInt(1);
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_DriveOperation.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        int IdGra = -1;
        String proveraDohvatiGrad = "select a.idgra from korisnik ko join adresa a on (ko.idadr=a.idadr) where ko.idkor=?";
        
        try(PreparedStatement ps = conn.prepareStatement(proveraDohvatiGrad)){
            
            ps.setInt(1, IdKor);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                IdGra = rs.getInt(1);
            }
            
        } catch(SQLException ex){
            Logger.getLogger(sm180228_DriveOperation.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String provera = "select IdLok, x_koord, y_koord, A.IdAdr from LokacijaMagazina M join Adresa A on (M.IdAdr=A.IdAdr) where IdGra=?";

        int IdMag = -1;
        int X_koord_mag = -1, Y_koord_mag = -1;
        int IdAdrMag = -1; //adresa magacina

        try(PreparedStatement ps = conn.prepareStatement(provera)){

            ps.setInt(1, IdGra);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                System.out.println("Greska, u gradu u kojem se nalazi kurir nema magacina!");
                return false;
            }

            IdMag = rs.getInt(1);
            X_koord_mag = rs.getInt(2);
            Y_koord_mag = rs.getInt(3);
            IdAdrMag = rs.getInt(4);

        } catch (SQLException ex) {
            Logger.getLogger(sm180228_DriveOperation.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String proveraDohvatiVozilaParkiranaUMagacinu = "select V.IdVoz, Nosivost from Vozilo V join Parkirano P on (P.IdVoz=V.IdVoz) where IdLok=? and V.IdVoz not in (select IdVoz from Vozi)";
        int IdVoz = -1;
        double Nosivost = 0, TezinaPaketa = 0;

        try(PreparedStatement ps = conn.prepareStatement(proveraDohvatiVozilaParkiranaUMagacinu)){

            ps.setInt(1, IdMag);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                System.out.println("Greska, u magacinu trenutno nema slobodnih vozila!");
                return false;
            }

            IdVoz = rs.getInt(1);
            Nosivost = (rs.getBigDecimal(2)).doubleValue();

        } catch (SQLException ex) {
            Logger.getLogger(sm180228_DriveOperation.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Provera da li treba pokupiti pakete iz istog grada
        String proveraPokupiPaketeIzIstogGrada = "select P.IdPak, x_koord, y_koord, P.PocetnaAdresa, P.ZavrsnaAdresa, Tezina"
                + " from ZahtevPaket P join Ponuda Po on (P.IdPak=Po.IdPak) join Adresa A on (P.PocetnaAdresa=A.IdAdr)"
                + " where IdGra=? and StatusIsporuke=1 order by VremePrihvatanjaPonude DESC"; //dohvata sve pakete sa prihvacenom ponudom!

        try(PreparedStatement ps = conn.prepareStatement(proveraPokupiPaketeIzIstogGrada)){

            ps.setInt(1, IdGra);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Paket pak = new Paket(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getInt(5), rs.getBigDecimal(6).doubleValue());
                if (TezinaPaketa + pak.Tezina <= Nosivost) {
                    listaPaketa.add(pak);
                    TezinaPaketa += pak.Tezina;
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(sm180228_DriveOperation.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String proveraDodajVoziEntitet = "insert into Vozi (IdKor, IdVoz, PredjenPut, TrenutnaLokacija) values(?,?,0,?)";

        try(PreparedStatement ps = conn.prepareStatement(proveraDodajVoziEntitet)){

            ps.setInt(1, IdKor);
            ps.setInt(2, IdVoz);
            ps.setInt(3, IdAdrMag);
            ps.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(sm180228_DriveOperation.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        List<Paket> listaPaketaMagacin = new ArrayList<>();
        provera = "select P.IdPak, x_koord, y_koord, PocetnaAdresa, ZavrsnaAdresa, Tezina from ZahtevPaket Z join Ponuda P on (Z.idpak=P.idpak) "
                + " join Adresa A on (Z.ZavrsnaAdresa=A.IdAdr) where StatusIsporuke=2 and Lokacija=?";

        try(PreparedStatement ps = conn.prepareStatement(provera)){

            ps.setInt(1, IdAdrMag);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Paket pak = new Paket(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getInt(5), rs.getBigDecimal(6).doubleValue());

                if (TezinaPaketa + pak.Tezina <= Nosivost) {
                    listaPaketaMagacin.add(pak);
                    TezinaPaketa += pak.Tezina;
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(sm180228_DriveOperation.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(listaPaketa.size() != 0  ||  listaPaketaMagacin.size() != 0){
            
            String proveraObrisiParkiranoVozilo = "delete from Parkirano where IdVoz=?";

            try(PreparedStatement ps = conn.prepareStatement(proveraObrisiParkiranoVozilo)){

                ps.setInt(1, IdVoz);
                ps.executeUpdate();

            } catch (SQLException ex) {
                Logger.getLogger(sm180228_DriveOperation.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
            int polaznaAdresa = IdAdrMag; //ako se treba vratiti u magacin onda isporuka krene od magacina u suprotnom od poslednje pokupljenog paketa u gradu
            if (listaPaketaMagacin.isEmpty()) {
                polaznaAdresa = listaPaketa.get(listaPaketa.size() - 1).PocetnaAdresa;
            }

            int Xt = -1;
            int Yt = -1;
            String proveraDohvatiKoordinate = "select x_koord, y_koord from Adresa where IdAdr=?";

            try(PreparedStatement ps = conn.prepareStatement(proveraDohvatiKoordinate)){

                ps.setInt(1, polaznaAdresa);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Xt = rs.getInt(1);
                    Yt = rs.getInt(2);
                }

            } catch (SQLException ex) {
                Logger.getLogger(sm180228_DriveOperation.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            String proveraAzurirajKurira = "update Kurir set Status=1 where IdKor=?";

            try(PreparedStatement stm = conn.prepareStatement(proveraAzurirajKurira)){

                stm.setInt(1, IdKor);
                stm.executeUpdate();

            } catch (SQLException ex) {
                Logger.getLogger(sm180228_DriveOperation.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            for (int i = 0; i < listaPaketa.size(); i++) {
                provera = "insert into uPrevozu (Flag, IdPak, IdKor, IdVoz) values(0,?,?,?)"; // status 0 - gledam PocetnaAdresa

                try(PreparedStatement ps = conn.prepareStatement(provera)){

                    ps.setInt(1, listaPaketa.get(i).IdPak);
                    ps.setInt(2, IdKor);
                    ps.setInt(3, IdVoz);
                    ps.executeUpdate();

                } catch (SQLException ex) {
                    Logger.getLogger(sm180228_DriveOperation.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            String proveraPaketiUMagacinu = "select z.IdPak, Tezina from ZahtevPaket z join ponuda p on (z.idpak=p.idpak) where Lokacija=? and StatusIsporuke!=3"; // dohvata sve pakete koji su u magacinu
            List<Integer> paketiUMagacinu = new ArrayList<>();

            try(PreparedStatement ps = conn.prepareStatement(proveraPaketiUMagacinu)){

                ps.setInt(1, IdAdrMag);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    double tezina = rs.getBigDecimal(2).doubleValue();
                    if (TezinaPaketa+tezina <= Nosivost) {
                        TezinaPaketa += tezina;
                        paketiUMagacinu.add(rs.getInt(1));
                    }

                }

            } catch (SQLException ex) {
                Logger.getLogger(sm180228_DriveOperation.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            for (int i = 0; i < paketiUMagacinu.size(); i++) {
                String provera1 = "insert into uPrevozu (Flag, IdPak, IdKor, IdVoz) values(4,?,?,?)"; // status 4 znaci da treba pokupiti pakete u magacinu

                try(PreparedStatement ps = conn.prepareStatement(provera1)) {

                    ps.setInt(1, paketiUMagacinu.get(i));
                    ps.setInt(2, IdKor);
                    ps.setInt(3, IdVoz);
                    ps.executeUpdate();

                } catch (SQLException ex) {
                    Logger.getLogger(sm180228_DriveOperation.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            List<Paket> listaZaIsporuku = pomeri_pakete(listaPaketa, conn); //sada ce paketi da se isporucuju 
            listaZaIsporuku.addAll(listaPaketaMagacin);
            sortiraj_pakete_po_euklidskoj_distanci(listaZaIsporuku, Xt, Yt); //sortira listu paketa koji ce se isporucivati po euklidu 

            //
            int IdGrad = -1;

            for (int i = 0; i < listaZaIsporuku.size(); ++i) {

                String proveraUbaciStatus1 = "insert into uPrevozu (Flag, IdPak, IdKor, IdVoz) values(1,?,?,?)"; // status 1 znaci da gledam IdAOd

                try(PreparedStatement ps = conn.prepareStatement(proveraUbaciStatus1)){

                    ps.setInt(1, listaZaIsporuku.get(i).IdPak);
                    ps.setInt(2, IdKor);
                    ps.setInt(3, IdVoz);
                    ps.executeUpdate();

                } catch (SQLException ex) {
                    Logger.getLogger(sm180228_DriveOperation.class.getName()).log(Level.SEVERE, null, ex);
                }

                List<Integer> listaPonovoPokupi = new ArrayList<>();

                String proveraImaPosiljki = "select IdGra from ZahtevPaket Z join Adresa A on(A.IdAdr=Z.ZavrsnaAdresa) where IdPak=? "; // Da li za taj grad gde se isporucuje ima jos posiljki za kupljenje

                try(PreparedStatement ps = conn.prepareStatement(proveraImaPosiljki)){

                    ps.setInt(1, listaZaIsporuku.get(i).IdPak);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        if (IdGrad == -1) {
                            IdGrad = rs.getInt(1);
                        }
                        if (IdGrad != rs.getInt(1) || i == listaZaIsporuku.size()-1) {
                            IdGrad = rs.getInt(1);

                            if (i != listaZaIsporuku.size()-1) {
                                String brisanje1 = "delete from uPrevozu where IdPak=? and IdKor=? and IdVoz=? and Flag=1";

                                try(PreparedStatement ps1 = conn.prepareStatement(brisanje1)){

                                    ps1.setInt(1, listaZaIsporuku.get(i).IdPak);
                                    ps1.setInt(2, IdKor);
                                    ps1.setInt(3, IdVoz);
                                    ps1.executeUpdate();

                                } catch (SQLException ex) {
                                    Logger.getLogger(sm180228_DriveOperation.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            String provera1 = "select P.IdPak, Tezina from ZahtevPaket Z join Adresa A on(Z.PocetnaAdresa=A.IdAdr) join Ponuda P on (P.IdPak=Z.IdPak)"
                                    + " where IdGra=? and StatusIsporuke=1 and P.IdPak not in "
                                    + "(select IdPak from uPrevozu) order by VremePrihvatanjaPonude ASC";

                            try(PreparedStatement ps2 = conn.prepareStatement(provera1)){

                                ps2.setInt(1, IdGrad);

                                ResultSet rsss = ps2.executeQuery();
                                while (rsss.next()) {

                                    if (TezinaPaketa + rsss.getBigDecimal(2).doubleValue() <= Nosivost) {
                                        TezinaPaketa += rsss.getBigDecimal(2).doubleValue();
                                        listaPonovoPokupi.add(rsss.getInt(1));
                                    }
                                }
                            } catch (SQLException ex) {
                                Logger.getLogger(sm180228_DriveOperation.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            String proveraPokupiIVrati = "insert into uPrevozu (Flag, IdPak, IdKor, IdVoz) values(3,?,?,?)"; // kupljenje ali lete u magacin

                            for (int j = 0; j < listaPonovoPokupi.size(); j++) {
                                try(PreparedStatement ps3 = conn.prepareStatement(proveraPokupiIVrati)){

                                    ps3.setInt(1, listaPonovoPokupi.get(j));
                                    ps3.setInt(2, IdKor);
                                    ps3.setInt(3, IdVoz);
                                    ps3.executeUpdate();

                                } catch (SQLException ex) {
                                    Logger.getLogger(sm180228_DriveOperation.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }

                            // provera za magacin
                            int adr_magacina_u_trenutnom_gradu = -1;
                            String proveraMagacin = "select M.IdAdr from Adresa A join LokacijaMagazina M on (M.IdAdr=A.IdAdr) where IdGra=?"; // Magacin u trenutnom gradu

                            try(PreparedStatement rs4 = conn.prepareStatement(proveraMagacin)){

                                rs4.setInt(1, IdGrad);
                                ResultSet rs1 = rs4.executeQuery();
                                if (rs1.next()) {
                                    adr_magacina_u_trenutnom_gradu = rs1.getInt(1);
                                }

                            } catch (SQLException ex) {
                                Logger.getLogger(sm180228_DriveOperation.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            //DEO ZA MAGACIN DA KOMBI ODE U MAGACIN PRVO
                            String proveraDohvPakUMagacinu = "select Z.IdPak, Tezina from ZahtevPaket Z join Ponuda P on (Z.idpak=p.idpak) where Lokacija=?"; // dohvata sve pakete koji su u magacinu
                            paketiUMagacinu = new ArrayList<>();

                            try(PreparedStatement ps5 = conn.prepareStatement(proveraDohvPakUMagacinu)){

                                ps5.setInt(1, adr_magacina_u_trenutnom_gradu);
                                ResultSet rs2 = ps5.executeQuery();

                                while (rs2.next()) {
                                    double tezina = rs2.getBigDecimal(2).doubleValue();
                                    if (TezinaPaketa + tezina <= Nosivost) {
                                        TezinaPaketa += tezina;
                                        paketiUMagacinu.add(rs2.getInt(1));
                                    }
                                }

                            } catch (SQLException ex) {
                                Logger.getLogger(sm180228_DriveOperation.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            for (int j = 0; j < paketiUMagacinu.size(); j++) {
                                String provera2 = "insert into uPrevozu (Flag, IdPak, IdKor, IdVoz) values(4,?,?,?)"; // status 4 znaci da treba pokupiti pakete u magacinu

                                try(PreparedStatement ps6 = conn.prepareStatement(provera2)){

                                    ps6.setInt(1, paketiUMagacinu.get(j));
                                    ps6.setInt(2, IdKor);
                                    ps6.setInt(3, IdVoz);
                                    ps6.executeUpdate();

                                } catch (SQLException ex) {
                                    Logger.getLogger(sm180228_DriveOperation.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            //kraj magacin provere
                            
                            if (i != listaZaIsporuku.size()-1) {

                                String provera3 = "insert into uPrevozu (Flag, IdPak, IdKor, IdVoz) values(1,?,?,?)"; // status 1 znaci da gledam IdAOd

                                try(PreparedStatement ps7 = conn.prepareStatement(provera3)){

                                    ps7.setInt(1, listaZaIsporuku.get(i).IdPak);
                                    ps7.setInt(2, IdKor);
                                    ps7.setInt(3, IdVoz);
                                    ps7.executeUpdate();

                                } catch (SQLException ex) {
                                    Logger.getLogger(sm180228_DriveOperation.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                    }

                } catch (SQLException ex) {
                    Logger.getLogger(sm180228_DriveOperation.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
            
            for (int i = 0; i < listaZaIsporuku.size(); i++) {
                System.out.println(listaZaIsporuku.get(i).IdPak);
            }
            
            ArrayList<Paket> listaZaMagacin = new ArrayList<>();

            for (int i = 0; i < listaZaIsporuku.size(); i++) {

                int AdresaIsporuka = listaZaIsporuku.get(i).ZavrsnaAdresa;
                String provera4 = "select IdPak, Tezina from ZahtevPaket where PocetnaAdresa=?"; // status 1 znaci da gledam IdAOd

                try(PreparedStatement ps = conn.prepareStatement(provera4)){

                    ps.setInt(1, AdresaIsporuka);
                    ResultSet rs = ps.executeQuery();

                    while (rs.next()) {
                        TezinaPaketa -= listaZaIsporuku.get(i).Tezina;

                        if (TezinaPaketa + rs.getBigDecimal(2).doubleValue() <= Nosivost) {
                            TezinaPaketa += rs.getBigDecimal(2).doubleValue();

                            Paket pak = new Paket(rs.getInt(1), 0, 0, -1, -1, rs.getBigDecimal(2).doubleValue());
                            listaZaMagacin.add(pak);
                        }

                    }

                } catch (SQLException ex) {
                    Logger.getLogger(sm180228_DriveOperation.class.getName()).log(Level.SEVERE, null, ex);
                }

            }

            for (int i = 0; i < listaZaMagacin.size(); i++){

                String provera5 = "insert into uPrevozu (Flag, IdPak, IdKor, IdVoz) values(2,?,?,?)"; // status 2 znaci da je Paket za magacin

                try ( PreparedStatement stm = conn.prepareStatement(provera5)){

                    stm.setInt(1, listaZaMagacin.get(i).IdPak);
                    stm.setInt(2, IdKor);
                    stm.setInt(3, IdVoz);

                    stm.executeUpdate();

                } catch (SQLException ex) {
                    Logger.getLogger(sm180228_DriveOperation.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            return true;

        }

        return false;
    }

    @Override
    public int nextStop(String username) {
        
        Connection conn = DB.getInstance().getConnection();
        // prvo proveri da li postoji takav kurir koji je slobodan
        int IdK = -1;
        int IdVoz = -1;
        int kurirovGrad = -1;
        String proveraKurirPostoji = "select K.idkor, idvoz, A.idgra from Vozi V join Korisnik K on (V.idkor=K.idkor)"
                + " join Adresa A on(A.idadr=K.idadr) where korisnickoime=?";

        try(PreparedStatement stm = conn.prepareStatement(proveraKurirPostoji)){

            stm.setString(1, username);
            ResultSet rs = stm.executeQuery();
            if (!rs.next()) {
                System.out.println("Greska, ciljni kurir ili nije slobodan ili ne postoji!");
                return -3;
            }
            IdK = rs.getInt(1);
            IdVoz = rs.getInt(2);
            kurirovGrad = rs.getInt(3);

        } catch (SQLException ex) {
            Logger.getLogger(sm180228_DriveOperation.class.getName()).log(Level.SEVERE, null, ex);
        }

        String proveraMagacinPostoji = "select M.idadr from LokacijaMagazina M join Adresa A on(M.idadr=A.idadr) where IdGra=?";

        int adresa_magacina = -1;

        try(PreparedStatement ps = conn.prepareStatement(proveraMagacinPostoji)){
            
            ps.setInt(1, kurirovGrad);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                System.out.println("Fatalna greska, u gradu nema magacina!");
                return -5;
            }
            adresa_magacina = rs.getInt(1);

        } catch (SQLException ex) {
            Logger.getLogger(sm180228_DriveOperation.class.getName()).log(Level.SEVERE, null, ex);
        }

        int trenutnaLokacija = -1;
        int trenutni_grad = -1;
        int X = -1;
        int Y = -1;
        String provera1 = "select V.TrenutnaLokacija, x_koord, y_koord, IdGra from Vozi V"
                + " join Adresa A on (V.trenutnalokacija=A.idadr) where idvoz=? and idkor=?";

        try(PreparedStatement ps = conn.prepareStatement(provera1)){

            ps.setInt(1, IdVoz);
            ps.setInt(2, IdK);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                System.out.println("Fatalna greska!");
                return -5;
            }
            trenutnaLokacija = rs.getInt(1);
            X = rs.getInt(2);
            Y = rs.getInt(3);
            trenutni_grad = rs.getInt(4);

        } catch (SQLException ex) {
            Logger.getLogger(sm180228_DriveOperation.class.getName()).log(Level.SEVERE, null, ex);
        }

        int IdPak = -1;
        int status = 0;
        String proveraPaketiZaDostavu = "select IdPak, Flag from uPrevozu where Flag!=2 and IdKor=? and IdVoz=?";

        try(PreparedStatement stm = conn.prepareStatement(proveraPaketiZaDostavu)){

            stm.setInt(1, IdK);
            stm.setInt(2, IdVoz);
            ResultSet rs = stm.executeQuery();
            
            if (rs.next()) {
                IdPak = rs.getInt(1);
                status = rs.getInt(2);

                if (status == 0 || status == 3) {//kupi paket iz grada ili leti u magacin

                    String query = "Select x_koord, y_koord, CenaIsporuke, A.IdAdr from ZahtevPaket Z join Adresa A"
                            + " on (Z.pocetnaAdresa=A.IdAdr) join ponuda P on (Z.idpak=P.idpak) where z.idpak=?";

                    try(PreparedStatement ps = conn.prepareStatement(query)){

                        ps.setInt(1, IdPak);
                        ResultSet rs2 = ps.executeQuery();
                        int Xdest = -1;
                        int Ydest = -1;
                        int adr_dest = -1;
                        double Cena = -1;

                        if (rs2.next()) {
                            Xdest = rs2.getInt(1);
                            Ydest = rs2.getInt(2);
                            Cena = rs2.getBigDecimal(3).doubleValue();
                            adr_dest = rs2.getInt(4);

                            double put = Math.sqrt(Math.pow(Xdest - X, 2) + Math.pow(Ydest - Y, 2));

                            if (status != 3) {}
                            if (status == 3) {
                                query = "update uPrevozu set Flag=2 where IdPak=?";
                                try(PreparedStatement stm3 = conn.prepareStatement(query)){
                                    stm3.setInt(1, IdPak);
                                    stm3.executeUpdate();

                                }
                            }

                            query = "update Vozi set PredjenPut= PredjenPut + ?, TrenutnaLokacija=? where IdKor=? and IdVoz=?";
                            try(PreparedStatement stm3 = conn.prepareStatement(query)){
                                stm3.setBigDecimal(1, BigDecimal.valueOf(put));
                                stm3.setInt(2, adr_dest);
                                stm3.setInt(3, IdK);
                                stm3.setInt(4, IdVoz);
                                stm3.executeUpdate();
                            }
                        }
                    }

                    query = "delete from uPrevozu where IdPak=? and Flag=0";

                    try(PreparedStatement stm5 = conn.prepareStatement(query)){
                        stm5.setInt(1, IdPak);
                        stm5.executeUpdate();
                    }

                    query = "update Ponuda set StatusIsporuke=2  where idpak=?";

                    try(PreparedStatement ps = conn.prepareStatement(query)){

                        ps.setInt(1, IdPak);
                        ps.executeUpdate();
                    }

                    return -2;

                } else if (status == 4) { //odes u magacin i kupis ih sve

                    int TrenutnaAdresaPaketa = -1;
                    String proba = "Select Lokacija from Ponuda where idpak=?";

                    try(PreparedStatement ps = conn.prepareStatement(proba)){
                        ps.setInt(1, IdPak);
                        
                        ResultSet rs1 = ps.executeQuery();
                        if (rs1.next()) {
                            TrenutnaAdresaPaketa = rs1.getInt(1);
                        }
                    }
                    
                    int noviMagacin = -1;
                    proba = "Select Idlok from LokacijaMagazina where idadr=?";

                    try(PreparedStatement ps = conn.prepareStatement(proba)){
                        
                        ps.setInt(1, TrenutnaAdresaPaketa);
                        ResultSet rs1 = ps.executeQuery();
                        if (rs1.next()) {
                            noviMagacin = rs1.getInt(1);
                        }

                    }
                    List<Integer> paketi = new ArrayList<>();
                    proba = "Select IdPak from Ponuda where Lokacija=? and IdPak not in(select IdPak from uPrevozu where flag=2)";

                    try(PreparedStatement ps = conn.prepareStatement(proba)){
                        ps.setInt(1, TrenutnaAdresaPaketa);
                        ResultSet rs1 = ps.executeQuery();

                        while (rs1.next()) {
                            paketi.add(rs1.getInt(1));
                        }

                    }
                    for (int k = 0; k < paketi.size(); k++) {                       

                        String proba2 = "Update uPrevozu set Flag=2 where Idpak=? and IdVoz=? and flag=4";
                        try(PreparedStatement ps = conn.prepareStatement(proba2)){
                            ps.setInt(1, paketi.get(k));
                            ps.setInt(2, IdVoz);
                            ps.executeUpdate();

                        }

                    }

                    proba = "select x_koord, y_koord from Adresa where idadr=?";
                    try(PreparedStatement ps = conn.prepareStatement(proba)) {

                        ps.setInt(1, TrenutnaAdresaPaketa);
                        ResultSet rs1 = ps.executeQuery();

                        if (rs1.next()) {
                            int Xmag = rs1.getInt(1);
                            int Ymag = rs1.getInt(2);

                            double predjenPut = Math.sqrt(Math.pow(Xmag - X, 2) + Math.pow(Ymag - Y, 2));

                            proba = "update Vozi set PredjenPut= PredjenPut + ?, TrenutnaLokacija=? where IdKor=? and IdVoz=?";
                            try(PreparedStatement ps1 = conn.prepareStatement(proba)){
                                ps1.setBigDecimal(1, BigDecimal.valueOf(predjenPut));
                                ps1.setInt(2, TrenutnaAdresaPaketa);
                                ps1.setInt(3, IdK);
                                ps1.setInt(4, IdVoz);
                                ps1.executeUpdate();
                            }
                        }

                    }

                    return -2;

                } else { //dostavlja paket

                    //Dostava Paketa
                    int OdredisnaAdr = -1;
                    int Xodr = -1;
                    int Yodr = -1;

                    String proba = "Select ZavrsnaAdresa, x_koord, y_koord from ZahtevPaket Z join Adresa A on (Z.ZavrsnaAdresa=A.IdAdr) where idpak=?";

                    try(PreparedStatement ps = conn.prepareStatement(proba)){
                        ps.setInt(1, IdPak);
                        ResultSet rs1 = ps.executeQuery();
                        if (rs1.next()) {
                            OdredisnaAdr = rs1.getInt(1);
                        }
                        Xodr = rs1.getInt(2);
                        Yodr = rs1.getInt(3);

                    }
                    double predjenPut = Math.sqrt(Math.pow(Xodr - X, 2) + Math.pow(Yodr - Y, 2));

                    String query = "update Vozi set PredjenPut= PredjenPut + ?, TrenutnaLokacija=? where IdKor=? and IdVoz=?";
                    try(PreparedStatement ps = conn.prepareStatement(query)){
                        ps.setBigDecimal(1, BigDecimal.valueOf(predjenPut));
                        ps.setInt(2, OdredisnaAdr);
                        ps.setInt(3, IdK);
                        ps.setInt(4, IdVoz);
                        ps.executeUpdate();

                    }

                    query = "delete from uPrevozu where IdPak=? and Flag=1";

                    try(PreparedStatement ps = conn.prepareStatement(query)){
                        
                        ps.setInt(1, IdPak);
                        ps.executeUpdate();
                    }

                    query = "update Ponuda set StatusIsporuke=3, Lokacija="
                            + "(select zavrsnaAdresa from zahtevPaket where idpak=?)  where idpak=?";

                    try(PreparedStatement ps = conn.prepareStatement(query)){
                        ps.setInt(1, IdPak);
                        ps.setInt(2, IdPak);
                        ps.executeUpdate();
                    }

                    query = "update kurir set BrIsporucenihPaketa=BrIsporucenihPaketa+1 where IdKor=?";

                    try(PreparedStatement ps = conn.prepareStatement(query)){
                        
                        ps.setInt(1, IdK);
                        ps.executeUpdate();
                    }
                    
                    BigDecimal Cena=BigDecimal.ZERO;
                    query = "select CenaIsporuke from ponuda where Idpak=?";
                    
                    try ( PreparedStatement ps = conn.prepareStatement(query)) {
                        
                        ps.setInt(1, IdPak);
                        ResultSet rs1=ps.executeQuery();
                        if(rs1.next()){
                            Cena = rs1.getBigDecimal(1);
                        }
                        
                    }
                    
                    query = "update Kurir set ostvarenProfit=ostvarenProfit+? where IdKor=?";
                    
                    try ( PreparedStatement ps = conn.prepareStatement(query)) {
                        
                        ps.setBigDecimal(1, Cena);
                        ps.setInt(2, IdK);
                        ps.executeUpdate();
                    }

                    return IdPak;

                }

            } else {//nema vise paketa, povratak u magacin

                String proba = "select x_koord, y_koord from Adresa where idadr=?";
                double predjeniPut = 0;
                try(PreparedStatement ps = conn.prepareStatement(proba)){

                    ps.setInt(1, adresa_magacina);
                    ResultSet rs1 = ps.executeQuery();

                    if (rs1.next()) {
                        int Xmag = rs1.getInt(1);
                        int Ymag = rs1.getInt(2);

                        double put = Math.sqrt(Math.pow(Xmag - X, 2) + Math.pow(Ymag - Y, 2));
                        proba = "select PredjenPut from Vozi where idkor=? and Idvoz=?";
                        
                        try(PreparedStatement ps1 = conn.prepareStatement(proba)){
                            ps1.setInt(1, IdK);
                            ps1.setInt(2, IdVoz);

                            ResultSet rs33 = ps1.executeQuery();
                            if (rs33.next()) {
                                predjeniPut = rs33.getBigDecimal(1).doubleValue() + put;
                            }
                        }
                    }
                }

                double potrosnja = -1;
                int tipGoriva = 0;
                double cenaGoriva = 0;
                proba = "select Potrosnja, TipGoriva from Vozilo where IdVoz=?";

                try(PreparedStatement ps = conn.prepareStatement(proba)){

                    ps.setInt(1, IdVoz);
                    ResultSet rs1 = ps.executeQuery();
                    if (rs1.next()) {
                        potrosnja = rs1.getBigDecimal(1).doubleValue();
                        tipGoriva = rs1.getInt(2);
                    }

                }
                
                switch (tipGoriva) {
                    case 0:
                        //plin
                        cenaGoriva = 15;
                        break;
                    case 1:
                        //dizel
                        cenaGoriva = 32;
                        break;
                    //benzin
                    default:
                        cenaGoriva = 36;
                        break;
                }

                double cenaNaGorivo = predjeniPut * potrosnja * cenaGoriva;
                proba = "update kurir set ostvarenProfit=ostvarenProfit-?, status=0 where idkor=?";
                
                try ( PreparedStatement stmend = conn.prepareStatement(proba)) {

                    stmend.setBigDecimal(1, BigDecimal.valueOf(cenaNaGorivo));
                    stmend.setInt(2, IdK);
                    stmend.executeUpdate();

                }
                // stavi pakete u mag
                List<Integer> paketiMagacin = new ArrayList<>();
                proba = "select IdPak from uPrevozu where (flag=2 or flag=3) and IdVoz=? and idkor=?";
                
                try(PreparedStatement ps = conn.prepareStatement(proba)){

                    ps.setInt(1, IdVoz);
                    ps.setInt(2, IdK);
                    ResultSet rs1 = ps.executeQuery();

                    while (rs1.next()) {
                        paketiMagacin.add(rs1.getInt(1));
                    }

                }

                proba = "delete from uPrevozu where idvoz=? and idkor=? and (flag=2 or flag=3)";
                
                try(PreparedStatement ps = conn.prepareStatement(proba)){
                    ps.setInt(1, IdVoz);
                    ps.setInt(2, IdK);
                    ps.executeUpdate();
                }

                for (int i = 0; i < paketiMagacin.size(); i++) {
                    proba = "update Ponuda set lokacija=? where idpak=?";
                    
                    try(PreparedStatement ps = conn.prepareStatement(proba)){
                        ps.setInt(1, adresa_magacina);
                        ps.setInt(2, paketiMagacin.get(i));
                        ps.executeUpdate();

                    }

                }

                //brisanje Vozila i Kurira iz Vozi
                proba = "delete from Vozi where idvoz=? and idkor=?";
                
                try(PreparedStatement ps = conn.prepareStatement(proba)){
                    ps.setInt(1, IdVoz);
                    ps.setInt(2, IdK);
                    ps.executeUpdate();
                }
                
                //parkiranje Vozila u Magacin
                int IdM = -1;
                proba = "select idlok from lokacijaMagazina where idadr=?";
                
                try(PreparedStatement ps = conn.prepareStatement(proba)){

                    ps.setInt(1, adresa_magacina);
                    ResultSet rs1 = ps.executeQuery();
                    if (rs1.next()) {
                        IdM = rs1.getInt(1);
                    }
                    
                }

                proba = "Insert into Parkirano (idvoz, idlok) values(?, ?)";
                
                try(PreparedStatement ps = conn.prepareStatement(proba)){

                    ps.setInt(1, IdVoz);
                    ps.setInt(2, IdM);
                    ps.executeUpdate();
                }

                return -1;
            }

        } catch (SQLException ex) {
            Logger.getLogger(sm180228_DriveOperation.class.getName()).log(Level.SEVERE, null, ex);
        }

        return -1;

    }

    @Override
    public List<Integer> getPackagesInVehicle(String courierUsername) {
        Connection conn = DB.getInstance().getConnection();
        // prvo proveri da li postoji kurir tog korisnickog imena
        // potom proveri da li vozi nesto trenutno
        // na kraju dohvati pakete koji su u prevozu
        List<Integer> list = new ArrayList<>();
        int IdKor = -1;
        String proveraKurirPostoji = "select k.idkor from kurir k join korisnik ko"
                + " on (k.idkor=ko.idkor) where ko.korisnickoime=?";
        
        try(PreparedStatement ps = conn.prepareStatement(proveraKurirPostoji)){
            
            ps.setString(1, courierUsername);
            ResultSet rs = ps.executeQuery();
            if(!rs.next()){
                System.out.println("Greska, ne postoji takav kurir!");
                return null;
            }
            IdKor = rs.getInt(1);
            
        } catch (SQLException ex) {
            Logger.getLogger(sm180228_DriveOperation.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        int IdVoz = -1;
        String proveraVoziTrenutno = "select idvoz from vozi where idkor=?";
        
        try(PreparedStatement ps = conn.prepareStatement(proveraVoziTrenutno)){
            
            ps.setInt(1, IdKor);
            ResultSet rs = ps.executeQuery();
            if(!rs.next()){
                System.out.println("Greska, ovaj kurir trenutno ne vozi nijedan auto!");
                return list;
            }
            IdVoz = rs.getInt(1);
            
        } catch (SQLException ex) {
            Logger.getLogger(sm180228_DriveOperation.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String proveraDohvatiPakete = "select distinct u.idpak from uprevozu u join zahtevpaket z on (u.idpak=z.idpak)"
                + " join ponuda p on (p.idpak=z.idpak) where idvoz=? and statusIsporuke=2";
        
        try(PreparedStatement ps = conn.prepareStatement(proveraDohvatiPakete)){
            
            ps.setInt(1, IdVoz);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                list.add(rs.getInt(1));
            }
            return list;
            
        } catch (SQLException ex) {
            Logger.getLogger(sm180228_DriveOperation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
}
