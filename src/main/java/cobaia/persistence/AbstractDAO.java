package cobaia.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import cobaia.model.AbstractModel;

public abstract class AbstractDAO <T extends AbstractModel> {

	protected final String SALT = "cobaiaforever";
	private static final String URL = "jdbc:hsqldb:database/files/mochinho";
	private static final String USR = "SA";
	private static final String PSW = "";
	
	protected Connection openConnection() throws SQLException {
		Connection con = DriverManager.getConnection(URL, USR, PSW);
		return con;
	}
	
	public abstract void create(T o);
	
	public abstract List<T> read();

	public abstract void update(T o);
	
	public abstract void delete(T o);

	public abstract T find(int id);
	
}
