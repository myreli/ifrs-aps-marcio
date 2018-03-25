package cobaia;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

import cobaia.modelo.Area;
import cobaia.modelo.Curso;
import cobaia.modelo.Usuario;
//import javafx.scene.image.Image;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import spark.TemplateEngine;
import spark.debug.DebugScreen;
import spark.template.pebble.PebbleTemplateEngine;

public class Main {

	public static void main(String[] args) {

		final String SALT = "cobaiaforever";
		final DateTimeFormatter DATE_FORMATTER_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

		try {
			Class.forName(org.hsqldb.jdbcDriver.class.getName());
		} catch (ClassNotFoundException e) {
			throw new ExceptionInInitializerError(e);
		}

		Spark.staticFileLocation("/public");
		// final TemplateEngine velocity = new VelocityTemplateEngine();
		final TemplateEngine pebble = new PebbleTemplateEngine();
		DebugScreen.enableDebugScreen();
		final SimpleDateFormat ISODateFormat = new SimpleDateFormat("yyyy-MM-dd");
		final SimpleDateFormat ISOTimeFormat = new SimpleDateFormat("hh:mm");

		Spark.get("/", new Route() {
			public Object handle(Request req, Response resp) throws Exception {
				Map<String, String> map = new HashMap<>();

				/* PASSAR O USUÁRIO SE PRESENTE NA SESSÃO PARA A VIEW */
				if (req.session().attribute("usuario") != null) {          
					map.put("usuario", req.session().attribute("usuario").toString());
					map.put("email", req.session().attribute("email").toString());
				}

				return pebble.render(new ModelAndView(map, "templates/index.pebble"));
			}
		});




		Spark.get("/login", new Route() {
			public Object handle(Request req, Response resp) throws Exception {
				Map<String, String> map = new HashMap<>();
				if (req.session().attribute("email") != null) map.put("email", req.session().attribute("email").toString());
				if (req.session().attribute("codigo") != null) map.put("codigo", req.session().attribute("codigo").toString());
				return pebble.render(new ModelAndView(map, "templates/login.pebble"));
			}
		});

		Spark.post("/login", new Route() {
			public Object handle(Request req, Response resp) throws Exception {
				Map<String, String> map = new HashMap<>();
				String email = req.queryParams("email");
				map.put("email", email);
				String senha = req.queryParams("senha");
				boolean encontrado = false;
				try {

					Connection con = DriverManager.getConnection("jdbc:hsqldb:database/files/mochinho", "SA", "");
					String sql = "SELECT id, nome, email, status FROM usuarios WHERE email = ? AND senha = ?";
					PreparedStatement stmt = con.prepareStatement(sql);
					stmt.setString(1, email);
					stmt.setString(2, DigestUtils.md5Hex(senha + SALT));
					ResultSet rs = stmt.executeQuery();
					if (rs.next()) {
						if (rs.getInt("status") == 0) {              
							map.put("erro", "Conta não está ativada, digite o código recebido no e-mail para ativá-la");
							return pebble.render(new ModelAndView(map, "templates/ativar.pebble"));
						}
						req.session().attribute("id", rs.getInt("id"));
						req.session().attribute("usuario", rs.getString("nome"));
						req.session().attribute("email", rs.getString("email"));
						encontrado = true;
					}
					con.close();
				} catch (Exception sqle) {
					throw new RuntimeException(sqle);
				}
				if (encontrado) {
					resp.redirect("/perfil");
				} else {
					map.put("erro", "E-mail e/ou senha não encontrados");
					return pebble.render(new ModelAndView(map, "templates/login.pebble"));
				}
				return "OK";
			}
		});

		Spark.get("/db/create", new Route() {
			public Object handle(Request req, Response resp) throws Exception {
				try {
					// String dbUrl = "jdbc:derby:database;create=true";
					// Connection con = DriverManager.getConnection(dbUrl);          
					String url = "jdbc:hsqldb:database/files/mochinho";
					Connection con = DriverManager.getConnection(url, "SA", "");
					Statement stmt = con.createStatement();
					stmt.execute(
							"CREATE TABLE usuarios (id INTEGER GENERATED BY DEFAULT AS IDENTITY (START WITH 1) NOT NULL PRIMARY KEY, nome VARCHAR(50) NOT NULL, email VARCHAR(50) NOT NULL, senha VARCHAR(32) NOT NULL, status INTEGER DEFAULT 0 NOT NULL, token CHAR(36) NULL)"
							);
					/*
          con.createStatement().executeUpdate(
              "CREATE TABLE usuarios (id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), nome VARCHAR(50) NOT NULL, email VARCHAR(100) NOT NULL, senha VARCHAR(20) NOT NULL)");

          con.createStatement().executeUpdate(
              "CREATE TABLE instituicoes (id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), nome VARCHAR(50) NOT NULL, PRIMARY KEY (id))");
					 */
					con.createStatement().execute(
							"CREATE TABLE areas (id INTEGER GENERATED BY DEFAULT AS IDENTITY (START WITH 1) NOT NULL PRIMARY KEY, nome VARCHAR(20) NOT NULL);"
							);

					con.createStatement().execute(
							"INSERT INTO areas (nome) VALUES ('Artes','Beleza','Comunicação','Informática','Gastronomia','Idiomas','Moda','Saúde')"
							);

					con.createStatement().execute(
							"CREATE TABLE cursos (id INTEGER GENERATED BY DEFAULT AS IDENTITY (START WITH 1) NOT NULL PRIMARY KEY, nome VARCHAR(50) NOT NULL, resumo VARCHAR(100), programa VARCHAR(500), vagas INTEGER NOT NULL, data_inicio DATE NOT NULL, data_termino DATE NOT NULL, dias VARCHAR(28) NOT NULL, horario_inicio TIME NOT NULL, horario_termino TIME NOT NULL, carga_horaria INTEGER NOT NULL, imagem BLOB, tipo_imagem VARCHAR(3), id_area INTEGER NOT NULL, CONSTRAINT area_fk FOREIGN KEY (id_area) REFERENCES areas (id))"
							);

					con.createStatement().execute(
							"CREATE TABLE inscricoes (id_usuario INTEGER NOT NULL, id_curso INTEGER NOT NULL, concluiu BOOLEAN DEFAULT FALSE NOT NULL, CONSTRAINT inscricao_pk PRIMARY KEY (id_usuario, id_curso))"
							);


					con.close();
					return "OK";
				} catch (Exception sqle) {
					throw new RuntimeException(sqle);
				}
			}
		});

		Spark.get("/db/drop", new Route() {
			public Object handle(Request req, Response resp) throws Exception {
				Map<String, String> map = new HashMap<>();
				try {
					String url = "jdbc:hsqldb:database/files/mochinho";
					Connection con = DriverManager.getConnection(url, "SA", "");
					Statement stmt = con.createStatement();
					stmt.execute("DROP TABLE IF EXISTS inscricoes");
					stmt.execute("DROP TABLE IF EXISTS usuarios");          
					stmt.execute("DROP TABLE IF EXISTS cursos");
					stmt.execute("DROP TABLE IF EXISTS areas");          
					con.close();
				} catch (Exception sqle) {
					throw new RuntimeException(sqle);
				}         
				return "OK";
			}        
		});

		Spark.get("/registro", new Route() {
			public Object handle(Request req, Response resp) throws Exception {
				Map<String, String> map = new HashMap<>();
				return pebble.render(new ModelAndView(map, "templates/registro.pebble"));
			}
		});

		Spark.get("/perfil", new Route() {
			public Object handle(Request req, Response resp) throws Exception {
				Map<String, String> map = new HashMap<>();
				if (req.session().attribute("usuario") == null) {
					Spark.halt(401, "nao autorizado");
				} else {
					map.put("usuario", req.session().attribute("usuario").toString());
					map.put("email", req.session().attribute("email").toString());
				}
				return pebble.render(new ModelAndView(map, "templates/perfil.pebble"));
			}
		});

		Spark.get("/mailer", new Route() {

			@Override
			public Object handle(Request req, Response resp) throws Exception {

				Map<String, String> map = new HashMap<>();
				HtmlEmail mailer = new HtmlEmail();  
				try {           
					mailer.setHostName("smtp.googlemail.com");
					mailer.setSmtpPort(465);
					mailer.setAuthenticator(new DefaultAuthenticator("nao.responda.ifrs.riogrande@gmail.com", 
							System.getenv("COBAIA_MAIL_PASSWORD")));
					mailer.setSSLOnConnect(true);
					mailer.setFrom("nao.responda.ifrs.riogrande@gmail.com");
					mailer.setSubject("[Cobaia] Confirmar seu registro");
					mailer.setHtmlMsg("Olá " + "Marcio" + "<br><br>Confirme sua conta com esse código 12323123 ou, se preferir, clique nesse link: <a href=\"http://localhost:4567/conta/confirmar/12323123\">http://localhost:4567/conta/confirmar/12323123</a> para direcioná-lo diretamente");
					mailer.addTo("marcio.torres@riogrande.ifrs.edu.br");
					mailer.send();          
				} catch (EmailException e) {  
					throw new RuntimeException(e);
				}   
				return "OK";
			}
		});


		Spark.get("/reenviar", new Route() {

			@Override
			public Object handle(Request req, Response resp) throws Exception {
				Map<String, String> map = new HashMap<>();
				// map.put("info", "Você precisa digitar o código recebido por e-mail para ativar sua conta");
				return pebble.render(new ModelAndView(map, "templates/reenviar.pebble"));
			}
		});

		Spark.post("/reenviar", new Route() {

			@Override
			public Object handle(Request req, Response resp) throws Exception {
				Map<String, String> map = new HashMap<>();
				String email = req.queryParams("email");
				String nome = null;
				map.put("email", email);

				if (!email.matches("[\\w._]+@\\w+(\\.\\w+)+")) {
					map.put("erro", "E-mail inválido, ele deve ter o formato de usuario@provedor");         
					return pebble.render(new ModelAndView(map, "templates/reenviar.pebble"));
				}

				try (Connection con = DriverManager.getConnection("jdbc:hsqldb:database/files/mochinho")) {

					String uid = UUID.randomUUID().toString().split("-")[0];

					con.setAutoCommit(false);

					String sql = "SELECT status, nome FROM usuarios WHERE email = ?";
					PreparedStatement stmt = con.prepareStatement(sql);
					stmt.setString(1, email);          
					ResultSet rs = stmt.executeQuery();
					if (rs.next()) {
						nome = rs.getString("nome");
						if (rs.getInt("status") > 0) {
							map.put("info", "Esta conta já está ativada, você pode fazer o login.");
							return pebble.render(new ModelAndView(map, "templates/login.pebble"));
						}
					} else {
						map.put("erro", "Este e-mail não existe no nosso sistema, você pode fazer o cadastro.");
						return pebble.render(new ModelAndView(map, "templates/reenviar.pebble"));
					}


					HtmlEmail mailer = new HtmlEmail();
					mailer.setHostName("smtp.googlemail.com");
					mailer.setSmtpPort(465);
					mailer.setAuthenticator(new DefaultAuthenticator("nao.responda.ifrs.riogrande@gmail.com", System.getenv("COBAIA_MAIL_PASSWORD")));
					mailer.setSSLOnConnect(true);
					mailer.setFrom("nao.responda.ifrs.riogrande@gmail.com");
					mailer.setSubject("[Cobaia] Confirmar seu registro");
					mailer.setHtmlMsg("Olá " + nome + "<br><br>Confirme sua conta com esse código: " + uid + " ou, se preferir, clique nesse link: <a href=\"http://localhost:4567/ativar/" + uid + "\">http://localhost:4567/ativar/" + uid + "</a> para direcioná-lo diretamente");
					mailer.addTo(email);
					mailer.send();  

					con.commit();

					return pebble.render(new ModelAndView(map, "templates/ativar.pebble"));

				} catch (Throwable  ex) {
					if (ex instanceof UnknownHostException) {
						map.put("erro", "O provedor deste e-mail não foi encontrado, confira o endereço por favor");
						return pebble.render(new ModelAndView(map, "templates/reenviar.pebble"));
					}
					throw new RuntimeException(ex);
				}
			}
		});

		Spark.get("/ativar", new Route() {

			@Override
			public Object handle(Request req, Response resp) throws Exception {
				Map<String, String> map = new HashMap<>();
				map.put("info", "Você precisa digitar o código recebido por e-mail para ativar sua conta");
				return pebble.render(new ModelAndView(map, "templates/ativar.pebble"));
			}
		});

		Spark.post("/ativar", new Route() {

			@Override
			public Object handle(Request req, Response resp) throws Exception {
				Map<String, String> map = new HashMap<>();

				String codigo = req.queryParams("codigo");
				String email = req.queryParams("email");
				int ativado = 0;

				try (Connection con = DriverManager.getConnection("jdbc:hsqldb:database/files/mochinho", "SA", "")) {

					String sql = "UPDATE usuarios SET status = 1, token = NULL "
							+ "WHERE token = ? AND email = ? AND status = 0";
					PreparedStatement stmt = con.prepareStatement(sql);
					stmt.setString(1, codigo);
					stmt.setString(2, email);
					// BUG: não está vindo o nro de rows atualizadas (hsql?)
					ativado = stmt.executeUpdate();          
					con.close();
				} catch (Exception e) {
					throw new RuntimeException(e);
				} 

				if (ativado > 0) {
					map.put("info", "Sua conta foi ativada! Entre com seu e-mail e senha para fazer o login.");
					return pebble.render(new ModelAndView(map, "templates/login.pebble"));
				} else {
					map.put("erro", "Código não encontrado. Talvez você já tenha ativado sua conta. Tente fazer o login." + ativado);
					return pebble.render(new ModelAndView(map, "templates/ativar.pebble"));
				}
			}
		});

		Spark.get("/ativar/:codigo", new Route() {

			@Override
			public Object handle(Request req, Response resp) throws Exception {
				Map<String, String> map = new HashMap<>();
				String email = null;
				String codigo = req.params("codigo");

				try {
					String url = "jdbc:hsqldb:database/files/mochinho";
					Connection con = DriverManager.getConnection(url, "SA", "");
					String sql = "SELECT email FROM usuarios WHERE token = ? AND status = 0";
					PreparedStatement stmt = con.prepareStatement(sql);
					stmt.setString(1, codigo);
					ResultSet rs = stmt.executeQuery();
					if (rs.next()) {
						email = rs.getString("email");
					}
					con.close();
				} catch (Exception sqle) {
					throw new RuntimeException(sqle);
				}   

				if (email == null) {
					map.put("erro", "Esta conta já foi ativada, tente fazer o login");
				} else {
					map.put("email", email);
				}

				map.put("codigo", codigo);
				return pebble.render(new ModelAndView(map, "templates/ativar.pebble"));
			}
		});

		Spark.post("/registro", new Route() {
			public Object handle(Request req, Response resp) throws Exception {
				Map<String, Object> map = new HashMap<>();

				Usuario usuario = new Usuario(req.queryParams("nome"), req.queryParams("email"), req.queryParams("senha"));

				String senha2 = req.queryParams("senha2");

				map.put("registro", usuario);

				usuario.validate();

				if (!usuario.isValid()) {
					map.put("erros", usuario.getErrors());
					return pebble.render(new ModelAndView(map, "templates/registro.pebble"));
				}

				if (!usuario.getSenha().equals(senha2)) {
					Map<String, String> erros = new HashMap<>();
					erros.put("senha", "As senhas não conferem");
					map.put("erros", erros);
					return pebble.render(new ModelAndView(map, "templates/registro.pebble"));
				}

				try (Connection con = DriverManager.getConnection("jdbc:hsqldb:database/files/mochinho")) {

					con.setAutoCommit(false);

					String token = UUID.randomUUID().toString().split("-")[0];

					PreparedStatement stmt = con.prepareStatement("SELECT status FROM usuarios WHERE email = ?");
					stmt.setString(1, usuario.getEmail());
					ResultSet rs = stmt.executeQuery();
					if (rs.next()) {
						Map<String, String> erros = new HashMap<>();
						erros.put("erro", "Este e-mail já está cadastrado.");         
						map.put("erros", erros);
						return pebble.render(new ModelAndView(map, "templates/registro.pebble"));
					}

					String sql = "INSERT INTO usuarios (nome, email, senha, token) VALUES (?, ?, ?, ?)";
					stmt = con.prepareStatement(sql);
					stmt.setString(1, usuario.getNome());
					stmt.setString(2, usuario.getEmail());
					stmt.setString(3, DigestUtils.md5Hex(usuario.getSenha() + SALT));
					stmt.setString(4, token);
					stmt.execute();

					HtmlEmail mailer = new HtmlEmail();  
					mailer.setHostName("smtp.googlemail.com");
					mailer.setSmtpPort(465);
					mailer.setAuthenticator(new DefaultAuthenticator("nao.responda.ifrs.riogrande@gmail.com", System.getenv("COBAIA_MAIL_PASSWORD")));
					mailer.setSSLOnConnect(true);
					mailer.setFrom("nao.responda.ifrs.riogrande@gmail.com");
					mailer.setSubject("[Cobaia] Confirmar seu registro");
					mailer.setHtmlMsg("Olá " + usuario.getNome() + "<br><br>Confirme sua conta com esse código: " + token + " ou, se preferir, clique nesse link: <a href=\"http://localhost:4567/ativar/" + token + "\">http://localhost:4567/ativar/" + token + "</a> para direcioná-lo diretamente");
					mailer.addTo(usuario.getEmail());
					mailer.send();  

					con.commit();

					resp.redirect("/ativar");
					return "OK";
				} catch (Exception  ex) {
					if (ex instanceof UnknownHostException) {
						Map<String, String> erros = new HashMap<>();
						erros.put("erro", "O provedor deste e-mail não foi encontrado, confira o endereço por favor");
						map.put("erros", erros);

						return pebble.render(new ModelAndView(map, "templates/registro.pebble"));
					}
					throw new RuntimeException(ex);
				}
			}
		});

		Spark.get("/cursos", new Route() {

			@Override
			public Object handle(Request req, Response resp) throws Exception {
				Map<String, Object> map = new HashMap<>();

				try (Connection con = DriverManager.getConnection("jdbc:hsqldb:database/files/mochinho", "SA", "")) {

					String sql = "SELECT * FROM cursos LIMIT 10";
					PreparedStatement stmt = con.prepareStatement(sql);          
					ResultSet rs = stmt.executeQuery();
					List<Map<String, Object>> cursos = new ArrayList<>();
					map.put("cursos", cursos);
					while (rs.next()) {
						Map<String, Object> curso = new HashMap<>();
						curso.put("nome", rs.getString("nome"));
						curso.put("id", rs.getInt("id"));
						curso.put("resumo", rs.getString("resumo"));
						cursos.add(curso);
					}

				} catch (Exception e) {
					throw new RuntimeException(e);
				}   

				return pebble.render(new ModelAndView(map, "templates/cursos.pebble"));
			}
		});

		Spark.get("/curso/:id", new Route() {

			@Override
			public Object handle(Request req, Response resp) throws Exception {
				Map<String, Object> map = new HashMap<>();

				/* PASSAR O USUÁRIO SE PRESENTE NA SESSÃO PARA A VIEW */
				if (req.session().attribute("usuario") != null) {          
					map.put("usuario", req.session().attribute("usuario").toString());
					map.put("email", req.session().attribute("email").toString());
				}

				int id = req.params("id") == null ? 0 : Integer.parseInt(req.params("id"));

				try (Connection con = DriverManager.getConnection("jdbc:hsqldb:database/files/mochinho", "SA", "")) {

					String sql = "SELECT c.*, a.nome AS area, (SELECT COUNT(*) FROM inscricoes WHERE id_curso = c.id) AS inscritos FROM cursos AS c JOIN areas AS a ON c.id_area = a.id WHERE c.id = ?";
					PreparedStatement stmt = con.prepareStatement(sql);     
					stmt.setInt(1, id);
					ResultSet rs = stmt.executeQuery();

					if (rs.next()) {
						Map<String, Object> curso = new HashMap<>();
						curso.put("id", rs.getInt("id"));
						curso.put("nome", rs.getString("nome"));            
						curso.put("resumo", rs.getString("resumo"));
						curso.put("vagas", rs.getInt("vagas"));
						curso.put("cargaHoraria", rs.getInt("carga_horaria"));
						curso.put("dataInicio", 
								rs.getDate("data_inicio").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
						curso.put("dataTermino", rs.getDate("data_Termino").toLocalDate());
						curso.put("dias", rs.getString("dias"));
						curso.put("horarioInicio", rs.getTime("horario_inicio").toLocalTime());
						curso.put("horarioTermino", rs.getTime("horario_termino").toLocalTime());
						curso.put("programa", rs.getString("programa"));
						curso.put("area", rs.getString("area"));
						curso.put("temImagem", rs.getString("tipo_imagem") != null);
						curso.put("inscritos", rs.getInt("inscritos"));
						map.put("curso", curso);
					} else {
						resp.status(404);
						return "Curso " + id + " não encontrado";
					}

				} catch (Exception e) {
					throw new RuntimeException(e);
				} 

				return pebble.render(new ModelAndView(map, "templates/curso.pebble"));
			}
		});

		Spark.get("/inscrever/:id", new Route() {

			@Override
			public Object handle(Request req, Response resp) throws Exception {
				Map<String, Object> map = new HashMap<>();

				int id = req.params("id") == null ? 0 : Integer.parseInt(req.params("id"));

				if (id == 0) {
					resp.status(401);
					return "Código do curso não informado";
				}

				/* PASSAR O USUÁRIO SE PRESENTE NA SESSÃO PARA A VIEW */
				if (req.session().attribute("usuario") == null) {          
					Spark.halt(401, "nao autorizado");
				}


				try (Connection con = DriverManager.getConnection("jdbc:hsqldb:database/files/mochinho")) {          
					String sql = "INSERT INTO inscricoes "
							+ "(id_usuario, id_curso) VALUES (?, ?);";
					PreparedStatement stmt = con.prepareStatement(sql);
					stmt.setInt(1, (Integer) req.session().attribute("id"));
					stmt.setInt(2, id);                    
					stmt.execute();

				} catch (Exception  ex) {          
					throw new RuntimeException(ex);
				}  

				resp.redirect("/curso/" + id);
				//return pebble.render(new ModelAndView(map, "templates/curso.pebble"));
				return "OK";
			}
		});


		Spark.get("/admin", new Route() {

			public Object handle(Request req, Response resp) throws Exception {
				Map<String, String> map = new HashMap<>();

				return pebble.render(new ModelAndView(map, "templates/admin.pebble"));        
			}
		});

		Spark.get("/curso/imagem/:id", new Route() {

			@Override
			public Object handle(Request req, Response resp) throws Exception {
				Map<String, String> map = new HashMap<>();
				int id = req.params("id") == null ? 0 : Integer.parseInt(req.params("id"));

				try (Connection con = DriverManager.getConnection("jdbc:hsqldb:database/files/mochinho")) {

					String sql = "SELECT imagem, tipo_imagem FROM cursos WHERE id = ?";
					PreparedStatement stmt = con.prepareStatement(sql);
					stmt.setInt(1, id);          
					ResultSet rs = stmt.executeQuery();
					if (rs.next()) {
						String contentType = "image/" + (rs.getString("tipo_imagem").equals("jpeg") ? "jpg" : rs.getString("tipo_imagem"));
						InputStream inputStream = rs.getBinaryStream("imagem");   
						OutputStream outputStream = resp.raw().getOutputStream();
						resp.raw().setContentType(contentType);      
						IOUtils.copy(inputStream, outputStream);
						return null;
					} else {
						resp.status(404);
						return "Imagem " + id + " não encontrada";
					}                   
				} catch (Exception  ex) {          
					throw new RuntimeException(ex);
				}
			}
		});

		Spark.post("/admin/curso/novo", new Route() {

			public Object handle(Request req, Response resp) throws Exception {
				Map<String, String> map = new HashMap<>();
				req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
				String nome = req.queryParams("nome");
				if (nome == null) {
					map.put("erro", "Nome não informado");
					return pebble.render(new ModelAndView(map, "templates/admin.curso.novo.pebble"));
				}
				String resumo = req.queryParams("resumo");
				if (resumo == null) {
					map.put("erro", "Resumo não informado");
					return pebble.render(new ModelAndView(map, "templates/admin.curso.novo.pebble"));
				}
				int vagas = req.queryMap("vagas").integerValue();
				if (vagas < 1) {
					map.put("erro", "Quantitativo de vagas não informado");
					return pebble.render(new ModelAndView(map, "templates/admin.curso.novo.pebble"));
				}
				int cargaHoraria = req.queryMap("carga_horaria").integerValue();
				if (cargaHoraria < 1) {
					map.put("erro", "Carga horária não informada");
					return pebble.render(new ModelAndView(map, "templates/admin.curso.novo.pebble"));
				}
				if (req.queryParams("data_inicio") == null) {
					map.put("erro", "Dada de início não informada");
					return pebble.render(new ModelAndView(map, "templates/admin.curso.novo.pebble"));
				}
				Date dataInicio =  ISODateFormat.parse(req.queryParams("data_inicio"));
				if (req.queryParams("data_termino") == null) {
					map.put("erro", "Dada de término não informada");
					return pebble.render(new ModelAndView(map, "templates/admin.curso.novo.pebble"));
				}
				Date dataTermino =  ISODateFormat.parse(req.queryParams("data_termino"));        
				String dias = String.join(", ",req.queryParamsValues("dias"));
				if (req.queryParams("horario_inicio") == null) {
					map.put("erro", "Horário de início não informado");
					return pebble.render(new ModelAndView(map, "templates/admin.curso.novo.pebble"));
				}
				Date horaInicio = ISOTimeFormat.parse(req.queryParams("horario_inicio"));
				if (req.queryParams("horario_termino") == null) {
					map.put("erro", "Horário de término não informado");
					return pebble.render(new ModelAndView(map, "templates/admin.curso.novo.pebble"));
				}
				Date horaTermino = ISOTimeFormat.parse(req.queryParams("horario_termino"));
				String programa = req.queryParams("programa");
				Part imagem = req.raw().getPart("imagem");
				InputStream imagemInputStream = null;
				String tipoImagem = null;
				if (imagem.getSize() > 0) {
					imagemInputStream = imagem.getInputStream();
					tipoImagem = imagem.getContentType().split("/")[1];
					//Image reader = new Image(imagemInputStream);
					// return reader.getWidth() + "/" + reader.getHeight();
				}

				if (req.queryParams("area") == null) {
					map.put("erro", "Área não selecionada de término não informado");
					return pebble.render(new ModelAndView(map, "templates/admin.curso.novo.pebble"));
				}

				int idArea = req.queryMap("area").integerValue();

				try (Connection con = DriverManager.getConnection("jdbc:hsqldb:database/files/mochinho")) {          
					String sql = "INSERT INTO cursos "
							+ "(nome, resumo, vagas, carga_horaria, data_inicio, data_termino, dias, horario_inicio, horario_termino, programa, imagem, tipo_imagem, id_area) "
							+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
					PreparedStatement stmt = con.prepareStatement(sql);
					stmt.setString(1, nome);
					stmt.setString(2, resumo);
					stmt.setInt(3, vagas);
					stmt.setInt(4, cargaHoraria);
					stmt.setDate(5, new java.sql.Date(dataInicio.getTime()));
					stmt.setDate(6, new java.sql.Date(dataTermino.getTime()));
					stmt.setString(7, dias);
					stmt.setTime(8, new java.sql.Time(horaInicio.getTime()));
					stmt.setTime(9, new java.sql.Time(horaTermino.getTime()));

					if (programa == null || programa.isEmpty()) {
						stmt.setNull(10, Types.VARCHAR);
					} else {
						stmt.setString(10, programa);
					}

					if (imagemInputStream == null) {
						stmt.setNull(11, Types.BLOB);
					} else {
						stmt.setBlob(11, imagemInputStream);
					}

					if (tipoImagem == null) {
						stmt.setNull(12, Types.VARCHAR);
					} else {
						stmt.setString(12, tipoImagem);
					}

					stmt.setInt(13, idArea);

					stmt.execute();

					resp.redirect("/admin");
					return "OK";
				} catch (Exception  ex) {          
					throw new RuntimeException(ex);
				}    
			}
		});

		Spark.get("/admin/curso/novo", new Route() {

			public Object handle(Request req, Response resp) throws Exception {
				Map<String, Object> map = new HashMap<>();
				try (Connection con = DriverManager.getConnection("jdbc:hsqldb:database/files/mochinho")) {          
					String sql = "SELECT id, nome FROM areas ORDER BY nome";
					ResultSet rs = con.prepareStatement(sql).executeQuery();
					List<Area> areas = new ArrayList<>();
					while (rs.next()) {
						Area area = new Area(rs.getString("nome"), rs.getInt("id"));
						areas.add(area);
					}
					map.put("areas", areas);
				} catch (Exception  ex) {          
					throw new RuntimeException(ex);
				} 
				return pebble.render(new ModelAndView(map, "templates/admin.curso.novo.pebble"));
			}
		});

		Spark.get("/admin/cursos", new Route() {

			@Override
			public Object handle(Request req, Response resp) throws Exception {
				Map<String, Object> map = new HashMap<>();

				try (Connection con = DriverManager.getConnection("jdbc:hsqldb:database/files/mochinho", "SA", "")) {

					String sql = "SELECT c.*, a.nome AS area, (SELECT COUNT(*) FROM inscricoes WHERE id_curso = c.id) AS inscritos FROM cursos AS c JOIN areas AS a ON c.id_area = a.id LIMIT 10";
					PreparedStatement stmt = con.prepareStatement(sql);          
					ResultSet rs = stmt.executeQuery();
					List<Curso> cursos = new ArrayList<>();
					map.put("cursos", cursos);
					
					while (rs.next()) {
						Curso curso = new Curso();
						curso.setId(rs.getInt("id"));
						curso.setNome(rs.getString("nome"));            
						curso.setResumo(rs.getString("resumo"));
						curso.setVagas(rs.getInt("vagas"));
						curso.setCargaHoraria(rs.getInt("carga_horaria"));
						curso.setDataInicio(LocalDate.parse(rs.getDate("data_inicio").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), DATE_FORMATTER_BR));
						curso.setDataTermino(rs.getDate("data_Termino").toLocalDate());
						curso.setDias(rs.getString("dias"));
						curso.setHorarioInicio(rs.getTime("horario_inicio").toLocalTime());
						curso.setHorarioTermino(rs.getTime("horario_termino").toLocalTime());
						curso.setPrograma(rs.getString("programa"));
						curso.setArea(new Area(rs.getString("area")));
						curso.setInscritos(rs.getString("inscritos"));
						cursos.add(curso);
					}

				} catch (Exception e) {
					throw new RuntimeException(e);
				}   

				return pebble.render(new ModelAndView(map, "templates/admin.cursos.pebble"));
			}
		});

		Spark.get("/admin/curso/editar/:id", new Route() {

			public Object handle(Request req, Response resp) throws Exception {
				Map<String, Object> map = new HashMap<>();
				int id = req.params("id") == null ? 0 : Integer.parseInt(req.params("id"));

				if (id == 0) {
					resp.status(400);
					return "Código do curso não informado";
				}

				try (Connection con = DriverManager.getConnection("jdbc:hsqldb:database/files/mochinho", "SA", "")) {

					String sql = "SELECT c.*, a.nome AS area, (SELECT COUNT(*) FROM inscricoes WHERE id_curso = c.id) AS inscritos FROM cursos AS c JOIN areas AS a ON c.id_area = a.id WHERE c.id = ?";
					PreparedStatement stmt = con.prepareStatement(sql);     
					stmt.setInt(1, id);
					ResultSet rs = stmt.executeQuery();

					if (rs.next()) {
						Curso curso = new Curso();
						curso.setId(rs.getInt("id"));
						curso.setNome(rs.getString("nome"));            
						curso.setResumo(rs.getString("resumo"));
						curso.setVagas(rs.getInt("vagas"));
						curso.setCargaHoraria(rs.getInt("carga_horaria"));
						curso.setDataInicio(rs.getDate("data_inicio").toLocalDate());
						curso.setDataTermino(rs.getDate("data_Termino").toLocalDate());
						curso.setDias(rs.getString("dias"));
						curso.setHorarioInicio(rs.getTime("horario_inicio").toLocalTime());
						curso.setHorarioTermino(rs.getTime("horario_termino").toLocalTime());
						curso.setPrograma(rs.getString("programa"));
						curso.setArea(new Area(rs.getString("area")));
						//curso.put("temImagem", rs.getString("tipo_imagem") != null);
						curso.setTipoImagem(rs.getString("tipo_imagem"));
						curso.setInscritos(rs.getString("inscritos"));
						map.put("curso", curso);

						rs = con.prepareStatement("SELECT id, nome FROM areas ORDER BY nome").executeQuery();
						List<Area> areas = new ArrayList<Area>();
						while (rs.next()) {
							Area area = new Area(rs.getString("nome"), rs.getInt("id"));
							areas.add(area);
						}
						map.put("areas", areas);

					} else {
						resp.status(404);
						return "Curso " + id + " não encontrado";
					}

				} catch (Exception e) {
					throw new RuntimeException(e);
				} 
				return pebble.render(new ModelAndView(map, "templates/admin.cursos.editar.pebble"));
			}
		});

		Spark.post("/admin/curso/editar/:id", new Route() {

			public Object handle(Request req, Response resp) throws Exception {

				int id = req.params("id") == null ? 0 : Integer.parseInt(req.params("id"));

				if (id == 0) {
					resp.status(400);
					return "Código do curso não informado";
				}

				Map<String, String> map = new HashMap<>();
				req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
				String nome = req.queryParams("nome");
				if (nome == null) {
					map.put("erro", "Nome não informado");
					return pebble.render(new ModelAndView(map, "templates/admin.curso.editar.pebble"));
				}
				String resumo = req.queryParams("resumo");
				if (resumo == null) {
					map.put("erro", "Resumo não informado");
					return pebble.render(new ModelAndView(map, "templates/admin.curso.editar.pebble"));
				}
				int vagas = req.queryMap("vagas").integerValue();
				if (vagas < 1) {
					map.put("erro", "Quantitativo de vagas não informado");
					return pebble.render(new ModelAndView(map, "templates/admin.curso.editar.pebble"));
				}
				int cargaHoraria = req.queryMap("carga_horaria").integerValue();
				if (cargaHoraria < 1) {
					map.put("erro", "Carga horária não informada");
					return pebble.render(new ModelAndView(map, "templates/admin.curso.editar.pebble"));
				}
				if (req.queryParams("data_inicio") == null) {
					map.put("erro", "Dada de início não informada");
					return pebble.render(new ModelAndView(map, "templates/admin.curso.editar.pebble"));
				}
				Date dataInicio =  ISODateFormat.parse(req.queryParams("data_inicio"));
				if (req.queryParams("data_termino") == null) {
					map.put("erro", "Dada de término não informada");
					return pebble.render(new ModelAndView(map, "templates/admin.curso.editar.pebble"));
				}
				Date dataTermino =  ISODateFormat.parse(req.queryParams("data_termino"));        
				String dias = String.join(", ",req.queryParamsValues("dias"));
				if (req.queryParams("horario_inicio") == null) {
					map.put("erro", "Horário de início não informado");
					return pebble.render(new ModelAndView(map, "templates/admin.curso.editar.pebble"));
				}
				Date horaInicio = ISOTimeFormat.parse(req.queryParams("horario_inicio"));
				if (req.queryParams("horario_termino") == null) {
					map.put("erro", "Horário de término não informado");
					return pebble.render(new ModelAndView(map, "templates/admin.curso.editar.pebble"));
				}
				Date horaTermino = ISOTimeFormat.parse(req.queryParams("horario_termino"));
				String programa = req.queryParams("programa");
				Part imagem = req.raw().getPart("imagem");
				InputStream imagemInputStream = null;
				String tipoImagem = null;
				if (imagem.getSize() > 0) {
					imagemInputStream = imagem.getInputStream();
					tipoImagem = imagem.getContentType().split("/")[1];
					//Image reader = new Image(imagemInputStream);
					// return reader.getWidth() + "/" + reader.getHeight();
				}

				if (req.queryParams("area") == null) {
					map.put("erro", "Área não selecionada de término não informado");
					return pebble.render(new ModelAndView(map, "templates/admin.curso.editar.pebble"));
				}

				int idArea = req.queryMap("area").integerValue();

				try (Connection con = DriverManager.getConnection("jdbc:hsqldb:database/files/mochinho")) {          
					String sql = "UPDATE cursos "
							+ "SET nome = ?, resumo = ?, vagas = ?, carga_horaria = ?, data_inicio = ?, data_termino = ?, dias = ?, horario_inicio = ?, horario_termino = ?, programa = ?, imagem = ?, tipo_imagem = ?, id_area = ? "
							+ "WHERE id = ?";
					PreparedStatement stmt = con.prepareStatement(sql);
					stmt.setString(1, nome);
					stmt.setString(2, resumo);
					stmt.setInt(3, vagas);
					stmt.setInt(4, cargaHoraria);
					stmt.setDate(5, new java.sql.Date(dataInicio.getTime()));
					stmt.setDate(6, new java.sql.Date(dataTermino.getTime()));
					stmt.setString(7, dias);
					stmt.setTime(8, new java.sql.Time(horaInicio.getTime()));
					stmt.setTime(9, new java.sql.Time(horaTermino.getTime()));

					if (programa == null || programa.isEmpty()) {
						stmt.setNull(10, Types.VARCHAR);
					} else {
						stmt.setString(10, programa);
					}

					if (imagemInputStream == null) {
						stmt.setNull(11, Types.BLOB);
					} else {
						stmt.setBlob(11, imagemInputStream);
					}

					if (tipoImagem == null) {
						stmt.setNull(12, Types.VARCHAR);
					} else {
						stmt.setString(12, tipoImagem);
					}

					stmt.setInt(13, idArea);

					stmt.setInt(14, id);

					stmt.execute();

					resp.redirect("/admin");
					return "OK";
				} catch (Exception  ex) {          
					throw new RuntimeException(ex);
				}    
			}
		});

		Spark.get("/admin/curso/:id", new Route() {

			public Object handle(Request req, Response resp) throws Exception {
				Map<String, Object> map = new HashMap<>();
				int id = req.params("id") == null ? 0 : Integer.parseInt(req.params("id"));

				if (id == 0) {
					resp.status(400);
					return "Código do curso não informado";
				}

				try (Connection con = DriverManager.getConnection("jdbc:hsqldb:database/files/mochinho", "SA", "")) {

					String sql = "SELECT c.*, a.nome AS area, (SELECT COUNT(*) FROM inscricoes WHERE id_curso = c.id) AS inscritos FROM cursos AS c JOIN areas AS a ON c.id_area = a.id WHERE c.id = ?";
					PreparedStatement stmt = con.prepareStatement(sql);     
					stmt.setInt(1, id);
					ResultSet rs = stmt.executeQuery();

					if (rs.next()) {
						Map<String, Object> curso = new HashMap<>();
						curso.put("id", rs.getInt("id"));
						curso.put("nome", rs.getString("nome"));            
						curso.put("resumo", rs.getString("resumo"));
						curso.put("vagas", rs.getInt("vagas"));
						curso.put("cargaHoraria", rs.getInt("carga_horaria"));
						curso.put("dataInicio", rs.getDate("data_inicio").toLocalDate());
						curso.put("dataTermino", rs.getDate("data_Termino").toLocalDate());
						curso.put("dias", rs.getString("dias"));
						curso.put("horarioInicio", rs.getTime("horario_inicio").toLocalTime());
						curso.put("horarioTermino", rs.getTime("horario_termino").toLocalTime());
						curso.put("programa", rs.getString("programa"));
						curso.put("area", rs.getString("area"));
						curso.put("temImagem", rs.getString("tipo_imagem") != null);
						curso.put("inscritos", rs.getString("inscritos"));
						map.put("curso", curso);

						stmt = con.prepareStatement("SELECT u.nome AS aluno FROM usuarios u JOIN inscricoes i ON u.id = i.id_usuario WHERE i.id_curso = ?");
						stmt.setInt(1, id);
						rs = stmt.executeQuery();
						List<String> alunos = new ArrayList<>();
						while (rs.next()) {
							alunos.add(rs.getString("aluno"));
						}
						map.put("alunos", alunos);

					} else {
						resp.status(404);
						return "Curso " + id + " não encontrado";
					}

				} catch (Exception e) {
					throw new RuntimeException(e);
				} 
				return pebble.render(new ModelAndView(map, "templates/admin.curso.pebble"));
			}
		});

	}

}
