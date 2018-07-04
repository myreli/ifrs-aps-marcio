package cobaia.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import cobaia.model.Area;

public class AreaDAO extends GenericDAO {

/*	@Override
	public void create(Area area) {
		try (Connection con = super.openConnection()) {
			String sql = "INSERT INTO areas (nome) VALUES (?)";
			PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, area.getNome());
			
			if (stmt.executeUpdate() > 0) {
				ResultSet rs = stmt.getGeneratedKeys();
				if (rs.next()) {
					area.setId(rs.getInt(1));
				}	
			}
		} catch (SQLException sqle) {
			throw new PersistenceException(sqle);
		}
		
	}
*/
/*	@Override
	public List<Area> read() {
		List<Area> areas = new ArrayList<>();
		try (Connection con = super.openConnection()) {
			String sql = "SELECT * FROM areas";
			PreparedStatement stmt = con.prepareStatement(sql);						
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				// TODO: row mapper
				Area area = new Area(rs.getString("nome"), rs.getInt("id"));
				areas.add(area);
			}
		} catch (SQLException sqle) {
			throw new PersistenceException(sqle);
		}
		return areas;
	}*/

/*	@Override
	public void update(Area area) {
		if (area.getId() == null) {
			throw new PersistenceException("O objeto é transiente e não pode ser atualizado");
		}
		try (Connection con = super.openConnection()) {
			String sql = "UPDATE areas SET nome = ? WHERE id = ?";
			PreparedStatement stmt = con.prepareStatement(sql);
			stmt.setString(1, area.getNome());
			stmt.setInt(2, area.getId());
			stmt.execute();
		} catch (SQLException sqle) {
			throw new PersistenceException(sqle);
		}
	}
*/
	/*@Override
	public void delete(Area area) {
		if (area.getId() == null) {
			throw new PersistenceException("O objeto é transiente e não pode ser deletado");
		}
		try (Connection con = super.openConnection()) {
			String sql = "DELETE FROM areas WHERE id = ?";
			PreparedStatement stmt = con.prepareStatement(sql);			
			stmt.setInt(1, area.getId());
			if (stmt.execute()) {
				area.setId(null);
			}
		} catch (SQLException sqle) {
			throw new PersistenceException(sqle);
		}
	}*/

	@Override
	public Area find(int id) {
		Area area = null;
		try (Connection con = super.openConnection()) {
			String sql = "SELECT * FROM areas WHERE id = ?";
			PreparedStatement stmt = con.prepareStatement(sql);			
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				area = new Area(rs.getString("nome"), id);
				area.setLast_update(rs.getTimestamp("last_updated"));
			}
		} catch (SQLException sqle) {
			throw new PersistenceException(sqle);
		}
		return area;
	}

}
