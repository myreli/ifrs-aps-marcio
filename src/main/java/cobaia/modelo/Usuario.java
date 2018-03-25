package cobaia.modelo;

/**
 * @author myreli
 *
 */

public class Usuario extends AbstractModel {

	public enum Status {
		REGISTRADO, ATIVADO;
	}
	
	private String 	nome;
	private String 	email;
	private String 	senha;
	private Status 	status;
	private String 	token;
	
	
	
	/**
	 * @param nome
	 * @param email
	 * @param senha
	 */
	public Usuario(String nome, String email, String senha) {
		this.nome = nome;
		this.email = email;
		this.senha = senha;
	}
	
	/**
	 * @return the nome
	 */ 
	public String getNome() {
		return nome;
	}
	/**
	 * @param nome the nome to set
	 */
	public void setNome(String nome) {
		this.nome = nome;
	}
	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}
	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	/**
	 * @return the senha
	 */
	public String getSenha() {
		return senha;
	}
	/**
	 * @param senha the senha to set
	 */
	public void setSenha(String senha) {
		this.senha = senha;
	}
	/**
	 * @return the status
	 */
	public Status getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(Status status) {
		this.status = status;
	}
	/**
	 * @return the token
	 */
	public String getToken() {
		return token;
	}
	/**
	 * @param token the token to set
	 */
	public void setToken(String token) {
		this.token = token;
	}
	/* (non-Javadoc)
	 * @see cobaia.modelo.AbstractModel#validate()
	 */
	@Override
	public void validate() {
		cleanErrors();
		
		checkLength("nome", nome, 3, 50);
		
		checkMail(email);
		
		checkLength("senha", senha, 5, 50);
		
		/*if (nome.length() < 3 || nome.length() > 50)
          addError("nome", "Nome deve ter entre 3 e 50 caracteres");         
        
        if (!email.matches("[\\w._]+@\\w+(\\.\\w+)+"))
          addError("e-mail", "E-mail inválido, ele deve ter o formato de usuario@provedor");         
        
        if (senha.length() < 5 || senha.length() > 50)
          addError("erro", "A sua senha deve ter entre 5 e 50 caracteres");*/         
	}
	
	
}