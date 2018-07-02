package cobaia.persistence;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import cobaia.model.AbstractModel;


public class GenericDAO extends AbstractDAO<AbstractModel> {

	@Override
	public void create(AbstractModel o) {
		String table = "";
		List<Field> fields = new ArrayList<>();
		
		if (o.getClass().isAnnotationPresent(Table.class)) {
			table = o.getClass().getAnnotation(Table.class).name();
		} else {
			table = o.getClass().getSimpleName().toLowerCase();
		}
		
		for (Field f : o.getClass().getDeclaredFields()) {
			if (f.isAnnotationPresent(Column.class)) {
				fields.add(f);
			}
		}
		
		try (Connection con = super.openConnection()) {
			StringBuilder sql = new StringBuilder("INSERT INTO ");
			sql.append(table).append(" (");
			
			for (int i = 0; i < fields.size(); i++) {
				Field f = fields.get(i);
				if (f.getAnnotation(Column.class).name().isEmpty()) {
					sql.append(f.getName());
				} else {
					sql.append(f.getAnnotation(Column.class).name());
				}
				
				if (i < fields.size() - 1) sql.append(", ");
			}
			
			sql.append(") VALUES (");
			
			for (int i = 0; i < fields.size(); i++) {
				sql.append("?");
				if (i < fields.size() - 1) sql.append(", ");
			}
			
			sql.append(")");
			
			PreparedStatement stmt = con.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);
			
			for (int i = 0; i < fields.size(); i++) {
				Field f = fields.get(i);
				f.setAccessible(true);
				stmt.setObject(i + 1, f.get(o));
			}

			if (stmt.executeUpdate() > 0) {
				ResultSet rs = stmt.getGeneratedKeys();
				if (rs.next()) {
					o.setId(rs.getInt(1));
				}	
			}
		} catch (Exception e) {
			throw new PersistenceException(e);
		}
	}

	@Override
	public List<AbstractModel> read() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(AbstractModel o) {
		String table = "";
		List<Field> fields = new ArrayList<>();
		
		if (o.getClass().isAnnotationPresent(Table.class)) {
			table = o.getClass().getAnnotation(Table.class).name();
		} else {
			table = o.getClass().getSimpleName().toLowerCase();
		}
		
		for (Field f : o.getClass().getDeclaredFields()) {
			if (f.isAnnotationPresent(Column.class)) {
				fields.add(f);
			}
		}
		
		try (Connection con = super.openConnection()) {
			StringBuilder sql = new StringBuilder("UPDATE ");
			sql.append(table).append(" SET ");
			
			for (int i = 0; i < fields.size(); i++) {
				Field f = fields.get(i);
				if (f.getAnnotation(Column.class).name().isEmpty()) {
					sql.append(f.getName()).append(" = ?");
				} else {
					sql.append(f.getAnnotation(Column.class).name());
				}
				
				if (i < fields.size() - 1) sql.append(", ");
			}
			
			PreparedStatement stmt = con.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);

			for (int i = 0; i < fields.size(); i++) {
				Field f = fields.get(i);
				f.setAccessible(true);
				stmt.setObject(i + 1, f.get(o));
			}

			if (stmt.executeUpdate() > 0) {
				ResultSet rs = stmt.getGeneratedKeys();
				if (rs.next()) {
					o.setId(rs.getInt(1));
				}	
			}
		} catch (Exception e) {
			throw new PersistenceException(e);
		}
	}

	@Override
	public void delete(AbstractModel o) {
		if (o.getId() == null)
			throw new PersistenceException("O objeto é transiente e não pode ser deletado");
		
		String table = "";
		
		if (o.getClass().isAnnotationPresent(Table.class)) {
			table = o.getClass().getAnnotation(Table.class).name();
		} else {
			table = o.getClass().getSimpleName().toLowerCase();
		}
		
		try (Connection con = super.openConnection()) {
			String sql = "DELETE FROM " + table + " WHERE id = ?";
			PreparedStatement stmt = con.prepareStatement(sql);			

			stmt.setInt(1, o.getId());
			if (stmt.executeUpdate() > 0) {
				o.setId(null);
			}
		} catch (SQLException sqle) {
			throw new PersistenceException(sqle);
		}
		
	}

	@Override
	public AbstractModel find(int id) {
		// TODO Auto-generated method stub
		return null;
	}

}
