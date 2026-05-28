
import com.mybank.domain.Bank;
import com.mybank.domain.CheckingAccount;
import com.mybank.domain.Customer;
import com.mybank.domain.SavingsAccount;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.jline.reader.*;
import org.jline.reader.impl.completer.*;
import org.fusesource.jansi.*;

public class CLIdemo {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";

    private String[] commandsList;

    public void init() {
        commandsList = new String[]{"help", "customers", "customer", "report", "exit"};
    }

    public void run() {

        AnsiConsole.systemInstall();
        printWelcomeMessage();

        LineReader reader = LineReaderBuilder.builder()
                .completer(new ArgumentCompleter(
                        new StringsCompleter(commandsList)))
                .build();

        String line;

        while ((line = readLine(reader)) != null) {

            // HELP
            if ("help".equals(line)) {
                printHelp();
            } // CUSTOMERS LIST
            else if ("customers".equals(line)) {

                System.out.println("\nThis is all of your customers:");

                if (Bank.getNumberOfCustomers() == 0) {
                    System.out.println(ANSI_RED + "No customers found!" + ANSI_RESET);
                    continue;
                }

                System.out.println("\nLast name\tFirst Name\tBalance");
                System.out.println("---------------------------------------");

                for (int i = 0; i < Bank.getNumberOfCustomers(); i++) {

                    Customer customer = Bank.getCustomer(i);

                    double balance = 0;

                    if (customer.getNumberOfAccounts() > 0) {
                        balance = customer.getAccount(0).getBalance();
                    }

                    System.out.println(
                            customer.getLastName() + "\t\t"
                            + customer.getFirstName() + "\t\t$"
                            + balance
                    );
                }
            } // SINGLE CUSTOMER
            else if (line.startsWith("customer")) {

                try {

                    int custNo = 0;

                    String[] parts = line.split(" ");
                    if (parts.length > 1) {
                        custNo = Integer.parseInt(parts[1]);
                    }

                    Customer cust = Bank.getCustomer(custNo);

                    System.out.println("\nCustomer #" + custNo);

                    if (cust.getNumberOfAccounts() == 0) {
                        System.out.println("No accounts");
                        continue;
                    }

                    String accType = (cust.getAccount(0) instanceof CheckingAccount)
                            ? "Checking"
                            : "Savings";

                    System.out.println(
                            cust.getLastName() + "\t"
                            + cust.getFirstName() + "\t"
                            + accType + "\t"
                            + cust.getAccount(0).getBalance()
                    );

                } catch (Exception e) {
                    System.out.println(ANSI_RED + "Wrong customer number!" + ANSI_RESET);
                }
            } // REPORT (NEW COMMAND)
            else if ("report".equals(line)) {

                System.out.println("\n===== CUSTOMER REPORT =====\n");

                if (Bank.getNumberOfCustomers() == 0) {
                    System.out.println("No customers found.");
                    continue;
                }

                double totalBankBalance = 0;

                for (int i = 0; i < Bank.getNumberOfCustomers(); i++) {

                    Customer customer = Bank.getCustomer(i);

                    System.out.println("Customer: "
                            + customer.getLastName() + " "
                            + customer.getFirstName());

                    if (customer.getNumberOfAccounts() == 0) {
                        System.out.println("\tNo accounts\n");
                        continue;
                    }

                    double customerTotal = 0;

                    for (int j = 0; j < customer.getNumberOfAccounts(); j++) {

                        double balance = customer.getAccount(j).getBalance();
                        customerTotal += balance;

                        String type = (customer.getAccount(j) instanceof CheckingAccount)
                                ? "Checking"
                                : "Savings";

                        System.out.println("\tAccount " + (j + 1)
                                + " [" + type + "] : $" + balance);
                    }

                    System.out.println("\tTOTAL: $" + customerTotal + "\n");

                    totalBankBalance += customerTotal;
                }

                System.out.println("=================================");
                System.out.println("BANK TOTAL BALANCE: $" + totalBankBalance);
                System.out.println("=================================\n");
            } // EXIT
            else if ("exit".equals(line)) {
                System.out.println("Exiting...");
                return;
            } // UNKNOWN COMMAND
            else {
                System.out.println(
                        ANSI_RED + "Unknown command. Type 'help'." + ANSI_RESET
                );
            }
        }

        AnsiConsole.systemUninstall();
    }

    private String readLine(LineReader reader) {
        try {
            return reader.readLine("\nbank> ").trim();
        } catch (Exception e) {
            return null;
        }
    }

    private void printWelcomeMessage() {
        System.out.println(
                ANSI_GREEN + "Welcome to MyBank CLI" + ANSI_RESET
                + "\nType 'help' for commands"
        );
    }

    private void printHelp() {
        System.out.println("help - show help");
        System.out.println("customers - list customers");
        System.out.println("customer <id> - show customer");
        System.out.println("exit - exit app");        
        System.out.println("report - call on clients");

    }

    /**
     * SAFE loader (FIXED)
     */
    private static void loadData(String filename) {

        try (Scanner sc = new Scanner(new File(filename))) {

            int numCustomers = Integer.parseInt(sc.next());

            for (int i = 0; i < numCustomers; i++) {

                String firstName = sc.next();
                String lastName = sc.next();
                int numAccounts = Integer.parseInt(sc.next());

                Bank.addCustomer(firstName, lastName);
                Customer customer = Bank.getCustomer(i);

                for (int j = 0; j < numAccounts; j++) {

                    String accountType = sc.next();

                    try {
                        if (accountType.equals("S")) {

                            double balance = Double.parseDouble(sc.next());
                            double interest = Double.parseDouble(sc.next());

                            customer.addAccount(
                                    new SavingsAccount(balance, interest)
                            );

                        } else if (accountType.equals("C")) {

                            double balance = Double.parseDouble(sc.next());
                            double overdraft = Double.parseDouble(sc.next());

                            customer.addAccount(
                                    new CheckingAccount(balance, overdraft)
                            );
                        }

                    } catch (Exception ex) {
                        System.out.println("Error in account data, skipping...");
                        break;
                    }
                }
            }

            System.out.println("Data loaded successfully!");

        } catch (Exception e) {
            System.out.println("Error reading file:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        loadData("test.dat");

        CLIdemo shell = new CLIdemo();
        shell.init();
        shell.run();
    }
}
