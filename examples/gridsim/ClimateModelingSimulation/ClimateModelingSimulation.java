import java.util.*;
import gridsim.*;

public class ClimateModelingSimulation extends GridSim {
    private Integer userId;
    private String userName;
    private GridletList gridletList;
    private GridletList receivedList;
    private int totalResources;

    ClimateModelingSimulation(String name, double baudRate, int totalResources) throws Exception {
        super(name, baudRate);
        this.userName = name;
        this.totalResources = totalResources;
        this.receivedList = new GridletList();
        this.userId = new Integer(getEntityId(name));
        System.out.println("Creating a grid user entity with name = " + name + ", and id = " + this.userId);
        this.gridletList = createGridlets(this.userId.intValue());
        System.out.println("Creating " + this.gridletList.size() + " Gridlets");
    }

    public void body() {
        int[] resourceID = new int[this.totalResources];
        double[] resourceCost = new double[this.totalResources];
        String[] resourceName = new String[this.totalResources];
        LinkedList resList;
        ResourceCharacteristics resChar;

        while (true) {
            super.gridSimHold(1.0);
            resList = super.getGridResourceList();
            if (resList.size() == this.totalResources) break;
            else System.out.println("Waiting to get list of resources ...");
        }

        for (int i = 0; i < this.totalResources; i++) {
            resourceID[i] = ((Integer) resList.get(i)).intValue();
            super.send(resourceID[i], GridSimTags.SCHEDULE_NOW, GridSimTags.RESOURCE_CHARACTERISTICS, this.userId);
            resChar = (ResourceCharacteristics) super.receiveEventObject();
            resourceName[i] = resChar.getResourceName();
            resourceCost[i] = resChar.getCostPerSec();
            System.out.println("Received ResourceCharacteristics from " + resourceName[i] + ", with id = " + resourceID[i]);
            super.recordStatistics("\"Received ResourceCharacteristics from " + resourceName[i] + "\"", "");
        }

        Gridlet gridlet;
        String info;
        Random random = new Random();
        int id;

        for (int i = 0; i < this.gridletList.size(); i++) {
            gridlet = (Gridlet) this.gridletList.get(i);
            info = "Gridlet_" + gridlet.getGridletID();
            id = random.nextInt(this.totalResources);
            System.out.println("Sending " + info + " to " + resourceName[id] + " with id = " + resourceID[id]);
            super.gridletSubmit(gridlet, resourceID[id]);
            gridlet = super.gridletReceive();
            System.out.println("Receiving Gridlet " + gridlet.getGridletID());
            super.recordStatistics("\"Received " + info + " from " + resourceName[id] + "\"", gridlet.getProcessingCost());
            this.receivedList.add(gridlet);
        }

        super.shutdownGridStatisticsEntity();
        super.shutdownUserEntity();
        super.terminateIOEntities();
    }

    public GridletList getGridletList() {
        return this.receivedList;
    }

    private GridletList createGridlets(int userId) {
        GridletList list = new GridletList();
        int id = 0;
        double length = 3500.0;
        long fileSize = 300;
        long outputSize = 300;

        Gridlet gridlet1 = new Gridlet(id++, length, fileSize, outputSize);
        Gridlet gridlet2 = new Gridlet(id++, 5000, 500, 500);
        Gridlet gridlet3 = new Gridlet(id++, 9000, 900, 900);

        gridlet1.setUserID(userId);
        gridlet2.setUserID(userId);
        gridlet3.setUserID(userId);

        list.add(gridlet1);
        list.add(gridlet2);
        list.add(gridlet3);

        long seed = 11L * 13 * 17 * 19 * 23 + 1;
        Random random = new Random(seed);
        GridSimStandardPE.setRating(100);
        int count = 5;

        for (int i = 1; i <= count; i++) {
            length = GridSimStandardPE.toMIs(random.nextDouble() * 50);
            fileSize = (long) GridSimRandom.real(100, 0.10, 0.40, random.nextDouble());
            outputSize = (long) GridSimRandom.real(250, 0.10, 0.50, random.nextDouble());
            Gridlet gridlet = new Gridlet(id++, length, fileSize, outputSize);
            gridlet.setUserID(userId);
            list.add(gridlet);
        }

        return list;
    }

