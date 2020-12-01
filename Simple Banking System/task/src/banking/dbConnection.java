package banking;

import org.sqlite.SQLiteDataSource;

import java.sql.*;

public class dbConnection {
    private static Connection connection = null;
    private static Statement statement = null;

    static void createConnection(){
        if (connection == null){
            try {
//                DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
//                String url = "jdbc:mysql://localhost:3306/";
                String url = "jdbc:sqlite:";
                SQLiteDataSource dataSource = new SQLiteDataSource();
                dataSource.setUrl(url + Main.getDbFileName());
                connection = dataSource.getConnection();
//                connection = DriverManager.getConnection(url + Main.getDbFileName(), Main.getUsername(), Main.getPassword());
//                        getDBProperties();

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
//        return connection;
    }
    static void createTable(){
        try{
            statement = connection.createStatement();

            String sql = "CREATE TABLE IF NOT EXISTS card(" +
                    "id INTEGER, " +
                    "number VARCHAR(16), " +
                    "pin VARCHAR(4), " +
                    "balance INTEGER DEFAULT 0" +
                    ");";

            statement.executeUpdate(sql);
            statement.close();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
    }
    static void addToDB(String cardNum, String pin) {
        try(Statement addStmt = connection.createStatement()) {
            String sql = "INSERT INTO card (number, pin) " +
                    "VALUES ('" +
                    cardNum + "', '" + pin + "');";
            addStmt.executeUpdate(sql);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
    static int selectFromDB(String cardNum, String pin){
        int balance = 0;
        try(Statement selectStmt = connection.createStatement()){
            String sql = "SELECT balance FROM card " +
                                "WHERE " +
                                    "number = '" + cardNum + "' " +
                                    "AND pin = '" + pin + "'" +
                        ";";
            ResultSet resultSet = selectStmt.executeQuery(sql);
            while (resultSet.next()){
                balance = resultSet.getInt("balance");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return balance;
    }

    public static void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    public static void deleteFromDB(String cardNum, String pin) {
        String sql = "DELETE FROM card" +
                    " WHERE" +
                    " number = ?" +
                    " AND pin = ?";
        PreparedStatement delStmt = null;
        try {
            delStmt = connection.prepareStatement(sql);
            delStmt.setString(1, cardNum);
            delStmt.setString(2, pin);

            delStmt.executeUpdate();

            delStmt.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            if(delStmt != null) {
                try {
                    delStmt.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
    }

    public static int addIncome(String cardNum, int income) {
        int rowsUpd = 0;
        PreparedStatement addStmt;
        try {
            String sql = "UPDATE card" +
                    " SET balance = balance + ?"  +
                    " WHERE number = ?";
            addStmt = connection.prepareStatement(sql);
            addStmt.setInt(1, income);
            addStmt.setString(2,cardNum);

            rowsUpd = addStmt.executeUpdate();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return rowsUpd;
    }

    public static void transferMoney(String sender, String receiver, int amount) {
        String getMoney = "UPDATE card" +
                " SET balance = balance - ?" +
                " WHERE number = ?";
        String sendMoney = "UPDATE card" +
                " SET balance = balance + ?" +
                " WHERE number = ?";
        try{
            connection.setAutoCommit(false);
            try(PreparedStatement getStmt = connection.prepareStatement(getMoney);
                 PreparedStatement sendStmt = connection.prepareStatement(sendMoney)){
                    getStmt.setInt(1, amount);
                    getStmt.setString(2, sender);
                    getStmt.executeUpdate();

                    sendStmt.setInt(1, amount);
                    sendStmt.setString(2, receiver);
                    sendStmt.executeUpdate();

                    connection.commit();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            if(connection != null){
                try {
                    connection.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
    }

    public static ResultSet loadFromDB() {
        ResultSet data = null;
        try{
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM card;";
            data = stmt.executeQuery(sql);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return data;
    }
    /*static void checkDB() {
        Statement statement = null;
        try {
            DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
            try(Connection connection = getDBProperties()){
                statement = connection.createStatement();
                String sql = "SELECT * FROM card";
                ResultSet resultSet = statement.executeQuery(sql);
                while (resultSet.next()){
                    int id = resultSet.getInt("id");
                    String cardNum = resultSet.getString("number");
                    String pin = resultSet.getString("pin");
                    System.out.print(id + ". CardNum: " + cardNum + " PIN: " + pin + "\n\n");
                }
                resultSet.close();
            } finally {
                if (statement != null) statement.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    /*private static Connection getDBProperties() throws IOException, SQLException {
        Properties properties = new Properties();
        properties.load(new FileInputStream("database.properties"));

        String url = properties.getProperty("url");
        String username = properties.getProperty("username");
        String password = properties.getProperty("password");

        return DriverManager.getConnection(url, username,password);
    }*/
}
