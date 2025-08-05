package travelbooking;

import java.util.*;
import java.io.*;

// Custom StaticQueue class for fixed-size queue
class StaticQueue<T> {
    private T[] queue;
    private int front;
    private int rear;
    private int size;
    private final int capacity;

    @SuppressWarnings("unchecked")
    public StaticQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Queue capacity must be positive");
        }
        this.capacity = capacity;
        this.queue = (T[]) new Object[capacity];
        this.front = 0;
        this.rear = -1;
        this.size = 0;
    }

    public void enqueue(T item) throws IllegalStateException {
        if (isFull()) {
            throw new IllegalStateException("Waiting list is full. Cannot add more passengers.");
        }
        rear = (rear + 1) % capacity;
        queue[rear] = item;
        size++;
    }

    public T dequeue() throws IllegalStateException {
        if (isEmpty()) {
            throw new IllegalStateException("Waiting list is empty.");
        }
        T item = queue[front];
        queue[front] = null;
        front = (front + 1) % capacity;
        size--;
        return item;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean isFull() {
        return size == capacity;
    }

    public int size() {
        return size;
    }

    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int current = front;
            private int count = 0;

            @Override
            public boolean hasNext() {
                return count < size;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                T item = queue[current];
                current = (current + 1) % capacity;
                count++;
                return item;
            }
        };
    }
}

class Passenger {
    private String passengerId;
    private String name;
    private String phone;
    private String email;
    private String city;
    private int age;

    public Passenger(String passengerId, String name, String phone, String email, String city, int age) throws IllegalArgumentException {
        if (passengerId == null || name == null || phone == null || email == null || city == null) {
            throw new IllegalArgumentException("Input parameters cannot be null");
        }
        this.passengerId = passengerId;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.city = city;
        this.age = age;
    }

    public String getPassengerId() { return passengerId; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getCity() { return city; }
    public int getAge() { return age; }

    public String toString() {
        return passengerId + ";" + name + ";" + phone + ";" + email + ";" + city + ";" + age;
    }
}

class Booking {
    private Passenger passenger;
    private int seatNumber;

    public Booking(Passenger passenger, int seatNumber) throws IllegalArgumentException {
        if (passenger == null) {
            throw new IllegalArgumentException("Passenger cannot be null");
        }
        if (seatNumber <= 0) {
            throw new IllegalArgumentException("Invalid seat number");
        }
        this.passenger = passenger;
        this.seatNumber = seatNumber;
    }

    public Passenger getPassenger() { return passenger; }
    public int getSeatNumber() { return seatNumber; }

    public String toString() {
        return "Seat " + seatNumber + " booked by " + passenger.getName() + " (ID: " + passenger.getPassengerId() + ")";
    }
}

class Bus {
    private String busNumber;
    private String startingPoint;
    private String endingPoint;
    private String startingTime;
    private int totalSeats;
    private double fare;
    private boolean[] seats;
    private Booking[] bookedSeats;
    private StaticQueue<Passenger> waitingList = new StaticQueue<>(100);

    public Bus(String busNumber, int totalSeats, String startingPoint, String endingPoint, String startingTime, double fare) throws IllegalArgumentException {
        if (busNumber == null || startingPoint == null || endingPoint == null || startingTime == null) {
            throw new IllegalArgumentException("Input parameters cannot be null");
        }
        if (totalSeats <= 0 || fare <= 0) {
            throw new IllegalArgumentException("Total seats and fare must be positive");
        }
        this.busNumber = busNumber;
        this.totalSeats = totalSeats;
        this.startingPoint = startingPoint;
        this.endingPoint = endingPoint;
        this.startingTime = startingTime;
        this.fare = fare;
        this.seats = new boolean[totalSeats + 1];
        this.bookedSeats = new Booking[totalSeats + 1];
        for (int i = 1; i <= totalSeats; i++) {
            seats[i] = true;
        }
    }

