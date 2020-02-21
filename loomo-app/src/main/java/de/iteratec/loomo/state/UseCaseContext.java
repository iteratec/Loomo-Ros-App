package de.iteratec.loomo.state;

import java.util.concurrent.Callable;

public class UseCaseContext implements Callable<UseCaseContext> {

    @Override
    public UseCaseContext call() throws Exception {
        return this;
    }

    @Override
    public String toString() {
        return "AddContext []";
    }
}
