package org.iconic.project.processing;

import com.google.inject.Inject;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.iconic.ea.data.DataManager;
import org.iconic.ea.data.preprocessing.*;
import org.iconic.project.Displayable;
import org.iconic.project.dataset.DatasetModel;
import org.iconic.workspace.WorkspaceService;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * A controller class for handling the ProcessData view.
 *
 * The ProcessDataController provides a bridge between applying pre-processing options from the GUI and updating
 * the modified data within the DataManager.
 */
@Log4j2
public class ProcessDataController implements Initializable {
    private final WorkspaceService workspaceService;

    // A flag which determines whether the pre-processing checkboxes are disabled by the user
    // or reset within this class. It's used to avoid firing the changeListener's changed()
    // event when manually enabling/disabling checkboxes.
    private boolean resetCheckboxFlag = false;

    @FXML
    private ListView<String> lvFeatures;
    @FXML
    private LineChart<Number, Number> lcDataView;
    @FXML
    private CheckBox cbSmoothData, cbHandleMissingValues, cbRemoveOutliers, cbNormalise, cbOffset;
    @FXML
    private VBox vbSmoothData, vbHandleMissingValues, vbRemoveOutliers, vbNormalise, vbOffset;
    @FXML
    private ComboBox<String> cbHandleMissingValuesOptions;
    @FXML
    private TextField tfNormaliseMin, tfNormaliseMax, tfOffsetValue;

    /**
     * <p>
     * Constructs a new ProcessDataController that attaches an invalidation listener onto the workspace service.
     * </p>
     */
    @Inject
    public ProcessDataController(final WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;

        // Update the workspace whenever the active dataset changes
        InvalidationListener selectionChangedListener = observable -> updateWorkspace();
        getWorkspaceService().activeWorkspaceItemProperty().addListener(selectionChangedListener);
    }