    public String getBusNumber() { return busNumber; }
    public String getStartingPoint() { return startingPoint; }
    public String getEndingPoint() { return endingPoint; }
    public String getStartingTime() { return startingTime; }
    public double getFare() { return fare; }
    public int getTotalSeats() { return totalSeats; }
    public Booking[] getBookedSeats() { return bookedSeats; }
    public StaticQueue<Passenger> getWaitingList() { return waitingList; }

    public void showBusDetails() {
        System.out.println("Bus Number: " + busNumber + " | Route: " + startingPoint + " to " + endingPoint +
                " | Time: " + startingTime + " | Total Seats: " + totalSeats + " | Fare: RS." + fare);
        int available = 0;
        int booked = 0;
        for (int i = 1; i <= totalSeats; i++) {
            if (seats[i]) available++;
            if (bookedSeats[i] != null) booked++;
        }
        System.out.println("Seats Available: " + available + " | Booked: " + booked);
    }

    public String toFileString() {
        return busNumber + ";" + totalSeats + ";" + startingPoint + ";" + endingPoint + ";" + startingTime + ";" + fare;
    }

    public boolean isSeatAvailable(int seatNumber) {
        if (seatNumber < 1 || seatNumber > totalSeats) return false;
        return seats[seatNumber];
    }

