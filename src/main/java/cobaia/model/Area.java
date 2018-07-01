package cobaia.model;

import cobaia.modelo.Area;
import cobaia.modelo.ModelNotFoundException;
import cobaia.persistence.AreaDAO;

/**
 * @author myreli
 *
 */
public class Area extends AbstractModel {

	private String nome;
	
	private AreaDAO dao = new AreaDAO();

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

	@Override
	public void load(int id) throws ModelNotFoundException {
		Area temp = dao.find(id);
		if (temp == null) throw new ModelNotFoundException();
		this.setId(temp.getId());
		this.setNome(temp.getNome());
	}

	@Override
	public void delete() {
		dao.delete(this);
	}

	@Override
	protected void doSave() {
		if (this.isPersistent()) dao.update(this);
		else dao.create(this);
	}
	
	public static Area find(int id) {
		return new AreaDAO().find(id);
	}
}