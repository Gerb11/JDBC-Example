import java.sql.*;
import java.io.*;

/**
 * This is a launcher class that will run a simple menu for options for the user to choose.
 * The menu will be able to display data from tables in a database, as well as insert into a purchase table 
 * a car they bought (as well as all other relevant and needed information)
 * @author Jeremy Winter
 *
 */
public class Launcher {

	private static BufferedReader br;

	public static void main(String[] args) {
		br = new BufferedReader(new InputStreamReader(System.in)); //for user input
		Connection con = null;

		try {
			Class.forName("org.sqlite.JDBC");
			String url = "jdbc:sqlite:Database.db"; //url for the database we want to connect to

			con = DriverManager.getConnection(url);

		} catch (ClassNotFoundException e1) { //cannot find the jdbc class
			e1.printStackTrace();
		} catch (SQLException e) { 
			e.printStackTrace();
		}

		String userInput = "";
		while(!userInput.equals("6")) { //run until the user inputs 6
			try {
				//options for the menu, user can type 0-6 
				System.out.println("0.	Echo a comment typed in from the terminal.");
				System.out.println("1.	List all the information for sales persons.");
				System.out.println("2.	List all the information in the cars table.");
				System.out.println("3.	List all the information on customers.");
				System.out.println("4.	List all the information on options.");
				System.out.println("5.	Process a purchase");
				System.out.println("6.	Quit.");
				System.out.println("Choose an option:");
				userInput = br.readLine();

				Statement stm = con.createStatement();

				if(userInput.equals("0")) { //for echoing a command
					System.out.println("Type a command to echo");
					System.out.println(userInput = br.readLine());
				} else if(userInput.equals("1")) {
					ResultSet result = stm.executeQuery("select * from sales_person");
					System.out.println("SIN, First Name, Last Name, Start Date");
					while(result.next()) {//lists all of the columns for each row for the sales person table
						System.out.print(result.getString("SIN") + ", "); 
						System.out.print(result.getString("First_name") + ", ");
						System.out.print(result.getString("last_name") + ", ");
						System.out.print(result.getString("start_date"));
						System.out.println("");
					}
				} else if(userInput.equals("2")) {
					ResultSet result = stm.executeQuery("select * from car");
					System.out.println("VIN, Model, Year, Colour, NoOfCylindars, Description, Arrival Date, Asking Price");
					while(result.next()) {//lists all of the columns for each row for the car table
						System.out.print(result.getString("VIN") + ", ");
						System.out.print(result.getString("model") + ", ");
						System.out.print(result.getString("year") + ", ");
						System.out.print(result.getString("colour") + ", ");
						System.out.print(result.getString("NoOfCylinders") + ", ");
						System.out.print(result.getString("description") + ", ");
						System.out.print(result.getString("arrival_date") + ", ");
						System.out.print(result.getString("asking_price"));
						System.out.println("");
					}
				} else if(userInput.equals("3")) {
					ResultSet result = stm.executeQuery("select * from customer");
					System.out.println("Customer number, First Name, Last Name, Address, Phone Number, Credit rating, Date Started");
					while(result.next()) {//lists all of the columns for each row for the customer table
						System.out.print(result.getString("customer_number") + ", ");
						System.out.print(result.getString("first_name") + ", ");
						System.out.print(result.getString("last_name") + ", ");
						System.out.print(result.getString("address") + ", ");
						System.out.print(result.getString("phone_number") + ", ");
						System.out.print(result.getString("credit_rating") + ", ");
						System.out.print(result.getString("date_started"));
						System.out.println("");
					}
				} else if(userInput.equals("4")) {
					ResultSet result = stm.executeQuery("select * from option");
					System.out.println("Name, Model, Price, Details");
					while(result.next()) {//lists all of the columns for each row for the option table
						System.out.print(result.getString("Name") + ", ");
						System.out.print(result.getString("Model") + ", ");
						System.out.print(result.getString("Price") + ", ");
						System.out.print(result.getString("details"));
						System.out.println("");
					}
				} else if(userInput.equals("5")) { //for when the user wants to insert a purchase
					System.out.println("Please input a VIN: ");
					String VIN = br.readLine(); //gets the vin from the user
					
					PreparedStatement getCar = con.prepareStatement("select * from car where vin=?");
					getCar.setString(1, VIN);
					ResultSet carResult = getCar.executeQuery();
					int askingPrice = Integer.parseInt(carResult.getString("asking_price")); //stores asking price. This also error checks that the vin exists in the db

					System.out.println("Please input a customer number: ");
					String cNum = br.readLine(); //gets customer number from the user
					PreparedStatement getCustomer = con.prepareStatement("select * from customer where customer_number=?");
					getCustomer.setString(1, cNum);
					ResultSet custResult = getCustomer.executeQuery();
					custResult.getString("first_name"); //test to make sure customer exists

					System.out.println("Please input a sales person SIN: ");
					String salesSIN = br.readLine(); //get sales SIN from user
					PreparedStatement getSPerson = con.prepareStatement("select * from sales_person where SIN=?");
					getSPerson.setString(1, salesSIN);
					ResultSet sPersonResult = getSPerson.executeQuery();
					sPersonResult.getString("SIN"); //test to make sure the sales person exists

					//getting the selling price, optionname, and option model from the user inorder to complete the purchase
					System.out.println("Please input the selling price: ");
					int sellingPrice = Integer.parseInt(br.readLine());
					
					System.out.println("Please input a name for the option");
					String optionName = br.readLine();

					PreparedStatement getOptions = con.prepareStatement("select * from option where "
							+ "Name=? and Model=?");
					getOptions.setString(1, optionName);
					getOptions.setString(2, carResult.getString("Model"));
					ResultSet optionsResult = getOptions.executeQuery();
					int optionPrice = Integer.parseInt(optionsResult.getString("price")); //price of the option from what the user entered
					
					//calculates the 3 output values, total price, difference in asking and selling price and sales person commision
					double totalPrice = (askingPrice + optionPrice) * 1.13;
					System.out.println("Total Amount due: " + Math.round(totalPrice));
					
					double diffPrice = askingPrice - sellingPrice;
					System.out.println("Difference between asking and selling price: " + diffPrice);
					
					double commision = (askingPrice + optionPrice) * .04;
					System.out.println("Sales Person commision: " + Math.round(commision));
					
					PreparedStatement deleteCar = con.prepareStatement("delete from car where VIN=?"); //car has been sold, no longer in db
					deleteCar.setString(1, VIN);
					deleteCar.execute(); //deletes from the car table
					
					PreparedStatement addPurchase = con.prepareStatement("insert into purchase values "
							+ "(?, ?, ?, ?, ?, ?)");
					addPurchase.setString(1, salesSIN);
					addPurchase.setString(2, cNum);
					addPurchase.setString(3, VIN);
					addPurchase.setString(4, "11/26/2013");
					addPurchase.setString(5, Integer.toString(sellingPrice));
					addPurchase.setString(6, "cash");
					addPurchase.execute(); //inserts into the purchase table. Will only get to this line if there were no errors
											//ie, all the data that was input by the user, also existed in the db
				}
				stm.close();
			} catch(NumberFormatException e) {
				System.out.println("Must be a number");
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				System.out.println("Sorry, what you entered does not exist");
			}
		} 
		try {
			if(con != null) {
				con.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
