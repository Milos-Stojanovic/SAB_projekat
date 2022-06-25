package rs.etf.sab.student;

import rs.etf.sab.operations.*;
import rs.etf.sab.tests.TestHandler;
import rs.etf.sab.tests.TestRunner;

import rs.etf.sab.operations.*;


public class StudentMain {

    public static void main(String[] args) {
        AddressOperations addressOperations = new sm180228_AddressOperations(); // Change this to your implementation.
        CityOperations cityOperations = new sm180228_CityOperations(); // Do it for all classes.
        CourierOperations courierOperations = new sm180228_CourierOperations(); // e.g. = new MyDistrictOperations();
        CourierRequestOperation courierRequestOperation = new sm180228_CourierRequestOperation();
        DriveOperation driveOperation = new sm180228_DriveOperation();
        GeneralOperations generalOperations = new sm180228_GeneralOperations();
        PackageOperations packageOperations = new sm180228_PackageOperations();
        StockroomOperations stockroomOperations = new sm180228_StockroomOperations();
        UserOperations userOperations = new sm180228_UserOperations();
        VehicleOperations vehicleOperations = new sm180228_VehicleOperations();

//        cityOperations.insertCity("Beograd", "11000");
//        addressOperations.insertAddress("Cingrijina", 2, 1, 5, 4);
//        courierOperations.insertCourier("ASD", "021365698");

//        final String street = "Bulevar kralja Aleksandra";
//        final int number = 73;
//        final int idCity = cityOperations.insertCity("Belgrade", "11000");
//        final int idAddress = addressOperations.insertAddress(street, number, idCity, 10, 10);
//        final String username = "crno.dete";
//        final String firstName = "Svetislav";
//        final String lastName = "Kisprdilov";
//        final String password = "Test_123";
//        userOperations.insertUser(username, firstName, lastName, password, idAddress);
//        userOperations.getAllUsers().contains(username);
//
//        courierRequestOperation.insertCourierRequest("blabla1", "1234567");
//        courierRequestOperation.insertCourierRequest("blabla1", "1234567");
//        System.out.println(courierRequestOperation.getAllCourierRequests().size());
//        System.out.println(courierRequestOperation.getAllCourierRequests().contains("blabla"));
            //generalOperations.eraseAll();
            //packageOperations.getAcceptanceTime(1);
        
        TestHandler.createInstance(
                addressOperations,
                cityOperations,
                courierOperations,
                courierRequestOperation,
                driveOperation,
                generalOperations,
                packageOperations,
                stockroomOperations,
                userOperations,
                vehicleOperations);

        TestRunner.runTests();
    }
}