    public static void main(String[] args) {
        System.out.println("Starting Climate Modeling Simulation");
        try {
            int numUser = 1;
            Calendar calendar = Calendar.getInstance();
            boolean traceFlag = false;
            String[] excludeFromFile = { "" };
            String[] excludeFromProcessing = { "" };
            String reportName = null;
            System.out.println("Initializing GridSim package");
            GridSim.init(numUser, calendar, traceFlag, excludeFromFile, excludeFromProcessing, reportName);

            GridResource resource0 = createGridResource("Resource_0");
            GridResource resource1 = createGridResource("Resource_1");
            GridResource resource2 = createGridResource("Resource_2");

            int totalResources = 3;
            ClimateModelingSimulation simulation = new ClimateModelingSimulation("ClimateModelingUser", 560.00, totalResources);
            GridSim.startGridSimulation();

            GridletList newList = simulation.getGridletList();
            printGridletList(newList);

            System.out.println("Finish Climate Modeling Simulation");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unwanted errors happened");
        }
    }

    private static GridResource createGridResource(String name) {
        System.out.println();
        System.out.println("Starting to create one Grid resource with 3 Machines");
        MachineList machineList = new MachineList();
        System.out.println("Creates a Machine list");

        int mipsRating = 377;
        machineList.add(new Machine(0, 4, mipsRating));
        System.out.println("Creates the 1st Machine that has 4 PEs and stores it into the Machine list");
        machineList.add(new Machine(1, 4, mipsRating));
        System.out.println("Creates the 2nd Machine that has 4 PEs and stores it into the Machine list");
        machineList.add(new Machine(2, 2, mipsRating));
        System.out.println("Creates the 3rd Machine that has 2 PEs and stores it into the Machine list");

        String architecture = "Sun Ultra";
        String os = "Solaris";
        double timeZone = 9.0;
        double cost = 3.0;

        ResourceCharacteristics resConfig = new ResourceCharacteristics(
                architecture, os, machineList, ResourceCharacteristics.TIME_SHARED, timeZone, cost);
        System.out.println("Creates the properties of a Grid resource and stores the Machine list");

        double baudRate = 100.0;
        long seed = 11L * 13 * 17 * 19 * 23 + 1;
        double peakLoad = 0.0;
        double offPeakLoad = 0.0;
        double holidayLoad = 0.0;

        LinkedList<Integer> weekends = new LinkedList<>();
        weekends.add(Calendar.SATURDAY);
        weekends.add(Calendar.SUNDAY);
        LinkedList<Integer> holidays = new LinkedList<>();

        GridResource gridResource = null;
        try {
            gridResource = new GridResource(name, baudRate, seed, resConfig, peakLoad, offPeakLoad, holidayLoad, weekends, holidays);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Finally, creates one Grid resource and stores the properties of a Grid resource");
        System.out.println();
        return gridResource;
    }

    private static void printGridletList(GridletList list) {
        int size = list.size();
        Gridlet gridlet;
        String indent = "    ";

        System.out.println();
        System.out.println("========== OUTPUT ==========");
        System.out.println("Gridlet ID" + indent + "STATUS" + indent + "Resource ID" + indent + "Cost");

        for (int i = 0; i < size; i++) {
            gridlet = (Gridlet) list.get(i);
            System.out.print(indent + gridlet.getGridletID() + indent + indent);
            if (gridlet.getGridletStatus() == Gridlet.SUCCESS) System.out.print("SUCCESS");
            System.out.println(indent + indent + gridlet.getResourceID() + indent + gridlet.getProcessingCost());
        }
    }
}
