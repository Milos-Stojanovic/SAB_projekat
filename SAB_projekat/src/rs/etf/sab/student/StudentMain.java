package rs.etf.sab.student;

import rs.etf.sab.operations.*;
import rs.etf.sab.tests.TestHandler;
import rs.etf.sab.tests.TestRunner;


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
