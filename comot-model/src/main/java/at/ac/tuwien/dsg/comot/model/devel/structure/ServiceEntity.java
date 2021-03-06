package at.ac.tuwien.dsg.comot.model.devel.structure;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;

import at.ac.tuwien.dsg.comot.model.HasUniqueId;
import at.ac.tuwien.dsg.comot.model.SyblDirective;
import at.ac.tuwien.dsg.comot.recorder.BusinessId;

@XmlSeeAlso({ CloudService.class, ServiceTopology.class, ServiceUnit.class })
@XmlAccessorType(XmlAccessType.FIELD)
@NodeEntity
public abstract class ServiceEntity implements HasUniqueId, Serializable {

	private static final long serialVersionUID = -889982124609754463L;

	@GraphId
	protected Long nodeId;

	@XmlID
	@XmlAttribute
	@BusinessId
	protected String id;
	@XmlAttribute
	protected String name;

	@XmlElementWrapper(name = "Directives")
	@XmlElement(name = "Directive")
	protected Set<SyblDirective> directives = new HashSet<>();

	public ServiceEntity() {
	}

	public ServiceEntity(String id) {
		this.id = id;
	}

	public ServiceEntity(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public void addDirective(SyblDirective directive) {
		if (directives == null) {
			directives = new HashSet<>();
		}
		directives.add(directive);
	}

	// GENERATED METHODS

	public Set<SyblDirective> getDirectives() {
		return directives;
	}

	public void setDirectives(Set<SyblDirective> directives) {
		this.directives = directives;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getNodeId() {
		return nodeId;
	}

	public void setNodeId(Long nodeId) {
		this.nodeId = nodeId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServiceEntity other = (ServiceEntity) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