    public void bookSeat(Passenger passenger, int seatNumber) throws IllegalArgumentException {
        if (passenger == null) {
            throw new IllegalArgumentException("Passenger cannot be null");
        }
        if (isSeatAvailable(seatNumber)) {
            seats[seatNumber] = false;
            bookedSeats[seatNumber] = new Booking(passenger, seatNumber);
        } else {
            System.out.println("Seat " + seatNumber + " is already booked or invalid.");
            try {
                waitingList.enqueue(passenger);
            } catch (IllegalStateException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    public void cancelSeat(int seatNumber, Passenger passenger) throws IllegalArgumentException {
        if (passenger == null) {
            throw new IllegalArgumentException("Passenger cannot be null");
        }
        if (seatNumber < 1 || seatNumber > totalSeats) {
            throw new IllegalArgumentException("Invalid seat number: " + seatNumber);
        }
        if (bookedSeats[seatNumber] != null && bookedSeats[seatNumber].getPassenger().getPassengerId().equalsIgnoreCase(passenger.getPassengerId())) {
            bookedSeats[seatNumber] = null;
            seats[seatNumber] = true;
        } else {
            throw new IllegalArgumentException("Reservation not found for seat " + seatNumber);
        }
    }
}

public class BusReservation {
    private static Scanner sc = new Scanner(System.in);
    private static LinkedList<Passenger> passengers = new LinkedList<>();
    private static LinkedList<Bus> buses = new LinkedList<>();
    private static int passengerIdCounter = 1;

    // Getter for passengers list to support encapsulation
    private static LinkedList<Passenger> getPassengers() {
        return new LinkedList<>(passengers); // Return a copy to prevent direct modification
    }

    public static void main(String[] args) {
        try {
            loadPassengersFromFile();
            loadBusesFromFile();
            loadBookingsFromFile();
            loadWaitingListFromFile();
        } catch (IOException e) {
            System.out.println("Error loading initial data: " + e.getMessage());
            return;
        } finally {
            System.out.println("Initial data loading completed.");
        }

        int choice = -1;
        do {
            try {
                System.out.println("\nTRAVEL BOOKING SYSTEM");
                System.out.println("1. Register Passenger");
                System.out.println("2. Register Bus");
                System.out.println("3. Search Buses");
                System.out.println("4. Book Seat");
                System.out.println("5. Cancel Booking");
                System.out.println("6. Request New Seat");
                System.out.println("7. View All Bookings");
                System.out.println("8. View All Passengers");
                System.out.println("9. View All Buses");
                System.out.println("10. View Available Seats");
                System.out.println("11. View Request New Seats");
                System.out.println("12. View Passengers (Newest to Oldest)");
                System.out.println("0. Exit");
                System.out.print("Choose the option: ");
                choice = Integer.parseInt(sc.nextLine().trim());

                switch (choice) {
                    case 1: registerPassenger(); break;
                    case 2: registerBus(); break;
                    case 3: searchBus(); break;
                    case 4: bookSeat(); break;
                    case 5: cancelBooking(); break;
                    case 6: requestNewSeat(); break;
                    case 7: viewAllBookings(); break;
                    case 8: viewAllPassengers(); break;
                    case 9: viewAllBuses(); break;
                    case 10: viewAvailableSeats(); break;
                    case 11: viewRequestNewSeats(); break;
                    case 12: viewPassengersNewestToOldest(); break;
                    case 0:
                        try {
                            savePassengersToFile();
                            saveBusesToFile();
                            saveBookingsToFile();
                            saveWaitingListToFile();
                        } catch (IOException e) {
                            System.out.println("Error saving data: " + e.getMessage());
                        } finally {
                            System.out.println("Data saving process completed. Exiting...");
                        }
                        break;
                    default: System.out.println("Invalid choice. Please select a number between 0 and 12.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
            }
        } while (choice != 0);

        try {
            if (sc != null) {
                sc.close();
            }
        } finally {
            System.out.println("Scanner resource closed.");
        }
    }

    private static String generatePassengerId() {
        return String.format("P%03d", passengerIdCounter++);
    }

    private static void registerPassenger() throws IOException {
        try {
            System.out.print("Enter Name: ");
            String name = sc.nextLine().trim();
            if (name.isEmpty()) throw new IllegalArgumentException("Name cannot be empty.");

            System.out.print("Phone (10 digits): ");
            String phone = sc.nextLine().trim();
            if (!phone.matches("\\d{10}")) throw new IllegalArgumentException("Invalid phone number. Must be 10 digits.");

            System.out.print("Email: ");
            String email = sc.nextLine().trim();
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) throw new IllegalArgumentException("Invalid email format.");

            System.out.print("City: ");
            String city = sc.nextLine().trim();
            if (city.isEmpty()) throw new IllegalArgumentException("City cannot be empty.");

            System.out.print("Age: ");
            int age = Integer.parseInt(sc.nextLine().trim());
            if (age <= 0 || age > 120) throw new IllegalArgumentException("Invalid age. Must be between 1 and 120.");

            String passengerId = generatePassengerId();
            passengers.add(new Passenger(passengerId, name, phone, email, city, age));
            System.out.println("Passenger registered successfully with ID: " + passengerId);
            savePassengersToFile();
        } catch (NumberFormatException e) {
            throw new IOException("Invalid age format. Please enter a number.", e);
        } catch (IllegalArgumentException e) {
            throw new IOException("Error: " + e.getMessage(), e);
        }
    }

    private static void registerBus() throws IOException {
    try {
        System.out.print("Enter Bus Number: ");
        String busNumber = sc.nextLine().trim();
        if (busNumber.isEmpty()) throw new IllegalArgumentException("Bus number cannot be empty.");
        if (findBus(busNumber) != null) throw new IllegalArgumentException("Bus number already exists.");

        System.out.print("Total Seats (1-100): ");
        int seats = Integer.parseInt(sc.nextLine().trim());
        if (seats <= 0 || seats > 100) throw new IllegalArgumentException("Invalid number of seats. Must be between 1 and 100.");

        System.out.print("Starting Point: ");
        String startingPoint = sc.nextLine().trim();
        if (startingPoint.isEmpty()) throw new IllegalArgumentException("Starting point cannot be empty.");

        System.out.print("Ending Point: ");
        String endingPoint = sc.nextLine().trim();
        if (endingPoint.isEmpty()) throw new IllegalArgumentException("Ending point cannot be empty.");
        if (endingPoint.equalsIgnoreCase(startingPoint)) throw new IllegalArgumentException("Starting and ending points cannot be the same.");

        System.out.print("Starting Time (HH:MM): ");
        String startingTime = sc.nextLine().trim();
        if (!startingTime.matches("^([01]\\d|2[0-3]):[0-5]\\d$")) throw new IllegalArgumentException("Invalid time format. Use HH:MM (24-hour).");

        System.out.print("Fare: ");
        double fare = Double.parseDouble(sc.nextLine().trim());
        if (fare <= 0) throw new IllegalArgumentException("Fare must be greater than 0.");

        buses.add(new Bus(busNumber, seats, startingPoint, endingPoint, startingTime, fare));
        System.out.println("Bus registered successfully.");
        saveBusesToFile();
    } catch (NumberFormatException e) {
        throw new IOException("Invalid number format for seats or fare.", e);
    } catch (IllegalArgumentException e) {
        throw new IOException("Error: " + e.getMessage(), e);
    }
}

    private static Bus findBus(String busNumber) {
        if (busNumber == null) return null;
        for (Bus b : buses) {
            if (b.getBusNumber().equalsIgnoreCase(busNumber)) return b;
        }
        return null;
    }

    private static Passenger findPassengerById(String passengerId) {
        if (passengerId == null) return null;
        for (Passenger p : passengers) {
            if (p.getPassengerId().equalsIgnoreCase(passengerId)) return p;
        }
        return null;
    }

    private static void searchBus() throws IOException {
        try {
            System.out.print("Enter Starting Point: ");
            String startingPoint = sc.nextLine().trim();
            if (startingPoint.isEmpty()) throw new IllegalArgumentException("Starting point cannot be empty.");

            System.out.print("Enter Ending Point: ");
            String endingPoint = sc.nextLine().trim();
            if (endingPoint.isEmpty()) throw new IllegalArgumentException("Ending point cannot be empty.");

            boolean found = false;
            System.out.println("\nBuses from " + startingPoint + " to " + endingPoint + ":");
            for (Bus bus : buses) {
                if (bus.getStartingPoint().equalsIgnoreCase(startingPoint) && 
                    bus.getEndingPoint().equalsIgnoreCase(endingPoint)) {
                    bus.showBusDetails();
                    found = true;
                }
            }
            if (!found) {
                System.out.println("No buses found for the route " + startingPoint + " to " + endingPoint + ".");
            }
        } catch (IllegalArgumentException e) {
            throw new IOException("Error: " + e.getMessage(), e);
        }
    }

    private static void bookSeat() throws IOException {
        try {
            System.out.print("Enter Passenger ID: ");
            String passengerId = sc.nextLine().trim();
            if (passengerId.isEmpty()) throw new IllegalArgumentException("Passenger ID cannot be empty.");
            Passenger passenger = findPassengerById(passengerId);
            if (passenger == null) throw new IllegalArgumentException("Passenger not found. Please register first. Click the option 1 for register");

            System.out.println("\nAvailable Buses:");
            for (Bus b : buses) {
                b.showBusDetails();
            }
            System.out.print("Enter Bus Number to book: ");
            String busNumber = sc.nextLine().trim();
            if (busNumber.isEmpty()) throw new IllegalArgumentException("Bus number cannot be empty.");
            Bus bus = findBus(busNumber);
            if (bus == null) throw new IllegalArgumentException("Bus not found.");

            System.out.print("Enter Seat Number (1-" + bus.getTotalSeats() + "): ");
int seatNumber = Integer.parseInt(sc.nextLine().trim());
if (seatNumber < 1 || seatNumber > bus.getTotalSeats()) throw new IllegalArgumentException("Invalid seat number.");

bus.bookSeat(passenger, seatNumber);
System.out.println("Seat " + seatNumber + " booked for " + passenger.getName() + " (ID: " + passenger.getPassengerId() + ") at RS." + bus.getFare());
saveBookingsToFile();
        } catch (NumberFormatException e) {
            throw new IOException("Invalid seat number format.", e);
        } catch (IllegalArgumentException e) {
            throw new IOException("Error: " + e.getMessage(), e);
        }
    }

    private static void cancelBooking() throws IOException {
        try {
            System.out.print("Enter Bus Number: ");
            String busNumber = sc.nextLine().trim();
            if (busNumber.isEmpty()) throw new IllegalArgumentException("Bus number cannot be empty.");
            Bus bus = findBus(busNumber);
            if (bus == null) throw new IllegalArgumentException("Bus not found.");

            System.out.print("Enter Passenger ID: ");
            String passengerId = sc.nextLine().trim();
            if (passengerId.isEmpty()) throw new IllegalArgumentException("Passenger ID cannot be empty.");
            Passenger passenger = findPassengerById(passengerId);
            if (passenger == null) throw new IllegalArgumentException("Passenger not found.");

            System.out.print("Enter Seat Number: ");
            int seatNumber = Integer.parseInt(sc.nextLine().trim());
            if (seatNumber < 1 || seatNumber > bus.getTotalSeats()) throw new IllegalArgumentException("Invalid seat number.");

            // Notify adjacent passengers before canceling
            Booking[] bookedSeats = bus.getBookedSeats();
            if (seatNumber > 1 && bookedSeats[seatNumber - 1] != null) {
                Passenger prevPassenger = bookedSeats[seatNumber - 1].getPassenger();
                System.out.println("Notification to " + prevPassenger.getName() + " (ID: " + prevPassenger.getPassengerId() + 
                    ", Seat " + (seatNumber - 1) + "): Your neighbor in seat " + seatNumber + " (" + passenger.getName() + ") has canceled their booking.");
            }
            if (seatNumber < bus.getTotalSeats() && bookedSeats[seatNumber + 1] != null) {
                Passenger nextPassenger = bookedSeats[seatNumber + 1].getPassenger();
                System.out.println("Notification to " + nextPassenger.getName() + " (ID: " + nextPassenger.getPassengerId() + 
                    ", Seat " + (seatNumber + 1) + "): Your neighbor in seat " + seatNumber + " (" + passenger.getName() + ") has canceled their booking.");
            }

            bus.cancelSeat(seatNumber, passenger);
            System.out.println("Reservation cancelled for " + passenger.getName() + " (ID: " + passenger.getPassengerId() + ")");

            saveBookingsToFile();
            if (!bus.getWaitingList().isEmpty()) {
                Passenger next = bus.getWaitingList().dequeue();
                bus.bookSeat(next, seatNumber);
                System.out.println("Seat " + seatNumber + " assigned to " + next.getName() + " (ID: " + next.getPassengerId() + ") from waiting list at RS." + bus.getFare());
                try {
                    saveBookingsToFile();
                    saveWaitingListToFile();
                } catch (IOException e) {
                    System.out.println("Error saving booking or waiting list after cancellation: " + e.getMessage());
                }
            }
        } catch (NumberFormatException e) {
            throw new IOException("Invalid seat number format.", e);
        } catch (IllegalArgumentException e) {
            throw new IOException("Error: " + e.getMessage(), e);
        } catch (IllegalStateException e) {
            throw new IOException("Error processing waiting list: " + e.getMessage(), e);
        }
    }

    private static void requestNewSeat() throws IOException {
        try {
            System.out.print("Enter Passenger ID: ");
            String passengerId = sc.nextLine().trim();
            if (passengerId.isEmpty()) throw new IllegalArgumentException("Passenger ID cannot be empty.");
            Passenger passenger = findPassengerById(passengerId);
            if (passenger == null) throw new IllegalArgumentException("Passenger not found.");

            System.out.print("Enter Bus Number: ");
            String busNumber = sc.nextLine().trim();
            if (busNumber.isEmpty()) throw new IllegalArgumentException("Bus number cannot be empty.");
            Bus bus = findBus(busNumber);
            if (bus == null) throw new IllegalArgumentException("Bus not found.");

            bus.getWaitingList().enqueue(passenger);
            System.out.println(passenger.getName() + " (ID: " + passenger.getPassengerId() + ") added to waiting list for bus " + busNumber);
            saveWaitingListToFile();
        } catch (IllegalArgumentException e) {
            throw new IOException("Error: " + e.getMessage(), e);
        } catch (IllegalStateException e) {
            throw new IOException("Error: " + e.getMessage(), e);
        }
    }

    private static void viewAllBookings() {
        try {
            for (Bus b : buses) {
                System.out.println("\nBus: " + b.getBusNumber());
                boolean hasBookings = false;
                for (Booking booking : b.getBookedSeats()) {
                    if (booking != null) {
                        System.out.println(booking);
                        hasBookings = true;
                    }
                }
                if (!hasBookings) {
                    System.out.println("No bookings.");
                }
            }
        } catch (Exception e) {
            System.out.println("An error occurred while viewing bookings: " + e.getMessage());
        }
    }

    private static void viewAllPassengers() {
        try {
            System.out.println("\nRegistered Passengers:");
            LinkedList<Passenger> passengersList = getPassengers();
            if (passengersList.isEmpty()) {
                System.out.println("No passengers registered yet.");
                return;
            }
            for (Passenger p : passengersList) {
                System.out.println("Passenger ID: " + p.getPassengerId());
                System.out.println("Name: " + p.getName());
                System.out.println("Phone: " + p.getPhone());
                System.out.println("Email: " + p.getEmail());
                System.out.println("City: " + p.getCity());
                System.out.println("Age: " + p.getAge());
                System.out.println("----------------------");
            }
        } catch (Exception e) {
            System.out.println("An error occurred while viewing passengers: " + e.getMessage());
        }
    }

    private static void viewPassengersNewestToOldest() {
        try {
            System.out.println("\nRegistered Passengers (Newest to Oldest):");
            LinkedList<Passenger> passengersList = getPassengers();
            if (passengersList.isEmpty()) {
                System.out.println("No passengers registered yet.");
                return;
            }
            // Iterate in reverse order to get newest to oldest
            ListIterator<Passenger> iterator = passengersList.listIterator(passengersList.size());
            while (iterator.hasPrevious()) {
                Passenger p = iterator.previous();
                if (p != null) {
                    System.out.println("Passenger ID: " + p.getPassengerId());
                    System.out.println("Name: " + p.getName());
                    System.out.println("Phone: " + p.getPhone());
                    System.out.println("Email: " + p.getEmail());
                    System.out.println("City: " + p.getCity());
                    System.out.println("Age: " + p.getAge());
                    System.out.println("----------------------");
                }
            }
        } catch (Exception e) {
            System.out.println("An error occurred while viewing passengers: " + e.getMessage());
        }
    }

    private static void viewAllBuses() {
        try {
            System.out.println("\nRegistered Buses:");
            if (buses.isEmpty()) {
                System.out.println("No buses registered yet.");
                return;
            }
            for (Bus b : buses) {
                b.showBusDetails();
                System.out.println("Waiting List Length: " + b.getWaitingList().size());
                System.out.println("----------------------");
            }
        } catch (Exception e) {
            System.out.println("An error occurred while viewing buses: " + e.getMessage());
        }
    }

    private static void viewAvailableSeats() throws IOException {
        try {
            System.out.print("Enter Bus Number: ");
            String busNumber = sc.nextLine().trim();
            if (busNumber.isEmpty()) throw new IllegalArgumentException("Bus number cannot be empty.");
            Bus bus = findBus(busNumber);
            if (bus == null) throw new IllegalArgumentException("Bus not found.");

            bus.showBusDetails();
            System.out.print("Available Seats: ");
            int availableCount = 0;
            List<Integer> availableSeats = new ArrayList<>();
            for (int i = 1; i <= bus.getTotalSeats(); i++) {
                if (bus.isSeatAvailable(i)) {
                    availableSeats.add(i);
                    availableCount++;
                }
            }
            if (availableCount == 0) {
                System.out.println("No seats available.");
            } else {
                System.out.println(availableSeats);
                System.out.println("Total Available Seats: " + availableCount);
            }
            int bookedCount = 0;
            for (Booking booking : bus.getBookedSeats()) {
                if (booking != null) bookedCount++;
            }
            System.out.println("Total Booked Seats: " + bookedCount);
        } catch (IllegalArgumentException e) {
            throw new IOException("Error: " + e.getMessage(), e);
        }
    }

    private static void viewRequestNewSeats() {
        try {
            System.out.println("\nWaiting List for Requested Seats:");
            boolean hasWaiting = false;
            for (Bus b : buses) {
                if (b == null || b.getWaitingList() == null) {
                    System.out.println("Error: Invalid bus or waiting list for bus number: " + (b != null ? b.getBusNumber() : "null"));
                    continue;
                }
                System.out.println("\nBus: " + b.getBusNumber());
                StaticQueue<Passenger> waitingList = b.getWaitingList();
                if (waitingList.isEmpty()) {
                    System.out.println("No passengers in waiting list.");
                } else {
                    hasWaiting = true;
                    System.out.println("Passengers in waiting list:");
                    Iterator<Passenger> iterator = waitingList.iterator();
                    while (iterator.hasNext()) {
                        Passenger p = iterator.next();
                        if (p != null) {
                            System.out.println("Passenger ID: " + p.getPassengerId() + ", Name: " + p.getName());
                        } else {
                            System.out.println("Warning: Null passenger found in waiting list for bus " + b.getBusNumber());
                        }
                    }
                }
            }
            if (!hasWaiting) {
                System.out.println("No passengers in any waiting list across all buses.");
            }
        } catch (NoSuchElementException e) {
            System.out.println("Error: Issue iterating waiting list - " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred while viewing waiting list: " + e.getMessage());
        }
    }

    private static void savePassengersToFile() throws IOException {
        File file = new File("passengers.txt");
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter(file, false));
            for (Passenger p : passengers) {
                pw.println(p);
            }
            System.out.println("Passengers saved to " + file.getAbsolutePath());
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }

    private static void loadPassengersFromFile() throws IOException {
        File file = new File("passengers.txt");
        Scanner fs = null;
        try {
            fs = new Scanner(file);
            while (fs.hasNextLine()) {
                String line = fs.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] d = line.split(";");
                if (d.length != 6) {
                    System.err.println("Skipping invalid passenger data: " + line);
                    continue;
                }
                try {
                    int age = Integer.parseInt(d[5]);
                    passengers.add(new Passenger(d[0], d[1], d[2], d[3], d[4], age));
                    String idNum = d[0].substring(1);
                    int id = Integer.parseInt(idNum);
                    if (id >= passengerIdCounter) {
                        passengerIdCounter = id + 1;
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Invalid age or ID format in passenger data: " + line + " - " + e.getMessage());
                }
            }
            System.out.println("Loaded " + passengers.size() + " passengers from " + file.getAbsolutePath());
        } catch (FileNotFoundException e) {
            System.err.println("Passenger file not found at " + file.getAbsolutePath() + ": " + e.getMessage());
        } catch (SecurityException e) {
            System.err.println("Permission denied accessing file " + file.getAbsolutePath() + ": " + e.getMessage());
            throw new IOException("Unable to access file due to permissions", e);
        } finally {
            if (fs != null) {
                fs.close();
            }
        }
    }

    private static void saveBusesToFile() throws IOException {
        File file = new File("buses.txt");
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter(file, false));
            for (Bus b : buses) {
                pw.println(b.toFileString());
            }
            System.out.println("Buses saved to " + file.getAbsolutePath());
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }

    private static void loadBusesFromFile() throws IOException {
        File file = new File("buses.txt");
        if (!file.exists()) {
            System.out.println("No bus file found at " + file.getAbsolutePath());
            return;
        }
        Scanner fs = null;
        try {
            fs = new Scanner(file);
            while (fs.hasNextLine()) {
                String line = fs.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] d = line.split(";");
                if (d.length != 6) throw new IOException("Invalid bus data format: " + line);
                try {
                    int seats = Integer.parseInt(d[1]);
                    double fare = Double.parseDouble(d[5]);
                    buses.add(new Bus(d[0], seats, d[2], d[3], d[4], fare));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid seats or fare format in bus data: " + line);
                }
            }
            System.out.println("Loaded " + buses.size() + " buses from " + file.getAbsolutePath());
        } finally {
            if (fs != null) {
                fs.close();
            }
        }
    }

    private static void saveBookingsToFile() throws IOException {
        File file = new File("bookings.txt");
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter(file, false));
            for (Bus b : buses) {
                for (Booking booking : b.getBookedSeats()) {
                    if (booking != null) {
                        pw.println(b.getBusNumber() + ";" + booking.getPassenger().getPassengerId() + ";" + booking.getSeatNumber());
                    }
                }
            }
            System.out.println("Bookings saved to " + file.getAbsolutePath());
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }

    private static void loadBookingsFromFile() throws IOException {
        File file = new File("bookings.txt");
        if (!file.exists()) {
            System.out.println("No bookings file found at " + file.getAbsolutePath());
            return;
        }
        Scanner fs = null;
        try {
            fs = new Scanner(file);
            while (fs.hasNextLine()) {
                String line = fs.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] d = line.split(";");
                if (d.length != 3) throw new IOException("Invalid booking data format: " + line);
                try {
                    Bus bus = findBus(d[0]);
                    if (bus != null) {
                        Passenger passenger = findPassengerById(d[1]);
                        if (passenger != null) {
                            int seatNumber = Integer.parseInt(d[2]);
                            bus.bookSeat(passenger, seatNumber);
                        } else {
                            System.out.println("Passenger not found for booking: " + line);
                        }
                    } else {
                        System.out.println("Bus not found for booking: " + line);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid seat number format in booking data: " + line);
                }
            }
            System.out.println("Bookings loaded from " + file.getAbsolutePath());
        } finally {
            if (fs != null) {
                fs.close();
            }
        }
    }

    private static void saveWaitingListToFile() throws IOException {
        File file = new File("waitinglist.txt");
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter(file, false));
            for (Bus b : buses) {
                Iterator<Passenger> iterator = b.getWaitingList().iterator();
                while (iterator.hasNext()) {
                    Passenger p = iterator.next();
                    pw.println(b.getBusNumber() + ";" + p.getPassengerId());
                }
            }
            System.out.println("Waiting list saved to " + file.getAbsolutePath());
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }

    private static void loadWaitingListFromFile() throws IOException {
        File file = new File("waitinglist.txt");
        if (!file.exists()) {
            System.out.println("No waiting list file found at " + file.getAbsolutePath());
            return;
        }
        Scanner fs = null;
        try {
            fs = new Scanner(file);
            while (fs.hasNextLine()) {
                String line = fs.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] d = line.split(";");
                if (d.length != 2) throw new IOException("Invalid waiting list data format: " + line);
                Bus bus = findBus(d[0]);
                if (bus != null) {
                    Passenger passenger = findPassengerById(d[1]);
                    if (passenger != null) {
                        try {
                            bus.getWaitingList().enqueue(passenger);
                        } catch (IllegalStateException e) {
                            System.out.println("Waiting list full for bus " + d[0] + ", cannot add passenger: " + d[1]);
                        }
                    } else {
                        System.out.println("Passenger not found for waiting list: " + line);
                    }
                } else {
                    System.out.println("Bus not found for waiting list: " + line);
                }
            }
            System.out.println("Loaded waiting list from " + file.getAbsolutePath());
        } finally {
            if (fs != null) {
                fs.close();
            }
        }
    }
}