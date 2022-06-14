/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.student.interfaces;

/**
 *
 * @author stoja
 */
public interface AddressOperations {
    
    int deleteAddresses(java.lang.String name, int number);
    boolean deleteAdress(int idAddress); // JEL OVO TYPO ILI NAMERNO?!
    int deleteAllAddressesFromCity(int idCity);
    java.util.List<java.lang.Integer> getAllAddresses();
    java.util.List<java.lang.Integer> getAllAddressesFromCity(int idCity);
    int insertAddress(java.lang.String street, int number, int cityId, int xCord, int yCord);
    
    
}
