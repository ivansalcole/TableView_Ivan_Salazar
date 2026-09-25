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
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

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

        personas.add(new Persona(siguienteId++, nombre, apellido, fecha));

        tfNombre.clear();
        tfApellido.clear();
        dpfecha.setValue(null);
        tfNombre.requestFocus();
    }

    @FXML
    void eliminar(ActionEvent event) {
        Persona seleccionada = tablaPersonas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAviso("Selecciona una fila de la tabla para eliminarla.");
            return;
        }
        personas.remove(seleccionada);
        eliminadas.push(seleccionada);
    }

    @FXML
    void restaurar(ActionEvent event) {
        if (eliminadas.isEmpty()) {
            mostrarAviso("No hay nada que restaurar.");
            return;
        }
        Persona p = eliminadas.pop();
        // Se vuelve a colocar en su sitio según el ID
        int pos = 0;
        while (pos < personas.size() && personas.get(pos).getId() < p.getId()) pos++;
        personas.add(pos, p);
        tablaPersonas.getSelectionModel().select(p);
    }

    private void mostrarAviso(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
