package cobaia.modelo;

/**
 * @author myreli
 *
 */
public class Area extends AbstractModel {

	private String nome;

	public Area(String nome) {
		this.nome = nome;
	}
	
	public Area(String nome, Integer id) {
		this(nome);
		this.id = id;
	}
		
	/* (non-Javadoc)
	 * @see cobaia.modelo.AbstractModel#validate()
	 */
	@Override
	public void validate() {
		cleanErrors();
		checkLength("nome", getNome(), 1, 20);		
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
}