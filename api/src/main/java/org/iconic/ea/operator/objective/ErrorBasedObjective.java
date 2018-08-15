package org.iconic.ea.operator.objective;

import lombok.extern.log4j.Log4j2;
import org.iconic.ea.chromosome.Chromosome;
import org.iconic.ea.data.DataManager;
import org.iconic.ea.data.FeatureClass;
import org.iconic.ea.operator.objective.error.ErrorFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@inheritDoc}
 * <p>
 * An error based objective is an objective function based around an error function.
 * </p>
 */
@Log4j2
public abstract class ErrorBasedObjective<T> implements Objective<T> {
    private final ErrorFunction lambda;
    private DataManager<T> dataManager;
    private List<T> expectedResults;
    private boolean changed;

    /**
     * <p>
     * Constructs a new ErrorBasedObjective with the provided error function and samples.
     * </p>
     *
     * @param lambda  The error function to apply
     * @param dataManager The dataset to apply the error function on
     */
    public ErrorBasedObjective(final ErrorFunction lambda, final DataManager dataManager) {
        this.lambda = lambda;
        this.dataManager = dataManager;
        this.expectedResults = new LinkedList<>();
        this.changed = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract double apply(final Chromosome<T> c);

    /**
     * <p>Returns the dataset used by this objective.</p>
     *
     * @return the dataset used by this objective
     */
    protected DataManager<T> getDataManager() {
        return dataManager;
    }

    /**
     * <p>Returns the error function used by this objective.</p>
     *
     * @return the error function used by this objective
     */
    protected ErrorFunction getLambda() {
        return lambda;
    }

    /**
     * <p>Returns the expected results for the samples used by this objective.</p>
     *
     * @return the expected results for the samples used by this objective
     */
    protected List<T> getExpectedResults() {
        // Check if the expected results need to be recalculated
        if (isChanged()) {
            HashMap<String, FeatureClass<Number>> dataset = getDataManager().getDataset();

            // Collect the expected answers
            List<FeatureClass<Number>> features = dataset.values().stream()
                    .filter(FeatureClass::isOutput)
                    .limit(1)
                    .collect(Collectors.toList());

            expectedResults = (ArrayList<T>) features.get(0).getSamples();

            setChanged(false);
        }

        return expectedResults;
    }

    /**
     * <p>Returns the changed status of this objective.</p>
     *
     * <p>
     * If the backing samples used by this objective are changed then the expected results need to be
     * recalculated.
     * </p>
     *
     * @return true if the backing samples used by this objective have changed
     */
    private boolean isChanged() {
        return changed;
    }

    /**
     * <p>
     * Sets the changed status of this objective to the provided value.
     * </p>
     *
     * @param changed The value to set the changed status of this objective to
     */
    private void setChanged(final boolean changed) {
        this.changed = changed;
    }

    /**
     * <p>
     * Sets the dataset of this objective to the provided value and marks it as changed.
     * </p>
     *
     * @param dataManager The value to set the dataset of this objective to
     */
    public void setDataManager(final DataManager dataManager) {
        setChanged(true);
        this.dataManager = dataManager;
    }
}
