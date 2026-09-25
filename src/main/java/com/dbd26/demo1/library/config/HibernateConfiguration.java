package com.dbd26.demo1.library.config;

import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.hibernate.HibernateTransactionManager;
import org.springframework.orm.jpa.hibernate.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

/*
 Configuracion de Hibernate.
 */
@Configuration
@EnableTransactionManagement
public class HibernateConfiguration {

    /*
     SessionFactory: el objeto central de Hibernate. Hay UNO por aplicacion, es costoso de crear (lee el mapeo
     de todas las entidades) Fabrica las Session, que son las unidades de trabajo
     */
    @Bean
    public LocalSessionFactoryBean sessionFactory(DataSource dataSource) {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        // Paquete donde buscar las clases anotadas con @Entity
        sessionFactory.setPackagesToScan("com.dbd26.demo1.library.model");
        sessionFactory.setHibernateProperties(hibernateProperties());
        return sessionFactory;
    }

    /*
     Gestor de transacciones: es lo que hace funcionar a @Transactional. Al entrar a un metodo anotado abre una
     Session y comienza la transaccion; al salir hace commit (o rollback si hubo excepcion) y cierra la Session.
     */
    @Bean
    public PlatformTransactionManager transactionManager(SessionFactory sessionFactory) {
        return new HibernateTransactionManager(sessionFactory);
    }

    private Properties hibernateProperties() {
        Properties properties = new Properties();
        // Define como Hibernate manipula el esquema de base de datos al iniciar la aplicacion:
        //   none        -> no toca la base (ideal para produccion)
        //   create      -> elimina y crea el esquema
        //   create-drop -> crea el esquema y lo destruye al cerrar (ideal para pruebas)
        //   validate    -> comprueba que el esquema coincida con el mapeo, si no falla
        //   update      -> agrega lo que falte sin destruir datos
        properties.setProperty("hibernate.hbm2ddl.auto", "create");
        // Mostrar en la consola todas las SQL que se ejecutan, formateadas
        properties.setProperty("hibernate.show_sql", "true");
        properties.setProperty("hibernate.format_sql", "true");
        // El dialecto (MySQL, H2, ...) se detecta automaticamente a partir de la conexion; no hace falta declararlo.
        return properties;
    }
}
