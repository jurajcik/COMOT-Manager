package at.ac.tuwien.dsg.comot.test.model;

//import org.neo4j.test.ImpermanentGraphDatabase;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.ImpermanentGraphDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@Transactional
public class AppContextModelTest {

	protected final Logger LOG = LoggerFactory.getLogger(getClass());
	private static final String DB_PATH = "target/data/db";

	@Bean(destroyMethod = "shutdown")
	public GraphDatabaseService graphDatabaseService() {
		// return new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH); 
	
		return new ImpermanentGraphDatabase(); 
	}

}
