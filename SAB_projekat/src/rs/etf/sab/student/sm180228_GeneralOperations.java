/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.student;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.GeneralOperations;

/**
 *
 * @author stoja
 */
public class sm180228_GeneralOperations implements GeneralOperations {

    @Override
    public void eraseAll() {
        Connection conn = DB.getInstance().getConnection();
        
        String query = "delete from Parkirano where 1=1";

        try ( PreparedStatement stm = conn.prepareStatement(query)) {

            stm.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(sm180228_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        query = "delete from Ponuda where 1=1";

        try ( PreparedStatement stm = conn.prepareStatement(query)) {

            stm.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(sm180228_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        query = "delete from Isporuka where 1=1";

        try ( PreparedStatement stm = conn.prepareStatement(query)) {

            stm.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(sm180228_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        query = "delete from ZahtevPaket where 1=1";

        try ( PreparedStatement stm = conn.prepareStatement(query)) {

            stm.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(sm180228_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        query = "delete from Paket where 1=1";

        try ( PreparedStatement stm = conn.prepareStatement(query)) {

            stm.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(sm180228_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
          query = "delete from Kupac where 1=1";

        try ( PreparedStatement stm = conn.prepareStatement(query)) {

            stm.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(sm180228_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        

        query = "delete from Vozio where 1=1";

        try ( PreparedStatement stm = conn.prepareStatement(query)) {

            stm.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(sm180228_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        query = "delete from Vozi where 1=1";

        try ( PreparedStatement stm = conn.prepareStatement(query)) {

            stm.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(sm180228_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        query = "delete from Administrator where 1=1";

        try ( PreparedStatement stm = conn.prepareStatement(query)) {

            stm.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(sm180228_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        query = "delete from ZahtevZaKurira where 1=1";

        try ( PreparedStatement stm = conn.prepareStatement(query)) {

            stm.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(sm180228_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        query = "delete from Vozilo where 1=1";

        try ( PreparedStatement stm = conn.prepareStatement(query)) {

            stm.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(sm180228_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        query = "delete from LokacijaMagazina where 1=1";

        try ( PreparedStatement stm = conn.prepareStatement(query)) {

            stm.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(sm180228_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        query = "delete from Kurir where 1=1";

        try ( PreparedStatement stm = conn.prepareStatement(query)) {

            stm.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(sm180228_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        query = "delete from Korisnik where 1=1";

        try ( PreparedStatement stm = conn.prepareStatement(query)) {

            stm.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(sm180228_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        query = "delete from Adresa where 1=1";

        try ( PreparedStatement stm = conn.prepareStatement(query)) {

            stm.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(sm180228_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        query = "delete from Grad where 1=1";

        try ( PreparedStatement stm = conn.prepareStatement(query)) {

            stm.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(sm180228_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //System.out.println("Baza je uspesno obrisana!");
    }
    
}
