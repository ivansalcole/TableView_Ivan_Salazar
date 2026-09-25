package com.ivanholamundo.tabla;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.Deque;

public class HelloController {
    @FXML private Button btAniadir;
    @FXML private Button btEliminar;
    @FXML private Button btRestaurar;
    @FXML private DatePicker dpfecha;
    @FXML private TextField tfApellido;
    @FXML private TextField tfNombre;

    @FXML private TableView<Persona> tablaPersonas;
    @FXML private TableColumn<Persona, Integer> colId;
    @FXML private TableColumn<Persona, String> colNombre;
    @FXML private TableColumn<Persona, String> colApellido;
    @FXML private TableColumn<Persona, LocalDate> colFecha;

    private final ObservableList<Persona> personas = FXCollections.observableArrayList();
    // Personas eliminadas, para poder restaurarlas (la última eliminada sale primero)
    private final Deque<Persona> eliminadas = new ArrayDeque<>();
    private int siguienteId = 1;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaNacimiento"));
        tablaPersonas.setItems(personas);
        cargarPersonas();
    }

    private void cargarPersonas() {
        String sql = "SELECT id, nombre, apellido, fecha_nacimiento FROM personas";
        Connection con = conexionDB.getConexion();
        if (con == null) {
            mostrarAviso("No se ha podido conectar con la base de datos.\n" + conexionDB.getUltimoError());
            return;
        }
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            personas.clear();
            int maxId = 0;
            while (rs.next()) {
                int id = rs.getInt("id");
                Persona p = new Persona(
                        id,
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getDate("fecha_nacimiento").toLocalDate());
                personas.add(p);
                maxId = Math.max(maxId, id);
            }
            siguienteId = maxId + 1;
        } catch (SQLException e) {
            mostrarAviso("Error al cargar los datos: " + e.getMessage());
        }
    }

    @FXML
    void aniadir(ActionEvent event) {
        String nombre = tfNombre.getText() == null ? "" : tfNombre.getText().trim();
        String apellido = tfApellido.getText() == null ? "" : tfApellido.getText().trim();
        LocalDate fecha = dpfecha.getValue();

        if (nombre.isEmpty() || apellido.isEmpty() || fecha == null) {
            mostrarAviso("Rellena el nombre, el apellido y la fecha de nacimiento.");
            return;
        }

        Persona p = new Persona(siguienteId, nombre, apellido, fecha);
        if (!insertarPersona(p)) {
            return;
        }
        personas.add(p);
        siguienteId++;

        tfNombre.clear();
        tfApellido.clear();
        dpfecha.setValue(null);
        tfNombre.requestFocus();
    }

    private boolean insertarPersona(Persona p) {
        String sql = "INSERT INTO personas (id, nombre, apellido, fecha_nacimiento) VALUES (?, ?, ?, ?)";
        Connection con = conexionDB.getConexion();
        if (con == null) {
            mostrarAviso("No se ha podido conectar con la base de datos.\n" + conexionDB.getUltimoError());
            return false;
        }
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, p.getId());
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getApellido());
            ps.setDate(4, java.sql.Date.valueOf(p.getFechaNacimiento()));
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            mostrarAviso("Error al guardar en la base de datos: " + e.getMessage());
            return false;
        }
    }

    @FXML
    void eliminar(ActionEvent event) {
        Persona seleccionada = tablaPersonas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAviso("Selecciona una fila de la tabla para eliminarla.");
            return;
        }
        if (!eliminarPersona(seleccionada)) {
            return;
        }
        personas.remove(seleccionada);
        eliminadas.push(seleccionada);
    }

    private boolean eliminarPersona(Persona p) {
        String sql = "DELETE FROM personas WHERE id = ?";
        Connection con = conexionDB.getConexion();
        if (con == null) {
            mostrarAviso("No se ha podido conectar con la base de datos.\n" + conexionDB.getUltimoError());
            return false;
        }
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, p.getId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            mostrarAviso("Error al eliminar de la base de datos: " + e.getMessage());
            return false;
        }
    }

    @FXML
    void restaurar(ActionEvent event) {
        if (eliminadas.isEmpty()) {
            mostrarAviso("No hay nada que restaurar.");
            return;
        }
        Persona p = eliminadas.pop();
        if (!insertarPersona(p)) {
            eliminadas.push(p);
            return;
        }
        // Se vuelve a colocar en su sitio según el ID
        int pos = 0;
        while (pos < personas.size() && personas.get(pos).getId() < p.getId()) pos++;
        personas.add(pos, p);
        tablaPersonas.getSelectionModel().select(p);
    }

    private void mostrarAviso(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setResizable(true);

        TextArea area = new TextArea(mensaje);
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefWidth(550);
        area.setPrefHeight(180);

        alert.getDialogPane().setContent(area);
        alert.showAndWait();
    }
}