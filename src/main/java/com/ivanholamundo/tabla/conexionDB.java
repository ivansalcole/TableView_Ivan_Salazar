package com.ivanholamundo.tabla;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class conexionDB {

    // Los datos de conexión se leen de src/main/resources/db.properties,
    // así no quedan escritos (hardcodeados) en el código fuente.
    private static final String ARCHIVO_PROPIEDADES = "/com/ivanholamundo/tabla/db.properties";

    private static Connection con;
    private static String ultimoError;

    // Constructor privado: no se puede instanciar, solo se usa de forma estática
    private conexionDB() {
    }

    private static Properties cargarPropiedades() throws IOException {
        Properties props = new Properties();
        try (InputStream in = conexionDB.class.getResourceAsStream(ARCHIVO_PROPIEDADES)) {
            if (in == null) {
                throw new IOException("No se encuentra " + ARCHIVO_PROPIEDADES
                        + " en resources. Crea el archivo con db.url, db.usuario y db.password.");
            }
            props.load(in);
        }
        return props;
    }

    public static Connection getConexion() {
        try {
            if (con == null || con.isClosed()) {
                Properties props = cargarPropiedades();
                String url = props.getProperty("db.url");
                String usuario = props.getProperty("db.usuario");
                String password = props.getProperty("db.password");
                con = DriverManager.getConnection(url, usuario, password);
                ultimoError = null;
            }
        } catch (IOException | SQLException e) {
            ultimoError = e.getClass().getSimpleName() + ": " + e.getMessage();
            System.err.println("Error al conectar con la base de datos:");
            e.printStackTrace();
            con = null;
        }
        return con;
    }

    // Detalle del último fallo de conexión, para mostrarlo en la UI
    public static String getUltimoError() {
        return ultimoError;
    }

    public static void cerrarConexion() {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }
}