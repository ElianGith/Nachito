package com.example.nachito;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.ProgressBarTableCell;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HelloController {

    @FXML
    private ComboBox<String> policyComboBox;

    @FXML
    private TextField timerTextField;

    @FXML
    private TextArea cpuTextField;

    @FXML
    private TextArea memoryTextField;

    @FXML
    private TextArea outputTextField;

    @FXML
    private TextField currentPolicyField;

    @FXML
    private TableView<Process> processTable;

    @FXML
    private TableColumn<Process, String> processColumn;

    @FXML
    private TableColumn<Process, Integer> durationColumn;

    @FXML
    private TableColumn<Process, Double> progressColumn;

    @FXML
    private TableView<Process> arrivalTable;

    @FXML
    private TableColumn<Process, String> arrivalProcessColumn;

    @FXML
    private TableColumn<Process, Integer> arrivalTimeColumn;

    @FXML
    private TableColumn<Process, Integer> durationArrivalColumn;

    private ObservableList<Process> processList;
    private ObservableList<Process> arrivalProcessList;

    private Queue<Process> fifoQueue;
    private Stack<Process> lifoStack;

    private ScheduledExecutorService executorService;

    public void initialize() {
        // Inicializar ComboBox con opciones FIFO y LIFO
        policyComboBox.setItems(FXCollections.observableArrayList("FIFO", "LIFO"));

        // Inicializar listas de procesos
        processList = FXCollections.observableArrayList();
        arrivalProcessList = FXCollections.observableArrayList();

        processTable.setItems(processList);
        arrivalTable.setItems(arrivalProcessList);

        // Configurar las columnas de las tablas
        processColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
        progressColumn.setCellValueFactory(new PropertyValueFactory<>("progress"));

        progressColumn.setCellFactory(ProgressBarTableCell.forTableColumn());

        arrivalProcessColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        arrivalTimeColumn.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        durationArrivalColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));

        fifoQueue = new LinkedList<>();
        lifoStack = new Stack<>();
    }

    // Método para ejecutar todos los procesos según la política seleccionada
    public void executeProcesses() {
        // Asegurarnos de que hay procesos pendientes
        if (!fifoQueue.isEmpty() || !lifoStack.isEmpty()) {
            // Mostrar la política seleccionada
            currentPolicyField.setText(policyComboBox.getValue());

            if ("FIFO".equals(policyComboBox.getValue())) {
                executeFIFO();
            } else if ("LIFO".equals(policyComboBox.getValue())) {
                executeLIFO();
            }
        } else {
            outputTextField.appendText("No hay procesos para ejecutar.\n");
        }
    }

    // Método para ejecutar procesos en política FIFO
    private void executeFIFO() {
        // Crear un nuevo executor que ejecutará cada proceso de manera secuencial
        executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(() -> {
            if (!fifoQueue.isEmpty()) {
                Process process = fifoQueue.poll(); // Obtener el siguiente proceso
                Platform.runLater(() -> simulateProcessExecution(process)); // Ejecutar el proceso
            } else {
                executorService.shutdown(); // Detener cuando no hay más procesos
            }
        }, 0, 1, TimeUnit.SECONDS); // Actualizar cada segundo
    }

    // Método para ejecutar procesos en política LIFO
    private void executeLIFO() {
        executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(() -> {
            if (!lifoStack.isEmpty()) {
                Process process = lifoStack.pop(); // Obtener el siguiente proceso
                Platform.runLater(() -> simulateProcessExecution(process)); // Ejecutar el proceso
            } else {
                executorService.shutdown(); // Detener cuando no hay más procesos
            }
        }, 0, 1, TimeUnit.SECONDS); // Actualizar cada segundo
    }

    // Simulación del tiempo de ejecución del proceso y actualización de la barra de progreso, CPU y memoria en tiempo real
    private void simulateProcessExecution(Process process) {
        final int totalDuration = process.getDuration(); // Duración del proceso
        final int[] elapsedTime = {0}; // Usamos un array para simular una variable "final" mutable

        // Actualizar y ejecutar CPU, memoria, y progreso en la UI en tiempo real
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            elapsedTime[0]++; // Incrementamos el tiempo transcurrido cada segundo

            double progress = Math.min(1.0, (double) elapsedTime[0] / totalDuration); // Progreso del proceso

            // Actualización de la UI en el hilo de JavaFX
            Platform.runLater(() -> {
                // Actualizar el progreso del proceso en la tabla
                process.setProgress(progress);
                processTable.refresh();  // REFRESCAR LA TABLA DE PROCESOS

                // Actualizar el Timer
                timerTextField.setText(String.valueOf(elapsedTime[0])); // Actualizamos el timer con el tiempo transcurrido

                // Mostrar el proceso en CPU y Memoria
                cpuTextField.appendText("Ejecutando: " + process.getName() + "\n");
                memoryTextField.appendText("Memoria usada por " + process.getName() + "\n");

                // Cuando el proceso haya completado su ejecución
                if (progress >= 1.0) {
                    outputTextField.appendText(process.getName() + " terminado\n");

                    // Limpiar el Timer y detener el executor para este proceso
                    timerTextField.clear();
                    executor.shutdown();
                }
            });
        }, 0, 1, TimeUnit.SECONDS); // Actualización cada segundo
    }

    // Método para reiniciar la simulación
    public void resetSimulation() {
        // Limpiar todas las listas y áreas de texto
        processList.clear();
        arrivalProcessList.clear();
        fifoQueue.clear();
        lifoStack.clear();
        timerTextField.clear();
        cpuTextField.clear();
        memoryTextField.clear();
        outputTextField.clear();

        // REFRESCAR LAS TABLAS
        processTable.refresh();
        arrivalTable.refresh();
    }

    // Método para agregar un proceso aleatorio
    public void addRandomProcess() {
        Random random = new Random();
        String processName = "P" + (arrivalProcessList.size() + 1);
        int arrivalTime = random.nextInt(10);  // Tiempo de llegada aleatorio entre 0 y 9
        int duration = random.nextInt(10) + 1; // Duración aleatoria entre 1 y 10

        Process newProcess = new Process(processName, arrivalTime, duration);
        arrivalProcessList.add(newProcess);

        // Añadir el proceso a la cola o pila según la política seleccionada
        if ("FIFO".equals(policyComboBox.getValue())) {
            fifoQueue.add(newProcess);
        } else if ("LIFO".equals(policyComboBox.getValue())) {
            lifoStack.push(newProcess);
        }

        // Añadir también el proceso a la tabla de procesos
        processList.add(newProcess);

        // REFRESCAR LAS TABLAS
        processTable.refresh();
        arrivalTable.refresh();
    }
}
