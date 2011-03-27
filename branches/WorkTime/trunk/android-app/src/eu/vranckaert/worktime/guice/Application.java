package eu.vranckaert.worktime.guice;

import roboguice.application.GuiceApplication;

import java.util.List;

public class Application extends GuiceApplication {
    @Override
    protected void addApplicationModules(List<com.google.inject.Module> modules) {
        modules.add(new Module());
    }
}
