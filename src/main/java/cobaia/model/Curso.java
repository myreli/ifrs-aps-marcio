package cobaia.model;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * @author myreli
 *
 */
public class Curso extends AbstractModel {

	private String 		nome;
	private String 		resumo;
	private String		programa;
	private Integer 	vagas;
	private LocalDate	dataInicio;
	private LocalDate	dataTermino;
	private String		dias;
	private LocalTime	horarioInicio;
	private LocalTime	horarioTermino;
	private Integer		cargaHoraria;
	private InputStream	imagem;
	private String		tipoImagem;
	private Area		area;
	private String		inscritos;
		
	
	/**
	 * @param nome
	 * @param resumo
	 * @param programa
	 * @param vagas
	 * @param dataInicio
	 * @param dataTermino
	 * @param horarioInicio
	 * @param horarioTermino
	 * @param cargaHoraria
	 * @param imagem
	 * @param tipoImagem
	 * @param area
	 */
	public Curso(String nome, String resumo, String programa, Integer vagas, LocalDate dataInicio, LocalDate dataTermino, String dias, LocalTime horarioInicio, LocalTime horarioTermino, 
			Integer cargaHoraria, InputStream imagem, String tipoImagem, Area area) {
		this.nome 			= nome;
		this.resumo 		= resumo;
		this.programa 		= programa;
		this.vagas 			= vagas;
		this.dataInicio 	= dataInicio;
		this.dataTermino 	= dataTermino;
		this.dias			= dias;
		this.horarioInicio 	= horarioInicio;
		this.horarioTermino = horarioTermino;
		this.cargaHoraria 	= cargaHoraria;
		this.imagem 		= imagem;
		this.tipoImagem 	= tipoImagem;
		this.area 			= area;
	}
	
	public Curso(String nome, String resumo, String programa, Integer vagas, LocalDate dataInicio, LocalDate dataTermino, String dias, LocalTime horarioInicio, LocalTime horarioTermino, 
			Integer cargaHoraria, InputStream imagem, String tipoImagem, Area area, Integer id) {
		
		this(nome, resumo, programa, vagas, dataInicio, dataTermino, dias, horarioInicio, horarioTermino, cargaHoraria, imagem, tipoImagem, area);
		this.id = id;
	}
	
	
	/**
	 * 
	 */
	public Curso() { }
	

	/* (non-Javadoc)
	 * @see cobaia.modelo.AbstractModel#validate()
	 */
	@Override
	public void validate() {
		
		checkEmpty("nome", nome);
		
		checkEmpty("resumo", resumo);
		
		checkQuantity("vagas", vagas);
		
		checkQuantity("cargaHoraria", cargaHoraria);
		
		checkDate("dataInicio", dataInicio);
		
		checkDate("dataTermino", dataTermino);
		
		checkTime("horarioInicio", horarioInicio);
		
		checkTime("horarioTermino", horarioTermino);
		
		if(!checkEmpty("area", area))
			area.validate();

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
	 * @return the resumo
	 */
	public String getResumo() {
		return resumo;
	}

	/**
	 * @param resumo the resumo to set
	 */
	public void setResumo(String resumo) {
		this.resumo = resumo;
	}

	/**
	 * @return the programa
	 */
	public String getPrograma() {
		return programa;
	}

	/**
	 * @param programa the programa to set
	 */
	public void setPrograma(String programa) {
		this.programa = programa;
	}

	/**
	 * @return the vagas
	 */
	public Integer getVagas() {
		return vagas;
	}

	/**
	 * @param vagas the vagas to set
	 */
	public void setVagas(Integer vagas) {
		this.vagas = vagas;
	}

	/**
	 * @return the dataInicio
	 */
	public LocalDate getDataInicio() {
		return dataInicio;
	}

	/**
	 * @param dataInicio the dataInicio to set
	 */
	public void setDataInicio(LocalDate dataInicio) {
		this.dataInicio = dataInicio;
	}

	/**
	 * @return the dataTermino
	 */
	public LocalDate getDataTermino() {
		return dataTermino;
	}

	/**
	 * @param dataTermino the dataTermino to set
	 */
	public void setDataTermino(LocalDate dataTermino) {
		this.dataTermino = dataTermino;
	}

	/**
	 * @return the horarioInicio
	 */
	public LocalTime getHorarioInicio() {
		return horarioInicio;
	}

	/**
	 * @param horarioInicio the horarioInicio to set
	 */
	public void setHorarioInicio(LocalTime horarioInicio) {
		this.horarioInicio = horarioInicio;
	}

	/**
	 * @return the horarioTermino
	 */
	public LocalTime getHorarioTermino() {
		return horarioTermino;
	}

	/**
	 * @param horarioTermino the horarioTermino to set
	 */
	public void setHorarioTermino(LocalTime horarioTermino) {
		this.horarioTermino = horarioTermino;
	}

	/**
	 * @return the cargaHoraria
	 */
	public Integer getCargaHoraria() {
		return cargaHoraria;
	}

	/**
	 * @param cargaHoraria the cargaHoraria to set
	 */
	public void setCargaHoraria(Integer cargaHoraria) {
		this.cargaHoraria = cargaHoraria;
	}

	/**
	 * @return the imagem
	 */
	public InputStream getImagem() {
		return imagem;
	}

	/**
	 * @param imagem the imagem to set
	 */
	public void setImagem(InputStream imagem) {
		this.imagem = imagem;
	}

	/**
	 * @return the tipoImagem
	 */
	public String getTipoImagem() {
		return tipoImagem;
	}

	/**
	 * @param tipoImagem the tipoImagem to set
	 */
	public void setTipoImagem(String tipoImagem) {
		this.tipoImagem = tipoImagem;
	}

	/**
	 * @return the dias
	 */
	public String getDias() {
		return dias;
	}

	/**
	 * @param dias the dias to set
	 */
	public void setDias(String dias) {
		this.dias = dias;
	}

	/**
	 * @return the area
	 */
	public Area getArea() {
		return area;
	}

	/**
	 * @param area the area to set
	 */
	public void setArea(Area area) {
		this.area = area;
	}

	/**
	 * @return the inscritos
	 */
	public String getInscritos() {
		return inscritos;
	}

	/**
	 * @param inscritos the inscritos to set
	 */
	public void setInscritos(String inscritos) {
		this.inscritos = inscritos;
	}
	
	public boolean temImagem() {
		return tipoImagem != null;
	}
	
	
	
}
