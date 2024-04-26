package br.com.adapters.out;

import br.com.domain.model.ConvertionRec;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;


public class Connectiondb {

    private Connection conn;
    private String url = "jdbc:postgresql://localhost:5432/br-com-conversion";

    public Connectiondb(){
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        props.setProperty("password", "1234");
        props.setProperty("ssl", "false");

        try {
            Class.forName("org.postgresql.Driver");
            this.conn = DriverManager.getConnection(url, props);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void initConnection() throws SQLException {
        Statement stmt =  conn.createStatement();
        String sql = "CREATE TABLE IF NOT EXISTS conversionhistory ("
                + "id serial PRIMARY KEY,"
                + "source VARCHAR(255),"
                + "target VARCHAR(255),"
                + "convertionValue FLOAT,"
                + "resultValue FLOAT,"
                + "conversionRate FLOAT,"
                + "date DATE,"
                + "time TIME )";

        stmt.execute(sql);
        stmt.close();
    }

    public void loadConversionHistory(List<ConvertionRec> lstConvertionHistory){

        try {
            Statement stmt = conn.createStatement();
            ResultSet resultSet = stmt.executeQuery("select * from conversionhistory");

            System.out.println("Quantidade de linhas obtidas: " + resultSet.getRow());
            while(resultSet.next()){
                ConvertionRec convertionRec = new ConvertionRec(
                resultSet.getString("source"),
                resultSet.getString("target"),
                resultSet.getDouble("convertionvalue"),
                resultSet.getDouble("resultvalue"),
                resultSet.getDouble("conversionrate"),
                LocalDateTime.of(resultSet.getDate("date").toLocalDate(), resultSet.getTime("time").toLocalTime())
                );

                lstConvertionHistory.add(convertionRec);
            }

            stmt.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public void saveConversion(ConvertionRec conversion) {

        String sql = "insert into conversionhistory ("
         + "source, target, convertionValue, "
        + "resultValue, conversionRate, date, time) "
        + " values (?, ?, ?, ?, ?, ?, ?)";

        Date data = Date.valueOf(conversion.dateTime().toLocalDate());
        Time time = Time.valueOf(conversion.dateTime().toLocalTime());

        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = conn.prepareStatement(sql);

            preparedStatement.setString(1, conversion.from());
            preparedStatement.setString(2, conversion.to());
            preparedStatement.setDouble(3, conversion.convertionValue());
            preparedStatement.setDouble(4, conversion.resultValue());
            preparedStatement.setDouble(5, conversion.conversionRate());
            preparedStatement.setDate(6,  data);
            preparedStatement.setTime(7, time);

            preparedStatement.execute();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println("Erro na etapa de salvar o registro  no banco. " + e.getMessage());
        }

    }
}
