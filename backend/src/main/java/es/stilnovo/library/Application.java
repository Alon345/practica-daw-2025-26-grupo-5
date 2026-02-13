package es.stilnovo.library;
// Es el main de la aplicación, el punto de entrada. Es el encargado de arrancar la aplicación y de cargar el contexto de Spring. Es el encargado de cargar los beans y de configurar la aplicación. Es el encargado de arrancar el servidor web y de escuchar las peticiones. Es el encargado de gestionar el ciclo de vida de la aplicación. 
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
}   