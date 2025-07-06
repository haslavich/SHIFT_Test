package ru.shift.Haslavskiy_DU.filemanager;

import ru.shift.Haslavskiy_DU.options.LaunchOptions;

public class FileManager {

    private final LaunchOptions launchOptions;

    public FileManager(String[] args) {
        this.launchOptions = new LaunchOptions(args);
    }

    public void process() {
        DataSorter sorter = new DataSorter(launchOptions);
        sorter.processFiles();
    }
}
