package cobaia.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;

import cobaia.model.Usuario;

public class UsuarioDAO extends AbstractDAO<Usuario> {

	@Override
	public void create(Usuario usuario) {
		try (Connection con = super.openConnection()) {
			String sql = "INSERT INTO usuarios (nome, email, senha, token) VALUES (?, ?, ?, ?)";
			String token = UUID.randomUUID().toString().split("-")[0];
			
			PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			
			stmt = con.prepareStatement(sql);
			stmt.setString(1, usuario.getNome());
			stmt.setString(2, usuario.getEmail());
			stmt.setString(3, DigestUtils.md5Hex(usuario.getSenha() + SALT));
			stmt.setString(4, token);
			
			if (stmt.executeUpdate() > 0) {
				ResultSet rs = stmt.getGeneratedKeys();
				if (rs.next()) {
					usuario.setId(rs.getInt(1));
				}	
			}
		} catch (SQLException sqle) {
			throw new PersistenceException(sqle);
		}
		
	}

	@Override
	public List<Usuario> read() {
		List<Usuario> usuarios = new ArrayList<>();
		try (Connection con = super.openConnection()) {
			String sql = "SELECT * FROM usuarios";
			PreparedStatement stmt = con.prepareStatement(sql);						
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				// TODO: row mapper
				Usuario usuario = new Usuario();
				usuario.setNome(rs.getString("nome"));
				usuarios.add(usuario);
			}
		} catch (SQLException sqle) {
			throw new PersistenceException(sqle);
		}
		return usuarios;
	}

	@Override
	public void update(Usuario usuario) {
		if (usuario.getId() == null) {
			throw new PersistenceException("O objeto é transiente e não pode ser atualizado");
		}
		try (Connection con = super.openConnection()) {
			String sql = "UPDATE usuarios SET nome = ? WHERE id = ?";
			PreparedStatement stmt = con.prepareStatement(sql);
			stmt.setString(1, usuario.getNome());
			stmt.setInt(2, usuario.getId());
			stmt.execute();
		} catch (SQLException sqle) {
			throw new PersistenceException(sqle);
		}
	}

	@Override
	public void delete(Usuario usuario) {
		if (usuario.getId() == null) {
			throw new PersistenceException("O objeto é transiente e não pode ser deletado");
		}
		try (Connection con = super.openConnection()) {
			String sql = "DELETE FROM usuarios WHERE id = ?";
			PreparedStatement stmt = con.prepareStatement(sql);			
			stmt.setInt(1, usuario.getId());
			if (stmt.execute()) {
				usuario.setId(null);
			}
		} catch (SQLException sqle) {
			throw new PersistenceException(sqle);
		}
	}

	@Override
	public Usuario find(int id) {
		Usuario usuario = null;
		try (Connection con = super.openConnection()) {
			String sql = "SELECT * FROM usuarios WHERE id = ?";
			PreparedStatement stmt = con.prepareStatement(sql);			
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				usuario = new Usuario();
			}
		} catch (SQLException sqle) {
			throw new PersistenceException(sqle);
		}
		return usuario;
	}

}