    @Override
    public void initialize(URL arg1, ResourceBundle arg2) {
        updateWorkspace();

        if (lvFeatures != null) {
            // lvFeatures - One of the items in the list is selected and the other objects need to be updates
            lvFeatures.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                // Once a feature is selected, the pre-processing checkboxes are enabled
                enablePreprocessingCheckBoxes();

                // Update the data view and pre-processing text fields
                int selectedIndex = lvFeatures.getSelectionModel().getSelectedIndex();
                featureSelected(selectedIndex);

                // Update the respective checkboxes of the selected feature
                updateCheckBoxes(selectedIndex);
            });
        }

        // A quick way to add a listener for the checkboxes
        addCheckBoxChangeListener(cbSmoothData, vbSmoothData);
        addCheckBoxChangeListener(cbHandleMissingValues, vbHandleMissingValues);
        addCheckBoxChangeListener(cbRemoveOutliers, vbRemoveOutliers);
        addCheckBoxChangeListener(cbNormalise, vbNormalise);
        addCheckBoxChangeListener(cbOffset, vbOffset);

        addComboBoxChangeListener(cbHandleMissingValuesOptions, cbHandleMissingValues);
    }

    /**
     * Adds a ChangeListener to the pre-processing checkboxes to fire the changed() event when the user
     * selects or deselects an option.
     *
     * @param checkbox The selected checkbox
     * @param vbox The pre-processing parameters to be enabled when a checkbox is selected
     */
    private void addCheckBoxChangeListener(CheckBox checkbox, VBox vbox) {
        if (checkbox == null) {
            return;
        }

        checkbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (!resetCheckboxFlag) {
                    Optional<DataManager<Double>> dataManager = getDataManager();

                    if (dataManager.isPresent()) {
                        int selectedIndex = lvFeatures.getSelectionModel().getSelectedIndex();
                        String selectedHeader = dataManager.get().getSampleHeaders().get(selectedIndex);

                        if (newValue) {
                            // Checkbox has been selected
                            addNewTransform(selectedHeader, convertCheckBoxToTransformType(checkbox));
                        } else {
                            // Checkbox has been unselected
                            removeExistingTransform(checkbox);
                        }

                        updateModifiedText(selectedIndex, selectedHeader);
                    }
                }

                // Shows or hides the checkboxes options based on whether it is selected or not
                if (vbox != null) {
                    vbox.setManaged(newValue);
                    vbox.setVisible(newValue);
                }
            }
        });
    }

    /**
     * Adds a ChangeListener to the handle missing values combo box in order to detect when a user
     * selects a new option. It then performs the handle missing values pre-processing.
     *
     * @param combobox The selected combo box
     * @param checkbox The selected check box to identify the transformType
     */
    private void addComboBoxChangeListener(ComboBox combobox, CheckBox checkbox) {
        if (combobox == null) {
            return;
        }

        cbHandleMissingValuesOptions.getSelectionModel().selectFirst();
        combobox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                Optional<DataManager<Double>> dataManager = getDataManager();

                if (dataManager.isPresent()) {
                    removeExistingTransform(checkbox);
                    handleMissingValuesOfDatasetFeature();

                    int selectedIndex = lvFeatures.getSelectionModel().getSelectedIndex();
                    String selectedHeader = dataManager.get().getSampleHeaders().get(selectedIndex);

                    addNewTransform(selectedHeader, convertCheckBoxToTransformType(checkbox));

                    updateModifiedText(selectedIndex, selectedHeader);
                }
            }
        });
    }

    /**
     * Called after a checkbox has been selected. A check is done to determine whether the feature
     * has been modified. If so, a 'modified' keyword is appended to the header and the feature list
     * is updated.
     *
     * @param selectedIndex Index of currently selected feature
     * @param selectedHeader Header of currently selected feature
     */
    private void updateModifiedText(int selectedIndex, String selectedHeader) {
        Optional<DataManager<Double>> dataManager = getDataManager();
        ObservableList items = lvFeatures.getItems();

        if (dataManager.isPresent()) {
            String newHeader = dataManager.get().getSampleHeaders().get(selectedIndex);

            if (dataManager.get().getDataset().get(selectedHeader).isModified()) {
                newHeader += "    modified";
            }

            items.set(selectedIndex, newHeader);

            lvFeatures.setItems(items);
        }
    }

    /**
     * Updates the lcDataView with new dataset values and the pre-processing checkboxes text
     * fields to reflect the currently selected feature.
     *
     * @param selectedIndex The item index that was selected in the ListView
     */
    private void featureSelected(int selectedIndex) {
        // We're updating lcDataView
        if (lcDataView == null) {
            return;
        }

        // Defining a series
        XYChart.Series<Number, Number> series = new XYChart.Series<>();

        // Populating the series with data
        lcDataView.getData().clear();
        Optional<DataManager<Double>> dataManager = getDataManager();

        if (dataManager.isPresent() && selectedIndex >= 0) {
            ArrayList<Number> values = dataManager.get().getSampleColumn(selectedIndex);

            for (int sample = 0; sample < values.size(); sample++) {
                double value = values.get(sample).doubleValue();
                series.getData().add(new XYChart.Data<>(sample, value));
            }
        }
        lcDataView.getData().add(series);

        // Updates the pre-processing methods text fields to reflect the respective header
        if (dataManager.isPresent() && selectedIndex >= 0) {
            String selectedHeader = dataManager.get().getSampleHeaders().get(selectedIndex);
            updatePreprocessingTextFields(selectedHeader);
        }
    }

    /**
     * Gets the current values from the DataManager, applies the Smooth classes functionality to said values,
     * and then stores the new values in DataManager.
     */
    public void smoothDatasetFeature() {
        if (lvFeatures == null) {
            return;
        }

        int selectedIndex = lvFeatures.getSelectionModel().getSelectedIndex();
        if (selectedIndex != -1) {
            Optional<DataManager<Double>> dataManager = getDataManager();

            if (cbSmoothData.isSelected() && dataManager.isPresent()) {
                ArrayList<Number> values = dataManager.get().getSampleColumn(selectedIndex);

                values = Smooth.apply(values);
                dataManager.get().setSampleColumn(selectedIndex, values);
            }
            // Otherwise reset the sample column
            else if (dataManager.isPresent()) {
                dataManager.get().resetSampleColumn(selectedIndex);
            }

            featureSelected(selectedIndex);
        }
    }

    /**
     * Gets the current values from the DataManager, applies the HandleMissingValues classes functionality to
     * said values, and then stores the new values in DataManager.
     */
    public void handleMissingValuesOfDatasetFeature() {
        if (lvFeatures == null) {
            return;
        }

        int selectedIndex = lvFeatures.getSelectionModel().getSelectedIndex();
        if (selectedIndex != -1) {
            Optional<DataManager<Double>> dataManager = getDataManager();

            if (cbHandleMissingValues.isSelected() && dataManager.isPresent()) {
                ArrayList<Number> values = dataManager.get().getSampleColumn(selectedIndex);

                HandleMissingValues.Mode mode = convertComboBoxIndexToMode(cbHandleMissingValuesOptions.getSelectionModel().getSelectedIndex());

                values = HandleMissingValues.apply(values, mode);
                dataManager.get().setSampleColumn(selectedIndex, values);
            }
            // Otherwise reset the sample column
            else if (dataManager.isPresent()) {
                dataManager.get().resetSampleColumn(selectedIndex);
            }

            featureSelected(selectedIndex);
        }
    }

    // TODO: once RemoveOutliers class has been implemented
    public void removeOutliersInDatasetFeature() {
        if (lvFeatures == null) {
            return;
        }

        int selectedIndex = lvFeatures.getSelectionModel().getSelectedIndex();
        if (selectedIndex != -1) {
            Optional<DataManager<Double>> dataManager = getDataManager();

            if (cbRemoveOutliers.isSelected() && dataManager.isPresent()) {
                ArrayList<Number> values = dataManager.get().getSampleColumn(selectedIndex);

                //values = RemoveOutliers.apply(values);

                dataManager.get().setSampleColumn(selectedIndex, values);
            }
            // Otherwise reset the sample column
            else if (dataManager.isPresent()) {
                dataManager.get().resetSampleColumn(selectedIndex);
            }

            featureSelected(selectedIndex);
        }
    }

    /**
     * Gets the current values from the DataManager, applies the Normalise classes functionality to said values,
     * and then stores the new values in DataManager.
     */
    public void normalizeDatasetFeature() {
        if (lvFeatures == null) {
            return;
        }

        int selectedIndex = lvFeatures.getSelectionModel().getSelectedIndex();
        if (selectedIndex != -1) {
            Optional<DataManager<Double>> dataManager = getDataManager();

            if (cbNormalise.isSelected() && dataManager.isPresent()) {
                ArrayList<Number> values = dataManager.get().getSampleColumn(selectedIndex);

                try {
                    double min = Double.parseDouble(tfNormaliseMin.getText());
                    double max = Double.parseDouble(tfNormaliseMax.getText());

                    if (min < max) {
                        values = Normalise.apply(values, min, max);
                        dataManager.get().setSampleColumn(selectedIndex, values);
                    }
                } catch (Exception e) {
                    log.error("Min and Max values must be a Number");
                }
            }
            // Otherwise reset the sample column
            else if (dataManager.isPresent()) {
                dataManager.get().resetSampleColumn(selectedIndex);
            }

            featureSelected(selectedIndex);
        }
    }

    /**
     * Gets the current values from the DataManager, applies the Offset classes functionality to said values,
     * and then stores the new values in DataManager.
     */
    public void offsetDatasetFeature() {
        if (lvFeatures == null) {
            return;
        }

        int selectedIndex = lvFeatures.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            Optional<DataManager<Double>> dataManager = getDataManager();

            if (cbOffset.isSelected() && dataManager.isPresent()) {
                ArrayList<Number> values = dataManager.get().getSampleColumn(selectedIndex);

                try {
                    double offset = Double.parseDouble(tfOffsetValue.getText());
                    System.out.println("Offset value: " + offset);

                    values = Offset.apply(values, offset);
                    dataManager.get().setSampleColumn(selectedIndex, values);

                } catch (Exception e) {
                    log.error("Offset value must be a Number");
                }
            }
            // Otherwise reset the sample column
            else if (dataManager.isPresent()) {
                dataManager.get().resetSampleColumn(selectedIndex);
            }

            featureSelected(selectedIndex);
        }
    }

    /**
     * Adds a new transformation to the FeatureClass list
     *
     * @param header Used to identify the specific FeatureClass and header of the new transformation
     * @param transformType Used to define the transform type of the new transformation
     */
    private void addNewTransform(String header, TransformType transformType) {
        Optional<DataManager<Double>> dataManager = getDataManager();

        // Creates a new transformation object, storing the feature that was transformed and
        // the type of transformation
        Transformation newTransformation = new Transformation(header, transformType);

        if (dataManager.isPresent()) {
            dataManager.get().getDataset().get(header).addTransformation(newTransformation);
            dataManager.get().getDataset().get(header).setModified(true);
        }
    }

    /**
     * Removes an existing transformation after a pre-processing checkbox has been deselected
     *
     * @param checkbox Identifies the type of transform to be removed
     */
    private void removeExistingTransform(CheckBox checkbox) {
        Optional<DataManager<Double>> dataManager = getDataManager();

        if (dataManager.isPresent()) {
            String selectedHeader = dataManager.get().getSampleHeaders().get(lvFeatures.getSelectionModel().getSelectedIndex());
            TransformType transformType = convertCheckBoxToTransformType(checkbox);

            dataManager.get().getDataset().get(selectedHeader).removeTransformation(transformType);

            if (dataManager.get().getDataset().get(selectedHeader).getTransformations().size() == 0) {
                dataManager.get().getDataset().get(selectedHeader).setModified(false);
            }
        }
    }

    /**
     * Updates the workspace to match the current active dataset.
     */
    private void updateWorkspace() {
        val item = getWorkspaceService().getActiveWorkspaceItem();

        // If no dataset is selected clear the UI
        if (!(item instanceof DatasetModel)) {
            clearUI();
        }

        //  Make sure the UI element actually exist
        if (lvFeatures != null) {
            // If the selected item is a dataset
            if (item instanceof DatasetModel) {
                // Add Dataset Headers to list
                Optional<DataManager<Double>> dataManager = getDataManager();

                if (dataManager.isPresent()) {
                    ObservableList<String> items = FXCollections.observableArrayList(dataManager.get().getSampleHeaders());
                    lvFeatures.setItems(items);
                }
            }
            // Otherwise clear the elements in the table
            else {
                lvFeatures.getItems().clear();
            }
        }
    }

    /**
     * Updates the pre-processing checkboxes to reflect the currently selected header.
     * This method is called each time a new feature is selected in lvFeatures.
     *
     * @param selectedHeader The string value of the currently selected header
     */
    private void updatePreprocessingTextFields(String selectedHeader) {
        // Updates the selected header in the transformation text fields
        cbSmoothData.setText("Smooth data points of (" + selectedHeader + ")");
        cbHandleMissingValues.setText("Handle missing values of (" + selectedHeader + ")");
        cbRemoveOutliers.setText("Remove outliers of (" + selectedHeader + ")");
        cbNormalise.setText("Normalise scale of (" + selectedHeader + ")");
        cbOffset.setText("Offset values of (" + selectedHeader + ")");
    }

    /**
     * Updates the pre-processing checkboxes to be either selected (ticked) or not selected (unticked)
     * based upon a list of applied transformations.
     * This method is called each time a new feature is selected in lvFeatures.
     *
     * @param selectedIndex The integer corresponding to the currently selected feature
     */
    private void updateCheckBoxes(int selectedIndex) {
        clearCheckBoxes();

        Optional<DataManager<Double>> dataManager = getDataManager();

        if (dataManager.isPresent() && selectedIndex >= 0) {
            String selectedHeader = dataManager.get().getSampleHeaders().get(selectedIndex);

            if (dataManager.get().getDataset().get(selectedHeader).isModified()) {
                List<Transformation> transformations = dataManager.get().getDataset().get(selectedHeader).getTransformations();

                for (Transformation transform : transformations) {
                    enableCheckBox(transform.getTransform());
                }
            }
        }
    }

    /**
     * Takes a transformation type as input and selects the checkbox corresponding with that
     * transformation type.
     *
     * @param transform The type of a previous transform
     */
    private void enableCheckBox(TransformType transform) {
        resetCheckboxFlag = true;

        switch (transform) {
            case Smoothed:
                cbSmoothData.setSelected(true);
                break;

            case MissingValuesHandled:
                cbHandleMissingValues.setSelected(true);
                break;

            case OutliersRemoved:
                cbRemoveOutliers.setSelected(true);
                break;

            case Normalised:
                cbNormalise.setSelected(true);
                break;

            case Offset:
                cbOffset.setSelected(true);
                break;
        }

        resetCheckboxFlag = false;
    }

    /**
     * Enabled all pre-processing checkboxes so that the user can interact with them.
     * This method is called once a feature is selected.
     */
    private void enablePreprocessingCheckBoxes() {
        cbSmoothData.setDisable(false);
        cbHandleMissingValues.setDisable(false);
        cbRemoveOutliers.setDisable(false);
        cbNormalise.setDisable(false);
        cbOffset.setDisable(false);
    }

    /**
     * Given one of the five pre-processing checkboxes, this method determines its corresponding TransformType.
     *
     * @param checkbox The checkbox that has been selected
     * @return The corresponding TransformType
     */
    private TransformType convertCheckBoxToTransformType(CheckBox checkbox) {
        if (checkbox == cbSmoothData) {
            return TransformType.Smoothed;
        }
        else if (checkbox == cbHandleMissingValues) {
            return TransformType.MissingValuesHandled;
        }
        else if (checkbox == cbRemoveOutliers) {
            return TransformType.OutliersRemoved;
        }
        else if (checkbox == cbNormalise) {
            return TransformType.Normalised;
        }
        else {
            return TransformType.Offset;
        }
    }

    /**
     *  Converts a handle missing values combo box index into the respective HandleMissingValues.Mode type.
     *
     * @param index Identifies the currently selected option in the combo box.
     */
    private HandleMissingValues.Mode convertComboBoxIndexToMode(int index) {
        switch (index) {
            /* Can be re-added once copyPreviousRow is fixed
            case 0:
                return Mode.COPY_PREVIOUS_ROW;*/

            case 0:
                return HandleMissingValues.Mode.MEAN;

            case 1:
                return HandleMissingValues.Mode.MEDIAN;

            case 2:
                return HandleMissingValues.Mode.ZERO;

            case 3:
            default:
                return HandleMissingValues.Mode.ONE;
        }
    }

    /**
     * Resets all checkboxes to an unselected state.
     * This method is called before checkboxes are updated to reflect previous transformations.
     */
    private void clearCheckBoxes() {
        resetCheckboxFlag = true;

        cbSmoothData.setSelected(false);
        cbHandleMissingValues.setSelected(false);
        cbRemoveOutliers.setSelected(false);
        cbNormalise.setSelected(false);
        cbOffset.setSelected(false);

        resetCheckboxFlag = false;
    }

    /**
     * Clears the search graphs.
     */
    private void clearUI() {
        // Make sure the UI element actually exists
        if (lcDataView != null) {
            lcDataView.getData().clear();
        }
    }

    /**
     * Returns the DataManager object which stores original and modified datasets.
     */
    private Optional<DataManager<Double>> getDataManager() {
        Displayable item = getWorkspaceService().getActiveWorkspaceItem();

        if (item instanceof DatasetModel) {
            DatasetModel dataset = (DatasetModel) item;
            return Optional.of(dataset.getDataManager());
        } else {
            return Optional.empty();
        }
    }

    /**
     * <p>
     * Returns the workspace service of this controller
     * </p>
     *
     * @return the workspace service of the controller
     */
    private WorkspaceService getWorkspaceService() {
        return workspaceService;
    }
}